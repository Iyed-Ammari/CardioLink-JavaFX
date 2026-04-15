package com.cardiolink.controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField        emailField;
    @FXML private PasswordField    passwordField;
    @FXML private TextField        nomField;
    @FXML private TextField        prenomField;
    @FXML private TextField        phoneField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextArea         allergiesField;
    @FXML private TextField        adresseField;
    @FXML private CheckBox         termsCheck;
    @FXML private Label            errorLabel;
    @FXML private Label            successLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(
                "ROLE_PATIENT",
                "ROLE_MEDECIN",
                "ROLE_ADMIN"
        ));
        roleCombo.setValue("ROLE_PATIENT");
    }

    @FXML
    private void handleRegister() {
        clearErrors();

        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String nom      = nomField.getText().trim();
        String prenom   = prenomField.getText().trim();
        String phone    = phoneField.getText().trim();
        String role     = roleCombo.getValue();
        String allergies = allergiesField != null ? allergiesField.getText().trim() : "";
        String adresse   = adresseField   != null ? adresseField.getText().trim()   : "";

        boolean hasError = false;

        if (email.isEmpty()) {
            setError(emailField, "L'email est obligatoire !");
            hasError = true;
        } else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            setError(emailField, "Format d'email invalide ! Ex: nom@domaine.com");
            hasError = true;
        }

        if (password.isEmpty()) {
            setError(passwordField, "Le mot de passe est obligatoire !");
            hasError = true;
        } else if (password.length() < 4) {
            setError(passwordField, "Minimum 4 caractères !");
            hasError = true;
        }

        if (nom.isEmpty()) {
            setError(nomField, "Le nom est obligatoire !");
            hasError = true;
        } else if (!nom.matches("[a-zA-ZÀ-ÿ\\s-]+")) {
            setError(nomField, "Le nom ne doit contenir que des lettres !");
            hasError = true;
        }

        if (prenom.isEmpty()) {
            setError(prenomField, "Le prénom est obligatoire !");
            hasError = true;
        } else if (!prenom.matches("[a-zA-ZÀ-ÿ\\s-]+")) {
            setError(prenomField, "Le prénom ne doit contenir que des lettres !");
            hasError = true;
        }

        if (!phone.isEmpty() && !phone.matches("^[\\+]?[0-9\\s-]{8,15}$")) {
            setError(phoneField, "Numéro de téléphone invalide !");
            hasError = true;
        }

        if (role == null || role.isEmpty()) {
            showError("Veuillez sélectionner un rôle !");
            hasError = true;
        }

        if (!termsCheck.isSelected()) {
            showError("Vous devez accepter les Terms and Conditions !");
            hasError = true;
        }

        if (hasError) return;

        // ✅ Tout OK → enregistrement
        try {
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setRoles(role);
            user.setActive(true);
            user.setTel(phone);
            user.setAdresse(adresse);
            user.setVerified(true);

            userService.addUser(user);

            showSuccess("✅ Compte créé avec succès ! Vous pouvez vous connecter.");
            clearFields();

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                setError(emailField, "Cet email est déjà utilisé !");
            } else {
                showError("Erreur serveur : " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login.fxml")
            );
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setError(Control field, String message) {
        field.setStyle(
                "-fx-border-color: #E24B4A; -fx-border-width: 1.5;" +
                        "-fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-padding: 10 14; -fx-font-size: 13px;"
        );
        showError(message);
    }

    private void clearErrors() {
        String normal =
                "-fx-background-color: #f0f0fb;" +
                        "-fx-border-color: #dedede; -fx-border-width: 1.5;" +
                        "-fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-padding: 10 14; -fx-font-size: 13px;";
        emailField.setStyle(normal);
        passwordField.setStyle(normal);
        nomField.setStyle(normal);
        prenomField.setStyle(normal);
        phoneField.setStyle(normal);
        errorLabel.setText("");
        successLabel.setText("");
    }

    private void showError(String msg)   { errorLabel.setText("⚠ " + msg);  successLabel.setText(""); }
    private void showSuccess(String msg) { successLabel.setText(msg);         errorLabel.setText(""); }

    private void clearFields() {
        emailField.clear();
        passwordField.clear();
        nomField.clear();
        prenomField.clear();
        phoneField.clear();
        if (allergiesField != null) allergiesField.clear();
        if (adresseField   != null) adresseField.clear();
        termsCheck.setSelected(false);
        roleCombo.setValue("ROLE_PATIENT");
    }
}