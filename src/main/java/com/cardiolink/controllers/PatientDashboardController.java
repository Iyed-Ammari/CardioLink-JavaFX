package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.controllers.DossierMedicalPatientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientDashboardController implements UserAwareController {

    @FXML private Label  avatarLabel;
    @FXML private Circle avatarCircle;
    @FXML private Label  welcomeLabel;
    @FXML private Button btnSuivis;
    @FXML private Button btnDossier;

    private User currentUser;

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user == null) return;

        String role    = user.getRoleClean();
        String initial = user.getNom() != null && !user.getNom().isEmpty()
                ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
        avatarLabel.setText(initial);

        switch (role) {
            case "ROLE_ADMIN" -> {
                welcomeLabel.setText("Welcome, Admin " + user.getPrenom() + " !");
                btnSuivis.setVisible(false);  btnSuivis.setManaged(false);
                btnDossier.setVisible(false); btnDossier.setManaged(false);
            }
            case "ROLE_MEDECIN" -> {
                welcomeLabel.setText("Welcome, Dr. " + user.getPrenom() + " !");
                btnSuivis.setVisible(false);  btnSuivis.setManaged(false);
                btnDossier.setVisible(false); btnDossier.setManaged(false);
            }
            case "ROLE_PATIENT" -> {
                welcomeLabel.setText("Welcome, " + user.getPrenom() + " !");
                btnSuivis.setVisible(true);  btnSuivis.setManaged(true);
                btnDossier.setVisible(true); btnDossier.setManaged(true);
            }
        }
    }

    // ✅ "View Dashboard" → reste sur cette page (on y est déjà)
    // Pour admin → redirige vers son dashboard avec sidebar
    @FXML
    private void viewDashboard() {
        if (currentUser == null) return;

        if ("ROLE_ADMIN".equals(currentUser.getRoleClean())) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/cardiolink/fxml/dashboard_admin.fxml"));
                Scene scene = new Scene(loader.load(), 1100, 650);
                AdminDashboardController ctrl = loader.getController();
                Stage stage = (Stage) avatarLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("CardioLink - Admin Dashboard");
                stage.show();
                ctrl.setCurrentUser(currentUser);
            } catch (IOException e) { e.printStackTrace(); }
        }
        // Pour ROLE_PATIENT et ROLE_MEDECIN → on est déjà sur le dashboard, ne rien faire
    }

    @FXML private void goHome() { /* déjà ici */ }

    @FXML private void goCommunity() {
        navigateTo("/com/cardiolink/fxml/community.fxml", "CardioLink - Community", 1100, 650);
    }

    @FXML private void goSuivis() {
        navigateTo("/com/cardiolink/fxml/suivis_patient.fxml", "CardioLink - Mes Suivis", 1100, 650);
    }

    @FXML private void goDossier() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/dossier_medical.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Dossier Médical");
            stage.setScene(scene);
            stage.show();
            DossierMedicalPatientController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/profil_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Profil");
            stage.setScene(scene);
            stage.show();
            ProfilPatientController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateTo(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), w, h);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            if (loader.getController() instanceof UserAwareController) {
                ((UserAwareController) loader.getController()).setCurrentUser(currentUser);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}