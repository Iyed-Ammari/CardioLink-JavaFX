package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class ModifierProfilController implements UserAwareController {

    @FXML private TextField     nomField;
    @FXML private TextField     prenomField;
    @FXML private TextField     telField;
    @FXML private TextField     adresseField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;
    @FXML private Label         avatarLabel;
    @FXML private Circle        avatarCircle;
    @FXML private Button        btnSuivis;
    @FXML private Button        btnDossier;

    private final UserService userService = new UserService();

    // ✅ Récupère l'user depuis ManagerSession
    public void init() {
        setCurrentUser(ManagerSession.getInstance().getCurrentUser());
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        nomField.setText(user.getNom()         != null ? user.getNom()     : "");
        prenomField.setText(user.getPrenom()   != null ? user.getPrenom()  : "");
        telField.setText(user.getTel()         != null ? user.getTel()     : "");
        adresseField.setText(user.getAdresse() != null ? user.getAdresse() : "");
        String initial = user.getNom() != null && !user.getNom().isEmpty()
                ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
        avatarLabel.setText(initial);
        boolean isPatient = "ROLE_PATIENT".equals(user.getRoleClean());
        btnSuivis.setVisible(isPatient);
        btnSuivis.setManaged(isPatient);
        btnDossier.setVisible(isPatient);
        btnDossier.setManaged(isPatient);
    }

    @FXML private void handleSave() {
        clearMessages();
        User user = ManagerSession.getInstance().getCurrentUser();
        if (user == null) return;

        String nom      = nomField.getText().trim();
        String prenom   = prenomField.getText().trim();
        String tel      = telField.getText().trim();
        String adresse  = adresseField.getText().trim();
        String password = passwordField.getText().trim();

        if (nom.isEmpty())                                { showError("Le nom est obligatoire !"); return; }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s-]+"))            { showError("Le nom ne doit contenir que des lettres !"); return; }
        if (prenom.isEmpty())                             { showError("Le prénom est obligatoire !"); return; }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s-]+"))         { showError("Le prénom ne doit contenir que des lettres !"); return; }
        if (!tel.isEmpty() && !tel.matches("^[\\+]?[0-9\\s-]{8,15}$")) { showError("Numéro invalide !"); return; }
        if (!password.isEmpty() && password.length() < 4){ showError("Minimum 4 caractères !"); return; }

        user.setNom(nom);
        user.setPrenom(prenom);
        user.setTel(tel);
        user.setAdresse(adresse);
        if (!password.isEmpty()) user.setPassword(password);

        try {
            userService.updateUser(user);
            // ✅ Mettre à jour dans ManagerSession
            ManagerSession.getInstance().setCurrentUser(user);
            showSuccess("✅ Profil mis à jour avec succès !");
            avatarLabel.setText(nom.isEmpty() ? "?" : String.valueOf(nom.charAt(0)).toUpperCase());
        } catch (SQLException e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle("CardioLink - Dashboard");
            stage.setScene(scene);
            stage.show();
            PatientDashboardController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goDossier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_medical.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Dossier Médical");
            stage.setScene(scene);
            stage.show();
            DossierMedicalPatientController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profil_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Profil");
            stage.setScene(scene);
            stage.show();
            ProfilPatientController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goSuivis() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterSuivi.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle("CardioLink - Mes Suivis");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goCommunity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_view.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle("CardioLink - Community");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout() {
        ManagerSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void clearMessages()         { errorLabel.setText(""); successLabel.setText(""); }
    private void showError(String msg)   { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg) { successLabel.setText(msg); errorLabel.setText(""); }
}