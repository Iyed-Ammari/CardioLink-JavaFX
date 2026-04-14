package com.cardiolink.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LigneCommande {

    private Integer id;
    private Integer commandeId;
    private int quantite;
    private BigDecimal prixUnitaire;
    private Produit produit;

    public LigneCommande() {
        this.quantite = 1;
        this.prixUnitaire = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public LigneCommande(Integer id, Integer commandeId, int quantite,
                         BigDecimal prixUnitaire, Produit produit) {
        this();
        this.id = id;
        this.commandeId = commandeId;
        setQuantite(quantite);
        setPrixUnitaire(prixUnitaire);
        this.produit = produit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Integer commandeId) {
        if (commandeId == null || commandeId <= 0) {
            throw new IllegalArgumentException("commandeId invalide.");
        }
        this.commandeId = commandeId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        if (quantite < 1) {
            throw new IllegalArgumentException("La quantité doit être >= 1.");
        }
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix unitaire doit être >= 0.");
        }
        this.prixUnitaire = prixUnitaire.setScale(2, RoundingMode.HALF_UP);
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public BigDecimal getTotalLigne() {
        return prixUnitaire
                .multiply(BigDecimal.valueOf(quantite))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "LigneCommande{" +
                "id=" + id +
                ", commandeId=" + commandeId +
                ", produit=" + (produit != null ? produit.getNom() : "null") +
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", total=" + getTotalLigne() +
                '}';
    }
}