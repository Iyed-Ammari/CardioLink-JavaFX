package com.cardiolink.controllers;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.Models.User;
import com.cardiolink.Services.DossierMedicalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class DossierMedicalPatientController implements UserAwareController {

    // Navbar
    @FXML private Label  avatarLabel;
    @FXML private Circle avatarCircle;

    // View mode labels
    @FXML private Label vGroupeSanguin;
    @FXML private Label vAntecedents;
    @FXML private Label vAllergies;
    @FXML private Label vPoids;
    @FXML private Label vTaille;
    @FXML private Label vTension;
    @FXML private Label vFreq;

    // Edit mode fields
    @FXML private ComboBox<String> eGroupeSanguin;
    @FXML private TextArea         eAntecedents;
    @FXML private TextArea         eAllergies;
    @FXML private TextField        ePoids;
    @FXML private TextField        eTaille;
    @FXML private TextField        eTenSys;
    @FXML private TextField        eTenDia;
    @FXML private TextField        eFreq;
    @FXML private Label            errorLabel;
    @FXML private Label            successLabel;

    // View / Edit toggle
    @FXML private VBox viewMode;
    @FXML private VBox editMode;

    private User          currentUser;
    private DossierMedical dossier;

    private final DossierMedicalService dossierService = new DossierMedicalService();

    @FXML
    public void initialize() {
        eGroupeSanguin.setItems(FXCollections.observableArrayList(
                "A+","A-","B+","B-","AB+","AB-","O+","O-"));
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            String initial = user.getNom() != null && !user.getNom().isEmpty()
                    ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
            avatarLabel.setText(initial);

            // Charger le dossier
            try {
                dossier = dossierService.getByUserId(user.getId());
                if (dossier == null) {
                    // Créer un dossier vide si inexistant
                    dossier = new DossierMedical();
                    dossier.setUserId(user.getId());
                    dossier.setGroupeSanguin("A+");
                    dossierService.save(dossier);
                    dossier = dossierService.getByUserId(user.getId());
                }
                fillViewMode();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ── Remplir la vue consultation ──────────────────────────
    private void fillViewMode() {
        if (dossier == null) return;
        vGroupeSanguin.setText(nvl(dossier.getGroupeSanguin()));
        vAntecedents.setText(nvl(dossier.getAntecedents()));
        vAllergies.setText(nvl(dossier.getAllergies()));
        vPoids.setText(dossier.getPoids()  != null ? dossier.getPoids()  + " kg" : "-");
        vTaille.setText(dossier.getTaille() != null ? dossier.getTaille() + " cm" : "-");
        vTension.setText(
                (dossier.getTensionSystolique()  != null ? dossier.getTensionSystolique()  + "" : "-") +
                        "/" +
                        (dossier.getTensionDiastolique() != null ? dossier.getTensionDiastolique() + " mmHg" : "-"));
        vFreq.setText(dossier.getFrequenceCardiaque() != null
                ? dossier.getFrequenceCardiaque() + " bpm" : "-");
    }

    // ── Remplir les champs d'édition ─────────────────────────
    private void fillEditMode() {
        if (dossier == null) return;
        eGroupeSanguin.setValue(dossier.getGroupeSanguin() != null ? dossier.getGroupeSanguin() : "A+");
        eAntecedents.setText(dossier.getAntecedents() != null ? dossier.getAntecedents() : "");
        eAllergies.setText(dossier.getAllergies()     != null ? dossier.getAllergies()     : "");
        ePoids.setText(dossier.getPoids()              != null ? String.valueOf(dossier.getPoids())              : "");
        eTaille.setText(dossier.getTaille()            != null ? String.valueOf(dossier.getTaille())            : "");
        eTenSys.setText(dossier.getTensionSystolique() != null ? String.valueOf(dossier.getTensionSystolique()) : "");
        eTenDia.setText(dossier.getTensionDiastolique() != null ? String.valueOf(dossier.getTensionDiastolique()) : "");
        eFreq.setText(dossier.getFrequenceCardiaque()  != null ? String.valueOf(dossier.getFrequenceCardiaque())  : "");
    }

    // ── Toggle view/edit ─────────────────────────────────────
    @FXML private void showEditMode() {
        fillEditMode();
        viewMode.setVisible(false);
        viewMode.setManaged(false);
        editMode.setVisible(true);
        editMode.setManaged(true);
        errorLabel.setText("");
        successLabel.setText("");
    }

    @FXML private void showViewMode() {
        viewMode.setVisible(true);
        viewMode.setManaged(true);
        editMode.setVisible(false);
        editMode.setManaged(false);
    }

    // ── Enregistrer ──────────────────────────────────────────
    @FXML private void handleSave() {
        errorLabel.setText("");
        successLabel.setText("");

        dossier.setGroupeSanguin(eGroupeSanguin.getValue());
        dossier.setAntecedents(eAntecedents.getText().trim());
        dossier.setAllergies(eAllergies.getText().trim());
        dossier.setPoids(parseDouble(ePoids.getText()));
        dossier.setTaille(parseDouble(eTaille.getText()));
        dossier.setTensionSystolique(parseDouble(eTenSys.getText()));
        dossier.setTensionDiastolique(parseDouble(eTenDia.getText()));
        dossier.setFrequenceCardiaque(parseDouble(eFreq.getText()));

        try {
            dossierService.save(dossier);
            fillViewMode();
            showViewMode();
            successLabel.setText("✅ Dossier mis à jour avec succès !");
        } catch (SQLException e) {
            errorLabel.setText("⚠ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Navigation ───────────────────────────────────────────
    @FXML private void goHome() {
        navigateTo("/com/cardiolink/fxml/dashboard_patient.fxml", "CardioLink - Dashboard", 1100, 650);
    }
    @FXML private void goCommunity() {
        navigateTo("/com/cardiolink/fxml/community.fxml", "CardioLink - Community", 1100, 650);
    }
    @FXML private void goSuivis() {
        navigateTo("/com/cardiolink/fxml/suivis_patient.fxml", "CardioLink - Mes Suivis", 1100, 650);
    }
    @FXML private void goDossier() { /* déjà ici */ }
    @FXML private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/profil_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Profil");
            stage.setScene(scene);
            stage.show();
            ProfilPatientController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/cardiolink/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateTo(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), w, h);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            if (loader.getController() instanceof UserAwareController) {
                ((UserAwareController) loader.getController()).setCurrentUser(currentUser);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Double parseDouble(String s) {
        try { return s == null || s.trim().isEmpty() ? null : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }
}