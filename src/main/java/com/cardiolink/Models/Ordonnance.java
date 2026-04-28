package com.cardiolink.Models;

import java.time.LocalDateTime;

public class Ordonnance {
    private int id;
    private String reference;
    private LocalDateTime dateCreation;
    private int consultationId;
    private String diagnostic;
    private String notes;
    private String medecinNom;
    private String patientNom;

    public Ordonnance() {}

    public Ordonnance(String reference, LocalDateTime dateCreation, int consultationId, String diagnostic, String notes, String medecinNom, String patientNom) {
        this.reference = reference;
        this.dateCreation = dateCreation;
        this.consultationId = consultationId;
        this.diagnostic = diagnostic;
        this.notes = notes;
        this.medecinNom = medecinNom;
        this.patientNom = patientNom;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public int getConsultationId() { return consultationId; }
    public void setConsultationId(int consultationId) { this.consultationId = consultationId; }
    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }
    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    @Override
    public String toString() {
        return "Ordonnance{" + "id=" + id + ", ref='" + reference + '\'' + ", patient='" + patientNom + '\'' + '}';
    }
}