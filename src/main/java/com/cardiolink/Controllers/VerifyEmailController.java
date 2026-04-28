package com.cardiolink.Controllers;

import com.cardiolink.Services.EmailVerificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class VerifyEmailController {

    @FXML private TextField codeField;
    @FXML private Label     emailLabel;
    @FXML private Label     errorLabel;
    @FXML private Label     successLabel;
    @FXML private Button    btnVerify;
    @FXML private Button    btnResend;

    private int    userId;
    private String email;
    private String prenom;

    private final EmailVerificationService verifyService =
            new EmailVerificationService();

    public void setUserData(int userId, String email, String prenom) {
        this.userId = userId;
        this.email  = email;
        this.prenom = prenom;
        emailLabel.setText(email);
    }

    // ── Vérifier le code ─────────────────────────────────────
    @FXML
    private void handleVerify() {
        clearMessages();
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            showError("Entrez le code reçu par email !"); return;
        }
        if (code.length() != 6) {
            showError("Le code doit contenir 6 chiffres !"); return;
        }

        btnVerify.setDisable(true);
        btnVerify.setText("Vérification...");

        new Thread(() -> {
            try {
                boolean ok = verifyService.verifyToken(code);
                javafx.application.Platform.runLater(() -> {
                    if (ok) {
                        showSuccess("✅ Compte vérifié ! Redirection...");
                        javafx.animation.PauseTransition pause =
                                new javafx.animation.PauseTransition(
                                        javafx.util.Duration.seconds(2));
                        pause.setOnFinished(e -> goToLogin());
                        pause.play();
                    } else {
                        showError("⚠ Code invalide ou expiré !");
                        btnVerify.setDisable(false);
                        btnVerify.setText("✅  Vérifier mon compte");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur : " + e.getMessage());
                    btnVerify.setDisable(false);
                    btnVerify.setText("✅  Vérifier mon compte");
                });
            }
        }).start();
    }

    // ── Renvoyer le code ─────────────────────────────────────
    @FXML
    private void handleResend() {
        clearMessages();
        btnResend.setDisable(true);
        btnResend.setText("Envoi...");

        new Thread(() -> {
            try {
                String newToken = verifyService.generateVerificationToken(userId);
                verifyService.sendVerificationEmail(email, newToken, prenom);
                javafx.application.Platform.runLater(() -> {
                    showSuccess("✅ Nouveau code envoyé à " + email + " !");
                    codeField.clear();
                    btnResend.setDisable(false);
                    btnResend.setText("🔄  Renvoyer le code");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur lors de l'envoi : " + e.getMessage());
                    btnResend.setDisable(false);
                    btnResend.setText("🔄  Renvoyer le code");
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
            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void clearMessages()         { errorLabel.setText(""); successLabel.setText(""); }
    private void showError(String msg)   { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg) { successLabel.setText(msg); errorLabel.setText(""); }
}