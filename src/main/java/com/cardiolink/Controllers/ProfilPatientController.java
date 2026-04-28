package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class ProfilPatientController implements UserAwareController {

    @FXML private Label  nomLabel;
    @FXML private Label  prenomLabel;
    @FXML private Label  emailLabel;
    @FXML private Label  telLabel;
    @FXML private Label  adresseLabel;
    @FXML private Label  avatarLabel;
    @FXML private Label  bigAvatarLabel;
    @FXML private Circle avatarCircle;
    @FXML private Circle bigAvatarCircle;
    @FXML private Button btnSuivis;
    @FXML private Button btnDossier;

    private final UserService userService = new UserService();

    // ✅ Récupère l'user depuis ManagerSession
    public void init() {
        setCurrentUser(ManagerSession.getInstance().getCurrentUser());
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        nomLabel.setText(user.getNom()         != null ? user.getNom()     : "-");
        prenomLabel.setText(user.getPrenom()   != null ? user.getPrenom()  : "-");
        emailLabel.setText(user.getEmail()     != null ? user.getEmail()   : "-");
        telLabel.setText(user.getTel()         != null && !user.getTel().isEmpty()     ? user.getTel()     : "-");
        adresseLabel.setText(user.getAdresse() != null && !user.getAdresse().isEmpty() ? user.getAdresse() : "-");
        String initial = user.getNom() != null && !user.getNom().isEmpty()
                ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
        avatarLabel.setText(initial);
        bigAvatarLabel.setText(initial);
        boolean isPatient = "ROLE_PATIENT".equals(user.getRoleClean());
        btnSuivis.setVisible(isPatient);
        btnSuivis.setManaged(isPatient);
        btnDossier.setVisible(isPatient);
        btnDossier.setManaged(isPatient);
    }

    @FXML private void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Dashboard");
            stage.setScene(scene);
            stage.show();
            PatientDashboardController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goCommunity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_view.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Community");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goSuivis() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterSuivi.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mes Suivis");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goDossier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_medical.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Mon Dossier Médical");
            stage.setScene(scene);
            stage.show();
            DossierMedicalPatientController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goProfil() { }

    @FXML private void goModifierProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_profil.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Modifier mon profil");
            stage.setScene(scene);
            stage.show();
            ModifierProfilController ctrl = loader.getController();
            ctrl.init();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleDelete() {
        User user = ManagerSession.getInstance().getCurrentUser();
        if (user == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le compte");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        alert.setContentText("Cette action est irréversible.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(user.getId());
                    ManagerSession.getInstance().logout();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                    Scene scene = new Scene(loader.load(), 900, 560);
                    Stage stage = (Stage) nomLabel.getScene().getWindow();
                    stage.setTitle("CardioLink - Login");
                    stage.setScene(scene);
                    stage.show();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
                } catch (IOException e) { e.printStackTrace(); }
            }
        });
    }

    @FXML private void handleLogout() {
        ManagerSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}