package com.cardiolink.Test;

import com.cardiolink.Models.Produit;
import com.cardiolink.Services.ProduitService;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {

        ProduitService serviceProduit = new ProduitService();

        try {
            // Ajouter avec Statement
            serviceProduit.add(new Produit("Electrocardiogramme ECG", "Mesure activité cardiaque", new BigDecimal("199.99"), 30, null, "MEDICAL"));

            // Ajouter avec PreparedStatement
            serviceProduit.add2(new Produit("Défibrillateur Portable", "Usage urgence cardiaque", new BigDecimal("499.99"), 10, null, "MEDICAL"));

            // Modifier
            serviceProduit.update(new Produit(2, "Tensiomètre Digital PRO", "Version PRO améliorée", new BigDecimal("69.99"), 90, null, "MEDICAL"));

            // Supprimer
            Produit pDelete = new Produit();
            pDelete.setId(14);
            serviceProduit.delete(pDelete);

            // Afficher tous
            System.out.println(serviceProduit.getall());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}