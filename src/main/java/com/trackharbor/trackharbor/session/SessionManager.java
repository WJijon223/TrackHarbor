package com.trackharbor.trackharbor.session;

import com.trackharbor.trackharbor.model.UserProfile;

public class SessionManager {

    private static UserProfile currentUser;

    public static void setCurrentUser(UserProfile user) {
        currentUser = user;
    }

    public static UserProfile getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in.");
        }
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}