package com.cardiolink.controllers;

import com.cardiolink.Services.PasswordResetService;
import com.cardiolink.Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ResetPasswordController {

    @FXML private TextField     codeField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;

    private final PasswordResetService resetService = new PasswordResetService();
    private final UserService          userService  = new UserService();

    @FXML
    private void handleReset() {
        String code     = codeField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm  = confirmField.getText().trim();

        errorLabel.setText("");
        successLabel.setText("");

        // ✅ Validations
        if (code.isEmpty()) {
            errorLabel.setText("⚠ Entrez le code reçu !"); return;
        }
        if (code.length() != 8) {
            errorLabel.setText("⚠ Le code doit contenir 8 caractères !"); return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("⚠ Le mot de passe est obligatoire !"); return;
        }
        if (password.length() < 4) {
            errorLabel.setText("⚠ Minimum 4 caractères !"); return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("⚠ Les mots de passe ne correspondent pas !"); return;
        }

        try {
            int userId = resetService.validateTokenByCode(code);

            switch (userId) {
                case -1 -> { errorLabel.setText("⚠ Code déjà utilisé !"); return; }
                case -2 -> { errorLabel.setText("⚠ Code expiré !"); return; }
                case -3 -> { errorLabel.setText("⚠ Code invalide !"); return; }
            }

            // ✅ Mettre à jour le mot de passe
            userService.updatePassword(userId, password);
            resetService.markTokenUsedByCode(code);

            successLabel.setText("✅ Mot de passe réinitialisé ! Redirection...");

            // ✅ Retour au login après 2 secondes
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(
                            javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> goToLogin(null));
            pause.play();

        } catch (Exception e) {
            errorLabel.setText("⚠ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}