package com.trackharbor.trackharbor;

import com.trackharbor.trackharbor.config.FirebaseInitializer;
import com.trackharbor.trackharbor.service.PositionService;
import com.trackharbor.trackharbor.service.UserService;
import com.trackharbor.trackharbor.service.NoteService;
import com.trackharbor.trackharbor.model.Position;
import com.trackharbor.trackharbor.model.Note;
import com.trackharbor.trackharbor.model.UserProfile;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

// This is a new comment (second part of test)

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FirebaseInitializer.initialize();

        String userId = "XFizAqdevuRI1zA63n6XV2goaIB2";

        UserService userService = new UserService();
        PositionService positionService = new PositionService();
        NoteService noteService = new NoteService();

        System.out.println("=== USER ===");
        UserProfile user = userService.getUserById(userId);

        if (user != null) {
            System.out.println("Name: " + user.getFirstName() + " " + user.getLastName());
            System.out.println("Email: " + user.getEmail());
        } else {
            System.out.println("User not found.");
        }

        System.out.println("\n=== POSITIONS ===");
        List<Position> positions = positionService.getPositionsForUser(userId);

        for (Position p : positions) {
            System.out.println("ID: " + p.getId());
            System.out.println("Name: " + p.getName());
            System.out.println("Status: " + p.getStatus());
            System.out.println("Applied: " + p.isApplied());
            System.out.println("----------------------");

            // --- Fetch Notes for each Position ---
            System.out.println("  Notes:");
            List<Note> notes = noteService.getNotesForPosition(userId, p.getId());

            for (Note n : notes) {
                System.out.println("   - " + n.getContent());
            }

            System.out.println();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("login-page.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("TrackHarbor");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}