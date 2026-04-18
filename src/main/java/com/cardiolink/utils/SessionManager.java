package com.cardiolink.utils;

public class SessionManager {

    private static SessionManager instance;
    private int currentUserId = -1; // ✅ Juste l'ID !

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUserId(int id) { this.currentUserId = id; }
    public int  getCurrentUserId()       { return currentUserId; }
    public void logout()                 { this.currentUserId = -1; }
    public boolean isLoggedIn()          { return currentUserId != -1; }
}