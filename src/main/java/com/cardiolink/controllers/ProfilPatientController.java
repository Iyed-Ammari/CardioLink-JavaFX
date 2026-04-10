package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class ProfilPatientController implements UserAwareController {

    @FXML private Label  nomLabel;
    @FXML private Label  prenomLabel;
    @FXML private Label  emailLabel;
    @FXML private Label  telLabel;
    @FXML private Label  adresseLabel;
    @FXML private Label  avatarLabel;
    @FXML private Label  bigAvatarLabel;
    @FXML private Circle avatarCircle;
    @FXML private Circle bigAvatarCircle;

    private User currentUser;
    private final UserService userService = new UserService();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            nomLabel.setText(user.getNom()         != null ? user.getNom()     : "-");
            prenomLabel.setText(user.getPrenom()   != null ? user.getPrenom()  : "-");
            emailLabel.setText(user.getEmail()     != null ? user.getEmail()   : "-");
            telLabel.setText(user.getTel() != null && !user.getTel().isEmpty() ? user.getTel() : "-");
            adresseLabel.setText(user.getAdresse() != null && !user.getAdresse().isEmpty() ? user.getAdresse() : "-");
            String initial = user.getNom() != null && !user.getNom().isEmpty()
                    ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
            avatarLabel.setText(initial);
            bigAvatarLabel.setText(initial);
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
    @FXML private void goProfil() { /* déjà ici */ }

    @FXML private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goModifierProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/modifier_profil.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Modifier mon profil");
            stage.setScene(scene);
            stage.show();
            ModifierProfilController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le compte");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        alert.setContentText("Cette action est irréversible. Toutes vos données seront perdues.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(currentUser.getId());
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/com/cardiolink/fxml/login.fxml"));
                    Scene scene = new Scene(loader.load(), 900, 560);
                    Stage stage = (Stage) nomLabel.getScene().getWindow();
                    stage.setTitle("CardioLink - Login");
                    stage.setScene(scene);
                    stage.show();
                } catch (SQLException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setContentText("Erreur lors de la suppression : " + e.getMessage());
                    error.showAndWait();
                    e.printStackTrace();
                } catch (IOException e) { e.printStackTrace(); }
            }
        });
    }

    private void navigateTo(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), w, h);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            if (loader.getController() instanceof UserAwareController) {
                ((UserAwareController) loader.getController()).setCurrentUser(currentUser);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}