package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.GoogleAuthService;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.sql.SQLException;
import java.util.UUID;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private final UserService       userService  = new UserService();
    private final GoogleAuthService googleAuth   = new GoogleAuthService();

    // ── Login classique ──────────────────────────────────────
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
                ManagerSession.getInstance().setCurrentUser(user);
                navigateToDashboard(user);
            } else {
                showError("Email ou mot de passe incorrect !");
                passwordField.clear();
            }

        } catch (SQLException e) {
            if ("EMAIL_NOT_VERIFIED".equals(e.getMessage())) {
                showError("⚠ Email non vérifié ! Vérifiez votre boîte mail.");
            } else if ("ACCOUNT_BLOCKED".equals(e.getMessage())) {
                showError("🔒 Compte bloqué ! Contactez l'administrateur.");
            } else {
                showError("Erreur de connexion !");
                e.printStackTrace();
            }
        } catch (Exception e) {
            showError("Erreur de connexion !");
            e.printStackTrace();
        }
    }

    // ── Login Google OAuth2 ──────────────────────────────────
    @FXML
    private void handleGoogleLogin() {
        errorLabel.setText("🔄 Connexion Google en cours...");

        new Thread(() -> {
            try {
                // ── Étape 1 : Ouvrir navigateur ──────────────
                String authUrl = googleAuth.getAuthorizationUrl();
                Platform.runLater(() ->
                        errorLabel.setText("🌐 Navigateur ouvert, connectez-vous..."));

                java.awt.Desktop.getDesktop().browse(new java.net.URI(authUrl));

                // ── Étape 2 : Attendre le code automatiquement ──
                String code = googleAuth.waitForAuthCode();

                Platform.runLater(() ->
                        errorLabel.setText("🔄 Récupération du profil Google..."));

                // ── Étape 3 : Obtenir access token ───────────
                String accessToken = googleAuth.exchangeCodeForToken(code);

                // ── Étape 4 : Obtenir infos user ─────────────
                JSONObject userInfo = googleAuth.getUserInfo(accessToken);

                String email   = userInfo.getString("email");
                String prenom  = userInfo.optString("given_name",  "");
                String nom     = userInfo.optString("family_name", "");
                String picture = userInfo.optString("picture",     "");

                // ── Étape 5 : Vérifier ou créer le compte ────
                User user = userService.findByEmail(email);

                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    user.setNom(nom.isEmpty() ? email.split("@")[0] : nom);
                    user.setPrenom(prenom);
                    user.setPassword(java.util.UUID.randomUUID().toString());
                    user.setRoles("ROLE_PATIENT");
                    user.setActive(true);
                    user.setVerified(true);
                    user.setAdresse("");
                    user.setTel("");
                    if (!picture.isEmpty()) user.setImageUrl(picture);
                    userService.addUser(user);
                    user = userService.findByEmail(email);
                }

                final User finalUser = user;
                Platform.runLater(() -> {
                    ManagerSession.getInstance().setCurrentUser(finalUser);
                    navigateToDashboard(finalUser);
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        showError("❌ Erreur Google : " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
    // ── Navigation vers dashboard selon rôle ─────────────────
    private void navigateToDashboard(User user) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();

            if ("ROLE_ADMIN".equals(user.getRoleClean())) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/dashboard_admin.fxml"));
                Scene scene = new Scene(loader.load(), 1100, 650);
                AdminUserDashboardController ctrl = loader.getController();
                stage.setScene(scene);
                stage.setTitle("CardioLink - Admin Dashboard");
                stage.show();
                ctrl.init();
            } else {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/dashboard_patient.fxml"));
                Scene scene = new Scene(loader.load(), 1100, 650);
                PatientDashboardController ctrl = loader.getController();
                stage.setScene(scene);
                stage.setTitle("CardioLink");
                stage.show();
                ctrl.init();
            }
        } catch (Exception e) {
            showError("Erreur de chargement !");
            e.printStackTrace();
        }
    }

    // ── Forgot Password ──────────────────────────────────────
    @FXML
    private void goToForgotPassword(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/forgot_password.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Mot de passe oublié");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Register ─────────────────────────────────────────────
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
    @FXML
    private void handleFaceLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/face_login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Connexion Faciale");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}