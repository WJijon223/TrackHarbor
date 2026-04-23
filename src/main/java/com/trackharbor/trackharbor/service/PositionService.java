package com.trackharbor.trackharbor.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.trackharbor.trackharbor.config.FirebaseInitializer;
import com.trackharbor.trackharbor.model.Position;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionService {

    private final Firestore db;

    public PositionService() {
        this.db = FirebaseInitializer.getFirestore();
    }

    public List<Position> getPositionsForUser(String userId) {
        List<Position> positions = new ArrayList<>();

        try {
            CollectionReference positionsRef = db.collection("users")
                    .document(userId)
                    .collection("positions");

            ApiFuture<QuerySnapshot> future = positionsRef.get();
            QuerySnapshot snapshot = future.get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Position position = mapDocumentToPosition(document);
                positions.add(position);
            }

            return positions;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch positions for user: " + userId, e);
        }
    }

    public Position getPositionById(String userId, String positionId) {
        try {
            DocumentSnapshot document = db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(positionId)
                    .get()
                    .get();

            if (!document.exists()) {
                return null;
            }

            return mapDocumentToPosition(document);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch position: " + positionId, e);
        }
    }

    public String createPosition(String userId, Position position) {
        try {
            CollectionReference positionsRef = db.collection("users")
                    .document(userId)
                    .collection("positions");

            DocumentReference newDocRef;

            if (position.getId() != null && !position.getId().isBlank()) {
                newDocRef = positionsRef.document(position.getId());
            } else {
                newDocRef = positionsRef.document();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", position.getName());
            data.put("link", position.getLink());
            data.put("applied", position.isApplied());

            if (position.getDateApplied() != null) {
                data.put("dateApplied", position.getDateApplied().toString());
            } else {
                data.put("dateApplied", null);
            }

            data.put("status", position.getStatus());
            data.put("aiTipsGenerated", position.isAiTipsGenerated());
            data.put("aiTips", position.getAiTips() != null ? position.getAiTips() : new ArrayList<>());

            data.put("createdAt",
                    position.getCreatedAt() != null
                            ? Timestamp.ofTimeSecondsAndNanos(
                            position.getCreatedAt().getEpochSecond(),
                            position.getCreatedAt().getNano()
                    )
                            : Timestamp.now()
            );

            data.put("updatedAt",
                    position.getUpdatedAt() != null
                            ? Timestamp.ofTimeSecondsAndNanos(
                            position.getUpdatedAt().getEpochSecond(),
                            position.getUpdatedAt().getNano()
                    )
                            : Timestamp.now()
            );

            newDocRef.set(data).get();
            return newDocRef.getId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create position for user: " + userId, e);
        }
    }

    public void updatePosition(String userId, Position position) {
        try {
            if (position.getId() == null || position.getId().isBlank()) {
                throw new IllegalArgumentException("Position ID is required for update.");
            }

            DocumentReference positionRef = db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(position.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("name", position.getName());
            data.put("link", position.getLink());
            data.put("applied", position.isApplied());
            data.put("dateApplied", position.getDateApplied() != null ? position.getDateApplied().toString() : null);
            data.put("status", position.getStatus());
            data.put("aiTipsGenerated", position.isAiTipsGenerated());
            data.put("aiTips", position.getAiTips() != null ? position.getAiTips() : new ArrayList<>());
            data.put("updatedAt", Timestamp.now());

            positionRef.update(data).get();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update position: " + position.getId(), e);
        }
    }

    public void deletePosition(String userId, String positionId) {
        try {
            db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(positionId)
                    .delete()
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete position: " + positionId, e);
        }
    }

    private Position mapDocumentToPosition(DocumentSnapshot document) {
        Position position = new Position();

        position.setId(document.getId());
        position.setName(document.getString("name"));
        position.setLink(document.getString("link"));

        Boolean applied = document.getBoolean("applied");
        position.setApplied(applied != null && applied);

        position.setStatus(document.getString("status"));

        String dateApplied = document.getString("dateApplied");
        if (dateApplied != null && !dateApplied.isBlank()) {
            position.setDateApplied(LocalDate.parse(dateApplied));
        }

        Boolean aiTipsGenerated = document.getBoolean("aiTipsGenerated");
        position.setAiTipsGenerated(aiTipsGenerated != null && aiTipsGenerated);

        List<String> aiTips = (List<String>) document.get("aiTips");
        position.setAiTips(aiTips != null ? aiTips : new ArrayList<>());

        Timestamp createdAt = document.getTimestamp("createdAt");
        if (createdAt != null) {
            position.setCreatedAt(createdAt.toDate().toInstant());
        }

        Timestamp updatedAt = document.getTimestamp("updatedAt");
        if (updatedAt != null) {
            position.setUpdatedAt(updatedAt.toDate().toInstant());
        }

        return position;
    }
}