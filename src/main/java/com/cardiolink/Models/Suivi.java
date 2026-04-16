package com.cardiolink.Models;

import java.time.LocalDateTime;

public class Suivi {
    private int id;
    private String typeDonnee;
    private float valeur;
    private String unite;
    private LocalDateTime dateSaisie;
    private String niveauUrgence;
    private int patientId;

    public Suivi() {
        this.dateSaisie = LocalDateTime.now();
        this.niveauUrgence = "Normal";
    }

    public Suivi(int id, String typeDonnee, float valeur, String unite, int patientId) {
        this.id = id;
        this.typeDonnee = typeDonnee;
        this.valeur = valeur;
        this.unite = unite;
        this.patientId = patientId;
        this.dateSaisie = LocalDateTime.now();
        this.updateNiveauUrgence();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // Ajouté

    public String getTypeDonnee() { return typeDonnee; }
    public void setTypeDonnee(String typeDonnee) { this.typeDonnee = typeDonnee; }

    public float getValeur() { return valeur; }
    public void setValeur(float valeur) {
        this.valeur = valeur;
        updateNiveauUrgence();
    }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    public LocalDateTime getDateSaisie() { return dateSaisie; }
    public String getNiveauUrgence() { return niveauUrgence; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public boolean isCritical() {
        switch (typeDonnee) {
            case "Fréquence Cardiaque": return valeur > 120 || valeur < 40;
            case "SpO2": return valeur < 90;
            case "Température": return valeur > 39 || valeur < 35;
            case "Glycémie": return valeur > 250 || valeur < 70;
            default: return false;
        }
    }

    private boolean isStable() {
        switch (typeDonnee) {
            case "Fréquence Cardiaque": return valeur >= 100 && valeur <= 120;
            case "SpO2": return valeur >= 90 && valeur < 95;
            case "Température": return valeur > 37.5 && valeur <= 39;
            case "Glycémie": return (valeur >= 200 && valeur <= 250) || (valeur >= 70 && valeur <= 100);
            default: return false;
        }
    }

    private void updateNiveauUrgence() {
        if (isCritical()) niveauUrgence = "Critique";
        else if (isStable()) niveauUrgence = "Stable";
        else niveauUrgence = "Normal";
    }
    public void setDateSaisie(LocalDateTime dateSaisie) {
        this.dateSaisie = dateSaisie;
    }
}