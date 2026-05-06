package com.cardiolink.Services;

import com.cardiolink.Models.MessageReaction;
import com.cardiolink.Models.ReactionSummary;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service CRUD pour les réactions emoji sur les messages.
 *
 * Règle métier : contrainte unique (message_id, user_id, emoji)
 * → On utilise INSERT IGNORE pour éviter les doublons silencieusement.
 */
public class MessageReactionService {

    private final Connection connection;

    public MessageReactionService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /* ─────────────────────────────────────────────────────────────────
       findReaction — vérifie si une réaction existe déjà
    ───────────────────────────────────────────────────────────────── */
    public Optional<MessageReaction> findReaction(int messageId, int userId, String emoji) {
        String sql = "SELECT * FROM message_reaction WHERE message_id = ? AND user_id = ? AND emoji = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setInt(2, userId);
            ps.setString(3, emoji);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MessageReactionService] findReaction: " + e.getMessage());
        }
        return Optional.empty();
    }

    /* ─────────────────────────────────────────────────────────────────
       addReaction — INSERT IGNORE (respecte la contrainte unique)
    ───────────────────────────────────────────────────────────────── */
    public boolean addReaction(MessageReaction reaction) {
        // INSERT IGNORE : si la combinaison existe déjà, pas d'erreur
        String sql = "INSERT IGNORE INTO message_reaction (message_id, user_id, emoji, created_at) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reaction.getMessageId());
            ps.setInt(2, reaction.getUserId());
            ps.setString(3, reaction.getEmoji());
            ps.setString(4, reaction.getCreatedAt());
            int rows = ps.executeUpdate();
            return rows > 0; // false si doublon ignoré
        } catch (SQLException e) {
            System.err.println("[MessageReactionService] addReaction: " + e.getMessage());
            return false;
        }
    }

    /* ─────────────────────────────────────────────────────────────────
       removeReaction — supprime une réaction spécifique
    ───────────────────────────────────────────────────────────────── */
    public boolean removeReaction(int messageId, int userId, String emoji) {
        String sql = "DELETE FROM message_reaction WHERE message_id = ? AND user_id = ? AND emoji = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setInt(2, userId);
            ps.setString(3, emoji);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MessageReactionService] removeReaction: " + e.getMessage());
            return false;
        }
    }

    /* ─────────────────────────────────────────────────────────────────
       toggleReaction — ajoute si absent, retire si présent
       Retourne true = réaction ajoutée, false = retirée
    ───────────────────────────────────────────────────────────────── */
    public boolean toggleReaction(int messageId, int userId, String emoji) {
        Optional<MessageReaction> existing = findReaction(messageId, userId, emoji);
        if (existing.isPresent()) {
            removeReaction(messageId, userId, emoji);
            return false; // retirée
        } else {
            String now = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            addReaction(new MessageReaction(messageId, userId, emoji, now));
            return true;  // ajoutée
        }
    }

    /* ─────────────────────────────────────────────────────────────────
       findReactionsSummary — agrège COUNT(*) GROUP BY emoji pour un message
       Retourne ex: [{"👍", 3}, {"❤️", 1}]
    ───────────────────────────────────────────────────────────────── */
    public List<ReactionSummary> findReactionsSummary(int messageId) {
        List<ReactionSummary> summaries = new ArrayList<>();
        String sql = "SELECT emoji, COUNT(*) AS cnt FROM message_reaction "
                   + "WHERE message_id = ? GROUP BY emoji ORDER BY cnt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summaries.add(new ReactionSummary(
                            rs.getString("emoji"),
                            rs.getInt("cnt")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MessageReactionService] findReactionsSummary: " + e.getMessage());
        }
        return summaries;
    }

    /* ─────────────────────────────────────────────────────────────────
       getReactionsByMessage — toutes les réactions d'un message
    ───────────────────────────────────────────────────────────────── */
    public List<MessageReaction> getReactionsByMessage(int messageId) {
        List<MessageReaction> list = new ArrayList<>();
        String sql = "SELECT * FROM message_reaction WHERE message_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MessageReactionService] getReactionsByMessage: " + e.getMessage());
        }
        return list;
    }

    /* ─────────────────────────────────────────────────────────────────
       Helpers
    ───────────────────────────────────────────────────────────────── */
    private MessageReaction mapRow(ResultSet rs) throws SQLException {
        return new MessageReaction(
                rs.getInt("id"),
                rs.getInt("message_id"),
                rs.getInt("user_id"),
                rs.getString("emoji"),
                rs.getString("created_at")
        );
    }
}
