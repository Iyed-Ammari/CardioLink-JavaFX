package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.utils.ManagerSession;
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

    // ✅ Récupère l'user depuis ManagerSession
    public void init() {
        User user = ManagerSession.getInstance().getCurrentUser();
        setCurrentUser(user);
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        welcomeLabel.setText("Welcome to CardioLink");
        String initial = user.getNom() != null && !user.getNom().isEmpty()
                ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
        avatarLabel.setText(initial);
        boolean isPatient = "ROLE_PATIENT".equals(user.getRoleClean());
        btnSuivis.setVisible(isPatient);
        btnSuivis.setManaged(isPatient);
        btnDossier.setVisible(isPatient);
        btnDossier.setManaged(isPatient);
    }

    @FXML private void viewDashboard() {
        User user = ManagerSession.getInstance().getCurrentUser();
        if (user == null) return;
        if ("ROLE_ADMIN".equals(user.getRoleClean())) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/dashboard_admin.fxml"));
                Scene scene = new Scene(loader.load(), 1100, 650);
                AdminUserDashboardController ctrl = loader.getController();
                Stage stage = (Stage) avatarLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("CardioLink - Admin Dashboard");
                stage.show();
                ctrl.init();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML private void goHome() { }

    @FXML private void goCommunity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_view.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Community");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goSuivis() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterSuivi.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mes Suivis");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goDossier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_medical.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Dossier Médical");
            stage.setScene(scene);
            stage.show();
            DossierMedicalPatientController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuRDV.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - RDV");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profil_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Profil");
            stage.setScene(scene);
            stage.show();
            ProfilPatientController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatView.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Chat");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goMarketplace() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/patient/produit-list-patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Marketplace");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout() {
        ManagerSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}