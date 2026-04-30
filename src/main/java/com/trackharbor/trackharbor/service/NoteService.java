package com.trackharbor.trackharbor.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
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

    // private helpers

    /**
     * Returns the notes sub collection reference for a given user + position.
     * Centralizes the Firestore path so every method stays consistent.
     */


    private CollectionReference notesRef(String userId, String positionId) {

        return db.collection("users")
                .document(userId)
                .collection("positions")
                .document(positionId)
                .collection("notes");
    }

    /**
     * validates that userId and positionId are non-null and non-blank.
     * called at the top of every public method before touching Firestore.
     */


    private void validateParameters(String userId, String positionId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        if (positionId == null || positionId.isBlank()) {
            throw new IllegalArgumentException("Position ID cannot be null or blank");
        }
    }

    // read

    /**
     * Fetches all notes for a position, ordered oldest → newest by createdAt.
     * Use this when displaying notes in the UI so they appear in creation order.
     */


    public List<Note> getNotesForPosition(String userId, String positionId) {
        validateParameters(userId, positionId);

        try {
            QuerySnapshot snapshot = notesRef(userId, positionId)
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get()
                    .get();

            List<Note> notes = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                notes.add(mapDocumentToNote(doc));
            }
            return notes;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch notes for position: " + positionId, e);
        }
    }

    /**
     * Fetches a single note by its ID.
     * Returns null if the note does not exist.
     */


    public Note getNoteById(String userId, String positionId, String noteId) {
        validateParameters(userId, positionId);

        if (noteId == null || noteId.isBlank()) {
            throw new IllegalArgumentException("Note ID cannot be null or blank");
        }

        try {
            DocumentSnapshot doc = notesRef(userId, positionId)
                    .document(noteId)
                    .get()
                    .get();

            return doc.exists() ? mapDocumentToNote(doc) : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch note: " + noteId, e);
        }
    }

    /**
     * Returns the number of notes for a given position.
     * Used by NotesPageController to populate the note count pill on each card.
     */


    public int getNoteCountForPosition(String userId, String positionId) {
        validateParameters(userId, positionId);

        try {
            return notesRef(userId, positionId)
                    .get()
                    .get()
                    .size();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch note count for position: " + positionId, e);
        }
    }

    // create

    /**
     * Creates a new note under the given position.
     * If note.getId() is set it uses that as the document ID,
     * otherwise Firestore auto-generates one.
     *
     * @return the Firestore document ID of the newly created note
     */


    public String createNote(String userId, String positionId, Note note) {
        validateParameters(userId, positionId);

        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        try {
            CollectionReference ref = notesRef(userId, positionId);

            DocumentReference docRef = (note.getId() != null && !note.getId().isBlank())
                    ? ref.document(note.getId())
                    : ref.document();

            Map<String, Object> data = new HashMap<>();
            data.put("content", note.getContent());
            data.put("createdAt",
                    note.getCreatedAt() != null
                            ? Timestamp.ofTimeSecondsAndNanos(
                                note.getCreatedAt().getEpochSecond(),
                                note.getCreatedAt().getNano())
                            : Timestamp.now()
            );

            docRef.set(data).get();
            return docRef.getId();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create note for position: " + positionId, e);
        }
    }

    // update

    /**
     * Updates the content of an existing note.
     * Only the content field is updated; createdAt is left unchanged.
     *
     * @throws IllegalArgumentException if note.getId() is null or blank
     */
    public void updateNote(String userId, String positionId, Note note) {
        validateParameters(userId, positionId);

        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }
        if (note.getId() == null || note.getId().isBlank()) {
            throw new IllegalArgumentException("Note ID is required for update");
        }

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("content", note.getContent());

            notesRef(userId, positionId)
                    .document(note.getId())
                    .update(data)
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update note: " + note.getId(), e);
        }
    }

    // delete

    /**
     * Permanently deletes a note from Firestore.
     */
    public void deleteNote(String userId, String positionId, String noteId) {
        validateParameters(userId, positionId);

        if (noteId == null || noteId.isBlank()) {
            throw new IllegalArgumentException("Note ID cannot be null or blank");
        }

        try {
            notesRef(userId, positionId)
                    .document(noteId)
                    .delete()
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete note: " + noteId, e);
        }
    }

    // mapping

    /**
     * Maps a Firestore document snapshot to a Note model object.
     */

    private Note mapDocumentToNote(DocumentSnapshot doc) {
        Note note = new Note();
        note.setId(doc.getId());
        note.setContent(doc.getString("content"));

        Timestamp createdAt = doc.getTimestamp("createdAt");
        if (createdAt != null) {
            note.setCreatedAt(createdAt.toDate().toInstant());
        }

        return note;
    }
}
