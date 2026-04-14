package com.cardiolink.Services;

import com.cardiolink.Models.Suivi;
import com.cardiolink.utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuiviService implements Iservice<Suivi> {
    private Connection connection;

    public SuiviService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Suivi s) throws SQLDataException {
        String req = "INSERT INTO suivi (type_donnee, valeur, unite, date_saisie, niveau_urgence, patient_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, s.getTypeDonnee());
            ps.setFloat(2, s.getValeur());
            ps.setString(3, s.getUnite());
            ps.setTimestamp(4, Timestamp.valueOf(s.getDateSaisie()));
            ps.setString(5, s.getNiveauUrgence());
            ps.setInt(6, s.getPatientId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void delete(Suivi s) throws SQLDataException {
        String req = "DELETE FROM suivi WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            // On lève une exception précise pour que le Main sache pourquoi ça a échoué
            throw new SQLDataException("Suppression impossible : " + e.getMessage());
        }
    }

    @Override
    public void update(Suivi s) throws SQLDataException {
        String req = "UPDATE suivi SET type_donnee=?, valeur=?, unite=?, niveau_urgence=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, s.getTypeDonnee());
            ps.setFloat(2, s.getValeur());
            ps.setString(3, s.getUnite());
            ps.setString(4, s.getNiveauUrgence());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'update : " + e.getMessage());
        }
    }

    @Override
    public List<Suivi> getAll() throws SQLDataException {
        List<Suivi> list = new ArrayList<>();
        String req = "SELECT * FROM suivi";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSetToSuivi(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    @Override
    public Suivi getById(int id) throws SQLDataException {
        String req = "SELECT * FROM suivi WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToSuivi(rs);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private Suivi mapResultSetToSuivi(ResultSet rs) throws SQLException {
        Suivi s = new Suivi();
        s.setId(rs.getInt("id")); // Crucial pour l'Update/Delete
        s.setTypeDonnee(rs.getString("type_donnee"));
        s.setValeur(rs.getFloat("valeur"));
        s.setUnite(rs.getString("unite"));
        s.setPatientId(rs.getInt("patient_id"));
        return s;
    }
}