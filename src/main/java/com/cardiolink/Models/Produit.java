package com.cardiolink.Models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Produit {

    private Integer id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private Integer stock;
    private String imageUrl;
    private String categorie;
    private List<LigneCommande> lignes;

    public Produit() {
        this.lignes = new ArrayList<>();
    }

    public Produit(Integer id, String nom, String description, BigDecimal prix,
                   Integer stock, String imageUrl, String categorie) {
        this();
        this.id = id;
        setNom(nom);
        setDescription(description);
        setPrix(prix);
        setStock(stock);
        setImageUrl(imageUrl);
        setCategorie(categorie);
    }

    public Produit(String nom, String description, BigDecimal prix,
                   Integer stock, String imageUrl, String categorie) {
        this();
        setNom(nom);
        setDescription(description);
        setPrix(prix);
        setStock(stock);
        setImageUrl(imageUrl);
        setCategorie(categorie);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        this.nom = nom.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        if (prix == null || prix.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit être strictement supérieur à 0.");
        }
        this.prix = prix;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Le stock doit être >= 0.");
        }
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = (categorie == null || categorie.trim().isEmpty())
                ? null
                : categorie.trim().toUpperCase();
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommande> lignes) {
        this.lignes = (lignes != null) ? lignes : new ArrayList<>();
    }

    public void addLigne(LigneCommande ligne) {
        if (ligne != null && !this.lignes.contains(ligne)) {
            this.lignes.add(ligne);
            if (ligne.getProduit() != this) {
                ligne.setProduit(this);
            }
        }
    }

    public void removeLigne(LigneCommande ligne) {
        if (ligne != null && this.lignes.remove(ligne)) {
            if (ligne.getProduit() == this) {
                ligne.setProduit(null);
            }
        }
    }

    public String getStockStatus() {
        return (this.stock != null && this.stock == 0) ? "RUPTURE" : "DISPONIBLE";
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", stock=" + stock +
                ", imageUrl='" + imageUrl + '\'' +
                ", categorie='" + categorie + '\'' +
                '}';
    }
}