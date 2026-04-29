package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import com.cardiolink.Services.CloudinaryService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.io.IOException;
import java.io.File;
import java.sql.SQLException; // <--- NE PAS OUBLIER CET IMPORT

public class ProfilPatientController implements UserAwareController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telLabel;
    @FXML private Label adresseLabel;
    @FXML private Label avatarLabel;
    @FXML private Label bigAvatarLabel;
    @FXML private Circle avatarCircle;
    @FXML private Circle bigAvatarCircle;
    @FXML private Button btnSuivis;
    @FXML private Button btnDossier;

    private final UserService userService = new UserService();
    private User currentUser;

    public void init() {
        setCurrentUser(ManagerSession.getInstance().getCurrentUser());
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        this.currentUser = user;

        nomLabel.setText(user.getNom() != null ? user.getNom() : "-");
        prenomLabel.setText(user.getPrenom() != null ? user.getPrenom() : "-");
        emailLabel.setText(user.getEmail() != null ? user.getEmail() : "-");
        telLabel.setText(user.getTel() != null && !user.getTel().isEmpty() ? user.getTel() : "-");
        adresseLabel.setText(user.getAdresse() != null && !user.getAdresse().isEmpty() ? user.getAdresse() : "-");

        String initial = (user.getNom() != null && !user.getNom().isEmpty())
                ? String.valueOf(user.getNom().charAt(0)).toUpperCase()
                : "?";

        avatarLabel.setText(initial);
        bigAvatarLabel.setText(initial);

        boolean isPatient = "ROLE_PATIENT".equals(user.getRoleClean());
        btnSuivis.setVisible(isPatient);
        btnSuivis.setManaged(isPatient);
        btnDossier.setVisible(isPatient);
        btnDossier.setManaged(isPatient);

        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            updateAvatarsUI(user.getImageUrl());
        }
    }

    private void updateAvatarsUI(String imageUrl) {
        try {
            Image img = new Image(imageUrl, true);
            // On attend que l'image soit chargée pour l'appliquer
            img.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() == 1.0) {
                    Platform.runLater(() -> {
                        ImagePattern pattern = new ImagePattern(img);
                        bigAvatarCircle.setFill(pattern);
                        avatarCircle.setFill(pattern);
                        bigAvatarLabel.setVisible(false);
                        avatarLabel.setVisible(false);
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur image: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(nomLabel.getScene().getWindow());

        if (file != null) {
            new Thread(() -> {
                try {
                    CloudinaryService cloudinary = new CloudinaryService();
                    String imageUrl = cloudinary.uploadImage(file);

                    currentUser.setImageUrl(imageUrl);
                    userService.updateUser(currentUser);

                    Platform.runLater(() -> updateAvatarsUI(imageUrl));
                } catch (Exception e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Erreur upload: " + e.getMessage()).show());
                }
            }).start();
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer définitivement votre compte ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userService.deleteUser(currentUser.getId());
                    handleLogout();
                } catch (SQLException e) { // <--- Ici SQLException est maintenant reconnu
                    new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
                }
            }
        });
    }

    @FXML private void handleLogout() {
        ManagerSession.getInstance().logout();
        switchScene("/login.fxml", "Login", false);
    }

    @FXML private void goHome() { switchScene("/dashboard_patient.fxml", "Home", true); }
    @FXML private void goCommunity() { switchScene("/post_view.fxml", "Community", false); }
    @FXML private void goSuivis() { switchScene("/AjouterSuivi.fxml", "Suivis", false); }
    @FXML private void goDossier() { switchScene("/dossier_medical.fxml", "Dossier", true); }
    @FXML private void goModifierProfil() { switchScene("/modifier_profil.fxml", "Modifier Profil", true); }
    @FXML private void goProfil() {}

    private void switchScene(String fxml, String title, boolean init) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            if(init && loader.getController() instanceof UserAwareController) {
                ((UserAwareController)loader.getController()).setCurrentUser(currentUser);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}