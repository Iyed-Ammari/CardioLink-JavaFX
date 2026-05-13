package com.cardiolink.Services;

import com.cardiolink.Models.Message;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service complet pour la gestion des messages.
 * Inclut les opérations avancées : épinglage, archivage,
 * lecture, filtrage par type et récupération optimisée.
 */
public class MessageService implements Iservice<Message> {
    private Connection connection;

    public MessageService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Message message) throws SQLDataException {
        String request = "INSERT INTO message (conversation_id, sender_id, content, created_at, is_read, classification, is_pinned, is_archived) VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(request,
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, message.getConversationId());
            pstmt.setInt(2, message.getSenderId());
            pstmt.setString(3, message.getContent());
            pstmt.setString(4, message.getDate());
            pstmt.setBoolean(5, message.isRead());
            pstmt.setString(6, message.getClassification());
            pstmt.setBoolean(7, message.isPinned());
            pstmt.setBoolean(8, message.isArchived());
            pstmt.executeUpdate();

            // Récupérer l'ID auto-généré et le setter sur l'objet
            try (java.sql.ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    message.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding message: " + e.getMessage());
        }
    }

    @Override
    public void delete(Message message) throws SQLDataException {
        String request = "DELETE FROM message WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, message.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
        }
    }

    @Override
    public void update(Message message) throws SQLDataException {
        String request = "UPDATE message SET conversation_id = ?, sender_id = ?, content = ?, created_at = ?, is_read = ?, classification = ?, is_pinned = ?, is_archived = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, message.getConversationId());
            pstmt.setInt(2, message.getSenderId());
            pstmt.setString(3, message.getContent());
            pstmt.setString(4, message.getDate());
            pstmt.setBoolean(5, message.isRead());
            pstmt.setString(6, message.getClassification());
            pstmt.setBoolean(7, message.isPinned());
            pstmt.setBoolean(8, message.isArchived());
            pstmt.setInt(9, message.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating message: " + e.getMessage());
        }
    }

    @Override
    public List<Message> getAll() throws SQLDataException {
        String request = "SELECT * FROM message";
        List<Message> messages = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(request)) {

            while (result.next()) {
                messages.add(mapRow(result));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all messages: " + e.getMessage());
        }
        return messages;
    }

    @Override
    public Message getById(int id) throws SQLDataException {
        String request = "SELECT * FROM message WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, id);
            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    return mapRow(result);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching message by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Message> getByConversationId(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String request = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, conversationId);
            try (ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    messages.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages by conversation: " + e.getMessage());
        }
        return messages;
    }

    public boolean togglePin(int messageId) {
        String sql = "UPDATE message SET is_pinned = NOT is_pinned WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
            Message m = getById(messageId);
            return m != null && m.isPinned();
        } catch (SQLException e) {
            System.err.println("[MessageService] togglePin: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleArchive(int messageId) {
        String sql = "UPDATE message SET is_archived = NOT is_archived WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
            Message m = getById(messageId);
            return m != null && m.isArchived();
        } catch (SQLException e) {
            System.err.println("[MessageService] toggleArchive: " + e.getMessage());
            return false;
        }
    }

    public void setPinned(int messageId, boolean pinned) {
        String sql = "UPDATE message SET is_pinned = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, pinned);
            ps.setInt(2, messageId);
            ps.executeUpdate();
            System.out.println("[MessageService] Message " + messageId + " épinglé : " + pinned);
        } catch (SQLException e) {
            System.err.println("[MessageService] setPinned: " + e.getMessage());
        }
    }

    public void setArchived(int messageId, boolean archived) {
        String sql = "UPDATE message SET is_archived = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, archived);
            ps.setInt(2, messageId);
            ps.executeUpdate();
            System.out.println("[MessageService] Message " + messageId + " archivé : " + archived);
        } catch (SQLException e) {
            System.err.println("[MessageService] setArchived: " + e.getMessage());
        }
    }

    public void markAsRead(int messageId) {
        String sql = "UPDATE message SET is_read = 1 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MessageService] markAsRead: " + e.getMessage());
        }
    }

    public void markAllAsReadByConversation(int conversationId, int currentUserId) {
        String sql = "UPDATE message SET is_read = 1 "
                + "WHERE conversation_id = ? AND sender_id != ? AND is_read = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MessageService] markAllAsReadByConversation: " + e.getMessage());
        }
    }

    public List<Message> findPinnedByConversation(int conversationId) {
        return queryByConversationAndFlag(conversationId, "is_pinned");
    }

    public List<Message> findArchivedByConversation(int conversationId) {
        return queryByConversationAndFlag(conversationId, "is_archived");
    }

    public List<Message> filterByType(int conversationId, String type) {
        List<Message> messages = new ArrayList<>();
        String sql;
        PreparedStatement ps;

        try {
            switch (type == null ? "all" : type.toLowerCase()) {
                case "pinned":
                    sql = "SELECT * FROM message WHERE conversation_id = ? AND is_pinned = 1 ORDER BY created_at ASC";
                    ps = connection.prepareStatement(sql);
                    ps.setInt(1, conversationId);
                    break;
                case "archived":
                    sql = "SELECT * FROM message WHERE conversation_id = ? AND is_archived = 1 ORDER BY created_at ASC";
                    ps = connection.prepareStatement(sql);
                    ps.setInt(1, conversationId);
                    break;
                case "urgent":
                    sql = "SELECT * FROM message WHERE conversation_id = ? AND classification = 'URGENT' ORDER BY created_at DESC";
                    ps = connection.prepareStatement(sql);
                    ps.setInt(1, conversationId);
                    break;
                case "normal":
                    sql = "SELECT * FROM message WHERE conversation_id = ? AND classification = 'NORMAL' AND is_archived = 0 ORDER BY created_at ASC";
                    ps = connection.prepareStatement(sql);
                    ps.setInt(1, conversationId);
                    break;
                default:
                    sql = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";
                    ps = connection.prepareStatement(sql);
                    ps.setInt(1, conversationId);
                    break;
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) messages.add(mapRow(rs));
            }
            ps.close();

        } catch (SQLException e) {
            System.err.println("[MessageService] filterByType: " + e.getMessage());
        }
        return messages;
    }

    public int countUnread(int conversationId, int currentUserId) {
        String sql = "SELECT COUNT(*) FROM message "
                + "WHERE conversation_id = ? AND sender_id != ? AND is_read = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[MessageService] countUnread: " + e.getMessage());
        }
        return 0;
    }

    public Message getLastMessageByConversation(int conversationId) {
        String sql = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[MessageService] getLastMessageByConversation: " + e.getMessage());
        }
        return null;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setConversationId(rs.getInt("conversation_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setContent(rs.getString("content"));
        m.setDate(rs.getString("created_at"));
        m.setRead(rs.getBoolean("is_read"));
        m.setClassification(rs.getString("classification"));
        m.setPinned(rs.getBoolean("is_pinned"));
        m.setArchived(rs.getBoolean("is_archived"));
        return m;
    }

    private List<Message> queryByConversationAndFlag(int conversationId, String column) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE conversation_id = ? AND " + column + " = 1 ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MessageService] queryByConversationAndFlag(" + column + "): " + e.getMessage());
        }
        return list;
    }

    /* ── NOUVEAUTÉS : SUPPRESSION ET MISE A JOUR DU CONTENU SEUL ── */

    public void deleteById(int id) {
        // 1. D'abord, on supprime les notifications liées à ce message
        String deleteNotifications = "DELETE FROM notification WHERE message_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteNotifications)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MessageService] Erreur lors de la suppression des notifications: " + e.getMessage());
        }

        // 2. Ensuite, on peut supprimer le message en toute sécurité
        String deleteMessage = "DELETE FROM message WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteMessage)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("[MessageService] Message supprimé : " + id);
        } catch (SQLException e) {
            System.err.println("[MessageService] Error deleting message by ID: " + e.getMessage());
        }
    }

    public void updateContent(int id, String newContent) {
        String request = "UPDATE message SET content = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setString(1, newContent);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("[MessageService] Message modifié : " + id);
        } catch (SQLException e) {
            System.err.println("[MessageService] Error updating message content: " + e.getMessage());
        }
    }
}