package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class AdminEditUserController {

    @FXML private Label            userInfoLabel;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label            errorLabel;
    @FXML private Label            successLabel;

    private User currentAdmin;
    private User targetUser;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(
                "ROLE_PATIENT", "ROLE_MEDECIN", "ROLE_ADMIN"));
    }

    public void setData(User admin, User user) {
        this.currentAdmin = admin;
        this.targetUser   = user;

        // ✅ Afficher les infos de l'utilisateur (lecture seule)
        userInfoLabel.setText(
                user.getPrenom() + " " + user.getNom() +
                        " — " + user.getEmail());

        // ✅ Rôle actuel sélectionné
        roleCombo.setValue(user.getRoleClean());
    }

    @FXML
    private void handleSave() {
        clearMessages();
        String role = roleCombo.getValue();

        if (role == null) {
            showError("Veuillez sélectionner un rôle !"); return;
        }

        // ✅ Modifier seulement le rôle
        targetUser.setRoles(role);

        try {
            userService.updateUserRole(targetUser.getId(), role);
            showSuccess("✅ Rôle modifié avec succès !");

            // ✅ Retour automatique après 1.5 secondes
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(
                            javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> goBack());
            pause.play();

        } catch (SQLException e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/cardiolink/fxml/dashboard_admin.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) roleCombo.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            AdminDashboardController ctrl = loader.getController();
            ctrl.initAdmin(currentAdmin, "users");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void clearMessages()
    { errorLabel.setText(""); successLabel.setText(""); }
    private void showError(String msg)
    { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg)
    { successLabel.setText(msg); errorLabel.setText(""); }
}