package com.cardiolink.utils;

public class ManagerSession {

    private static ManagerSession instance;
    private int currentUserId = -1;

    private ManagerSession() {}

    public static ManagerSession getInstance() {
        if (instance == null) instance = new ManagerSession();
        return instance;
    }

    public void setCurrentUserId(int id) { this.currentUserId = id; }
    public int  getCurrentUserId()       { return currentUserId; }
    public void logout()                 { this.currentUserId = -1; }
    public boolean isLoggedIn()          { return currentUserId != -1; }
}