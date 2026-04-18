package com.cardiolink.controllers;

import com.cardiolink.Services.EmailVerificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class VerifyEmailController {

    @FXML private TextField tokenField;
    @FXML private Label     emailLabel;
    @FXML private Label     errorLabel;
    @FXML private Label     successLabel;

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

    @FXML
    private void handleVerify() {
        String token = tokenField.getText().trim();

        if (token.isEmpty()) {
            errorLabel.setText("⚠ Entrez le code reçu par email !");
            return;
        }

        try {
            boolean ok = verifyService.verifyToken(token);
            if (ok) {
                successLabel.setText("✅ Compte vérifié ! Redirection...");
                errorLabel.setText("");

                // ✅ Aller vers login après 1.5 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(this::goToLogin);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                errorLabel.setText("⚠ Code invalide !");
                successLabel.setText("");
            }
        } catch (Exception e) {
            errorLabel.setText("⚠ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) tokenField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}