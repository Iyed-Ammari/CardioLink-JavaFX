package com.cardiolink.Controllers;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.Models.User;
import com.cardiolink.Services.DossierMedicalService;
import com.cardiolink.Services.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class AdminAddUserController {

    @FXML private TextField        nomField;
    @FXML private TextField        prenomField;
    @FXML private TextField        emailField;
    @FXML private TextField        telField;
    @FXML private TextField        adresseField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField    passwordField;
    @FXML private VBox             dossierSection;
    @FXML private ComboBox<String> groupeSanguinCombo;
    @FXML private TextArea         antecedentsArea;
    @FXML private TextArea         allergiesArea;
    @FXML private TextField        poidsField;
    @FXML private TextField        tailleField;
    @FXML private TextField        tenSysField;
    @FXML private TextField        tenDiaField;
    @FXML private TextField        freqField;
    @FXML private Label            errorLabel;
    @FXML private Label            successLabel;

    private User currentAdmin;
    private final UserService           userService    = new UserService();
    private final DossierMedicalService dossierService = new DossierMedicalService();

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(
                "ROLE_PATIENT", "ROLE_MEDECIN", "ROLE_ADMIN"));
        roleCombo.setValue("ROLE_PATIENT");

        groupeSanguinCombo.setItems(FXCollections.observableArrayList(
                "A+","A-","B+","B-","AB+","AB-","O+","O-"));
        groupeSanguinCombo.setValue("A+");

        roleCombo.valueProperty().addListener((obs, old, newVal) -> {
            boolean isPatient = "ROLE_PATIENT".equals(newVal);
            dossierSection.setVisible(isPatient);
            dossierSection.setManaged(isPatient);
        });

        dossierSection.setVisible(true);
        dossierSection.setManaged(true);
    }

    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
    }

    @FXML
    private void handleCreate() {
        clearMessages();
        String nom      = nomField.getText().trim();
        String prenom   = prenomField.getText().trim();
        String email    = emailField.getText().trim();
        String tel      = telField.getText().trim();
        String adresse  = adresseField.getText().trim();
        String role     = roleCombo.getValue();
        String password = passwordField.getText().trim();

        if (nom.isEmpty()) { showError("Le nom est obligatoire !"); return; }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s-]+")) { showError("Le nom ne doit contenir que des lettres !"); return; }
        if (prenom.isEmpty()) { showError("Le prénom est obligatoire !"); return; }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s-]+")) { showError("Le prénom ne doit contenir que des lettres !"); return; }
        if (email.isEmpty() || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) { showError("Email invalide !"); return; }
        if (password.length() < 4) { showError("Mot de passe minimum 4 caractères !"); return; }

        if ("ROLE_PATIENT".equals(role)) {
            if (poidsField.getText().trim().isEmpty()) { showError("Le poids est obligatoire !"); return; }
            if (tailleField.getText().trim().isEmpty()) { showError("La taille est obligatoire !"); return; }
            try {
                double poids  = Double.parseDouble(poidsField.getText().trim());
                double taille = Double.parseDouble(tailleField.getText().trim());
                if (poids  <= 0 || poids  > 300) { showError("Poids invalide (1-300 kg) !"); return; }
                if (taille <= 0 || taille > 250) { showError("Taille invalide (1-250 cm) !"); return; }
            } catch (NumberFormatException e) {
                showError("Poids et taille doivent être des nombres !"); return;
            }
        }

        try {
            User user = new User();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setTel(tel);
            user.setAdresse(adresse);
            user.setRoles(role);
            user.setPassword(password);
            user.setActive(true);
            user.setVerified(false);
            userService.addUser(user);
            if ("ROLE_PATIENT".equals(role)) {
                User created = userService.findByEmail(email);
                if (created != null) {
                    try {
                        DossierMedical d = new DossierMedical();
                        d.setUserId(created.getId());
                        d.setGroupeSanguin(groupeSanguinCombo.getValue());
                        d.setAntecedents(antecedentsArea.getText().trim());
                        d.setAllergies(allergiesArea.getText().trim());
                        d.setPoids(Double.parseDouble(poidsField.getText()));
                        d.setTaille(Double.parseDouble(tailleField.getText()));
                        d.setTensionSystolique(Double.parseDouble(tenSysField.getText()));
                        d.setTensionDiastolique(Double.parseDouble(tenDiaField.getText()));
                        d.setFrequenceCardiaque(Double.parseDouble(freqField.getText()));
                        dossierService.save(d);
                    } catch (NumberFormatException e) {
                        // Afficher une alerte à l'utilisateur
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur de saisie");
                        alert.setHeaderText(null);
                        alert.setContentText("Veuillez entrer des nombres valides pour les champs numériques (poids, taille, tensions, fréquence).");
                        alert.showAndWait();
                    }
                }
            }
            showSuccess("✅ Utilisateur créé avec succès !");
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> goBack());
            pause.play();

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate"))
                showError("Cet email est déjà utilisé !");
            else
                showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dashboard_admin.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            AdminDashboardController ctrl = loader.getController();
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CardioLink - Admin Dashboard");
            stage.show();
            // ✅ initAdmin au lieu de setCurrentUser
            if (currentAdmin != null) {
                ctrl.initAdmin(currentAdmin, "users");
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void clearMessages()         { errorLabel.setText("");  successLabel.setText(""); }
    private void showError(String msg)   { errorLabel.setText("⚠ " + msg); successLabel.setText(""); }
    private void showSuccess(String msg) { successLabel.setText(msg); errorLabel.setText(""); }
}