package com.trackharbor.trackharbor.controllers;

import com.trackharbor.trackharbor.model.UserProfile;
import com.trackharbor.trackharbor.service.AuthService;
import com.trackharbor.trackharbor.service.UserService;
import com.trackharbor.trackharbor.session.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginPageController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Hyperlink signupLink;

    private AuthService authService;
    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthService();
        userService = new UserService();
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email.");
            return;
        }

        try {
            // 🔐 Authenticate user
            String userId = authService.signInWithEmailAndPassword(email, password);
            System.out.println("UID: " + userId);

            // 📦 Fetch user profile from Firestore
            UserProfile user = userService.getUserById(userId);

            System.out.println("=== SIGNED IN USER ===");

            if (user != null) {
                System.out.println("ID: " + user.getId());
                System.out.println("Name: " + user.getFirstName() + " " + user.getLastName());
                System.out.println("Email: " + user.getEmail());

                // ✅ Set session
                SessionManager.setCurrentUser(user);
                System.out.println("Session set for user: " + user.getId());

            } else {
                System.out.println("User authenticated, but profile not found in Firestore.");
                showError("User profile not found.");
                return;
            }

            // 🚀 Navigate to dashboard
            Parent dashboardRoot = FXMLLoader.load(
                    getClass().getResource("/com/trackharbor/trackharbor/dashboard-page.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(dashboardRoot);

        } catch (Exception e) {
            showError("Invalid email or password.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        System.out.println("Navigate to Sign-Up page.");
    }

    private void showError(String message) {
        System.err.println("Login error: " + message);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
}