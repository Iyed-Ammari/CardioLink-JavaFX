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
}