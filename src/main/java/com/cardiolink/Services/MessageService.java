package com.cardiolink.Services;

import com.cardiolink.Models.Message;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService implements Iservice<Message> {
    private Connection connection;

    public MessageService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Message message) throws SQLDataException {
        String request = "INSERT INTO message (conversation_id, sender_id, content, created_at, is_read, classification, is_pinned, is_archived) VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, message.getConversationId());
            pstmt.setInt(2, message.getSenderId());
            pstmt.setString(3, message.getContent());
            pstmt.setString(4, message.getDate());
            pstmt.setBoolean(5, message.isRead());
            pstmt.setString(6, message.getClassification());
            pstmt.setBoolean(7, message.isPinned());
            pstmt.setBoolean(8, message.isArchived());
            pstmt.executeUpdate();
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
                Message message = new Message();
                message.setId(result.getInt("id"));
                message.setConversationId(result.getInt("conversation_id"));
                message.setSenderId(result.getInt("sender_id"));
                message.setContent(result.getString("content"));
                message.setDate(result.getString("created_at"));
                message.setRead(result.getBoolean("is_read"));
                message.setClassification(result.getString("classification"));
                message.setPinned(result.getBoolean("is_pinned"));
                message.setArchived(result.getBoolean("is_archived"));
                messages.add(message);
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
                    Message message = new Message();
                    message.setId(result.getInt("id"));
                    message.setConversationId(result.getInt("conversation_id"));
                    message.setSenderId(result.getInt("sender_id"));
                    message.setContent(result.getString("content"));
                    message.setDate(result.getString("created_at"));
                    message.setRead(result.getBoolean("is_read"));
                    message.setClassification(result.getString("classification"));
                    message.setPinned(result.getBoolean("is_pinned"));
                    message.setArchived(result.getBoolean("is_archived"));

                    return message;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching message by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Message> getByConversationId(int conversationId) {
        List<Message> messages = new ArrayList<>();
        // On trie par date pour avoir les plus anciens en haut et les plus récents en bas
        String request = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(request)) {
            pstmt.setInt(1, conversationId);
            try (ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    Message message = new Message();
                    message.setId(result.getInt("id"));
                    message.setConversationId(result.getInt("conversation_id"));
                    message.setSenderId(result.getInt("sender_id"));
                    message.setContent(result.getString("content"));
                    message.setDate(result.getString("created_at"));
                    message.setRead(result.getBoolean("is_read"));
                    message.setClassification(result.getString("classification"));
                    message.setPinned(result.getBoolean("is_pinned"));
                    message.setArchived(result.getBoolean("is_archived"));

                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages by conversation: " + e.getMessage());
        }
        return messages;
    }
}