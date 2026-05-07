package com.cardiolink.Services;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DossierMedicalService implements Iservice<DossierMedical> {

    @Override
    public void add(DossierMedical d) throws SQLDataException {
        try { insert(d); }
        catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    @Override
    public void delete(DossierMedical d) throws SQLDataException {
        try { deleteByUserId(d.getUserId()); }
        catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    @Override
    public void update(DossierMedical d) throws SQLDataException {
        try {
            String sql = "UPDATE dossier_medical SET groupe_sanguin=?, antecedents=?, allergies=?, " +
                    "poids=?, taille=?, tension_systolique=?, tension_diastolique=?, " +
                    "frequence_cardiaque=? WHERE id=?";
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, d.getGroupeSanguin());
            ps.setString(2, d.getAntecedents());
            ps.setString(3, d.getAllergies());
            setDoubleOrNull(ps, 4, d.getPoids());
            setDoubleOrNull(ps, 5, d.getTaille());
            setDoubleOrNull(ps, 6, d.getTensionSystolique());
            setDoubleOrNull(ps, 7, d.getTensionDiastolique());
            setDoubleOrNull(ps, 8, d.getFrequenceCardiaque());
            ps.setInt(9, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    @Override
    public List<DossierMedical> getAll() throws SQLDataException {
        try {
            List<DossierMedical> list = new ArrayList<>();
            String sql = "SELECT * FROM dossier_medical";
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    @Override
    public DossierMedical getById(int id) throws SQLDataException {
        try {
            String sql = "SELECT * FROM dossier_medical WHERE id = ?";
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
            return null;
        } catch (SQLException e) { throw new SQLDataException(e.getMessage()); }
    }

    // ── Méthodes supplémentaires (hors interface) ────────────

    public DossierMedical getByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM dossier_medical WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return map(rs);
        return null;
    }

    public void save(DossierMedical d) throws SQLException {
        DossierMedical existing = getByUserId(d.getUserId());
        if (existing == null) {
            insert(d);
        } else {
            d.setId(existing.getId());
            try { update(d); }
            catch (SQLDataException e) { throw new SQLException(e.getMessage()); }
        }
    }

    public void deleteByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM dossier_medical WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    private void insert(DossierMedical d) throws SQLException {
        String sql = "INSERT INTO dossier_medical (groupe_sanguin, antecedents, allergies, " +
                "poids, taille, tension_systolique, tension_diastolique, " +
                "frequence_cardiaque, user_id) VALUES (?,?,?,?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, d.getGroupeSanguin());
        ps.setString(2, d.getAntecedents());
        ps.setString(3, d.getAllergies());
        setDoubleOrNull(ps, 4, d.getPoids());
        setDoubleOrNull(ps, 5, d.getTaille());
        setDoubleOrNull(ps, 6, d.getTensionSystolique());
        setDoubleOrNull(ps, 7, d.getTensionDiastolique());
        setDoubleOrNull(ps, 8, d.getFrequenceCardiaque());
        ps.setInt(9, d.getUserId());
        ps.executeUpdate();
    }

    private DossierMedical map(ResultSet rs) throws SQLException {

        DossierMedical d = new DossierMedical();
        d.setId(rs.getInt("id"));
        d.setGroupeSanguin(rs.getString("groupe_sanguin"));
        d.setAntecedents(rs.getString("antecedents"));
        d.setAllergies(rs.getString("allergies"));
        d.setPoids(rs.getObject("poids")              != null ? rs.getDouble("poids")              : null);
        d.setTaille(rs.getObject("taille")            != null ? rs.getDouble("taille")             : null);
        d.setTensionSystolique(rs.getObject("tension_systolique")  != null ? rs.getDouble("tension_systolique")  : null);
        d.setTensionDiastolique(rs.getObject("tension_diastolique") != null ? rs.getDouble("tension_diastolique") : null);
        d.setFrequenceCardiaque(rs.getObject("frequence_cardiaque") != null ? rs.getDouble("frequence_cardiaque") : null);
        d.setUserId(rs.getInt("user_id"));
        return d;
    }

    private void setDoubleOrNull(PreparedStatement ps, int idx, Double val) throws SQLException {
        if (val != null) ps.setDouble(idx, val);
        else             ps.setNull(idx, Types.DOUBLE);
    }
}