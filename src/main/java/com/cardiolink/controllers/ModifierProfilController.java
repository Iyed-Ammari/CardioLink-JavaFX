package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
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
    @FXML private Label         photoNameLabel;
    @FXML private Label         photoInitial;
    @FXML private Label         avatarLabel;
    @FXML private Circle        avatarCircle;
    @FXML private Circle        photoCircle;

    private User currentUser;
    private File selectedPhoto;
    private final UserService userService = new UserService();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            nomField.setText(user.getNom()         != null ? user.getNom()     : "");
            prenomField.setText(user.getPrenom()   != null ? user.getPrenom()  : "");
            telField.setText(user.getTel()         != null ? user.getTel()     : "");
            adresseField.setText(user.getAdresse() != null ? user.getAdresse() : "");
            String initial = user.getNom() != null && !user.getNom().isEmpty()
                    ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
            avatarLabel.setText(initial);
            photoInitial.setText(initial);
        }
    }

    @FXML private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = chooser.showOpenDialog(nomField.getScene().getWindow());
        if (file != null) {
            selectedPhoto = file;
            photoNameLabel.setText(file.getName());
        }
    }

    @FXML private void handleSave() {
        clearMessages();
        String nom      = nomField.getText().trim();
        String prenom   = prenomField.getText().trim();
        String tel      = telField.getText().trim();
        String adresse  = adresseField.getText().trim();
        String password = passwordField.getText().trim();

        if (nom.isEmpty())                                  { showError("Le nom est obligatoire !"); return; }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s-]+"))              { showError("Le nom ne doit contenir que des lettres !"); return; }
        if (prenom.isEmpty())                               { showError("Le prénom est obligatoire !"); return; }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s-]+"))           { showError("Le prénom ne doit contenir que des lettres !"); return; }
        if (!tel.isEmpty() && !tel.matches("^[\\+]?[0-9\\s-]{8,15}$")) { showError("Numéro de téléphone invalide !"); return; }
        if (!password.isEmpty() && password.length() < 4)  { showError("Minimum 4 caractères pour le mot de passe !"); return; }

        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setTel(tel);
        currentUser.setAdresse(adresse);
        if (!password.isEmpty())   currentUser.setPassword(password);
        if (selectedPhoto != null) currentUser.setImageUrl(selectedPhoto.getAbsolutePath());

        try {
            userService.updateUser(currentUser);
            showSuccess("✅ Profil mis à jour avec succès !");
            String initial = nom.isEmpty() ? "?" : String.valueOf(nom.charAt(0)).toUpperCase();
            avatarLabel.setText(initial);
            photoInitial.setText(initial);
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void goHome() {
        navigateTo("/com/cardiolink/fxml/dashboard_patient.fxml", "CardioLink - Dashboard", 1100, 650);
    }
    @FXML private void goCommunity() {
        navigateTo("/com/cardiolink/fxml/community.fxml", "CardioLink - Community", 1100, 650);
    }
    @FXML private void goSuivis() {
        navigateTo("/com/cardiolink/fxml/suivis_patient.fxml", "CardioLink - Mes Suivis", 1100, 650);
    }
    @FXML private void goDossier() {
        navigateTo("/com/cardiolink/fxml/dossier_medical.fxml", "CardioLink - Mon Dossier Médical", 1100, 650);
    }
    @FXML private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/profil_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomField.getScene().getWindow();
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
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateTo(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), w, h);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            if (loader.getController() instanceof UserAwareController) {
                ((UserAwareController) loader.getController()).setCurrentUser(currentUser);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void clearMessages()        { errorLabel.setText(""); successLabel.setText(""); }
    private void showError(String msg)  { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg){ successLabel.setText(msg); errorLabel.setText(""); }
}