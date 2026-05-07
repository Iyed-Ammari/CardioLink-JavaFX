package com.cardiolink.utils;

import com.cardiolink.Models.Produit;
import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ManagerSession {

    private static ManagerSession instance;
    private int  currentUserId = -1;
    private User currentUser   = null;

    // ── Favoris : IDs des produits mis en favori (en mémoire)
    private final Set<Integer> favoris = new HashSet<>();

    // ── Récemment vus : max 3 produits, FIFO
    private final LinkedList<Produit> recentlyViewed = new LinkedList<>();
    private static final int MAX_RECENTLY_VIEWED = 3;

    // ── Produits déjà notés par ce patient (en mémoire)
    private final Set<Integer> produitsNotes = new HashSet<>();

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
        this.favoris.clear();
        this.recentlyViewed.clear();
        this.produitsNotes.clear();
    }

    public boolean isLoggedIn() { return currentUserId != -1; }

    // ── FAVORIS ──────────────────────────────────────────────

    public boolean isFavori(int produitId) {
        return favoris.contains(produitId);
    }

    public void toggleFavori(int produitId) {
        if (favoris.contains(produitId)) favoris.remove(produitId);
        else favoris.add(produitId);
    }

    public Set<Integer> getFavoris() {
        return favoris;
    }

    // ── RÉCEMMENT VUS ────────────────────────────────────────

    public void ajouterRecentlyViewed(Produit produit) {
        if (produit == null) return;
        // Supprimer si déjà présent pour éviter les doublons
        recentlyViewed.removeIf(p -> p.getId().equals(produit.getId()));
        // Ajouter en tête
        recentlyViewed.addFirst(produit);
        // Garder max 3
        while (recentlyViewed.size() > MAX_RECENTLY_VIEWED) {
            recentlyViewed.removeLast();
        }
    }

    public List<Produit> getRecentlyViewed() {
        return new ArrayList<>(recentlyViewed);
    }

    // ── AVIS ÉTOILES ─────────────────────────────────────────

    public boolean aDejaNote(int produitId) {
        return produitsNotes.contains(produitId);
    }

    public void marquerCommeNote(int produitId) {
        produitsNotes.add(produitId);
    }
}