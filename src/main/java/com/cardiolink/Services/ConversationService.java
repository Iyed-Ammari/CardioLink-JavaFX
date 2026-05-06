package com.cardiolink.Services;

import com.cardiolink.Models.Conversation;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationService implements Iservice<Conversation> {
    private Connection connection;

    public ConversationService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Conversation conversation) throws SQLDataException {
        String request = "INSERT INTO conversation (patient_id, medecin_id, created_at, updated_at, is_active) VALUES (?,?,?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, conversation.getPatientId());
            pstmt.setInt(2, conversation.getMedecinId());
            pstmt.setString(3, conversation.getCreated_at());
            pstmt.setString(4, conversation.getUpdated_at());
            pstmt.setBoolean(5, conversation.isActive());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding conversation: " + e.getMessage());
        }
    }

    @Override
    public void delete(Conversation conversation) throws SQLDataException {
        String request = "DELETE FROM conversation WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, conversation.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting conversation: " + e.getMessage());
        }
    }

    @Override
    public void update(Conversation conversation) throws SQLDataException {
        String request = "UPDATE conversation SET patient_id = ?, medecin_id = ?, created_at = ?, updated_at = ?, is_active = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, conversation.getPatientId());
            pstmt.setInt(2, conversation.getMedecinId());
            pstmt.setString(3, conversation.getCreated_at());
            pstmt.setString(4, conversation.getUpdated_at());
            pstmt.setBoolean(5, conversation.isActive());
            pstmt.setInt(6, conversation.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating conversation: " + e.getMessage());
        }
    }

    @Override
    public List<Conversation> getAll() throws SQLDataException {
        String request = "SELECT * FROM conversation";
        List<Conversation> conversations = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(request)) {

            while (result.next()) {
                Conversation conversation = new Conversation();
                conversation.setId(result.getInt("id"));
                conversation.setPatientId(result.getInt("patient_id"));
                conversation.setMedecinId(result.getInt("medecin_id"));
                conversation.setCreated_at(result.getString("created_at"));
                conversation.setUpdated_at(result.getString("updated_at"));
                conversation.setActive(result.getBoolean("is_active"));
                conversations.add(conversation);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all conversations: " + e.getMessage());
        }
        return conversations;
    }

    @Override
    public Conversation getById(int id) throws SQLDataException {
        String request = "SELECT * FROM conversation WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, id);
            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    Conversation conversation = new Conversation();
                    conversation.setId(result.getInt("id"));
                    conversation.setPatientId(result.getInt("patient_id"));
                    conversation.setMedecinId(result.getInt("medecin_id"));
                    conversation.setCreated_at(result.getString("created_at"));
                    conversation.setUpdated_at(result.getString("updated_at"));
                    conversation.setActive(result.getBoolean("is_active"));

                    return conversation;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching conversation by ID: " + e.getMessage());
        }

        return null;
    }

    /* ─────────────────────────────────────────────────────────────────
       getConversationsByUser — conversations où l'user est patient OU médecin
    ───────────────────────────────────────────────────────────────── */
    public List<Conversation> getConversationsByUser(int userId) {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT * FROM conversation "
                   + "WHERE patient_id = ? OR medecin_id = ? "
                   + "ORDER BY updated_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) conversations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ConversationService] getConversationsByUser: " + e.getMessage());
        }
        return conversations;
    }

    /* ─────────────────────────────────────────────────────────────────
       findByUserWithSearchAndSort — Query builder avancé

       Paramètres :
         userId  → utilisateur connecté (patient_id OR medecin_id)
         search  → filtre sur le nom du contact (null = pas de filtre)
         sortBy  → "updated" | "created" | "contact" | "status"
         order   → "ASC" | "DESC"

       Jointures avec la table user pour :
         up (user patient)  → permettre filtre/tri par nom patient
         um (user médecin) → permettre filtre/tri par nom médecin
    ───────────────────────────────────────────────────────────────── */
    public List<Conversation> findByUserWithSearchAndSort(
            int userId, String search, String sortBy, String order) {

        List<Conversation> conversations = new ArrayList<>();

        // Sécurisation de l'ordre (anti-injection)
        String safeOrder = "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";

        // Colonne de tri
        String orderClause;
        switch (sortBy == null ? "updated" : sortBy.toLowerCase()) {
            case "created":
                orderClause = "c.created_at";
                break;
            case "contact":
                // Tri par nom du contact (l'autre participant)
                orderClause = "CASE WHEN c.patient_id = ? THEN um.nom ELSE up.nom END";
                break;
            case "status":
                orderClause = "c.is_active";
                break;
            default: // "updated"
                orderClause = "c.updated_at";
                break;
        }

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasDynamicSort = "contact".equalsIgnoreCase(sortBy);

        StringBuilder sql = new StringBuilder(
            "SELECT c.* FROM conversation c "
          + "LEFT JOIN user up ON c.patient_id  = up.id "
          + "LEFT JOIN user um ON c.medecin_id = um.id "
          + "WHERE (c.patient_id = ? OR c.medecin_id = ?) "
        );

        if (hasSearch) {
            sql.append("AND (up.nom LIKE ? OR up.prenom LIKE ? "
                     + "OR um.nom LIKE ? OR um.prenom LIKE ?) ");
        }

        sql.append("ORDER BY ").append(orderClause).append(" ").append(safeOrder);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;

            // Paramètre pour hasDynamicSort (CASE WHEN)
            if (hasDynamicSort) ps.setInt(idx++, userId);

            // WHERE patient_id = ? OR medecin_id = ?
            ps.setInt(idx++, userId);
            ps.setInt(idx++, userId);

            // Filtre de recherche
            if (hasSearch) {
                String like = "%" + search.trim() + "%";
                ps.setString(idx++, like); // up.nom
                ps.setString(idx++, like); // up.prenom
                ps.setString(idx++, like); // um.nom
                ps.setString(idx++, like); // um.prenom
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) conversations.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ConversationService] findByUserWithSearchAndSort: " + e.getMessage());
        }

        return conversations;
    }

    /* ──────── Helper mapper ────────────────────────────────────────── */
    private Conversation mapRow(ResultSet rs) throws SQLException {
        Conversation c = new Conversation();
        c.setId(rs.getInt("id"));
        c.setPatientId(rs.getInt("patient_id"));
        c.setMedecinId(rs.getInt("medecin_id"));
        c.setCreated_at(rs.getString("created_at"));
        c.setUpdated_at(rs.getString("updated_at"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}