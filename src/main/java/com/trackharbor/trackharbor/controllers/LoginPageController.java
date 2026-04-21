package com.trackharbor.trackharbor.controllers;

import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class LoginPageController implements Initializable {

    // ── FXML Injected Fields ─────────────────────────────────────────────────

    /** Email input field */
    @FXML
    private TextField emailField;

    /** Password input field */
    @FXML
    private PasswordField passwordField;

    /** "here" hyperlink for sign-up navigation */
    @FXML
    private Hyperlink signupLink;


    // ── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Use this to set up any default values or listeners.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Example: clear any placeholder state, set up listeners, etc.
        // emailField.textProperty().addListener(...);
    }


    // ── Event Handlers ───────────────────────────────────────────────────────

    /**
     * Triggered when the Submit button is clicked.
     * Redirects the user to the dashboard page.
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        try {
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/trackharbor/trackharbor/dashboard-page.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(dashboardRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dashboard page.", e);
        }
    }

    /**
     * Triggered when the "here" hyperlink is clicked.
     * Navigate to your registration/sign-up scene here.
     */
    @FXML
    private void handleSignup(ActionEvent event) {
        // ── TODO: Load and switch to the Sign-Up scene ──
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("SignupView.fxml"));
        // Parent root = loader.load();
        // Stage stage = (Stage) signupLink.getScene().getWindow();
        // stage.setScene(new Scene(root));

        System.out.println("Navigate to Sign-Up page.");
    }


    // ── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Displays an error message to the user.
     * Hook this up to an error Label in the FXML if needed.
     */
    private void showError(String message) {
        // ── TODO: Show message in a Label or Alert ──
        // errorLabel.setText(message);
        System.err.println("Login error: " + message);
    }

    /**
     * Simple email format check using regex.
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
}

