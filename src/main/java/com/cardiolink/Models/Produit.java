package com.cardiolink.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Produit {

    private Integer id;
    private String  nom;
    private String  description;
    private BigDecimal prix;
    private Integer stock;
    private String  imageUrl;
    private String  categorie;

    public Produit() {}

    public Produit(Integer id, String nom, String description, BigDecimal prix,
                   Integer stock, String imageUrl, String categorie) {
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
        setNom(nom);
        setDescription(description);
        setPrix(prix);
        setStock(stock);
        setImageUrl(imageUrl);
        setCategorie(categorie);
    }


    public Integer getId()           { return id; }
    public void    setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }


    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty())
            throw new IllegalArgumentException("Le nom du produit est obligatoire.");

        String v = nom.trim();

        if (v.length() < 2)
            throw new IllegalArgumentException("Le nom doit contenir au moins 2 caractères.");
        if (v.length() > 255)
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 255 caractères.");

        if (!v.matches("^[A-Za-zÀ-ÿ0-9][A-Za-zÀ-ÿ0-9\\s''()\\-+°%/]{1,254}$"))
            throw new IllegalArgumentException(
                    "Le nom doit commencer par une lettre ou un chiffre et ne contenir que des caractères valides."
            );

        this.nom = v;
    }


    public String getDescription() { return description; }

    public void setDescription(String description) {
        this.description = (description == null || description.trim().isEmpty())
                ? null : description.trim();
    }

    public BigDecimal getPrix() { return prix; }

    public void setPrix(BigDecimal prix) {
        if (prix == null || prix.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Le prix doit être strictement supérieur à 0.");
        if (prix.compareTo(new BigDecimal("99999.99")) > 0)
            throw new IllegalArgumentException("Le prix ne peut pas dépasser 99 999.99 DT.");
        this.prix = prix.setScale(2, RoundingMode.HALF_UP);
    }


    public Integer getStock() { return stock; }

    public void setStock(Integer stock) {
        if (stock == null || stock < 0)
            throw new IllegalArgumentException("Le stock doit être >= 0.");
        if (stock > 999999)
            throw new IllegalArgumentException("Le stock ne peut pas dépasser 999 999.");
        this.stock = stock;
    }

    public String getImageUrl() { return imageUrl; }


    public void setImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            this.imageUrl = null;
            return;
        }

        String v = imageUrl.trim();

        if (!v.matches("(?i)^(https?://.+|file:/{1,3}.+|/uploads/.+)$")) {
            throw new IllegalArgumentException(
                    "L'image doit être une URL http(s), un fichier local (file:/...) ou un chemin /uploads/..."
            );
        }

        this.imageUrl = v;
    }

    public String getCategorie() { return categorie; }

    public void setCategorie(String categorie) {
        if (categorie == null || categorie.trim().isEmpty())
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        if (categorie.trim().length() > 50)
            throw new IllegalArgumentException("La catégorie ne doit pas dépasser 50 caractères.");
        this.categorie = categorie.trim().toUpperCase();
    }


    public String getStockStatus() {
        return (this.stock != null && this.stock == 0) ? "RUPTURE" : "DISPONIBLE";
    }

    @Override
    public String toString() {
        return "Produit{id=" + id + ", nom='" + nom + "', prix=" + prix
                + ", stock=" + stock + ", categorie='" + categorie + "'}";
    }
}