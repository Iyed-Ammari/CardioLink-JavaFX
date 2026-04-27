package com.cardiolink.Controllers;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.Models.User;
import com.cardiolink.Services.DossierMedicalService;
import com.cardiolink.utils.ManagerSession;
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

    @FXML private Label  avatarLabel;
    @FXML private Circle avatarCircle;

    @FXML private Label vGroupeSanguin;
    @FXML private Label vAntecedents;
    @FXML private Label vAllergies;
    @FXML private Label vPoids;
    @FXML private Label vTaille;
    @FXML private Label vTension;
    @FXML private Label vFreq;

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

    @FXML private VBox viewMode;
    @FXML private VBox editMode;

    private DossierMedical dossier;
    private final DossierMedicalService dossierService = new DossierMedicalService();

    @FXML
    public void initialize() {
        eGroupeSanguin.setItems(FXCollections.observableArrayList(
                "A+","A-","B+","B-","AB+","AB-","O+","O-"));
    }

    // ✅ Récupère l'user depuis ManagerSession
    public void init() {
        setCurrentUser(ManagerSession.getInstance().getCurrentUser());
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        String initial = user.getNom() != null && !user.getNom().isEmpty()
                ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
        avatarLabel.setText(initial);
        try {
            dossier = dossierService.getByUserId(user.getId());
            if (dossier == null) {
                dossier = new DossierMedical();
                dossier.setUserId(user.getId());
                dossier.setGroupeSanguin("A+");
                dossierService.save(dossier);
                dossier = dossierService.getByUserId(user.getId());
            }
            fillViewMode();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void fillViewMode() {
        if (dossier == null) return;
        vGroupeSanguin.setText(nvl(dossier.getGroupeSanguin()));
        vAntecedents.setText(nvl(dossier.getAntecedents()));
        vAllergies.setText(nvl(dossier.getAllergies()));
        vPoids.setText(dossier.getPoids()   != null ? dossier.getPoids()   + " kg" : "-");
        vTaille.setText(dossier.getTaille() != null ? dossier.getTaille()  + " cm" : "-");
        vTension.setText(
                (dossier.getTensionSystolique()  != null ? dossier.getTensionSystolique()  + "" : "-") +
                        "/" +
                        (dossier.getTensionDiastolique() != null ? dossier.getTensionDiastolique() + " mmHg" : "-"));
        vFreq.setText(dossier.getFrequenceCardiaque() != null
                ? dossier.getFrequenceCardiaque() + " bpm" : "-");
    }

    private void fillEditMode() {
        if (dossier == null) return;
        eGroupeSanguin.setValue(dossier.getGroupeSanguin() != null ? dossier.getGroupeSanguin() : "A+");
        eAntecedents.setText(dossier.getAntecedents() != null ? dossier.getAntecedents() : "");
        eAllergies.setText(dossier.getAllergies()     != null ? dossier.getAllergies()     : "");
        ePoids.setText(dossier.getPoids()               != null ? String.valueOf(dossier.getPoids())               : "");
        eTaille.setText(dossier.getTaille()             != null ? String.valueOf(dossier.getTaille())             : "");
        eTenSys.setText(dossier.getTensionSystolique()  != null ? String.valueOf(dossier.getTensionSystolique())  : "");
        eTenDia.setText(dossier.getTensionDiastolique() != null ? String.valueOf(dossier.getTensionDiastolique()) : "");
        eFreq.setText(dossier.getFrequenceCardiaque()   != null ? String.valueOf(dossier.getFrequenceCardiaque())   : "");
    }

    @FXML private void showEditMode() {
        fillEditMode();
        viewMode.setVisible(false); viewMode.setManaged(false);
        editMode.setVisible(true);  editMode.setManaged(true);
        errorLabel.setText(""); successLabel.setText("");
    }

    @FXML private void showViewMode() {
        viewMode.setVisible(true);  viewMode.setManaged(true);
        editMode.setVisible(false); editMode.setManaged(false);
    }

    @FXML private void handleSave() {
        errorLabel.setText(""); successLabel.setText("");
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

    @FXML private void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Dashboard");
            stage.setScene(scene);
            stage.show();
            PatientDashboardController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profil_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Profil");
            stage.setScene(scene);
            stage.show();
            ProfilPatientController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goCommunity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_view.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Community");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goSuivis() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterSuivi.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mes Suivis");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goDossier() { }

    @FXML private void handleLogout() {
        ManagerSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Double parseDouble(String s) {
        try { return s == null || s.trim().isEmpty() ? null : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }
    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }
}