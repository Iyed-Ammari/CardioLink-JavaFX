package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.FaceRecognitionService;
import com.cardiolink.Services.UserService;
import com.cardiolink.Services.WebcamService;
import com.cardiolink.utils.ManagerSession;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import java.io.IOException;

public class FaceLoginController {

    @FXML private ImageView webcamView;
    @FXML private Label     cameraStatus;
    @FXML private TextField emailField;
    @FXML private Label     statusLabel;
    @FXML private Label     errorLabel;
    @FXML private Button    btnVerify;

    private final WebcamService          webcamService = new WebcamService();
    private final FaceRecognitionService faceService   = new FaceRecognitionService();
    private final UserService            userService   = new UserService();
    private       AnimationTimer         timer;

    @FXML
    public void initialize() {
        startCamera();
    }

    // ── Démarrer la caméra et prévisualisation ────────────────
    private void startCamera() {
        new Thread(() -> {
            boolean opened = webcamService.openCamera();
            Platform.runLater(() -> {
                if (opened) {
                    cameraStatus.setText("");
                    startPreview();
                } else {
                    cameraStatus.setText("❌ Webcam non détectée !");
                    btnVerify.setDisable(true);
                }
            });
        }).start();
    }

    // ── Afficher le flux webcam en temps réel ────────────────
    private void startPreview() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Mat frame = webcamService.getCurrentFrame();
                if (frame != null) {
                    webcamView.setImage(webcamService.matToFxImage(frame));
                }
            }
        };
        timer.start();
    }

    // ── Vérifier le visage ───────────────────────────────────
    @FXML
    private void handleFaceVerify() {
        clearMessages();
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Entrez votre email !"); return;
        }

        btnVerify.setDisable(true);
        btnVerify.setText("Vérification...");
        statusLabel.setText("📸 Capture en cours...");

        new Thread(() -> {
            try {
                // ── 1. Récupérer l'image stockée en DB ───────
                String storedImage = userService.getFaceImage(email);

                if (storedImage == null || storedImage.isEmpty()) {
                    Platform.runLater(() -> {
                        showError("Aucun visage enregistré pour cet email !\n" +
                                "Activez la reconnaissance depuis votre profil.");
                        resetButton();
                    });
                    return;
                }

                // ── 2. Capturer le visage actuel ──────────────
                Platform.runLater(() ->
                        statusLabel.setText("📸 Capture du visage..."));

                String capturedBase64 = webcamService.captureAsBase64();

                Platform.runLater(() ->
                        statusLabel.setText("🔄 Comparaison en cours..."));

                // ── 3. Comparer avec HuggingFace ─────────────
                boolean match = faceService.verifyFace(
                        capturedBase64, storedImage);

                Platform.runLater(() -> {
                    if (match) {
                        try {
                            statusLabel.setText("✅ Visage reconnu !");
                            User user = userService.findByEmail(email);
                            if (user != null) {
                                stopCamera();
                                ManagerSession.getInstance()
                                        .setCurrentUser(user);
                                navigateToDashboard(user);
                            }
                        } catch (Exception e) {
                            showError("Erreur : " + e.getMessage());
                            resetButton();
                        }
                    } else {
                        showError("❌ Visage non reconnu ! Réessayez.");
                        resetButton();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Erreur : " + e.getMessage());
                    resetButton();
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ── Navigation ───────────────────────────────────────────
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
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void goToLogin(MouseEvent event) {
        stopCamera();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void stopCamera() {
        if (timer != null) timer.stop();
        webcamService.closeCamera();
    }

    private void resetButton() {
        btnVerify.setDisable(false);
        btnVerify.setText("📸  Vérifier mon visage");
    }

    private void clearMessages() {
        statusLabel.setText(""); errorLabel.setText("");
    }
    private void showError(String msg) {
        errorLabel.setText(msg); statusLabel.setText("");
    }
}