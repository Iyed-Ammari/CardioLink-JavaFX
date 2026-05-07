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
    private boolean archive;

    public Suivi() {
        this.dateSaisie = LocalDateTime.now();
        this.niveauUrgence = "Normal";
        this.archive = false;
    }

    public Suivi(int id, String typeDonnee, float valeur, String unite, int patientId) {
        this.id = id;
        this.typeDonnee = typeDonnee;
        this.valeur = valeur;
        this.unite = unite;
        this.patientId = patientId;
        this.dateSaisie = LocalDateTime.now();
        this.archive = false;
        this.updateNiveauUrgence();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeDonnee() {
        return typeDonnee;
    }

    public void setTypeDonnee(String typeDonnee) {
        this.typeDonnee = typeDonnee;
        updateNiveauUrgence();
    }

    public float getValeur() {
        return valeur;
    }

    public void setValeur(float valeur) {
        this.valeur = valeur;
        updateNiveauUrgence();
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public LocalDateTime getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(LocalDateTime dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public String getNiveauUrgence() {
        return niveauUrgence;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public boolean isValeurValide() {
        switch (typeDonnee) {
            case "Fréquence Cardiaque":
                return valeur >= 0 && valeur <= 300;

            case "SpO2":
                return valeur >= 0 && valeur <= 100;

            case "Température":
                return valeur >= 25 && valeur <= 45;

            case "Glycémie":
                return valeur >= 0 && valeur <= 600; // mg/dL

            case "Tension":
                return valeur >= 0 && valeur <= 300; // approximation systolique seule

            default:
                return true;
        }
    }

    public boolean isCritical() {
        switch (typeDonnee) {
            case "Fréquence Cardiaque":
                // Critique : <40 ou >130 bpm
                return valeur < 40 || valeur > 130;

            case "SpO2":
                // Critique : <90 %
                return valeur < 90;

            case "Température":
                // Critique : <35 ou >=40 °C
                return valeur < 35 || valeur >= 40;

            case "Glycémie":
                // Critique : <54 ou >300 mg/dL
                return valeur < 54 || valeur > 300;

            case "Tension":
                // Approximation sur la systolique seule
                // Critique : <80 ou >180
                return valeur < 80 || valeur > 180;

            default:
                return false;
        }
    }

    private boolean isStable() {
        switch (typeDonnee) {
            case "Fréquence Cardiaque":
                // Stable : 40–59 ou 101–130 bpm
                return (valeur >= 40 && valeur <= 59) || (valeur >= 101 && valeur <= 130);

            case "SpO2":
                // Stable : 90–94 %
                return valeur >= 90 && valeur <= 94;

            case "Température":
                // Stable : 35–36.4 ou 37.6–39.9 °C
                return (valeur >= 35 && valeur <= 36.4f) || (valeur >= 37.6f && valeur <= 39.9f);

            case "Glycémie":
                // Stable : 54–69 ou 141–300 mg/dL
                return (valeur >= 54 && valeur <= 69) || (valeur >= 141 && valeur <= 300);

            case "Tension":
                // Approximation sur la systolique seule
                // Stable : 80–89 ou 121–180
                return (valeur >= 80 && valeur <= 89) || (valeur >= 121 && valeur <= 180);

            default:
                return false;
        }
    }

    private void updateNiveauUrgence() {
        if (!isValeurValide()) {
            niveauUrgence = "Valeur invalide";
            return;
        }

        if (isCritical()) {
            niveauUrgence = "Critique";
        } else if (isStable()) {
            niveauUrgence = "Stable";
        } else {
            niveauUrgence = "Normal";
        }
    }
}