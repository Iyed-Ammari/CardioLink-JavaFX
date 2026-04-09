package com.cardiolink.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {

    public static final String STATUT_PANIER = "PANIER";
    public static final String STATUT_EN_ATTENTE_PAIEMENT = "EN_ATTENTE_PAIEMENT";
    public static final String STATUT_PAYEE = "PAYEE";
    public static final String STATUT_LIVREE = "LIVREE";
    public static final String STATUT_ANNULEE = "ANNULEE";

    private Integer id;
    private Integer userId;
    private LocalDateTime dateCommande;
    private String statut;
    private BigDecimal montantTotal;
    private List<LigneCommande> lignes;

    public Commande() {
        this.dateCommande = LocalDateTime.now();
        this.statut = STATUT_PANIER;
        this.montantTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.lignes = new ArrayList<>();
    }

    public Commande(Integer id, Integer userId, LocalDateTime dateCommande,
                    String statut, BigDecimal montantTotal) {
        this();
        this.id = id;
        this.userId = userId;
        this.dateCommande = (dateCommande != null) ? dateCommande : LocalDateTime.now();
        this.montantTotal = (montantTotal != null)
                ? montantTotal.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        setStatut(statut != null ? statut : STATUT_PANIER);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        if (!STATUT_PANIER.equals(statut)
                && !STATUT_EN_ATTENTE_PAIEMENT.equals(statut)
                && !STATUT_PAYEE.equals(statut)
                && !STATUT_LIVREE.equals(statut)
                && !STATUT_ANNULEE.equals(statut)) {
            throw new IllegalArgumentException("Statut commande invalide : " + statut);
        }
        this.statut = statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    private void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal.setScale(2, RoundingMode.HALF_UP);
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommande> lignes) {
        this.lignes = (lignes != null) ? lignes : new ArrayList<>();

        for (LigneCommande ligne : this.lignes) {
            if (ligne != null && ligne.getCommande() != this) {
                ligne.setCommande(this);
            }
        }

        recalculateTotal();
    }

    public void addLigne(LigneCommande ligne) {
        if (ligne != null && !this.lignes.contains(ligne)) {
            this.lignes.add(ligne);
            if (ligne.getCommande() != this) {
                ligne.setCommande(this);
            }
        }
        recalculateTotal();
    }

    public void removeLigne(LigneCommande ligne) {
        if (ligne != null && this.lignes.remove(ligne)) {
            if (ligne.getCommande() == this) {
                ligne.setCommande(null);
            }
        }
        recalculateTotal();
    }

    public void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (LigneCommande ligne : lignes) {
            if (ligne != null && ligne.getPrixUnitaire() != null) {
                total = total.add(
                        ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantite()))
                );
            }
        }

        setMontantTotal(total);
    }

    public boolean canEditPanier() {
        return STATUT_PANIER.equals(this.statut);
    }

    public void validerCommande() {
        if (!STATUT_PANIER.equals(this.statut)) {
            throw new IllegalStateException("Seul un panier peut être validé.");
        }
        if (this.lignes == null || this.lignes.isEmpty()) {
            throw new IllegalStateException("Panier vide.");
        }
        this.setStatut(STATUT_EN_ATTENTE_PAIEMENT);
        this.recalculateTotal();
    }

    public void annuler() {
        if (STATUT_PAYEE.equals(this.statut) || STATUT_LIVREE.equals(this.statut)) {
            throw new IllegalStateException("Impossible d'annuler une commande payée ou livrée.");
        }
        this.setStatut(STATUT_ANNULEE);
    }

    public void marquerPayee() {
        if (!STATUT_EN_ATTENTE_PAIEMENT.equals(this.statut)) {
            throw new IllegalStateException("Commande non valide pour paiement.");
        }
        this.setStatut(STATUT_PAYEE);
    }

    public void marquerLivree() {
        if (!STATUT_PAYEE.equals(this.statut)) {
            throw new IllegalStateException("Une commande doit être PAYEE avant LIVREE.");
        }
        this.setStatut(STATUT_LIVREE);
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", userId=" + userId +
                ", dateCommande=" + dateCommande +
                ", statut='" + statut + '\'' +
                ", montantTotal=" + montantTotal +
                '}';
    }
}