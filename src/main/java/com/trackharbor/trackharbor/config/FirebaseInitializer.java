package com.trackharbor.trackharbor.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseInitializer {
    private static Firestore firestore;

    public static void initialize() {
        if (firestore != null) {
            return;
        }

        try {
            // Option 1: explicit local JSON path

            System.out.println("Working dir: " + System.getProperty("user.dir"));
            InputStream serviceAccount = new FileInputStream(
                    "C:\\TrackHarbor\\TrackHarbor\\src\\main\\resources\\trackharbor-firebase-service-account.json"
            );


            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            firestore = FirestoreClient.getFirestore();

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    public static Firestore getFirestore() {
        if (firestore == null) {
            initialize();
        }
        return firestore;
    }



}