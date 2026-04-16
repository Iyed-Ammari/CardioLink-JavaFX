package com.cardiolink.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {

    public enum Statut {
        PANIER,
        EN_ATTENTE_PAIEMENT,
        PAYEE,
        LIVREE,
        ANNULEE
    }

    private Integer id;
    private Integer userId;
    private LocalDateTime dateCommande;
    private Statut statut;
    private BigDecimal montantTotal;
    private List<LigneCommande> lignes;

    public Commande() {
        this.dateCommande = LocalDateTime.now();
        this.statut = Statut.PANIER;
        this.montantTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.lignes = new ArrayList<>();
    }

    public Commande(Integer id, Integer userId, LocalDateTime dateCommande,
                    Statut statut, BigDecimal montantTotal) {
        this();
        this.id = id;
        this.userId = userId;
        this.dateCommande = (dateCommande != null) ? dateCommande : LocalDateTime.now();
        this.statut = (statut != null) ? statut : Statut.PANIER;
        setMontantTotal(montantTotal != null ? montantTotal : BigDecimal.ZERO);
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
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId invalide.");
        }
        this.userId = userId;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = (dateCommande != null) ? dateCommande : LocalDateTime.now();
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut ne peut pas être null.");
        }
        this.statut = statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        if (montantTotal == null || montantTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total doit être >= 0.");
        }
        this.montantTotal = montantTotal.setScale(2, RoundingMode.HALF_UP);
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommande> lignes) {
        this.lignes = (lignes != null) ? lignes : new ArrayList<>();
        recalculateTotal();
    }

    public void addLigne(LigneCommande ligne) {
        if (ligne != null && !this.lignes.contains(ligne)) {
            this.lignes.add(ligne);
        }
        recalculateTotal();
    }

    public void removeLigne(LigneCommande ligne) {
        this.lignes.remove(ligne);
        recalculateTotal();
    }

    public void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LigneCommande ligne : lignes) {
            if (ligne != null && ligne.getPrixUnitaire() != null) {
                total = total.add(ligne.getTotalLigne());
            }
        }
        setMontantTotal(total);
    }

    public boolean canEditPanier() {
        return Statut.PANIER.equals(this.statut);
    }

    public void validerCommande() {
        if (!Statut.PANIER.equals(this.statut)) {
            throw new IllegalStateException("Seul un panier peut être validé.");
        }
        if (this.lignes == null || this.lignes.isEmpty()) {
            throw new IllegalStateException("Panier vide.");
        }
        this.statut = Statut.EN_ATTENTE_PAIEMENT;
        recalculateTotal();
    }

    public void annuler() {
        if (Statut.PAYEE.equals(this.statut) || Statut.LIVREE.equals(this.statut)) {
            throw new IllegalStateException("Impossible d'annuler une commande payée ou livrée.");
        }
        this.statut = Statut.ANNULEE;
    }

    public void marquerPayee() {
        if (!Statut.EN_ATTENTE_PAIEMENT.equals(this.statut)) {
            throw new IllegalStateException("Commande non valide pour paiement.");
        }
        this.statut = Statut.PAYEE;
    }

    public void marquerLivree() {
        if (!Statut.PAYEE.equals(this.statut)) {
            throw new IllegalStateException("La commande doit être PAYEE avant LIVREE.");
        }
        this.statut = Statut.LIVREE;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", userId=" + userId +
                ", dateCommande=" + dateCommande +
                ", statut=" + statut +
                ", montantTotal=" + montantTotal +
                '}';
    }
}