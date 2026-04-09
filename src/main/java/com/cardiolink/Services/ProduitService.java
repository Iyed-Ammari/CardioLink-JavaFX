package com.cardiolink.Services;

import com.cardiolink.interfaces.GlobalInterface;
import com.cardiolink.Models.Produit;
import com.cardiolink.utils.MyDatabase;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements GlobalInterface<Produit> {

    Connection cnx = MyDatabase.getInstance().getConnection();

    // ===== ADD (Statement) =====
    @Override
    public void add(Produit produit) {
        String req;
        if (produit.getImageUrl() == null) {
            req = "INSERT INTO produit (nom, description, prix, stock, image_url, categorie) VALUES ('"
                    + produit.getNom() + "','"
                    + produit.getDescription() + "',"
                    + produit.getPrix() + ","
                    + produit.getStock() + ","
                    + "NULL" + ",'"
                    + produit.getCategorie() + "')";
        } else {
            req = "INSERT INTO produit (nom, description, prix, stock, image_url, categorie) VALUES ('"
                    + produit.getNom() + "','"
                    + produit.getDescription() + "',"
                    + produit.getPrix() + ","
                    + produit.getStock() + ",'"
                    + produit.getImageUrl() + "','"
                    + produit.getCategorie() + "')";
        }
        try {
            Statement st = cnx.createStatement();
            st.execute(req);
            System.out.println("✅ Produit ajouté avec succès (Statement) !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== ADD2 (PreparedStatement) =====
    @Override
    public void add2(Produit produit) {
        String req = "INSERT INTO produit (nom, description, prix, stock, image_url, categorie) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setBigDecimal(3, produit.getPrix());
            ps.setInt(4, produit.getStock());
            if (produit.getImageUrl() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, produit.getImageUrl());
            }
            ps.setString(6, produit.getCategorie());
            ps.executeUpdate();
            System.out.println("✅ Produit ajouté avec succès (PreparedStatement) !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== UPDATE =====
    @Override
    public void update(Produit produit) {
        String req = "UPDATE produit SET nom=?, description=?, prix=?, stock=?, image_url=?, categorie=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setBigDecimal(3, produit.getPrix());
            ps.setInt(4, produit.getStock());
            if (produit.getImageUrl() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, produit.getImageUrl());
            }
            ps.setString(6, produit.getCategorie());
            ps.setInt(7, produit.getId());
            ps.executeUpdate();
            System.out.println("✅ Produit modifié avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== DELETE =====
    @Override
    public void delete(Produit produit) {
        String req = "DELETE FROM produit WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, produit.getId());
            ps.executeUpdate();
            System.out.println("✅ Produit supprimé avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== GET ALL =====
    @Override
    public List<Produit> getall() {
        List<Produit> produits = new ArrayList<>();
        String req = "SELECT * FROM produit";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setDescription(rs.getString("description"));
                p.setPrix(rs.getBigDecimal("prix"));
                p.setStock(rs.getInt("stock"));
                p.setImageUrl(rs.getString("image_url"));
                p.setCategorie(rs.getString("categorie"));
                produits.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }
}