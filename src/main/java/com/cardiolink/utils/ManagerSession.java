package com.cardiolink.utils;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import java.sql.SQLException;

public class ManagerSession {

    private static ManagerSession instance;
    private int  currentUserId = -1;
    private User currentUser   = null;

    private ManagerSession() {}

    public static ManagerSession getInstance() {
        if (instance == null) instance = new ManagerSession();
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser   = user;
        this.currentUserId = user != null ? user.getId() : -1;
    }

    public void setCurrentUserId(int id) { this.currentUserId = id; }
    public int  getCurrentUserId()       { return currentUserId; }

    public User getCurrentUser() {
        if (currentUser != null) return currentUser;
        if (currentUserId == -1) return null;
        try {
            currentUser = new UserService().getUserById(currentUserId);
        } catch (SQLException e) { e.printStackTrace(); }
        return currentUser;
    }

    public void logout() {
        this.currentUserId = -1;
        this.currentUser   = null;
    }

    public boolean isLoggedIn() { return currentUserId != -1; }
}