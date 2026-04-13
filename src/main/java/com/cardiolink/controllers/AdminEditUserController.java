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

    @FXML private TextField        nomField;
    @FXML private TextField        prenomField;
    @FXML private TextField        telField;
    @FXML private TextField        adresseField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField    passwordField;
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
        nomField.setText(user.getNom()         != null ? user.getNom()     : "");
        prenomField.setText(user.getPrenom()   != null ? user.getPrenom()  : "");
        telField.setText(user.getTel()         != null ? user.getTel()     : "");
        adresseField.setText(user.getAdresse() != null ? user.getAdresse() : "");
        roleCombo.setValue(user.getRoleClean());
    }

    @FXML
    private void handleSave() {
        clearMessages();
        String nom      = nomField.getText().trim();
        String prenom   = prenomField.getText().trim();
        String tel      = telField.getText().trim();
        String adresse  = adresseField.getText().trim();
        String role     = roleCombo.getValue();
        String password = passwordField.getText().trim();

        if (nom.isEmpty())    { showError("Le nom est obligatoire !"); return; }
        if (prenom.isEmpty()) { showError("Le prénom est obligatoire !"); return; }
        if (!password.isEmpty() && password.length() < 4)
        { showError("Mot de passe minimum 4 caractères !"); return; }

        targetUser.setNom(nom);
        targetUser.setPrenom(prenom);
        targetUser.setTel(tel);
        targetUser.setAdresse(adresse);
        targetUser.setRoles(role);
        if (!password.isEmpty()) targetUser.setPassword(password);

        try {
            userService.updateUser(targetUser);
            showSuccess("✅ Utilisateur modifié avec succès !");
        } catch (SQLException e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/dashboard_admin.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            AdminDashboardController ctrl = loader.getController();
            // ✅ initAdmin avec section "users"
            ctrl.initAdmin(currentAdmin, "users");
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void clearMessages()        { errorLabel.setText(""); successLabel.setText(""); }
    private void showError(String msg)  { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg){ successLabel.setText(msg); errorLabel.setText(""); }
}