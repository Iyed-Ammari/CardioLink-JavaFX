package com.cardiolink.Services;

import com.cardiolink.Models.Rendezvous;
import com.cardiolink.utils.MyDatabase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceRendezvous implements Iservice<Rendezvous> {

    private Connection connection;

    public ServiceRendezvous() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Rendezvous rv) throws SQLDataException {
        // Changement du nom de la table ici
        String query = "INSERT INTO rendez_vous (date_heure, statut, type, lien_visio, remarques, patient_id, medecin_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setTimestamp(1, Timestamp.valueOf(rv.getDateHeure()));
            pst.setString(2, rv.getStatut());
            pst.setString(3, rv.getType());
            pst.setString(4, rv.getLienVisio());
            pst.setString(5, rv.getRemarques());
            pst.setInt(6, rv.getPatientId());
            pst.setInt(7, rv.getMedecinId());
            pst.executeUpdate();
            System.out.println("Rendez-vous ajouté !");
        } catch (SQLException e) {
            System.err.println("Erreur add rdv: " + e.getMessage());
        }
    }

    @Override
    public List<Rendezvous> getAll() throws SQLDataException {
        List<Rendezvous> liste = new ArrayList<>();
        String query = "SELECT * FROM rendez_vous";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Rendezvous rv = new Rendezvous();
                rv.setId(rs.getInt("id"));
                Timestamp ts = rs.getTimestamp("date_heure");
                if (ts != null) rv.setDateHeure(ts.toLocalDateTime());
                rv.setStatut(rs.getString("statut"));
                rv.setType(rs.getString("type"));
                rv.setLienVisio(rs.getString("lien_visio"));
                rv.setRemarques(rs.getString("remarques"));
                rv.setPatientId(rs.getInt("patient_id"));
                rv.setMedecinId(rs.getInt("medecin_id"));
                liste.add(rv);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAll rdv: " + e.getMessage());
        }
        return liste;
    }

    @Override
    public void update(Rendezvous rv) throws SQLDataException {
        String query = "UPDATE rendez_vous SET date_heure=?, statut=?, type=?, lien_visio=?, remarques=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setTimestamp(1, Timestamp.valueOf(rv.getDateHeure()));
            pst.setString(2, rv.getStatut());
            pst.setString(3, rv.getType());
            pst.setString(4, rv.getLienVisio());
            pst.setString(5, rv.getRemarques());
            pst.setInt(6, rv.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur update rdv: " + e.getMessage());
        }
    }

    @Override
    public void delete(Rendezvous rv) throws SQLDataException {
        String query = "DELETE FROM rendez_vous WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, rv.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur delete rdv: " + e.getMessage());
        }
    }

    @Override
    public Rendezvous getById(int id) throws SQLDataException {
        String query = "SELECT * FROM rendez_vous WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Rendezvous rv = new Rendezvous();
                rv.setId(rs.getInt("id"));
                Timestamp ts = rs.getTimestamp("date_heure");
                if (ts != null) rv.setDateHeure(ts.toLocalDateTime());
                rv.setStatut(rs.getString("statut"));
                rv.setType(rs.getString("type"));
                rv.setLienVisio(rs.getString("lien_visio"));
                rv.setRemarques(rs.getString("remarques"));
                rv.setPatientId(rs.getInt("patient_id"));
                rv.setMedecinId(rs.getInt("medecin_id"));
                return rv;
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById rdv: " + e.getMessage());
        }
        return null;
    }

    public List<Rendezvous> getByPatientId(int patientId) throws SQLDataException {
        List<Rendezvous> liste = new ArrayList<>();
        // La requête sélectionne tous les records correspondants au patient
        String query = "SELECT * FROM rendez_vous WHERE patient_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, patientId);

            try (ResultSet rs = pst.executeQuery()) {
                // Utilisation de 'while' pour parcourir TOUS les résultats du ResultSet
                while (rs.next()) {
                    Rendezvous rv = new Rendezvous();
                    rv.setId(rs.getInt("id"));

                    Timestamp ts = rs.getTimestamp("date_heure");
                    if (ts != null) {
                        rv.setDateHeure(ts.toLocalDateTime());
                    }

                    rv.setStatut(rs.getString("statut"));
                    rv.setType(rs.getString("type"));
                    rv.setRemarques(rs.getString("remarques")); // Ajouté pour être complet
                    rv.setLienVisio(rs.getString("lien_visio")); // Ajouté pour être complet
                    rv.setPatientId(rs.getInt("patient_id"));
                    rv.setMedecinId(rs.getInt("medecin_id"));

                    liste.add(rv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des rendez-vous du patient " + patientId + ": " + e.getMessage());
        }
        return liste;
    }

    public List<Rendezvous> getByMedecinId(int medecinId) throws SQLDataException {
        List<Rendezvous> liste = new ArrayList<>();
        // La requête sélectionne tous les records correspondants au patient
        String query = "SELECT * FROM rendez_vous WHERE medecin_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, medecinId);

            try (ResultSet rs = pst.executeQuery()) {
                // Utilisation de 'while' pour parcourir TOUS les résultats du ResultSet
                while (rs.next()) {
                    Rendezvous rv = new Rendezvous();
                    rv.setId(rs.getInt("id"));

                    Timestamp ts = rs.getTimestamp("date_heure");
                    if (ts != null) {
                        rv.setDateHeure(ts.toLocalDateTime());
                    }

                    rv.setStatut(rs.getString("statut"));
                    rv.setType(rs.getString("type"));
                    rv.setRemarques(rs.getString("remarques")); // Ajouté pour être complet
                    rv.setLienVisio(rs.getString("lien_visio")); // Ajouté pour être complet
                    rv.setPatientId(rs.getInt("patient_id"));
                    rv.setMedecinId(rs.getInt("medecin_id"));

                    liste.add(rv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des rendez-vous du patient " + medecinId + ": " + e.getMessage());
        }
        return liste;
    }

    public List<Rendezvous> getAppointmentsForTomorrow() {
        List<Rendezvous> liste = new ArrayList<>();
        // Sélectionne les rendez-vous dont la date est exactement demain
        String query = "SELECT * FROM rendez_vous WHERE DATE(date_heure) = CURDATE() + INTERVAL 1 DAY";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Rendezvous rv = new Rendezvous();
                rv.setId(rs.getInt("id"));
                Timestamp ts = rs.getTimestamp("date_heure");
                if (ts != null) {
                    rv.setDateHeure(ts.toLocalDateTime());
                }
                rv.setStatut(rs.getString("statut"));
                rv.setType(rs.getString("type"));
                rv.setRemarques(rs.getString("remarques"));
                rv.setLienVisio(rs.getString("lien_visio"));
                rv.setPatientId(rs.getInt("patient_id"));
                rv.setMedecinId(rs.getInt("medecin_id"));
                liste.add(rv);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAppointmentsForTomorrow: " + e.getMessage());
        }
        return liste;
    }
}