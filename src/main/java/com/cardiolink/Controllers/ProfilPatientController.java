package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.CloudinaryService;
import com.cardiolink.Services.UserService;
import com.cardiolink.Services.WebcamService;
import com.cardiolink.utils.ManagerSession;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import java.io.File;
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
    @FXML private Button btnFaceStatus;
    @FXML private Label  faceStatusLabel;

    private final UserService userService = new UserService();
    private User currentUser;

    public void init() {
        setCurrentUser(ManagerSession.getInstance().getCurrentUser());
    }

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        this.currentUser = user;

        nomLabel.setText(user.getNom()     != null ? user.getNom()     : "-");
        prenomLabel.setText(user.getPrenom()!= null ? user.getPrenom() : "-");
        emailLabel.setText(user.getEmail() != null ? user.getEmail()   : "-");
        telLabel.setText(user.getTel()     != null && !user.getTel().isEmpty()
                ? user.getTel() : "-");
        adresseLabel.setText(user.getAdresse() != null && !user.getAdresse().isEmpty()
                ? user.getAdresse() : "-");

        String initial = user.getNom() != null && !user.getNom().isEmpty()
                ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
        avatarLabel.setText(initial);
        bigAvatarLabel.setText(initial);

        boolean isPatient = "ROLE_PATIENT".equals(user.getRoleClean());
        btnSuivis.setVisible(isPatient);
        btnSuivis.setManaged(isPatient);
        btnDossier.setVisible(isPatient);
        btnDossier.setManaged(isPatient);

        // ── Photo de profil ──────────────────────────────────
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            updateAvatarsUI(user.getImageUrl());
        }

        // ── Statut reconnaissance faciale ────────────────────
        checkFaceStatus();
    }

    // ── Vérifier si visage enregistré ───────────────────────
    private void checkFaceStatus() {
        new Thread(() -> {
            try {
                String faceImage = userService.getFaceImage(currentUser.getEmail());
                Platform.runLater(() -> {
                    if (faceImage != null && !faceImage.isEmpty()) {
                        faceStatusLabel.setText("✅ Reconnaissance faciale activée");
                        faceStatusLabel.setStyle(
                                "-fx-font-size: 12px; -fx-text-fill: #27ae60;");
                        btnFaceStatus.setText("🔄  Mettre à jour mon visage");
                        btnFaceStatus.setStyle(
                                "-fx-background-color: #e8f5e9; -fx-text-fill: #27ae60;" +
                                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                                        "-fx-padding: 8 20; -fx-background-radius: 20;" +
                                        "-fx-cursor: hand; -fx-border-color: #27ae60;" +
                                        "-fx-border-width: 1.5; -fx-border-radius: 20;");
                    } else {
                        faceStatusLabel.setText("⚠ Reconnaissance faciale non activée");
                        faceStatusLabel.setStyle(
                                "-fx-font-size: 12px; -fx-text-fill: #e67e22;");
                        btnFaceStatus.setText("👤  Activer la reconnaissance faciale");
                        btnFaceStatus.setStyle(
                                "-fx-background-color: #f0f0fb; -fx-text-fill: #7F77DD;" +
                                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                                        "-fx-padding: 8 20; -fx-background-radius: 20;" +
                                        "-fx-cursor: hand; -fx-border-color: #7F77DD;" +
                                        "-fx-border-width: 1.5; -fx-border-radius: 20;");
                    }
                });
            } catch (SQLException e) { e.printStackTrace(); }
        }).start();
    }

    // ── Enregistrer le visage depuis le profil ───────────────
    @FXML
    private void handleRegisterFace() {
        // Afficher dialog avec webcam inline
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reconnaissance Faciale");
        dialog.setHeaderText("Positionnez votre visage face à la caméra");

        // Contenu du dialog
        javafx.scene.layout.VBox content =
                new javafx.scene.layout.VBox(12);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(20));

        javafx.scene.image.ImageView webcamView =
                new javafx.scene.image.ImageView();
        webcamView.setFitWidth(320);
        webcamView.setFitHeight(240);
        webcamView.setPreserveRatio(true);
        webcamView.setStyle("-fx-background-color: #1a1a2e;");

        Label statusLbl = new Label("📷 Démarrage de la caméra...");
        statusLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F77DD;");

        content.getChildren().addAll(webcamView, statusLbl);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(400);

        ButtonType captureBtn = new ButtonType(
                "📸 Capturer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn  = new ButtonType(
                "Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(captureBtn, cancelBtn);

        // Démarrer webcam
        WebcamService webcamService = new WebcamService();
        AnimationTimer[] timerHolder = {null};

        new Thread(() -> {
            boolean opened = webcamService.openCamera();
            Platform.runLater(() -> {
                if (opened) {
                    statusLbl.setText("✅ Caméra prête — positionnez votre visage");
                    timerHolder[0] = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            Mat frame = webcamService.getCurrentFrame();
                            if (frame != null) {
                                webcamView.setImage(
                                        webcamService.matToFxImage(frame));
                            }
                        }
                    };
                    timerHolder[0].start();
                } else {
                    statusLbl.setText("❌ Webcam non détectée !");
                    dialog.getDialogPane()
                            .lookupButton(captureBtn).setDisable(true);
                }
            });
        }).start();

        // Résultat dialog
        dialog.showAndWait().ifPresent(result -> {
            // Arrêter preview
            if (timerHolder[0] != null) timerHolder[0].stop();

            if (result == captureBtn) {
                statusLbl.setText("📸 Capture en cours...");

                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                        String base64 = webcamService.captureAsBase64();
                        userService.saveFaceImage(currentUser.getId(), base64);
                        webcamService.closeCamera();

                        Platform.runLater(() -> {
                            faceStatusLabel.setText(
                                    "✅ Reconnaissance faciale activée");
                            faceStatusLabel.setStyle(
                                    "-fx-font-size: 12px; -fx-text-fill: #27ae60;");
                            btnFaceStatus.setText("🔄  Mettre à jour mon visage");
                            btnFaceStatus.setStyle(
                                    "-fx-background-color: #e8f5e9;" +
                                            "-fx-text-fill: #27ae60;" +
                                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                                            "-fx-padding: 8 20; -fx-background-radius: 20;" +
                                            "-fx-cursor: hand; -fx-border-color: #27ae60;" +
                                            "-fx-border-width: 1.5; -fx-border-radius: 20;");

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Succès !");
                            alert.setHeaderText(null);
                            alert.setContentText(
                                    "✅ Visage enregistré ! Vous pouvez maintenant " +
                                            "vous connecter par reconnaissance faciale.");
                            alert.showAndWait();
                        });
                    } catch (Exception e) {
                        webcamService.closeCamera();
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setContentText(
                                    "❌ Erreur : " + e.getMessage());
                            alert.showAndWait();
                        });
                        e.printStackTrace();
                    }
                }).start();
            } else {
                webcamService.closeCamera();
            }
        });
    }

    // ── Photo profil ─────────────────────────────────────────
    private void updateAvatarsUI(String imageUrl) {
        try {
            Image img = new Image(imageUrl, true);
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
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(
                nomLabel.getScene().getWindow());

        if (file != null) {
            new Thread(() -> {
                try {
                    CloudinaryService cloudinary = new CloudinaryService();
                    String imageUrl = cloudinary.uploadImage(file);
                    currentUser.setImageUrl(imageUrl);
                    userService.updateUser(currentUser);
                    Platform.runLater(() -> updateAvatarsUI(imageUrl));
                } catch (Exception e) {
                    Platform.runLater(() ->
                            new Alert(Alert.AlertType.ERROR,
                                    "Erreur upload: " + e.getMessage()).show());
                }
            }).start();
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer définitivement votre compte ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userService.deleteUser(currentUser.getId());
                    handleLogout();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR,
                            "Erreur : " + e.getMessage()).show();
                }
            }
        });
    }

    // ── Navigation ───────────────────────────────────────────
    @FXML private void handleLogout() {
        ManagerSession.getInstance().logout();
        switchScene("/login.fxml", "Login", false);
    }
    @FXML private void goHome()         { switchScene("/dashboard_patient.fxml", "Home", true); }
    @FXML private void goCommunity()    { switchScene("/post_view.fxml", "Community", false); }
    @FXML private void goSuivis()       { switchScene("/AjouterSuivi.fxml", "Suivis", false); }
    @FXML private void goDossier()      { switchScene("/dossier_medical.fxml", "Dossier", true); }
    @FXML private void goModifierProfil(){ switchScene("/modifier_profil.fxml", "Modifier Profil", true); }
    @FXML private void goProfil()       {}

    private void switchScene(String fxml, String title, boolean passUser) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            if (passUser && loader.getController()
                    instanceof UserAwareController ctrl) {
                ctrl.setCurrentUser(currentUser);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}