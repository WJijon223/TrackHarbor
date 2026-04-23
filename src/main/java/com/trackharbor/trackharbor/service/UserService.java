package com.trackharbor.trackharbor.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.trackharbor.trackharbor.config.FirebaseInitializer;
import com.trackharbor.trackharbor.model.UserProfile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class UserService {

    private final Firestore db;

    public UserService() {
        this.db = FirebaseInitializer.getFirestore();
    }

    public UserProfile getUserById(String userId) {
        try {
            DocumentReference userRef = db.collection("users").document(userId);
            ApiFuture<DocumentSnapshot> future = userRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                return null;
            }

            UserProfile user = new UserProfile();
            user.setId(document.getId());
            user.setFirstName(document.getString("firstName"));
            user.setLastName(document.getString("lastName"));
            user.setEmail(document.getString("email"));

            Timestamp createdAt = document.getTimestamp("createdAt");
            if (createdAt != null) {
                user.setCreatedAt(createdAt.toDate().toInstant());
            }

            return user;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user with ID: " + userId, e);
        }
    }

    public void createUser(UserProfile user) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("firstName", user.getFirstName());
            data.put("lastName", user.getLastName());
            data.put("email", user.getEmail());

            if (user.getCreatedAt() != null) {
                data.put("createdAt",
                        Timestamp.ofTimeSecondsAndNanos(
                                user.getCreatedAt().getEpochSecond(),
                                user.getCreatedAt().getNano()
                        )
                );
            } else {
                data.put("createdAt", Timestamp.now());
            }

            db.collection("users")
                    .document(user.getId())
                    .set(data)
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user with ID: " + user.getId(), e);
        }
    }

    public boolean userExists(String userId) {
        try {
            DocumentSnapshot document = db.collection("users")
                    .document(userId)
                    .get()
                    .get();

            return document.exists();

        } catch (Exception e) {
            throw new RuntimeException("Failed to check if user exists: " + userId, e);
        }
    }
}
