package com.trackharbor.trackharbor.controllers;
import com.trackharbor.trackharbor.model.UserProfile;
import com.trackharbor.trackharbor.service.AuthService;
import com.trackharbor.trackharbor.service.UserService;
import com.trackharbor.trackharbor.session.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpPageController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // setup goes here later
    }


    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(
                    getClass().getResource("/com/trackharbor/trackharbor/login-page.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Please enter a valid email.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        try {
            // 1. Register in Firebase Auth
            AuthService authService = new AuthService();
            String userId = authService.registerWithEmailAndPassword(email, password);

            // 2. Build the user profile
            UserProfile newUser = new UserProfile();
            newUser.setId(userId);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setCreatedAt(java.time.Instant.now());

            // 3. Save to Firestore
            UserService userService = new UserService();
            userService.createUser(newUser);

            // 4. Set session
            SessionManager.setCurrentUser(newUser);

            // 5. Show success alert then navigate to dashboard
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText(null);
            success.setContentText("Account created successfully!");
            success.showAndWait();

// 6. Navigate to dashboard
            Parent dashboardRoot = FXMLLoader.load(
                    getClass().getResource("/com/trackharbor/trackharbor/dashboard-page.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(dashboardRoot);
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }



}