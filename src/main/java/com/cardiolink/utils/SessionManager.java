package com.cardiolink.utils;

public final class SessionManager {

    private static int currentUserId = -1;
    private static String currentUserEmail;
    private static String currentUserRole;

    private SessionManager() {}

    public static void setSession(int userId, String email, String role) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId invalide : doit être > 0.");
        }

        currentUserId = userId;
        currentUserEmail = email;
        currentUserRole = role;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static boolean isLoggedIn() {
        return currentUserId > 0;
    }

    public static boolean isPatient() {
        return currentUserRole != null && currentUserRole.contains("ROLE_PATIENT");
    }

    public static boolean isMedecin() {
        return currentUserRole != null && currentUserRole.contains("ROLE_MEDECIN");
    }

    public static void logout() {
        currentUserId = -1;
        currentUserEmail = null;
        currentUserRole = null;
    }
}