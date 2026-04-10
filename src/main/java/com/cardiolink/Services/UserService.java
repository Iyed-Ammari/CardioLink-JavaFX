package com.cardiolink.Services;

import com.cardiolink.Models.User;
import com.cardiolink.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // 🔐 LOGIN — supporte BCrypt ($2y$) ET mot de passe en clair
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
                // ✅ Password hashé Symfony BCrypt
                // $2y$ (PHP) → $2a$ (Java) — jbcrypt utilise $2a$
                String javaHash = storedPassword.replace("$2y$", "$2a$");
                passwordOk = BCrypt.checkpw(password, javaHash);
            } else {
                // ✅ Password en clair (anciens comptes test)
                passwordOk = storedPassword.equals(password);
            }

            if (passwordOk) return mapUser(rs);
        }
        return null;
    }

    // 📋 LISTE tous les users
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user";
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) list.add(mapUser(rs));
        return list;
    }

    // ➕ AJOUTER — password hashé BCrypt + format roles Symfony
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO user (email, password, nom, prenom, roles, adresse, tel, " +
                "is_active, is_verified, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, NOW())";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        // 🔒 Hasher le password avec BCrypt ($2a$ compatible PHP $2y$)
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));

        stmt.setString(1, user.getEmail());
        stmt.setString(2, hashedPassword);
        stmt.setString(3, user.getNom());
        stmt.setString(4, user.getPrenom());
        // ROLE_PATIENT → ["ROLE_PATIENT"] format Symfony
        stmt.setString(5, "[\"" + user.getRoles() + "\"]");
        stmt.setString(6, user.getAdresse());
        stmt.setString(7, user.getTel());
        stmt.executeUpdate();
    }

    // ✏️ MODIFIER — si password fourni on le re-hashe, sinon on garde l'ancien
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE user SET email=?, nom=?, prenom=?, roles=?, adresse=?, tel=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, user.getEmail());
        stmt.setString(2, user.getNom());
        stmt.setString(3, user.getPrenom());
        stmt.setString(4, "[\"" + user.getRoles() + "\"]");
        stmt.setString(5, user.getAdresse());
        stmt.setString(6, user.getTel());
        stmt.setInt(7, user.getId());
        stmt.executeUpdate();
    }

    // ✏️ MODIFIER PASSWORD séparément
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        stmt.setString(1, hashed);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
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