package com.cardiolink.Models;

import java.time.LocalDateTime;

public class Rendezvous {
    private int id;
    private LocalDateTime dateHeure;
    private String statut;
    private String type;
    private String lienVisio;
    private String remarques;
    private int patientId;
    private int medecinId;

    public Rendezvous() {}

    public Rendezvous(LocalDateTime dateHeure, String statut, String type, String lienVisio, String remarques, int patientId, int medecinId) {
        this.dateHeure = dateHeure;
        this.statut = statut;
        this.type = type;
        this.lienVisio = lienVisio;
        this.remarques = remarques;
        this.patientId = patientId;
        this.medecinId = medecinId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLienVisio() { return lienVisio; }
    public void setLienVisio(String lienVisio) { this.lienVisio = lienVisio; }
    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    @Override
    public String toString() {
        return "Rendezvous{" + "id=" + id + ", dateHeure=" + dateHeure + ", type='" + type + '\'' + ", statut='" + statut + '\'' + '}';
    }
}