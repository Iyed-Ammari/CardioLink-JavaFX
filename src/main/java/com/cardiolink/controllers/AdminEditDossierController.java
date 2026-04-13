package com.cardiolink.controllers;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.Models.User;
import com.cardiolink.Services.DossierMedicalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class AdminEditDossierController {

    @FXML private Label            sidebarInitial;
    @FXML private Label            sidebarName;
    @FXML private Label            patientNameLabel;
    @FXML private ComboBox<String> groupeSanguinCombo;
    @FXML private TextArea         antecedentsArea;
    @FXML private TextArea         allergiesArea;
    @FXML private TextField        poidsField;
    @FXML private TextField        tailleField;
    @FXML private TextField        tenSysField;
    @FXML private TextField        tenDiaField;
    @FXML private TextField        freqField;
    @FXML private Label            errorLabel;
    @FXML private Label            successLabel;

    private User           currentAdmin;
    private DossierMedical dossier;
    private User           patient;

    private final DossierMedicalService dossierService = new DossierMedicalService();

    @FXML
    public void initialize() {
        groupeSanguinCombo.setItems(FXCollections.observableArrayList(
                "A+","A-","B+","B-","AB+","AB-","O+","O-"));
    }

    public void setData(User admin, DossierMedical dossier, User patient) {
        this.currentAdmin = admin;
        this.dossier      = dossier;
        this.patient      = patient;

        if (admin != null) {
            sidebarName.setText(admin.getPrenom() + " " + admin.getNom());
            String initial = admin.getNom() != null && !admin.getNom().isEmpty()
                    ? String.valueOf(admin.getNom().charAt(0)).toUpperCase() : "A";
            sidebarInitial.setText(initial);
        }

        patientNameLabel.setText(patient != null
                ? patient.getPrenom() + " " + patient.getNom() : "-");

        groupeSanguinCombo.setValue(
                dossier.getGroupeSanguin() != null ? dossier.getGroupeSanguin() : "A+");
        antecedentsArea.setText(
                dossier.getAntecedents() != null ? dossier.getAntecedents() : "");
        allergiesArea.setText(
                dossier.getAllergies() != null ? dossier.getAllergies() : "");
        poidsField.setText(dossier.getPoids()               != null
                ? String.valueOf(dossier.getPoids())               : "");
        tailleField.setText(dossier.getTaille()             != null
                ? String.valueOf(dossier.getTaille())             : "");
        tenSysField.setText(dossier.getTensionSystolique()  != null
                ? String.valueOf(dossier.getTensionSystolique())  : "");
        tenDiaField.setText(dossier.getTensionDiastolique() != null
                ? String.valueOf(dossier.getTensionDiastolique()) : "");
        freqField.setText(dossier.getFrequenceCardiaque()   != null
                ? String.valueOf(dossier.getFrequenceCardiaque())   : "");
    }

    @FXML
    private void handleSave() {
        clearMessages();
        dossier.setGroupeSanguin(groupeSanguinCombo.getValue());
        dossier.setAntecedents(antecedentsArea.getText().trim());
        dossier.setAllergies(allergiesArea.getText().trim());
        dossier.setPoids(parseDouble(poidsField.getText()));
        dossier.setTaille(parseDouble(tailleField.getText()));
        dossier.setTensionSystolique(parseDouble(tenSysField.getText()));
        dossier.setTensionDiastolique(parseDouble(tenDiaField.getText()));
        dossier.setFrequenceCardiaque(parseDouble(freqField.getText()));
        try {
            dossierService.save(dossier);
            showSuccess("✅ Dossier mis à jour avec succès !");
        } catch (SQLException e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Navigation ───────────────────────────────────────────
    @FXML private void goDossiers() { backToDashboard("dossiers"); }
    @FXML private void goHome()     { backToDashboard("home"); }
    @FXML private void goUsers()    { backToDashboard("users"); }

    // ✅ AJOUT : manquait dans l'ancien code
    @FXML private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) patientNameLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void backToDashboard(String section) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/dashboard_admin.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) patientNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            AdminDashboardController ctrl = loader.getController();
            ctrl.initAdmin(currentAdmin, section);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Double parseDouble(String s) {
        try { return s == null || s.trim().isEmpty() ? null : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private void clearMessages()        { errorLabel.setText(""); successLabel.setText(""); }
    private void showError(String msg)  { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg){ successLabel.setText(msg); errorLabel.setText(""); }
}