package com.cardiolink.Models;

public class Conversation {
    private int id;
    private int patientId;
    private int medecinId;
    private String created_at;
    private String updated_at;
    private boolean isActive;

    public Conversation(int id, int patientId, int medecinId, String created_at, String updated_at, boolean isActive) {
        this.id = id;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.isActive = isActive;
    }
    public Conversation() {
    }

    public Conversation(int patientId, int medecinId, String created_at, String updated_at, boolean isActive) {
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", medecinId=" + medecinId +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
