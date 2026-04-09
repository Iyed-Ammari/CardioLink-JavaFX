package com.cardiolink.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LigneCommande {

    private Integer id;
    private int quantite;
    private BigDecimal prixUnitaire;
    private Commande commande;
    private Produit produit;

    public LigneCommande() {
        this.quantite = 1;
        this.prixUnitaire = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public LigneCommande(Integer id, int quantite, BigDecimal prixUnitaire,
                         Commande commande, Produit produit) {
        this();
        this.id = id;
        setQuantite(quantite);
        setPrixUnitaire(prixUnitaire);
        this.commande = commande;
        this.produit = produit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        if (quantite < 1) {
            throw new IllegalArgumentException("La quantité doit être >= 1.");
        }
        this.quantite = quantite;

        if (this.commande != null) {
            this.commande.recalculateTotal();
        }
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        if (prixUnitaire == null) {
            throw new IllegalArgumentException("Le prix unitaire est obligatoire.");
        }
        if (prixUnitaire.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix unitaire doit être >= 0.");
        }

        this.prixUnitaire = prixUnitaire.setScale(2, RoundingMode.HALF_UP);

        if (this.commande != null) {
            this.commande.recalculateTotal();
        }
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
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
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", totalLigne=" + getTotalLigne() +
                '}';
    }
}