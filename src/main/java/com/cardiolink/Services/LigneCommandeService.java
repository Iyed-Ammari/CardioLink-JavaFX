package com.cardiolink.Services;

import com.cardiolink.Models.LigneCommande;
import com.cardiolink.Models.Produit;
import com.cardiolink.utils.MyDatabase;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneCommandeService implements Iservice<LigneCommande> {

    private final Connection cnx = MyDatabase.getInstance().getConnection();
    private final ProduitService produitService = new ProduitService();

    private static final String SELECT_JOIN =
            "SELECT " +
                    "lc.id AS lc_id, " +
                    "lc.quantite, " +
                    "lc.prix_unitaire, " +
                    "lc.commande_id, " +
                    "lc.produit_id, " +
                    "p.id AS p_id, " +
                    "p.nom AS p_nom, " +
                    "p.description AS p_description, " +
                    "p.prix AS p_prix, " +
                    "p.stock AS p_stock, " +
                    "p.image_url AS p_image_url, " +
                    "p.categorie AS p_categorie " +
                    "FROM ligne_commande lc " +
                    "JOIN produit p ON p.id = lc.produit_id ";

    private LigneCommande mapResultSet(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getInt("p_id"));

        setFieldDirectly(p, "nom", rs.getString("p_nom"));
        p.setDescription(rs.getString("p_description"));

        BigDecimal prix = rs.getBigDecimal("p_prix");
        if (prix != null && prix.compareTo(BigDecimal.ZERO) > 0) {
            try {
                p.setPrix(prix);
            } catch (Exception ignored) {
                setFieldDirectly(p, "prix", prix);
            }
        } else {
            p.setPrix(BigDecimal.ONE);
        }

        try {
            p.setStock(rs.getInt("p_stock"));
        } catch (Exception ignored) {
            p.setStock(0);
        }

        String imgUrl = rs.getString("p_image_url");
        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
            try {
                p.setImageUrl(imgUrl);
            } catch (Exception ignored) {
                setFieldDirectly(p, "imageUrl", imgUrl);
            }
        }

        String cat = rs.getString("p_categorie");
        if (cat != null && !cat.trim().isEmpty()) {
            try {
                p.setCategorie(cat);
            } catch (Exception ignored) {
                setFieldDirectly(p, "categorie", cat.toUpperCase());
            }
        } else {
            try {
                p.setCategorie("AUTRE");
            } catch (Exception ignored) {
            }
        }

        LigneCommande l = new LigneCommande();
        l.setId(rs.getInt("lc_id"));
        l.setCommandeId(rs.getInt("commande_id"));
        l.setQuantite(rs.getInt("quantite"));
        l.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
        l.setProduit(p);

        return l;
    }

    private void setFieldDirectly(Produit p, String fieldName, Object value) {
        try {
            java.lang.reflect.Field f = Produit.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(p, value);
        } catch (Exception ignored) {
        }
    }

    private void validerQuantite(int quantite) {
        if (quantite < 1)
            throw new IllegalArgumentException("La quantité doit être >= 1.");
    }

    private void validerStockSuffisant(Produit p, int demande) {
        int dispo = (p.getStock() == null) ? 0 : p.getStock();
        if (dispo < demande)
            throw new IllegalArgumentException(
                    "Stock insuffisant pour '" + p.getNom() +
                            "'. Disponible : " + dispo + " | Demandé : " + demande
            );
    }

    private int insertLigne(int commandeId, int produitId, int quantite, BigDecimal prixUnitaire) {
        String sql = "INSERT INTO ligne_commande (quantite, prix_unitaire, commande_id, produit_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, quantite);
            ps.setBigDecimal(2, prixUnitaire);
            ps.setInt(3, commandeId);
            ps.setInt(4, produitId);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion de la ligne.", e);
        }

        throw new RuntimeException("Impossible de récupérer l'id de la ligne insérée.");
    }

    @Override
    public void add(LigneCommande ligne) {
        if (ligne == null) throw new IllegalArgumentException("La ligne est null.");
        if (ligne.getCommandeId() == null) throw new IllegalArgumentException("commandeId obligatoire.");
        if (ligne.getProduit() == null || ligne.getProduit().getId() == null)
            throw new IllegalArgumentException("Le produit est obligatoire.");

        validerQuantite(ligne.getQuantite());

        Produit p = produitService.getById(ligne.getProduit().getId());
        if (p == null) throw new IllegalArgumentException("Produit introuvable (id=" + ligne.getProduit().getId() + ").");

        validerStockSuffisant(p, ligne.getQuantite());

        int newId = insertLigne(
                ligne.getCommandeId(),
                p.getId(),
                ligne.getQuantite(),
                ligne.getPrixUnitaire() != null ? ligne.getPrixUnitaire() : p.getPrix()
        );

        ligne.setId(newId);
        ligne.setProduit(p);
        if (ligne.getPrixUnitaire() == null) ligne.setPrixUnitaire(p.getPrix());

        System.out.println("✅ Ligne ajoutée : produit=" + p.getNom() + " qté=" + ligne.getQuantite());
    }

    @Override
    public void update(LigneCommande ligne) {
        if (ligne == null || ligne.getId() == null) throw new IllegalArgumentException("Ligne invalide.");
        validerQuantite(ligne.getQuantite());

        if (ligne.getProduit() != null && ligne.getProduit().getId() != null) {
            Produit p = produitService.getById(ligne.getProduit().getId());
            if (p != null) validerStockSuffisant(p, ligne.getQuantite());
        }

        updateQuantite(ligne.getId(), ligne.getQuantite());
    }

    @Override
    public void delete(LigneCommande ligne) {
        if (ligne == null || ligne.getId() == null) throw new IllegalArgumentException("Ligne invalide.");

        String sql = "DELETE FROM ligne_commande WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, ligne.getId());

            if (ps.executeUpdate() == 0)
                throw new RuntimeException("Ligne introuvable (id=" + ligne.getId() + ").");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression ligne.", e);
        }
    }

    @Override
    public List<LigneCommande> getAll() {
        List<LigneCommande> lignes = new ArrayList<>();
        String sql = SELECT_JOIN + "ORDER BY lc.id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lignes.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("❌ getAll : " + e.getMessage());
        }

        return lignes;
    }

    @Override
    public LigneCommande getById(int id) {
        String sql = SELECT_JOIN + "WHERE lc.id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ getById : " + e.getMessage());
        }

        return null;
    }

    public void add2(LigneCommande ligne) {
        add(ligne);
    }

    public List<LigneCommande> getall() {
        return getAll();
    }

    // ─── Méthodes métier
    public LigneCommande addToCommande(int commandeId, int produitId, int quantite) {
        validerQuantite(quantite);

        Produit p = produitService.getById(produitId);
        if (p == null) throw new IllegalArgumentException("Produit introuvable (id=" + produitId + ").");

        LigneCommande existante = findByCommandeAndProduit(commandeId, produitId);

        if (existante != null) {
            int nouvelleQte = existante.getQuantite() + quantite;
            validerStockSuffisant(p, nouvelleQte);
            updateQuantite(existante.getId(), nouvelleQte);
            existante.setQuantite(nouvelleQte);
            return existante;
        }

        validerStockSuffisant(p, quantite);
        int newId = insertLigne(commandeId, p.getId(), quantite, p.getPrix());
        return new LigneCommande(newId, commandeId, quantite, p.getPrix(), p);
    }

    private void updateQuantite(int ligneId, int quantite) {
        String sql = "UPDATE ligne_commande SET quantite=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, quantite);
            ps.setInt(2, ligneId);

            if (ps.executeUpdate() == 0)
                throw new RuntimeException("Ligne introuvable (id=" + ligneId + ").");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour ligne.", e);
        }
    }

    public void supprimerLigne(int ligneId) {
        LigneCommande ligne = getById(ligneId);
        if (ligne == null) throw new RuntimeException("Ligne introuvable (id=" + ligneId + ").");
        delete(ligne);
    }

    public List<LigneCommande> getByCommande(int commandeId) {
        List<LigneCommande> lignes = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE lc.commande_id=? ORDER BY lc.id ASC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lignes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ getByCommande : " + e.getMessage());
        }

        return lignes;
    }

    public LigneCommande findByCommandeAndProduit(int commandeId, int produitId) {
        String sql = SELECT_JOIN + "WHERE lc.commande_id=? AND lc.produit_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ps.setInt(2, produitId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ findByCommandeAndProduit : " + e.getMessage());
        }

        return null;
    }

    public void deleteAllByCommande(int commandeId) {
        String sql = "DELETE FROM ligne_commande WHERE commande_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur vidage panier.", e);
        }
    }

    public void viderPanier(int commandeId) {
        deleteAllByCommande(commandeId);
    }

    public BigDecimal calculerTotal(int commandeId) {
        String sql = "SELECT COALESCE(SUM(quantite * prix_unitaire), 0) FROM ligne_commande WHERE commande_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ calculerTotal : " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    public int countByCommande(int commandeId) {
        String sql = "SELECT COUNT(*) FROM ligne_commande WHERE commande_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ countByCommande : " + e.getMessage());
        }

        return 0;
    }
}