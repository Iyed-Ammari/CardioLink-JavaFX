package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() && password.isEmpty()) {
            showError("Veuillez remplir tous les champs !");
            highlight(emailField, true);
            highlight(passwordField, true);
            return;
        }

        if (email.isEmpty()) {
            showError("L'email est obligatoire !");
            highlight(emailField, true);
            highlight(passwordField, false);
            return;
        }

        if (password.isEmpty()) {
            showError("Le mot de passe est obligatoire !");
            highlight(passwordField, true);
            highlight(emailField, false);
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Format d'email invalide ! Ex: nom@domaine.com");
            highlight(emailField, true);
            return;
        }

        if (password.length() < 4) {
            showError("Le mot de passe doit contenir au moins 4 caractères !");
            highlight(passwordField, true);
            return;
        }

        // ✅ Tout OK → tentative connexion
        try {
            User user = userService.login(email, password);

            if (user != null) {
                highlight(emailField, false);
                highlight(passwordField, false);
                errorLabel.setText("");

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/cardiolink/fxml/dashboard.fxml")
                );
                Scene scene = new Scene(loader.load(), 1100, 650);  // ← IOException possible ici
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setTitle("CardioLink - Dashboard");
                stage.setScene(scene);
                stage.show();

            } else {
                showError("Email ou mot de passe incorrect !");
                highlight(emailField, true);
                highlight(passwordField, true);
                passwordField.clear();
            }

        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données !");
            e.printStackTrace();
        } catch (IOException e) {  // ✅ Fix : ajout du catch IOException
            showError("Erreur lors du chargement du dashboard !");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/register.fxml")
            );
            Scene scene = new Scene(loader.load(), 900, 650);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Register");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void highlight(Control field, boolean error) {
        if (error) {
            field.setStyle(field.getStyle()
                    .replace("-fx-border-color: #7F77DD;", "")
                    .replace("-fx-border-color: #dedede;", "")
                    + " -fx-border-color: #E24B4A; -fx-border-width: 1.5;"
            );
        } else {
            if (field == emailField) {
                field.setStyle(
                        "-fx-background-color: #f0f0fb;" +
                                "-fx-border-color: #7F77DD; -fx-border-width: 1.5;" +
                                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                                "-fx-padding: 10 14; -fx-font-size: 13px;"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: #f5f5fb;" +
                                "-fx-border-color: #dedede; -fx-border-width: 1.5;" +
                                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                                "-fx-padding: 10 14; -fx-font-size: 13px;"
                );
            }
        }
    }

    private void showError(String msg) {
        errorLabel.setText("⚠ " + msg);
        errorLabel.setStyle("-fx-text-fill: #E24B4A; -fx-font-size: 12px;");
    }
}