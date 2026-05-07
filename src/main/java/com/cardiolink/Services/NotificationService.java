package com.cardiolink.Services;

import com.cardiolink.Models.Notification;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour les notifications.
 *
 * Notifications créées automatiquement à chaque nouveau message envoyé.
 * Le destinataire est l'autre participant de la conversation
 * (patient → médecin, médecin → patient).
 */
public class NotificationService {

    private final Connection connection;

    public NotificationService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /* ─────────────────────────────────────────────────────────────────
       createNotification — INSERT notification en DB
    ───────────────────────────────────────────────────────────────── */
    public void createNotification(Notification notif) {
        String sql = "INSERT INTO notification "
                   + "(content, is_read, created_at, recipient_id, sender_id, conversation_id, message_id) "
                   + "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, notif.getContent());
            ps.setBoolean(2, notif.isRead());
            ps.setString(3, notif.getCreatedAt());
            ps.setInt(4, notif.getRecipientId());
            ps.setInt(5, notif.getSenderId());
            ps.setInt(6, notif.getConversationId());
            ps.setInt(7, notif.getMessageId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] createNotification: " + e.getMessage());
        }
    }

    /* ─────────────────────────────────────────────────────────────────
       getUnreadByRecipient — notifications non lues d'un utilisateur
    ───────────────────────────────────────────────────────────────── */
    public List<Notification> getUnreadByRecipient(int recipientId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE recipient_id = ? AND is_read = 0 "
                   + "ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[NotificationService] getUnreadByRecipient: " + e.getMessage());
        }
        return list;
    }

    /* ─────────────────────────────────────────────────────────────────
       countUnread — nombre de notifications non lues
    ───────────────────────────────────────────────────────────────── */
    public int countUnread(int recipientId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE recipient_id = ? AND is_read = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[NotificationService] countUnread: " + e.getMessage());
        }
        return 0;
    }

    /* ─────────────────────────────────────────────────────────────────
       markAsRead — marque une notification individuelle comme lue
    ───────────────────────────────────────────────────────────────── */
    public void markAsRead(int notificationId) {
        String sql = "UPDATE notification SET is_read = 1 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] markAsRead: " + e.getMessage());
        }
    }

    /* ─────────────────────────────────────────────────────────────────
       markAllAsReadByConversation — marque toutes les notifs d'une conv
    ───────────────────────────────────────────────────────────────── */
    public void markAllAsReadByConversation(int recipientId, int conversationId) {
        String sql = "UPDATE notification SET is_read = 1 "
                   + "WHERE recipient_id = ? AND conversation_id = ? AND is_read = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            ps.setInt(2, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationService] markAllAsReadByConversation: " + e.getMessage());
        }
    }

    /* ─────────────────────────────────────────────────────────────────
       getAllByRecipient — toutes les notifications (lues + non lues)
    ───────────────────────────────────────────────────────────────── */
    public List<Notification> getAllByRecipient(int recipientId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE recipient_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[NotificationService] getAllByRecipient: " + e.getMessage());
        }
        return list;
    }

    /* ──────── Helper mapper ──────────────────────────────────────── */
    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("id"),
                rs.getString("content"),
                rs.getBoolean("is_read"),
                rs.getString("created_at"),
                rs.getInt("recipient_id"),
                rs.getInt("sender_id"),
                rs.getInt("conversation_id"),
                rs.getInt("message_id")
        );
    }
}
