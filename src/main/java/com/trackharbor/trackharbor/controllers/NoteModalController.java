package com.trackharbor.trackharbor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class NoteModalController {
	private static final String DEFAULT_AI_TIPS = String.join("\n",
			"• Brush up on Python Skills",
			"• Use GlassDoor questions to practice",
			"• Prepare one STAR story for a technical challenge"
	);

	private Runnable onCloseRequest;

	@FXML
	private StackPane modalRoot;

	@FXML
	private TextArea interviewNotesArea;

	@FXML
	private TextArea aiTipsArea;

	@FXML
	private Button editNotesButton;

	public void setOnCloseRequest(Runnable onCloseRequest) {
		this.onCloseRequest = onCloseRequest;
	}

	@FXML
	private void handleEditNotes() {
		boolean makeEditable = !interviewNotesArea.isEditable();
		interviewNotesArea.setEditable(makeEditable);
		interviewNotesArea.setFocusTraversable(makeEditable);
		editNotesButton.setText(makeEditable ? "Done" : "Edit");

		if (makeEditable) {
			interviewNotesArea.requestFocus();
			interviewNotesArea.positionCaret(interviewNotesArea.getText().length());
		}
	}

	@FXML
	private void handleGenerateTips() {
		if (aiTipsArea.getText() == null || aiTipsArea.getText().isBlank()) {
			aiTipsArea.setText(DEFAULT_AI_TIPS);
			return;
		}

		if (!aiTipsArea.getText().contains("STAR story")) {
			aiTipsArea.appendText("\n• Prepare one STAR story for a technical challenge");
		}
	}

	@FXML
	private void handleDeleteNote() {
		interviewNotesArea.clear();
		aiTipsArea.clear();
		interviewNotesArea.setEditable(false);
		interviewNotesArea.setFocusTraversable(false);
		editNotesButton.setText("Edit");
	}

	@FXML
	private void handleSaveChanges() {
		closeWindow();
	}

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

		if (editNotesButton != null && editNotesButton.getScene() != null) {
			window = editNotesButton.getScene().getWindow();
		} else if (interviewNotesArea != null && interviewNotesArea.getScene() != null) {
			window = interviewNotesArea.getScene().getWindow();
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
}
