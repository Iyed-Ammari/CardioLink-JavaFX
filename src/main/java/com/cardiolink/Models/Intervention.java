package com.cardiolink.Models;

import java.time.LocalDateTime;

public class Intervention {
    private int id;
    private String type;
    private String description;
    private String statut;
    private LocalDateTime datePlanifiee;
    private LocalDateTime dateCompletion;
    private int medecinId;
    private Suivi suiviOrigine;

    public Intervention() {
        this.datePlanifiee = LocalDateTime.now();
        this.statut = "En attente";
        this.type = "Consultation";
    }

    public Intervention(int id, String type, String description, int medecinId) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.medecinId = medecinId;
        this.datePlanifiee = LocalDateTime.now();
        this.statut = "En attente";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // INDISPENSABLE

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDatePlanifiee() { return datePlanifiee; }
    public LocalDateTime getDateCompletion() { return dateCompletion; }
    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }
    public Suivi getSuiviOrigine() { return suiviOrigine; }
    public void setSuiviOrigine(Suivi suiviOrigine) { this.suiviOrigine = suiviOrigine; }

    public void markAsCompleted() {
        this.statut = "Effectuée";
        this.dateCompletion = LocalDateTime.now();
    }
}