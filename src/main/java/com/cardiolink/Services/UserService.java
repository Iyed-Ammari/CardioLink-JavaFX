package com.cardiolink.Services;

import com.cardiolink.Models.User;
import com.cardiolink.utils.DatabaseConnection;
import com.cardiolink.utils.ManagerSession;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements Iservice<User> {

    @Override
    public void add(User user) throws SQLDataException {
        try {
            String sql = "INSERT INTO user (email, password, nom, prenom, " +
                    "roles, adresse, tel, is_active, is_verified, " +
                    "created_at) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, NOW())";
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            // ✅ Toujours hasher avec $2a$ (compatible jBCrypt)
            String salt   = BCrypt.gensalt(10); // 10 au lieu de 12 = plus rapide
            String hashed = BCrypt.hashpw(user.getPassword(), salt);

            stmt.setString(1, user.getEmail());
            stmt.setString(2, hashed);
            stmt.setString(3, user.getNom());
            stmt.setString(4, user.getPrenom());
            stmt.setString(5, "[\"" + user.getRoles() + "\"]");
            stmt.setString(6, user.getAdresse() != null ?
                    user.getAdresse() : "");
            stmt.setString(7, user.getTel() != null ?
                    user.getTel() : "");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void delete(User user) throws SQLDataException {
        try {
            String sql = "DELETE FROM user WHERE id = ?";
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    @Override
    public void update(User user) throws SQLDataException {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE user SET nom=?, prenom=?, tel=?, adresse=?, image_url=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getNom());
                ps.setString(2, user.getPrenom());
                ps.setString(3, user.getTel()     != null ? user.getTel()     : "");
                ps.setString(4, user.getAdresse() != null ? user.getAdresse() : "");
                ps.setString(5, user.getImageUrl());
                ps.setInt   (6, user.getId());
                ps.executeUpdate();
            }
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String sqlPwd = "UPDATE user SET password=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlPwd)) {
                    String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
                    ps.setString(1, hashed);
                    ps.setInt   (2, user.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    @Override
    public List<User> getAll() throws SQLDataException {
        try {
            List<User> list = new ArrayList<>();
            String sql = "SELECT * FROM user";
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapUser(rs));
            return list;
        } catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    @Override
    public User getById(int id) throws SQLDataException {
        try {
            String sql = "SELECT * FROM user WHERE id = ?";
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
            return null;
        } catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String storedPassword = rs.getString("password");
            boolean passwordOk = false;

            // ✅ Gérer tous les préfixes BCrypt : $2a$, $2b$, $2y$
            if (storedPassword != null && storedPassword.startsWith("$2")) {
                // ✅ Normaliser vers $2a$ pour jBCrypt
                String normalizedHash = storedPassword
                        .replaceFirst("^\\$2[by]\\$", "\\$2a\\$");
                try {
                    passwordOk = BCrypt.checkpw(password, normalizedHash);
                } catch (Exception e) {
                    // ✅ Si BCrypt échoue, essayer comparaison directe
                    passwordOk = storedPassword.equals(password);
                }
            } else {
                // Mot de passe en clair (ancien)
                passwordOk = storedPassword != null &&
                        storedPassword.equals(password);
            }

            if (passwordOk) {
                if (!rs.getBoolean("is_verified")) {
                    throw new SQLException("EMAIL_NOT_VERIFIED");
                }
                if (!rs.getBoolean("is_active")) {
                    throw new SQLException("ACCOUNT_BLOCKED");
                }
                return mapUser(rs);
            }
        }
        return null;
    }

    public void addUser(User user) throws SQLException {
        // ✅ Vérifier si email déjà utilisé
        User existing = findByEmail(user.getEmail());
        if (existing != null) {
            if (!existing.isVerified()) {
                deleteUser(existing.getId());
            } else {
                throw new SQLException(
                        "Duplicate entry '" + user.getEmail() + "'");
            }
        }
        try { add(user); }
        catch (SQLDataException e) { throw new SQLException(e.getMessage()); }
    }

    public void updateUser(User user) throws SQLException {
        try { update(user); }
        catch (SQLDataException e) { throw new SQLException(e.getMessage()); }
    }

    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }

    public List<User> getAllUsers() throws SQLException {
        try { return getAll(); }
        catch (SQLDataException e) { throw new SQLException(e.getMessage()); }
    }

    public void setActive(int userId, boolean active) throws SQLException {
        String sql = "UPDATE user SET is_active = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setBoolean(1, active);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapUser(rs);
        return null;
    }

    public long countRegistrationsThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user " +
                "WHERE MONTH(created_at) = MONTH(NOW()) " +
                "AND YEAR(created_at) = YEAR(NOW())";
        Connection conn = DatabaseConnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) return rs.getLong(1);
        return 0;
    }

    public void updateUserRole(int userId, String role) throws SQLException {
        String sql = "UPDATE user SET roles = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "[\"" + role + "\"]");
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    // ✅ APRÈS — version corrigée
    public void updatePassword(int userId, String newPassword)
            throws SQLException {
        String toStore;
        if (newPassword.startsWith("$2")) {
            toStore = newPassword; // déjà hashé
        } else {
            toStore = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        }
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, toStore);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapUser(rs);
        return null;
    }

    /**
     * Retourne tous les utilisateurs ayant le rôle ROLE_MEDECIN.
     * La colonne roles stocke la valeur sous la forme : ["ROLE_MEDECIN"]
     */
    public List<User> getMedecins() throws SQLException {
        String sql = "SELECT * FROM user WHERE roles LIKE '%ROLE_MEDECIN%' AND is_active = 1";
        Connection conn = DatabaseConnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<User> medecins = new ArrayList<>();
        while (rs.next()) medecins.add(mapUser(rs));
        return medecins;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User(
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

       //ligne pour model ia matching
        user.setInterestVector(rs.getString("interest_vector"));

        return user;
    }
    // ── Sauvegarder l'image de visage ────────────────────────────
    public void saveFaceImage(int userId, String base64Image) throws SQLException {
        String sql = "UPDATE user SET face_image = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, base64Image);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    // ── Récupérer l'image de visage ──────────────────────────────
    public String getFaceImage(String email) throws SQLException {
        String sql = "SELECT face_image FROM user WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("face_image");
        return null;
    }
}