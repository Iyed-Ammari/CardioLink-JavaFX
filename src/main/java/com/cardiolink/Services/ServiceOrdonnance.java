package com.cardiolink.Services;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrdonnance implements Iservice<Ordonnance> {

    private Connection connection;

    public ServiceOrdonnance() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Ordonnance ord) throws SQLDataException {
        String query = "INSERT INTO ordonnance (reference, date_creation, consultation_id, diagnostic, notes, medecin_nom, patient_nom) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, ord.getReference());
            pst.setTimestamp(2, Timestamp.valueOf(ord.getDateCreation()));
            pst.setInt(3, ord.getConsultationId());
            pst.setString(4, ord.getDiagnostic());
            pst.setString(5, ord.getNotes());
            pst.setString(6, ord.getMedecinNom());
            pst.setString(7, ord.getPatientNom());
            pst.executeUpdate();
            System.out.println("Ordonnance ajoutée !");
        } catch (SQLException e) {
            System.err.println("Erreur add ordonnance: " + e.getMessage());
        }
    }

    @Override
    public List<Ordonnance> getAll() throws SQLDataException {
        List<Ordonnance> liste = new ArrayList<>();
        String query = "SELECT * FROM ordonnance";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Ordonnance ord = new Ordonnance();
                ord.setId(rs.getInt("id"));
                ord.setReference(rs.getString("reference"));
                Timestamp ts = rs.getTimestamp("date_creation");
                if (ts != null) ord.setDateCreation(ts.toLocalDateTime());
                ord.setConsultationId(rs.getInt("consultation_id"));
                ord.setDiagnostic(rs.getString("diagnostic"));
                ord.setNotes(rs.getString("notes"));
                ord.setMedecinNom(rs.getString("medecin_nom"));
                ord.setPatientNom(rs.getString("patient_nom"));
                liste.add(ord);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAll ordonnance: " + e.getMessage());
        }
        return liste;
    }

    @Override
    public void update(Ordonnance ord) throws SQLDataException {
        String query = "UPDATE ordonnance SET reference=?, diagnostic=?, notes=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, ord.getReference());
            pst.setString(2, ord.getDiagnostic());
            pst.setString(3, ord.getNotes());
            pst.setInt(4, ord.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur update ordonnance: " + e.getMessage());
        }
    }

    @Override
    public void delete(Ordonnance ord) throws SQLDataException {
        String query = "DELETE FROM ordonnance WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, ord.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur delete ordonnance: " + e.getMessage());
        }
    }

    @Override
    public Ordonnance getById(int id) throws SQLDataException {
        // Logique similaire au getAll mais avec WHERE id = ?
        return null; // À compléter si besoin d'un unitaire
    }
    // Dans ServiceOrdonnance.java
    public boolean existeDeja(String reference) {
        String query = "SELECT count(*) FROM ordonnance WHERE reference = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, reference);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}