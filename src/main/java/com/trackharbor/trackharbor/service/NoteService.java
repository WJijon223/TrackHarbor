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
import com.trackharbor.trackharbor.model.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteService {

    private final Firestore db;

    public NoteService() {
        this.db = FirebaseInitializer.getFirestore();
    }

    public List<Note> getNotesForPosition(String userId, String positionId) {
        List<Note> notes = new ArrayList<>();

        try {
            CollectionReference notesRef = db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(positionId)
                    .collection("notes");

            ApiFuture<QuerySnapshot> future = notesRef.get();
            QuerySnapshot snapshot = future.get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Note note = mapDocumentToNote(document);
                notes.add(note);
            }

            return notes;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch notes for position: " + positionId, e);
        }
    }

    public Note getNoteById(String userId, String positionId, String noteId) {
        try {
            DocumentSnapshot document = db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(positionId)
                    .collection("notes")
                    .document(noteId)
                    .get()
                    .get();

            if (!document.exists()) {
                return null;
            }

            return mapDocumentToNote(document);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch note: " + noteId, e);
        }
    }

    public String createNote(String userId, String positionId, Note note) {
        try {
            CollectionReference notesRef = db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(positionId)
                    .collection("notes");

            DocumentReference newDocRef;

            if (note.getId() != null && !note.getId().isBlank()) {
                newDocRef = notesRef.document(note.getId());
            } else {
                newDocRef = notesRef.document();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("content", note.getContent());
            data.put("createdAt",
                    note.getCreatedAt() != null
                            ? Timestamp.ofTimeSecondsAndNanos(
                            note.getCreatedAt().getEpochSecond(),
                            note.getCreatedAt().getNano()
                    )
                            : Timestamp.now()
            );

            newDocRef.set(data).get();
            return newDocRef.getId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create note for position: " + positionId, e);
        }
    }

    public void updateNote(String userId, String positionId, Note note) {
        try {
            if (note.getId() == null || note.getId().isBlank()) {
                throw new IllegalArgumentException("Note ID is required for update.");
            }

            DocumentReference noteRef = db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(positionId)
                    .collection("notes")
                    .document(note.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("content", note.getContent());

            noteRef.update(data).get();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update note: " + note.getId(), e);
        }
    }

    public void deleteNote(String userId, String positionId, String noteId) {
        try {
            db.collection("users")
                    .document(userId)
                    .collection("positions")
                    .document(positionId)
                    .collection("notes")
                    .document(noteId)
                    .delete()
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete note: " + noteId, e);
        }
    }

    private Note mapDocumentToNote(DocumentSnapshot document) {
        Note note = new Note();

        note.setId(document.getId());
        note.setContent(document.getString("content"));

        Timestamp createdAt = document.getTimestamp("createdAt");
        if (createdAt != null) {
            note.setCreatedAt(createdAt.toDate().toInstant());
        }

        return note;
    }
}