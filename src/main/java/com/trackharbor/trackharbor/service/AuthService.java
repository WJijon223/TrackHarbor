package com.trackharbor.trackharbor.service;

import io.github.cdimascio.dotenv.Dotenv;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    // Load environment variables from .env
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static final String API_KEY = dotenv.get("FIREBASE_API_KEY");

    private final HttpClient httpClient;

    public AuthService() {
        this.httpClient = HttpClient.newHttpClient();

        // Safety check
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("FIREBASE_API_KEY not found in .env file.");
        }
    }

    /**
     * Signs in a user with email and password using Firebase Auth REST API.
     *
     * @param email user's email
     * @param password user's password
     * @return Firebase userId (localId)
     */
    public String signInWithEmailAndPassword(String email, String password) {
        try {
            String endpoint =
                    "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                            + API_KEY;

            String requestBody = """
                    {
                        "email": "%s",
                        "password": "%s",
                        "returnSecureToken": true
                    }
                    """.formatted(email, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            // Handle Firebase error response
            if (response.statusCode() != 200) {
                String errorMessage = json
                        .getAsJsonObject("error")
                        .get("message")
                        .getAsString();

                throw new RuntimeException("Firebase Auth failed: " + errorMessage);
            }

            // Return Firebase UID
            return json.get("localId").getAsString();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to sign in user.", e);
        }
    }

    public String registerWithEmailAndPassword(String email, String password) {
        try {
            String endpoint =
                    "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key="
                            + API_KEY;

            String requestBody = """
                {
                    "email": "%s",
                    "password": "%s",
                    "returnSecureToken": true
                }
                """.formatted(email, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            if (response.statusCode() != 200) {
                String errorMessage = json
                        .getAsJsonObject("error")
                        .get("message")
                        .getAsString();

                throw new RuntimeException("Firebase registration failed: " + errorMessage);
            }

            return json.get("localId").getAsString();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to register user.", e);
        }
    }
}
