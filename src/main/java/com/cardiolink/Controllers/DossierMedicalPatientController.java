package com.cardiolink.Controllers;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.Models.User;
import com.cardiolink.Services.DossierMedicalService;
import com.cardiolink.Services.ImcRisqueService;
import com.cardiolink.Services.PdfService;
import com.cardiolink.utils.ManagerSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class DossierMedicalPatientController implements UserAwareController {

    @FXML private Label  avatarLabel;
    @FXML private Circle avatarCircle;

    // ── Vue consultation ─────────────────────────────────────
    @FXML private Label vGroupeSanguin;
    @FXML private Label vAntecedents;
    @FXML private Label vAllergies;
    @FXML private Label vPoids;
    @FXML private Label vTaille;
    @FXML private Label vTension;
    @FXML private Label vFreq;
    @FXML private Label vImc;
    @FXML private Label vCategorieImc;
    @FXML private Label vRisque;

    // ── Vue édition ──────────────────────────────────────────
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
    private User           currentUser;

    private final DossierMedicalService dossierService = new DossierMedicalService();
    private final ImcRisqueService      imcService     = new ImcRisqueService();
    private final PdfService            pdfService     = new PdfService();

    @FXML
    public void initialize() {
        eGroupeSanguin.setItems(FXCollections.observableArrayList(
                "A+","A-","B+","B-","AB+","AB-","O+","O-"));
    }

    public void init() {
        setCurrentUser(ManagerSession.getInstance().getCurrentUser());
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        this.currentUser = user;
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

    // ── Remplir la vue consultation ──────────────────────────
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
        vFreq.setText(dossier.getFrequenceCardiaque() != null ?
                dossier.getFrequenceCardiaque() + " bpm" : "-");

        // ── IMC + Risque ─────────────────────────────────────
        if (dossier.getPoids() != null && dossier.getTaille() != null
                && dossier.getTaille() > 0) {
            double imc = imcService.calculerImc(dossier.getPoids(), dossier.getTaille());
            vImc.setText(imcService.imcFormate(imc));
            vCategorieImc.setText(imcService.categorieImc(imc));
        } else {
            vImc.setText("-");
            vCategorieImc.setText("Données insuffisantes");
        }

        String risque  = imcService.risqueCardiaque(dossier);
        String couleur = imcService.couleurRisque(dossier);
        vRisque.setText(risque);
        vRisque.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");
    }

    // ── Remplir la vue édition ───────────────────────────────
    private void fillEditMode() {
        if (dossier == null) return;
        eGroupeSanguin.setValue(dossier.getGroupeSanguin() != null ? dossier.getGroupeSanguin() : "A+");
        eAntecedents.setText(dossier.getAntecedents() != null ? dossier.getAntecedents() : "");
        eAllergies.setText(dossier.getAllergies()     != null ? dossier.getAllergies()     : "");
        ePoids.setText(dossier.getPoids()              != null ? String.valueOf(dossier.getPoids())              : "");
        eTaille.setText(dossier.getTaille()            != null ? String.valueOf(dossier.getTaille())            : "");
        eTenSys.setText(dossier.getTensionSystolique() != null ? String.valueOf(dossier.getTensionSystolique()) : "");
        eTenDia.setText(dossier.getTensionDiastolique()!= null ? String.valueOf(dossier.getTensionDiastolique()): "");
        eFreq.setText(dossier.getFrequenceCardiaque()  != null ? String.valueOf(dossier.getFrequenceCardiaque()): "");
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
            errorLabel.setText("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Télécharger PDF ──────────────────────────────────────
    @FXML private void handleDownloadPdf() {
        if (currentUser == null || dossier == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le dossier médical");
        chooser.setInitialFileName("dossier_medical_" +
                currentUser.getNom() + "_" + currentUser.getPrenom() + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File file = chooser.showSaveDialog(avatarLabel.getScene().getWindow());

        if (file != null) {
            try {
                pdfService.generateDossierPdf(currentUser, dossier, file.getAbsolutePath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF généré !");
                alert.setHeaderText("Dossier médical téléchargé !");
                alert.setContentText("Fichier : " + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur PDF : " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    // ── Navigation ───────────────────────────────────────────
    @FXML private void goHome() {
        navigateTo("/dashboard_patient.fxml", "CardioLink - Dashboard");
    }
    @FXML private void goProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/profil_patient.fxml"));
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
        navigateTo("/post_view.fxml", "CardioLink - Community");
    }
    @FXML private void goSuivis() {
        navigateTo("/AjouterSuivi.fxml", "CardioLink - Mes Suivis");
    }
    @FXML private void goDossier() { }

    @FXML private void handleLogout() {
        ManagerSession.getInstance().logout();
        navigateTo("/login.fxml", "CardioLink - Login");
    }

    private void navigateTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) avatarLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            if (loader.getController() instanceof UserAwareController ctrl) {
                ctrl.setCurrentUser(currentUser);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Double parseDouble(String s) {
        try { return s == null || s.trim().isEmpty() ? null : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }
}