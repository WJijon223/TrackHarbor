package com.trackharbor.trackharbor.controllers;

import com.trackharbor.trackharbor.model.Position;
import com.trackharbor.trackharbor.service.NoteService;
import com.trackharbor.trackharbor.service.PositionService;
import com.trackharbor.trackharbor.session.SessionManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotesPageController {

    // FXML Bindings

    @FXML private StackPane pageRoot;
    @FXML private TextField mainSearch;
    @FXML private VBox      cardsContainer;
    @FXML private Label     sectionCount;
    @FXML private Label     avatarLabel;

    @FXML private Button addNoteBtn;
    @FXML private Button sortRecentBtn;
    @FXML private Button sortCompanyBtn;
    @FXML private Button sortStatusBtn;

    // services

    // all firestore access goes through these; no direct db calls in this controller
    private final PositionService positionService = new PositionService();
    private final NoteService     noteService     = new NoteService();

    // background thread pool keeps service calls off the JavaFX UI thread

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {


        Thread t = new Thread(r, "firestore-worker");
        t.setDaemon(true);
        return t;
    });

    // state


    private final List<Position>       allPositions = new ArrayList<>();



    // note counts stored separately position model doesn't carry this field.
    // Key: positionId → Value: number of notes in that position's sub-collection.
    private final Map<String, Integer> noteCounts   = new HashMap<>();

    private enum SortMode { RECENT, COMPANY, STATUS }
    private SortMode currentSort = SortMode.RECENT;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());

    // initialization

    @FXML
    public void initialize() {

        // populate sidebar avatar with logged-in user's initials

        String first = SessionManager.getCurrentUser().getFirstName();
        String last  = SessionManager.getCurrentUser().getLastName();
        avatarLabel.setText(("" + first.charAt(0) + last.charAt(0)).toUpperCase());

        // live search — re-filters cards on every keystroke

        mainSearch.textProperty().addListener(
                (obs, old, val) -> renderCards(val.trim().toLowerCase())
        );

        showLoadingState();
        loadPositions();
    }

    // load positions

    /**
     * Loads all positions for the current user via PositionService,
     * then fetches each position's note count via NoteService.
     *
     * All Firestore access is delegated to the service layer — no direct
     * db calls exist in this controller.
     *
     * Runs on a background thread; hands results to the UI via Platform.runLater().
     */


    private void loadPositions() {

        String userId = SessionManager.getCurrentUser().getId();

        executor.submit(() -> {

            try {

                // positionService handles all firestore querying and field mapping

                List<Position> loaded = positionService.getPositionsForUser(userId);

                // sort by updatedAt descending — most recently updated appears first.
                // positionService returns in firestore insertion order so we sort here.

                loaded.sort((a, b) -> {

                    if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) return 0;
                    if (a.getUpdatedAt() == null) return 1;
                    if (b.getUpdatedAt() == null) return -1;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                });

                // fetch note count for each position via NoteService

                Map<String, Integer> counts = new HashMap<>();
                for (Position p : loaded) {

                    int count = noteService.getNoteCountForPosition(userId, p.getId());
                    counts.put(p.getId(), count);
                }

                Platform.runLater(() -> {


                    allPositions.clear();
                    allPositions.addAll(loaded);
                    noteCounts.clear();
                    noteCounts.putAll(counts);
                    renderCards("");
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        showError("Could not load positions. Check your connection.")
                );
                e.printStackTrace();
            }
        });
    }

    // rendering

    private void renderCards(String query) {
        // 1. filter by search query across name and status


        List<Position> visible = new ArrayList<>();
        for (Position p : allPositions) {

            String name   = p.getName()   != null ? p.getName().toLowerCase()   : "";
            String status = p.getStatus() != null ? p.getStatus().toLowerCase() : "";
            if (query.isEmpty() || name.contains(query) || status.contains(query)) {
                visible.add(p);
            }
        }

        // 2. sort


        switch (currentSort) {
            case COMPANY -> visible.sort(Comparator.comparing(
                    p -> p.getName() != null ? p.getName() : ""));
            case STATUS  -> visible.sort(Comparator.comparing(
                    p -> p.getStatus() != null ? p.getStatus() : ""));
            case RECENT  -> visible.sort((a, b) -> {
                if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) return 0;
                if (a.getUpdatedAt() == null) return 1;
                if (b.getUpdatedAt() == null) return -1;
                return b.getUpdatedAt().compareTo(a.getUpdatedAt());
            });
        }

        // 3. rebuild card list


        cardsContainer.getChildren().clear();

        if (allPositions.isEmpty()) {


            cardsContainer.getChildren().add(buildEmptyState(
                    "No positions yet.",
                    "Add a position from the Table page first."
            ));
        } else if (visible.isEmpty()) {


            cardsContainer.getChildren().add(buildEmptyState(
                    "No matches found.",
                    "Try a different search term."
            ));
        } else {
            for (Position p : visible) {
                cardsContainer.getChildren().add(buildCard(p));
            }
        }

        // 4. update entry count label

        if (sectionCount != null) {
            int n = visible.size();
            sectionCount.setText(n + (n == 1 ? " entry" : " entries"));
        }
    }

    // card builder


    private HBox buildCard(Position p) {

        String name      = p.getName()   != null ? p.getName()   : "Unknown";
        String status    = p.getStatus() != null ? p.getStatus() : "Other";
        int    noteCount = noteCounts.getOrDefault(p.getId(), 0);

        String updatedStr = "—";
        if (p.getUpdatedAt() != null) {


            updatedStr = DISPLAY_FMT.format(p.getUpdatedAt());
        }

        // company badge


        StackPane badge = new StackPane();
        badge.getStyleClass().addAll("company-badge", badgeColor(name));
        badge.setMinSize(46, 46);
        badge.setPrefSize(46, 46);
        Label badgeLabel = new Label(initials(name));
        badgeLabel.getStyleClass().add("badge-text");
        badge.getChildren().add(badgeLabel);

        // info column


        Label companyLbl = new Label(name);
        companyLbl.getStyleClass().add("card-company");

        Label statusLbl = new Label(status);
        statusLbl.getStyleClass().add("card-role");

        Label metaLbl = new Label("Updated " + updatedStr);
        metaLbl.getStyleClass().add("card-meta");

        VBox info = new VBox(3, companyLbl, statusLbl, metaLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        // right side: note count pill + arrow


        Label pill = new Label(noteCount + (noteCount == 1 ? " note" : " notes"));
        pill.getStyleClass().add("notes-pill");

        Label arrow = new Label("›");
        arrow.getStyleClass().add("card-arrow");

        VBox right = new VBox(6, pill, arrow);
        right.setAlignment(Pos.CENTER_RIGHT);

        // assemble


        HBox card = new HBox(16, badge, info, right);
        card.getStyleClass().add("note-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setOnMouseClicked(e -> openModal(p));

        return card;
    }

    // modal overlay

    /**
     * Loads note-modal.fxml and pushes it onto pageRoot as an overlay layer.
     *
     * The modal StackPane becomes the second child of pageRoot, sitting on top
     * of the page. NoteModalController.closeWindow() removes itself from pageRoot
     * automatically — no cleanup needed here.
     *
     * After the modal closes, refreshNoteCount() updates the pill badge for
     * that position without triggering a full page reload.
     *
     * ── Handoff note for teammate ──────────────────────────────────────────
     * Add initForPosition(Position p, String userId) to NoteModalController,
     * then uncomment the call below. That method should:
     *   - populate the company name / role labels in the modal header
     *   - load existing notes via NoteService.getNotesForPosition()
     *   - wire Save to NoteService.createNote() / updateNote()
     *   - wire Delete to NoteService.deleteNote()
     * ──────────────────────────────────────────────────────────────────────
     */


    private void openModal(Position position) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/trackharbor/trackharbor/note-modal.fxml")
            );
            Node modalNode = loader.load();

            NoteModalController modalCtrl = loader.getController();

            // TODO: uncomment once will adds initForPosition() to NoteModalController
            // modalCtrl.initForPosition(position, SessionManager.getCurrentUser().getId());

            // refresh pill count when modal closes
            modalCtrl.setOnCloseRequest(() -> refreshNoteCount(position));

            // push modal on top of the page
            pageRoot.getChildren().add(modalNode);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not open note modal.");
        }
    }

    // note count refresh

    /**
     * After the modal closes, re-fetches the note count for that position
     * via NoteService and re-renders the card list so the pill stays accurate
     * without doing a full page reload.
     */


    private void refreshNoteCount(Position position) {
        String userId = SessionManager.getCurrentUser().getId();

        executor.submit(() -> {
            try {
                // goes through NoteService — no direct Firestore access
                int count = noteService.getNoteCountForPosition(userId, position.getId());

                Platform.runLater(() -> {
                    noteCounts.put(position.getId(), count);
                    renderCards(mainSearch.getText().trim().toLowerCase());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // sort handlers

    @FXML
    private void handleSortRecent() {
        currentSort = SortMode.RECENT;
        setActiveChip(sortRecentBtn);
        renderCards(mainSearch.getText().trim().toLowerCase());
    }

    @FXML
    private void handleSortCompany() {
        currentSort = SortMode.COMPANY;
        setActiveChip(sortCompanyBtn);
        renderCards(mainSearch.getText().trim().toLowerCase());
    }

    @FXML
    private void handleSortStatus() {
        currentSort = SortMode.STATUS;
        setActiveChip(sortStatusBtn);
        renderCards(mainSearch.getText().trim().toLowerCase());
    }

    private void setActiveChip(Button active) {
        for (Button btn : List.of(sortRecentBtn, sortCompanyBtn, sortStatusBtn)) {
            btn.getStyleClass().remove("sort-chip-active");
        }
        if (!active.getStyleClass().contains("sort-chip-active")) {
            active.getStyleClass().add("sort-chip-active");
        }
    }

    // add note button

    @FXML
    private void handleAddNote() {
        showError("Select a position card to view and add notes.");
    }


    // ui state helpers

    private void showLoadingState() {

        cardsContainer.getChildren().clear();
        Label lbl = new Label("Loading positions…");
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #9B89C4; -fx-padding: 32 0 0 4;");
        cardsContainer.getChildren().add(lbl);
    }

    private void showError(String message) {
        cardsContainer.getChildren().clear();
        Label lbl = new Label(message);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #BE123C; -fx-padding: 32 0 0 4;");
        cardsContainer.getChildren().add(lbl);
    }

    private VBox buildEmptyState(String heading, String sub) {
        Label h = new Label(heading);
        h.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #9B89C4;");
        Label s = new Label(sub);
        s.setStyle("-fx-font-size: 12px; -fx-text-fill: #B8ABD8;");
        VBox box = new VBox(6, h, s);
        box.setStyle("-fx-padding: 40 0 0 4;");
        return box;
    }

    // Badge / Initials Helpers

    private String initials(String name) {

        if (name == null || name.isBlank()) return "?";
        String[] words = name.trim().split("\\s+");
        if (words.length == 1)
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    }

    private String badgeColor(String name) {


        if (name == null || name.isBlank()) return "badge-gray";
        return switch (Character.toUpperCase(name.charAt(0)) % 4) {
            case 0 -> "badge-red";
            case 1 -> "badge-blue";
            case 2 -> "badge-purple";
            default -> "badge-gray";
        };
    }
}
