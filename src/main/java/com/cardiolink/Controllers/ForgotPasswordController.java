package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.PasswordResetService;
import com.cardiolink.Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label     errorLabel;
    @FXML private Label     successLabel;

    private final UserService          userService  = new UserService();
    private final PasswordResetService resetService = new PasswordResetService();

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        errorLabel.setText("");
        successLabel.setText("");

        // ✅ Validations
        if (email.isEmpty()) {
            errorLabel.setText("⚠ Veuillez entrer votre email !"); return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("⚠ Format d'email invalide !"); return;
        }

        try {
            User user = userService.findByEmail(email);

            // ✅ Sécurité : ne pas révéler si l'email existe
            if (user == null) {
                successLabel.setText(
                        "✅ Si cet email existe, un code a été envoyé !");
                return;
            }

            // ✅ Générer et envoyer le code
            String token = resetService.generateToken(user.getId());
            resetService.sendResetEmail(email, token);

            successLabel.setText(
                    "✅ Code envoyé à " + email + " !\n" +
                            "Vérifiez votre boîte mail.");

            // ✅ Aller vers reset_password après 2 secondes
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(
                            javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> goToReset());
            pause.play();

        } catch (Exception e) {
            errorLabel.setText("⚠ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void goToReset() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/cardiolink/reset_password.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Réinitialisation");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToLogin(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/cardiolink/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}