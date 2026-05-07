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
    private boolean archive;
    private Double latitude;
    private Double longitude;

    public Intervention() {
        this.datePlanifiee = LocalDateTime.now();
        this.statut = "En attente";
        this.type = "Consultation";
        this.archive = false;
    }

    public Intervention(int id, String type, String description, int medecinId) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.medecinId = medecinId;
        this.datePlanifiee = LocalDateTime.now();
        this.statut = "En attente";
        this.archive = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDatePlanifiee() {
        return datePlanifiee;
    }

    public void setDatePlanifiee(LocalDateTime datePlanifiee) {
        this.datePlanifiee = datePlanifiee;
    }

    public LocalDateTime getDateCompletion() {
        return dateCompletion;
    }

    public void setDateCompletion(LocalDateTime dateCompletion) {
        this.dateCompletion = dateCompletion;
    }

    public int getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public Suivi getSuiviOrigine() {
        return suiviOrigine;
    }

    public void setSuiviOrigine(Suivi suiviOrigine) {
        this.suiviOrigine = suiviOrigine;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void markAsCompleted() {
        this.statut = "Effectuée";
        this.dateCompletion = LocalDateTime.now();
    }
}