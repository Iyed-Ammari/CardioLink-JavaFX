package com.cardiolink.Services;

import com.cardiolink.Models.Intervention;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterventionService implements Iservice<Intervention> {
    private Connection connection;

    public InterventionService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Intervention i) throws SQLDataException {
        String req = "INSERT INTO intervention (type, description, statut, date_planifiee, medecin_id, suivi_origine_id, archive, latitude, longitude) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, i.getType());
            ps.setString(2, i.getDescription());
            ps.setString(3, i.getStatut());
            ps.setTimestamp(4, Timestamp.valueOf(i.getDatePlanifiee()));
            ps.setInt(5, i.getMedecinId());

            if (i.getSuiviOrigine() != null) {
                ps.setInt(6, i.getSuiviOrigine().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setBoolean(7, i.isArchive());

            if (i.getLatitude() != null) {
                ps.setDouble(8, i.getLatitude());
            } else {
                ps.setNull(8, Types.DOUBLE);
            }

            if (i.getLongitude() != null) {
                ps.setDouble(9, i.getLongitude());
            } else {
                ps.setNull(9, Types.DOUBLE);
            }

            ps.executeUpdate();
            System.out.println("Intervention ajoutée !");
        } catch (SQLException e) {
            System.err.println("Erreur Add Intervention: " + e.getMessage());
        }
    }

    @Override
    public void delete(Intervention i) throws SQLDataException {
        String req = "DELETE FROM intervention WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, i.getId());
            ps.executeUpdate();
            System.out.println("Intervention supprimée !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void update(Intervention i) throws SQLDataException {
        String req = "UPDATE intervention SET type=?, description=?, statut=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, i.getType());
            ps.setString(2, i.getDescription());
            ps.setString(3, i.getStatut());
            ps.setInt(4, i.getId());
            ps.executeUpdate();
            System.out.println("Intervention mise à jour !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Intervention> getAll() throws SQLDataException {
        List<Intervention> list = new ArrayList<>();
        String req = "SELECT * FROM intervention WHERE archive = false";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSetToIntervention(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    public List<Intervention> getArchived() throws SQLDataException {
        List<Intervention> list = new ArrayList<>();
        String req = "SELECT * FROM intervention WHERE archive = true";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSetToIntervention(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    public void archive(Intervention i) throws SQLDataException {
        String req = "UPDATE intervention SET archive = true WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, i.getId());
            ps.executeUpdate();
            System.out.println("Intervention archivée !");
        } catch (SQLException e) {
            System.err.println("Erreur archive Intervention: " + e.getMessage());
        }
    }

    @Override
    public Intervention getById(int id) throws SQLDataException {
        String req = "SELECT * FROM intervention WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToIntervention(rs);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private Intervention mapResultSetToIntervention(ResultSet rs) throws SQLException {
        Intervention i = new Intervention();
        i.setId(rs.getInt("id"));
        i.setType(rs.getString("type"));
        i.setDescription(rs.getString("description"));
        i.setStatut(rs.getString("statut"));
        i.setMedecinId(rs.getInt("medecin_id"));
        i.setArchive(rs.getBoolean("archive"));

        Timestamp datePlanifiee = rs.getTimestamp("date_planifiee");
        if (datePlanifiee != null) {
            i.setDatePlanifiee(datePlanifiee.toLocalDateTime());
        }

        Timestamp dateCompletion = rs.getTimestamp("date_completion");
        if (dateCompletion != null) {
            i.setDateCompletion(dateCompletion.toLocalDateTime());
        }

        double latitude = rs.getDouble("latitude");
        if (!rs.wasNull()) {
            i.setLatitude(latitude);
        }

        double longitude = rs.getDouble("longitude");
        if (!rs.wasNull()) {
            i.setLongitude(longitude);
        }

        return i;
    }
}