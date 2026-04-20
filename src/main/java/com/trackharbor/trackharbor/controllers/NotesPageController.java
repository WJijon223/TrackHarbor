package com.trackharbor.trackharbor.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NotesPageController {

    // FXML Bindings

    @FXML private TextField mainSearch;
    @FXML private VBox      cardsContainer;
    @FXML private Label     sectionCount;
    @FXML private StackPane pageRoot;

    @FXML private Button addNoteBtn;
    @FXML private Button sortRecentBtn;
    @FXML private Button sortCompanyBtn;
    @FXML private Button sortStatusBtn;

    private Parent activeModalOverlay;

    // in memory data model

    //represents one position entry the user has added. when  DB is ready this gets saved to:
    // users/{userId}/positions/{positionId}

    private static class PositionEntry {


        String id;
        String companyName;
        String role;
        String status;
        int    noteCount;




        PositionEntry(String id, String companyName, String role, String status) {


            this.id          = id;
            this.companyName = companyName;
            this.role        = role;
            this.status      = status;
            this.noteCount   = 0; // starts at 0, increments as user adds notes
        }

    }

    private enum SortMode { RECENT, COMPANY, STATUS }

    // all entries the user has added this session

    private final List<PositionEntry> allPositions = new ArrayList<>();
    private SortMode currentSort = SortMode.RECENT;
    private int nextId = 1;

    // initialization

    @FXML
    public void initialize() {


        // live search
        mainSearch.textProperty().addListener(

                (obs, oldVal, newVal) -> renderCards(newVal.trim().toLowerCase())
        );

        // TODO: when DB is ready load existing positions here and call renderCards("")
        // for now the list starts empty — user adds entries via the modal

        if (sortRecentBtn != null) {
            setActiveChip(sortRecentBtn);
        }

        renderCards("");


    }

    //add note Modal

    @FXML
    private void handleAddNote() {


        // build  modal dialog
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Add New Position");
        modal.setResizable(false);

        // form fields
        TextField companyField = new TextField();
        companyField.setPromptText("e.g. Capital One");

        TextField roleField = new TextField();
        roleField.setPromptText("e.g. Technology Internship");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Interview", "OA", "Waiting", "Accepted", "Rejected", "Other");

        statusBox.setValue("Waiting");
        statusBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #BE123C; -fx-font-size: 11px;");



        Button submitBtn = new Button("Add Position");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setDefaultButton(true);
        submitBtn.setStyle(

            "-fx-background-color: #6C2BD9; -fx-text-fill: white; " +
            "-fx-background-radius: 10px; -fx-font-weight: bold; -fx-padding: 10 0;"
        );

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setCancelButton(true);
        cancelBtn.setStyle(
            "-fx-background-color: #F0EEFF; -fx-text-fill: #6C2BD9; " +
            "-fx-background-radius: 10px; -fx-font-weight: bold; -fx-padding: 10 0;"

        );

        // layout
        VBox form = new VBox(12,
            styledField("Company Name", companyField),
            styledField("Role / Position", roleField),
            styledField("Status", statusBox),
            errorLabel,
            submitBtn,
            cancelBtn


        );
        form.setPadding(new Insets(28));
        form.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16px;");
        form.setPrefWidth(360);

        // submit validate then add to list


        submitBtn.setOnAction(e -> {
            String company = companyField.getText().trim();
            String role    = roleField.getText().trim();
            String status  = statusBox.getValue();

            if (company.isEmpty() || role.isEmpty()) {

                errorLabel.setText("Company name and role are required.");
                return;
            }

            PositionEntry entry = new PositionEntry(

                String.valueOf(nextId++), company, role, status
            );

            allPositions.add(0, entry); // add to top (most recent)

            // TODO: save entry to Firestore here when DB is ready



            modal.close();
            renderCards(mainSearch.getText().trim().toLowerCase());
        });

        cancelBtn.setOnAction(e -> modal.close());

        modal.setScene(new Scene(form));
        modal.showAndWait();
    }

    // rendering

    private void renderCards(String query) {


        // 1. filter


        List<PositionEntry> visible = new ArrayList<>();
        for (PositionEntry p : allPositions) {


            if (query.isEmpty()
                    || p.companyName.toLowerCase().contains(query)
                    || p.role.toLowerCase().contains(query)
                    || p.status.toLowerCase().contains(query)) {
                visible.add(p);
            }


        }

        // 2. sort
        switch (currentSort) {


            case COMPANY -> visible.sort(Comparator.comparing(p -> p.companyName));
            case STATUS  -> visible.sort(Comparator.comparing(p -> p.status));
            case RECENT  -> { /* insertion order = most recent first */ }
        }

        // 3. rebuild card list
        cardsContainer.getChildren().clear();

        if (allPositions.isEmpty()) {


            cardsContainer.getChildren().add(buildEmptyState(
                "No positions yet.", "Click \"+ Add Note\" to get started."
            ));


        } else if (visible.isEmpty()) {

            cardsContainer.getChildren().add(buildEmptyState(
                "No matches found.", "Try a different search term."
            ));
        } else {

            for (PositionEntry p : visible) {
                cardsContainer.getChildren().add(buildCard(p));
            }
        }

        // 4. update count label
        if (sectionCount != null) {
            int n = visible.size();
            sectionCount.setText(n + (n == 1 ? " entry" : " entries"));
        }
    }

    // card builder

    private HBox buildCard(PositionEntry p) {

        // badge
        StackPane badge = new StackPane();
        badge.getStyleClass().addAll("company-badge", badgeColor(p.companyName));
        badge.setMinSize(46, 46);
        badge.setPrefSize(46, 46);
        Label badgeLabel = new Label(initials(p.companyName));
        badgeLabel.getStyleClass().add("badge-text");
        badge.getChildren().add(badgeLabel);

        // info
        Label company = new Label(p.companyName);
        company.getStyleClass().add("card-company");

        Label role = new Label(p.role);
        role.getStyleClass().add("card-role");

        Label statusLabel = new Label(p.status);
        statusLabel.getStyleClass().add("card-meta");

        VBox info = new VBox(3, company, role, statusLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        // right side


        Label pill = new Label(p.noteCount + (p.noteCount == 1 ? " note" : " notes"));
        pill.getStyleClass().add("notes-pill");

        Label arrow = new Label("›");
        arrow.getStyleClass().add("card-arrow");

        VBox right = new VBox(6, pill, arrow);
        right.setAlignment(Pos.CENTER_RIGHT);

        // card


        HBox card = new HBox(16, badge, info, right);
        card.getStyleClass().add("note-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setOnMouseClicked(e -> handleCardClick(p.id, p.companyName));

        return card;
    }

    //  empty state



    private VBox buildEmptyState(String heading, String sub) {


        Label h = new Label(heading);
        h.getStyleClass().add("card-company");
        h.setStyle("-fx-text-fill: #9B89C4;");

        Label s = new Label(sub);
        s.getStyleClass().add("card-meta");

        VBox box = new VBox(6, h, s);
        box.setStyle("-fx-padding: 40 0 0 4;");
        return box;
    }

    // card click

    /**
     * TODO: navigate to the notes detail page for this position.
     */


    private void handleCardClick(String positionId, String companyName) {
        if (pageRoot == null) {
            return;
        }

        if (activeModalOverlay != null && activeModalOverlay.getParent() != null) {
            activeModalOverlay.toFront();
            return;
        }

        activeModalOverlay = null;

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/trackharbor/trackharbor/note-modal.fxml")
            );
            Parent modalOverlay = loader.load();

            NoteModalController modalController = loader.getController();
            if (modalController != null) {
                modalController.setOnCloseRequest(() -> activeModalOverlay = null);
            }

            activeModalOverlay = modalOverlay;
            pageRoot.getChildren().add(modalOverlay);
            modalOverlay.toFront();
        } catch (IOException ex) {
            throw new IllegalStateException(
                "Unable to open notes modal for position " + companyName + " [id=" + positionId + "]",
                ex
            );
        }
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



    // Wraps a label + control together for the modal form.


    private VBox styledField(String labelText, Control control) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7C5FA8;");
        control.setStyle(
            "-fx-background-color: #F5F3FF; -fx-background-radius: 8px; " +
            "-fx-border-color: #DDD6FE; -fx-border-radius: 8px; " +
            "-fx-border-width: 1px; -fx-padding: 8 12; -fx-font-size: 13px;"
        );


        if (control instanceof TextField tf) tf.setPrefHeight(38);
        VBox box = new VBox(5, lbl, control);
        VBox.setVgrow(control, Priority.ALWAYS);
        return box;
    }

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
