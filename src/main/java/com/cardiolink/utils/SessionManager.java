package com.cardiolink.utils;


public final class SessionManager {

    private static int currentUserId = 4; // valeur par défaut pour les tests

    private SessionManager() {}

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        if (userId <= 0)
            throw new IllegalArgumentException("userId invalide : doit être > 0.");
        currentUserId = userId;
    }

    public static boolean isLoggedIn() {
        return currentUserId > 0;
    }

    public static void logout() {
        currentUserId = -1;
    }
}