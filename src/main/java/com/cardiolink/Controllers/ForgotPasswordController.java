package com.cardiolink.Controllers;

import com.cardiolink.Services.PasswordResetService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class ForgotPasswordController {

    // ── Étape 1 : saisir email ───────────────────────────────
    @FXML private TextField emailField;
    @FXML private Button    btnSendCode;

    // ── Étape 2 : saisir code + nouveau mdp ─────────────────
    @FXML private TextField    codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        btnResetPassword;

    // ── Panels ───────────────────────────────────────────────
    @FXML private javafx.scene.layout.VBox step1Panel;
    @FXML private javafx.scene.layout.VBox step2Panel;

    // ── Messages ─────────────────────────────────────────────
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final PasswordResetService resetService = new PasswordResetService();
    private String currentEmail = "";

    @FXML
    public void initialize() {
        step1Panel.setVisible(true);
        step1Panel.setManaged(true);
        step2Panel.setVisible(false);
        step2Panel.setManaged(false);
    }

    // ── Étape 1 : envoyer le code ────────────────────────────
    @FXML
    private void handleSendCode() {
        clearMessages();
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("L'email est obligatoire !"); return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Format d'email invalide !"); return;
        }

        btnSendCode.setDisable(true);
        btnSendCode.setText("Envoi en cours...");

        new Thread(() -> {
            try {
                resetService.generateAndSendResetToken(email);
                currentEmail = email;
                javafx.application.Platform.runLater(() -> {
                    showSuccess("✅ Code envoyé à " + email + " !");
                    step1Panel.setVisible(false);
                    step1Panel.setManaged(false);
                    step2Panel.setVisible(true);
                    step2Panel.setManaged(true);
                    btnSendCode.setDisable(false);
                    btnSendCode.setText("Envoyer le code");
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    if ("EMAIL_NOT_FOUND".equals(e.getMessage()))
                        showError("Aucun compte vérifié avec cet email !");
                    else if ("EMAIL_SEND_FAILED".equals(e.getMessage()))
                        showError("Erreur lors de l'envoi de l'email !");
                    else
                        showError("Erreur : " + e.getMessage());
                    btnSendCode.setDisable(false);
                    btnSendCode.setText("Envoyer le code");
                });
            }
        }).start();
    }

    // ── Étape 2 : réinitialiser le mot de passe ──────────────
    @FXML
    private void handleResetPassword() {
        clearMessages();
        String code           = codeField.getText().trim();
        String newPassword    = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (code.isEmpty()) {
            showError("Entrez le code reçu par email !"); return;
        }
        if (code.length() != 6) {
            showError("Le code doit contenir 6 chiffres !"); return;
        }
        if (newPassword.isEmpty()) {
            showError("Le nouveau mot de passe est obligatoire !"); return;
        }
        if (newPassword.length() < 4) {
            showError("Mot de passe minimum 4 caractères !"); return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas !"); return;
        }

        btnResetPassword.setDisable(true);
        btnResetPassword.setText("Réinitialisation...");

        new Thread(() -> {
            try {
                resetService.resetPassword(currentEmail, code, newPassword);
                javafx.application.Platform.runLater(() -> {
                    showSuccess("✅ Mot de passe réinitialisé avec succès !");
                    // Attendre 2 secondes puis retourner au login
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(
                                    javafx.util.Duration.seconds(2));
                    pause.setOnFinished(e -> goToLogin());
                    pause.play();
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    if ("INVALID_TOKEN".equals(e.getMessage()))
                        showError("Code invalide ou expiré !");
                    else
                        showError("Erreur : " + e.getMessage());
                    btnResetPassword.setDisable(false);
                    btnResetPassword.setText("Réinitialiser");
                });
            }
        }).start();
    }

    // ── Retour au login ──────────────────────────────────────
    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void clearMessages()         { errorLabel.setText(""); successLabel.setText(""); }
    private void showError(String msg)   { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg) { successLabel.setText(msg); errorLabel.setText(""); }
}