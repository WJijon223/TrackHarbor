package com.trackharbor.trackharbor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class NoteModalController {
	private static final String DEFAULT_AI_TIPS = String.join("\n",
			"• Brush up on Python Skills",
			"• Use GlassDoor questions to practice",
			"• Prepare one STAR story for a technical challenge"
	);

	@FXML
	private TextArea interviewNotesArea;

	@FXML
	private TextArea aiTipsArea;

	@FXML
	private Button editNotesButton;

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
		Stage stage = null;

		if (editNotesButton != null && editNotesButton.getScene() != null) {
			stage = (Stage) editNotesButton.getScene().getWindow();
		} else if (interviewNotesArea != null && interviewNotesArea.getScene() != null) {
			stage = (Stage) interviewNotesArea.getScene().getWindow();
		} else if (aiTipsArea != null && aiTipsArea.getScene() != null) {
			stage = (Stage) aiTipsArea.getScene().getWindow();
		}

		if (stage != null) {
			stage.close();
		}
	}
}
