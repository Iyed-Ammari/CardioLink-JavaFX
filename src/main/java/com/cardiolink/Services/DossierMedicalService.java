package com.cardiolink.Services;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.utils.DatabaseConnection;
import java.sql.*;

public class DossierMedicalService {

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
        // Insert or update
        DossierMedical existing = getByUserId(d.getUserId());
        if (existing == null) {
            insert(d);
        } else {
            d.setId(existing.getId());
            update(d);
        }
    }

    private void insert(DossierMedical d) throws SQLException {
        String sql = "INSERT INTO dossier_medical (groupe_sanguin, antecedents, allergies, " +
                "poids, taille, tension_systolique, tension_diastolique, frequence_cardiaque, user_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
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

    private void update(DossierMedical d) throws SQLException {
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
    }

    public void deleteByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM dossier_medical WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    private DossierMedical map(ResultSet rs) throws SQLException {
        DossierMedical d = new DossierMedical();
        d.setId(rs.getInt("id"));
        d.setGroupeSanguin(rs.getString("groupe_sanguin"));
        d.setAntecedents(rs.getString("antecedents"));
        d.setAllergies(rs.getString("allergies"));
        d.setPoids(rs.getObject("poids") != null ? rs.getDouble("poids") : null);
        d.setTaille(rs.getObject("taille") != null ? rs.getDouble("taille") : null);
        d.setTensionSystolique(rs.getObject("tension_systolique") != null ? rs.getDouble("tension_systolique") : null);
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