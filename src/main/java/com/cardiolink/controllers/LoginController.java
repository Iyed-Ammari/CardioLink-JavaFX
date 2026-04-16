package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private final UserService userService = new UserService();
    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs !"); return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Format d'email invalide !"); return;
        }
        if (password.length() < 4) {
            showError("Mot de passe minimum 4 caractères !"); return;
        }

        try {
            User user = userService.login(email, password);

            if (user != null) {
                Stage stage = (Stage) emailField.getScene().getWindow();
                String role = user.getRoleClean();

                if ("ROLE_ADMIN".equals(role)) {
                    // ✅ Admin → directement dashboard_admin
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/dashboard_admin.fxml"));
                    Scene scene = new Scene(loader.load(), 1100, 650);
                    AdminDashboardController ctrl = loader.getController();
                    stage.setScene(scene);
                    stage.setTitle("CardioLink - Admin Dashboard");
                    stage.show();
                    ctrl.setCurrentUser(user);

                } else {
                    // ✅ Patient et Médecin → Welcome page
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/dashboard_patient.fxml"));
                    Scene scene = new Scene(loader.load(), 1100, 650);
                    PatientDashboardController ctrl = loader.getController();
                    stage.setScene(scene);
                    stage.setTitle("CardioLink");
                    stage.show();
                    ctrl.setCurrentUser(user);
                }

            } else {
                showError("Email ou mot de passe incorrect !");
                passwordField.clear();
            }

        } catch (Exception e) {
            showError("Erreur de connexion !");
            e.printStackTrace();
        }
    }
    @FXML
    private void goToRegister(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/register.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Register");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        errorLabel.setText("⚠ " + msg);
    }
}