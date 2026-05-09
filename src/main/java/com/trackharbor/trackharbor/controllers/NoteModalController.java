package com.trackharbor.trackharbor.controllers;

import com.trackharbor.trackharbor.model.Note;
import com.trackharbor.trackharbor.model.Position;
import com.trackharbor.trackharbor.service.NoteService;
import com.trackharbor.trackharbor.service.PositionService;
import com.trackharbor.trackharbor.service.AiTipsService;
import com.trackharbor.trackharbor.session.SessionManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteModalController {

    // fxml bindings

    @FXML private StackPane modalRoot;

    // company header labels — populated by initForPosition()
    @FXML private Label companyInitialsLabel;
    @FXML private Label companyNameLabel;
    @FXML private Label companyStatusLabel;
    @FXML private Label companyMetaLabel;
    @FXML private Label noteCountLabel;

    // notes list — note rows are injected here dynamically
    @FXML private ScrollPane notesScrollPane;
    @FXML private VBox       notesListContainer;

    // add new note
    @FXML private TextArea newNoteArea;
    @FXML private Button   addNoteButton;

    // ai tips
    @FXML private TextArea aiTipsArea;
    @FXML private Button generateTipsButton;


    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());

    private final NoteService noteService = new NoteService();
    private final PositionService positionService = new PositionService();
    private final AiTipsService aiTipsService = new AiTipsService();

    // background thread pool — keeps NoteService calls off the JavaFX thread
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "modal-worker");
        t.setDaemon(true);
        return t;
    });

    private String     userId;
    private String     positionId;
    private Runnable   onCloseRequest;
    private Position currentPosition;

    // entry point called by NotesPageController

    /**
     * Called by NotesPageController immediately after the modal is loaded.
     * Populates the company header and triggers the initial notes fetch.
     *
     * Uncomment the call in NotesPageController.openModal() once this is in place:
     *   modalCtrl.initForPosition(position, SessionManager.getCurrentUser().getId());
     */
    public void initForPosition(Position position, String userId) {
        this.userId     = userId;
        this.positionId = position.getId();
        this.currentPosition = position;

        // populate company header row
        String name   = position.getName()   != null ? position.getName()   : "Unknown";
        String status = position.getStatus() != null ? position.getStatus() : "—";

        companyNameLabel.setText(name);
        companyStatusLabel.setText(status);
        companyInitialsLabel.setText(initials(name));

        String dateStr = position.getUpdatedAt() != null
                ? "Updated " + DATE_FMT.format(position.getUpdatedAt()) : "—";
        companyMetaLabel.setText(dateStr);

        // display ai tips notes
        displayAiTips(position);

        // load exsisting notes from firestore
        loadNotes();
    }

    public void setOnCloseRequest(Runnable onCloseRequest) {
        this.onCloseRequest = onCloseRequest;
    }

    // load notes

    /**
     * Fetches all notes for this position via NoteService on a background thread,
     * then rebuilds the notes list on the JavaFX thread.
     */

    private void loadNotes() {
        showListLoading();

        executor.submit(() -> {
            try {
                List<Note> notes = noteService.getNotesForPosition(userId, positionId);
                Platform.runLater(() -> renderNotesList(notes));
            } catch (Exception e) {
                Platform.runLater(() -> showListError("Could not load notes."));
                e.printStackTrace();
            }
        });
    }

    // render notes list

    /**
     * Clears the notes list container and rebuilds it from the given list.
     * Each note gets its own row with inline Edit and Delete controls.
     */

    private void renderNotesList(List<Note> notes) {
        notesListContainer.getChildren().clear();

        if (notes.isEmpty()) {
            Label empty = new Label("No notes yet — add one below.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #9B89C4; -fx-padding: 8 0;");
            notesListContainer.getChildren().add(empty);
        } else {
            for (Note note : notes) {
                notesListContainer.getChildren().add(buildNoteRow(note));
            }
        }

        // update count label
        int n = notes.size();
        noteCountLabel.setText(n + (n == 1 ? " note" : " notes"));
    }

    // note row builder

    /**
     * Builds a single note row with:
     *   - Note content label
     *   - Created date
     *   - Edit button (swaps to inline TextArea + Save/Cancel)
     *   - Delete button (confirm then remove)
     */

    private VBox buildNoteRow(Note note) {
        // content
        Label contentLabel = new Label(note.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("note-row-content");

        // date
        String dateStr = note.getCreatedAt() != null
                ? DATE_FMT.format(note.getCreatedAt()) : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("note-row-date");

        // edit button
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("note-row-edit-btn");

        // delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("note-row-delete-btn");

        HBox actions = new HBox(8, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox row = new VBox(5, contentLabel, dateLabel, actions);
        row.getStyleClass().add("note-row");

        // inline TextArea
        editBtn.setOnAction(e -> {
            TextArea editArea = new TextArea(note.getContent());
            editArea.setWrapText(true);
            editArea.setPrefRowCount(3);
            editArea.getStyleClass().add("note-row-edit-area");

            Button saveBtn = new Button("Save");
            saveBtn.getStyleClass().add("note-row-save-btn");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("note-row-cancel-btn");

            HBox editActions = new HBox(8, saveBtn, cancelBtn);
            editActions.setAlignment(Pos.CENTER_LEFT);

            // swap in the edit UI
            row.getChildren().setAll(editArea, editActions);

            saveBtn.setOnAction(ev -> {
                String updated = editArea.getText().trim();
                if (updated.isEmpty()) return;

                saveBtn.setDisable(true);
                note.setContent(updated);

                executor.submit(() -> {
                    try {
                        noteService.updateNote(userId, positionId, note);
                        Platform.runLater(this::loadNotes); // refresh list
                    } catch (Exception ex) {
                        Platform.runLater(() -> saveBtn.setDisable(false));
                        ex.printStackTrace();
                    }
                });
            });

            cancelBtn.setOnAction(ev ->
                    row.getChildren().setAll(contentLabel, dateLabel, actions)
            );
        });

        // delete
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete this note? This cannot be undone.",
                    ButtonType.YES, ButtonType.CANCEL);
            confirm.setTitle("Delete Note");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    deleteBtn.setDisable(true);
                    executor.submit(() -> {
                        try {
                            noteService.deleteNote(userId, positionId, note.getId());
                            Platform.runLater(this::loadNotes); // refresh list
                        } catch (Exception ex) {
                            Platform.runLater(() -> deleteBtn.setDisable(false));
                            ex.printStackTrace();
                        }
                    });
                }
            });
        });

        return row;
    }

    // add note

    /**
     * Reads content from newNoteArea, calls NoteService.createNote(),
     * then reloads the notes list so the new note appears immediately.
     */

    @FXML
    private void handleAddNote() {
        String content = newNoteArea.getText().trim();
        if (content.isEmpty()) return;

        addNoteButton.setDisable(true);

        Note newNote = new Note(null, content, Instant.now());

        executor.submit(() -> {
            try {
                noteService.createNote(userId, positionId, newNote);
                Platform.runLater(() -> {
                    newNoteArea.clear();
                    addNoteButton.setDisable(false);
                    loadNotes(); // refresh list
                });
            } catch (Exception e) {
                Platform.runLater(() -> addNoteButton.setDisable(false));
                e.printStackTrace();
            }
        });
    }

    // ai tips

    @FXML
    private void handleGenerateTips() {
        if (currentPosition == null) {
            aiTipsArea.setText("No position selected.");
            return;
        }

        if (currentPosition.isAiTipsGenerated()) {
            displayAiTips(currentPosition);
            return;
        }

        String jobUrl = currentPosition.getLink();

        if (jobUrl == null || jobUrl.isBlank()) {
            aiTipsArea.setText("This position does not have a job link.");
            return;
        }

        generateTipsButton.setDisable(true);
        generateTipsButton.setText("Generating...");
        aiTipsArea.setText("Generating AI tips...");

        executor.submit(() -> {
            try {
                List<String> generatedTips = aiTipsService.generateTips(
                        currentPosition.getName(),
                        currentPosition.getName(),
                        currentPosition.getLink()
                );

                positionService.updateAiTips(userId, positionId, generatedTips);

                currentPosition.setAiTips(generatedTips);
                currentPosition.setAiTipsGenerated(true);

                Platform.runLater(() -> displayAiTips(currentPosition));

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    generateTipsButton.setDisable(false);
                    generateTipsButton.setText("Generate Tips");
                    aiTipsArea.setText("Failed to generate AI tips. Check console.");
                });
            }
        });
    }
    private void displayAiTips(Position position) {
        List<String> tips = position.getAiTips();

        if (tips == null || tips.isEmpty()) {
            aiTipsArea.setText("No AI tips generated yet.");
            generateTipsButton.setDisable(false);
            generateTipsButton.setText("Generate Tips");
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (String tip : tips) {
            builder.append("• ")
                    .append(tip)
                    .append("\n");
        }

        aiTipsArea.setText(builder.toString().trim());

        if (position.isAiTipsGenerated()) {
            generateTipsButton.setDisable(true);
            generateTipsButton.setText("Tips Generated");
        } else {
            generateTipsButton.setDisable(false);
            generateTipsButton.setText("Generate Tips");
        }
    }

    // close

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void closeWindow() {
        if (modalRoot != null && modalRoot.getParent() instanceof Pane parent) {
            parent.getChildren().remove(modalRoot);
            notifyClosed();
            return;
        }

        Window window = null;
        if (addNoteButton != null && addNoteButton.getScene() != null) {
            window = addNoteButton.getScene().getWindow();
        } else if (newNoteArea != null && newNoteArea.getScene() != null) {
            window = newNoteArea.getScene().getWindow();
        } else if (aiTipsArea != null && aiTipsArea.getScene() != null) {
            window = aiTipsArea.getScene().getWindow();
        }

        if (window instanceof Stage stage) {
            stage.close();
        }

        notifyClosed();
    }

    private void notifyClosed() {
        if (onCloseRequest != null) {
            onCloseRequest.run();
        }
    }

    // list state helpers

    private void showListLoading() {
        notesListContainer.getChildren().clear();
        Label lbl = new Label("Loading notes…");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #9B89C4; -fx-padding: 8 0;");
        notesListContainer.getChildren().add(lbl);
    }

    private void showListError(String message) {
        notesListContainer.getChildren().clear();
        Label lbl = new Label(message);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #BE123C; -fx-padding: 8 0;");
        notesListContainer.getChildren().add(lbl);
    }

    // helpers

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] words = name.trim().split("\\s+");
        if (words.length == 1)
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    }
}
