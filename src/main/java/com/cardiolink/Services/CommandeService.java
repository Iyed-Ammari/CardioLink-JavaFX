package com.cardiolink.Services;

import com.cardiolink.Models.Commande;
import com.cardiolink.Models.LigneCommande;
import com.cardiolink.Models.Produit;
import com.cardiolink.utils.MyDatabase;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements Iservice<Commande> {

    private final Connection cnx = MyDatabase.getInstance().getConnection();
    private final LigneCommandeService ligneService = new LigneCommandeService();

    private static final DateTimeFormatter DB_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Commande mapResultSet(ResultSet rs) throws SQLException {
        Commande c = new Commande();

        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));

        String dateStr = rs.getString("date_commande");
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                c.setDateCommande(LocalDateTime.parse(dateStr, DB_DATE_TIME_FORMATTER));
            } catch (Exception e) {
                System.err.println("⚠️ date_commande invalide : '" + dateStr + "'");
                c.setDateCommande(LocalDateTime.now());
            }
        }

        String statutStr = rs.getString("statut");
        try {
            c.setStatut(Commande.Statut.valueOf(statutStr));
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Statut inconnu en BDD : '" + statutStr + "' → ANNULEE");
            c.setStatut(Commande.Statut.ANNULEE);
        }

        BigDecimal montant = rs.getBigDecimal("montant_total");
        c.setMontantTotal(montant != null ? montant : BigDecimal.ZERO);

        return c;
    }

    @Override
    public void add(Commande commande) {
        if (commande == null) {
            throw new IllegalArgumentException("La commande ne peut pas être null.");
        }
        if (commande.getUserId() == null) {
            throw new IllegalArgumentException("user_id est obligatoire.");
        }

        String sql = "INSERT INTO commande (date_commande, statut, montant_total, user_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime date = commande.getDateCommande() != null
                    ? commande.getDateCommande()
                    : LocalDateTime.now();

            ps.setString(1, date.format(DB_DATE_TIME_FORMATTER));
            ps.setString(2, commande.getStatut().name());
            ps.setBigDecimal(3, commande.getMontantTotal());
            ps.setInt(4, commande.getUserId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    commande.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ add : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la création de la commande.", e);
        }
    }

    @Override
    public void update(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }

        String sql = "UPDATE commande SET statut=?, montant_total=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, commande.getStatut().name());
            ps.setBigDecimal(2, commande.getMontantTotal());
            ps.setInt(3, commande.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Commande introuvable (id=" + commande.getId() + ").");
            }
        } catch (SQLException e) {
            System.err.println("❌ update : " + e.getMessage());
            throw new RuntimeException("Erreur mise à jour commande.", e);
        }
    }

    @Override
    public void delete(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }

        if (ligneService.countByCommande(commande.getId()) > 0) {
            throw new IllegalStateException("Suppression interdite : la commande contient des lignes.");
        }

        String sql = "DELETE FROM commande WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commande.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Commande introuvable (id=" + commande.getId() + ").");
            }
        } catch (SQLException e) {
            System.err.println("❌ delete : " + e.getMessage());
            throw new RuntimeException("Erreur suppression commande.", e);
        }
    }

    @Override
    public List<Commande> getAll() {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT id, date_commande, statut, montant_total, user_id FROM commande ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Commande commande = mapResultSet(rs);
                loadLignes(commande);
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println("❌ getAll : " + e.getMessage());
        }

        return commandes;
    }

    @Override
    public Commande getById(int id) {
        String sql = "SELECT id, date_commande, statut, montant_total, user_id FROM commande WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Commande commande = mapResultSet(rs);
                    loadLignes(commande);
                    return commande;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ getById : " + e.getMessage());
        }

        return null;
    }


    public void add2(Commande commande) {
        add(commande);
    }

    public List<Commande> getall() {
        return getAll();
    }

    // ─── Méthodes métier ─────────────────────────────────────────────────────
    public Commande getOrCreatePanier(int userId) {
        Commande panier = findPanierByUser(userId);
        if (panier != null) {
            return panier;
        }

        Commande nouveau = new Commande();
        nouveau.setUserId(userId);
        add(nouveau);
        return nouveau;
    }

    public Commande findPanierByUser(int userId) {
        String sql = "SELECT id, date_commande, statut, montant_total, user_id " +
                "FROM commande WHERE user_id=? AND statut=? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, Commande.Statut.PANIER.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Commande commande = mapResultSet(rs);
                    loadLignes(commande);
                    return commande;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ findPanierByUser : " + e.getMessage());
        }

        return null;
    }

    public List<Commande> findByUser(int userId) {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT id, date_commande, statut, montant_total, user_id FROM commande WHERE user_id=? ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande commande = mapResultSet(rs);
                    loadLignes(commande);
                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ findByUser : " + e.getMessage());
        }

        return commandes;
    }

    public List<Commande> getAllNonPanier() {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT id, date_commande, statut, montant_total, user_id FROM commande WHERE statut != ? ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, Commande.Statut.PANIER.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande commande = mapResultSet(rs);
                    loadLignes(commande);
                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ getAllNonPanier : " + e.getMessage());
        }

        return commandes;
    }

    public List<Commande> getByStatut(Commande.Statut statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Statut requis.");
        }

        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT id, date_commande, statut, montant_total, user_id FROM commande WHERE statut=? ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande commande = mapResultSet(rs);
                    loadLignes(commande);
                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ getByStatut : " + e.getMessage());
        }

        return commandes;
    }

    public void loadLignes(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }

        List<LigneCommande> lignes = ligneService.getByCommande(commande.getId());
        commande.setLignes(lignes);
    }

    public void refreshMontantTotal(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }

        BigDecimal total = ligneService.calculerTotal(commande.getId());
        commande.setMontantTotal(total);
        update(commande);
    }

    public LigneCommande ajouterProduitAuPanier(int userId, int produitId, int quantite) {
        Commande panier = getOrCreatePanier(userId);

        if (!panier.canEditPanier()) {
            throw new IllegalStateException("Panier non modifiable.");
        }

        LigneCommande ligne = ligneService.addToCommande(panier.getId(), produitId, quantite);
        refreshMontantTotal(panier);
        loadLignes(panier);

        return ligne;
    }

    public void supprimerLigneDuPanier(int ligneId, int commandeId) {
        ligneService.supprimerLigne(ligneId);

        Commande commande = getById(commandeId);
        if (commande == null) {
            throw new RuntimeException("Commande introuvable.");
        }

        refreshMontantTotal(commande);
        loadLignes(commande);
    }

    public void viderPanier(int commandeId) {
        Commande commande = getById(commandeId);
        if (commande == null) {
            throw new RuntimeException("Commande introuvable.");
        }

        if (!commande.canEditPanier()) {
            throw new IllegalStateException("Panier non modifiable.");
        }

        ligneService.viderPanier(commandeId);
        refreshMontantTotal(commande);
        loadLignes(commande);
    }

    public void validerCommande(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }

        loadLignes(commande);

        if (commande.getLignes().isEmpty()) {
            throw new IllegalStateException("Le panier est vide.");
        }

        for (LigneCommande l : commande.getLignes()) {
            Produit p = l.getProduit();
            if (p == null) {
                throw new IllegalStateException("Produit introuvable dans une ligne.");
            }

            int dispo = (p.getStock() == null) ? 0 : p.getStock();
            if (dispo < l.getQuantite()) {
                throw new IllegalStateException(
                        "Stock insuffisant pour '" + p.getNom() +
                                "'. Disponible : " + dispo + " | Demandé : " + l.getQuantite()
                );
            }
        }

        commande.validerCommande();
        update(commande);
    }

    public void payer(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }
        if (commande.getStatut() != Commande.Statut.EN_ATTENTE_PAIEMENT) {
            throw new IllegalStateException(
                    "La commande doit être EN_ATTENTE_PAIEMENT. Statut actuel : " + commande.getStatut()
            );
        }

        List<LigneCommande> lignes = ligneService.getByCommande(commande.getId());
        if (lignes.isEmpty()) {
            throw new IllegalStateException("Impossible de payer une commande sans lignes.");
        }

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            String sqlStock = "UPDATE produit SET stock = stock - ? WHERE id = ? AND stock >= ?";

            for (LigneCommande l : lignes) {
                Produit p = l.getProduit();
                if (p == null) {
                    throw new IllegalStateException("Produit introuvable pour une ligne.");
                }

                try (PreparedStatement ps = cnx.prepareStatement(sqlStock)) {
                    ps.setInt(1, l.getQuantite());
                    ps.setInt(2, p.getId());
                    ps.setInt(3, l.getQuantite());

                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        throw new IllegalStateException(
                                "Stock insuffisant pour '" + p.getNom() + "' (vérification finale)."
                        );
                    }
                }
            }

            commande.setLignes(lignes);
            commande.marquerPayee();

            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE commande SET statut=?, montant_total=? WHERE id=?")) {
                ps.setString(1, commande.getStatut().name());
                ps.setBigDecimal(2, commande.getMontantTotal());
                ps.setInt(3, commande.getId());
                ps.executeUpdate();
            }

            cnx.commit();
        } catch (Exception e) {
            try {
                cnx.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ rollback : " + ex.getMessage());
            }
            throw new RuntimeException("Paiement échoué : " + e.getMessage(), e);
        } finally {
            try {
                cnx.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    public void marquerLivree(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }

        commande.marquerLivree();
        update(commande);
    }

    public void annuler(Commande commande) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide.");
        }

        commande.annuler();
        update(commande);
    }

    public BigDecimal getChiffreAffaires() {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM commande WHERE statut IN ('PAYEE', 'LIVREE')";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal ca = rs.getBigDecimal(1);
                return ca != null ? ca : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("❌ getChiffreAffaires : " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    public int countByStatut(Commande.Statut statut) {
        if (statut == null) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM commande WHERE statut=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ countByStatut : " + e.getMessage());
        }

        return 0;
    }

    public String getDashboardSummary() {
        return String.format(
                "📊 Tableau de bord :%n" +
                        "  En attente de paiement : %d%n" +
                        "  Payées                 : %d%n" +
                        "  Livrées                : %d%n" +
                        "  Annulées               : %d%n" +
                        "  💰 CA total            : %s DT",
                countByStatut(Commande.Statut.EN_ATTENTE_PAIEMENT),
                countByStatut(Commande.Statut.PAYEE),
                countByStatut(Commande.Statut.LIVREE),
                countByStatut(Commande.Statut.ANNULEE),
                getChiffreAffaires().toPlainString()
        );
    }
}