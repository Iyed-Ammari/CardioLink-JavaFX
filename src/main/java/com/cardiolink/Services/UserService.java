package com.cardiolink.Services;

import com.cardiolink.Models.User;
import com.cardiolink.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // 🔐 LOGIN
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String storedPassword = rs.getString("password");
            boolean passwordOk;
            if (storedPassword.startsWith("$2y$") || storedPassword.startsWith("$2a$")) {
                String javaHash = storedPassword.replace("$2y$", "$2a$");
                passwordOk = BCrypt.checkpw(password, javaHash);
            } else {
                passwordOk = storedPassword.equals(password);
            }
            if (passwordOk) return mapUser(rs);
        }
        return null;
    }

    // 📋 LISTE
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user";
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) list.add(mapUser(rs));
        return list;
    }

    // ➕ AJOUTER
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO user (email, password, nom, prenom, roles, adresse, tel, " +
                "is_active, is_verified, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, NOW())";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        stmt.setString(1, user.getEmail());
        stmt.setString(2, hashedPassword);
        stmt.setString(3, user.getNom());
        stmt.setString(4, user.getPrenom());
        stmt.setString(5, "[\"" + user.getRoles() + "\"]");
        stmt.setString(6, user.getAdresse());
        stmt.setString(7, user.getTel());
        stmt.executeUpdate();
    }

    // ✏️ MODIFIER — gère nom/prenom/tel/adresse + password optionnel + image
    public void updateUser(User user) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        // Mise à jour des infos de base
        String sql = "UPDATE user SET nom=?, prenom=?, tel=?, adresse=?, image_url=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getTel());
            ps.setString(4, user.getAdresse());
            ps.setString(5, user.getImageUrl());
            ps.setInt   (6, user.getId());
            ps.executeUpdate();
        }

        // Mise à jour du password seulement s'il est fourni
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String sqlPwd = "UPDATE user SET password=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlPwd)) {
                String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
                ps.setString(1, hashed);
                ps.setInt   (2, user.getId());
                ps.executeUpdate();
            }
        }
    }

    // 🗑️ SUPPRIMER
    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }

    // 🔄 Mapper ResultSet → User
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("roles"),
                rs.getString("adresse"),
                rs.getString("tel"),
                rs.getString("cabinet"),
                rs.getBoolean("is_active"),
                rs.getBoolean("is_verified"),
                rs.getString("image_url")
        );
    }
}