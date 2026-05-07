package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
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
import javafx.stage.Stage;
import org.opencv.core.Mat;
import java.io.IOException;

public class RegisterFaceController {

    @FXML private ImageView webcamView;
    @FXML private Label     cameraStatus;
    @FXML private Label     statusLabel;
    @FXML private Label     errorLabel;
    @FXML private Button    btnCapture;

    private final WebcamService webcamService = new WebcamService();
    private final UserService   userService   = new UserService();
    private       AnimationTimer timer;
    private       User          currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        startCamera();
    }

    private void startCamera() {
        new Thread(() -> {
            boolean opened = webcamService.openCamera();
            Platform.runLater(() -> {
                if (opened) {
                    cameraStatus.setText("");
                    startPreview();
                } else {
                    cameraStatus.setText("❌ Webcam non détectée !");
                    btnCapture.setDisable(true);
                }
            });
        }).start();
    }

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

    // ── Capturer et enregistrer le visage ────────────────────
    @FXML
    private void handleCapture() {
        clearMessages();
        btnCapture.setDisable(true);
        btnCapture.setText("Capture en cours...");
        statusLabel.setText("📸 Prenez la pose...");

        new Thread(() -> {
            try {
                Thread.sleep(1000); // Laisser 1 sec pour se préparer

                String base64 = webcamService.captureAsBase64();

                // Sauvegarder en DB
                userService.saveFaceImage(currentUser.getId(), base64);

                Platform.runLater(() -> {
                    statusLabel.setText("✅ Visage enregistré avec succès !");
                    // Attendre 2 sec puis aller au dashboard
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(
                                    javafx.util.Duration.seconds(2));
                    pause.setOnFinished(e -> goToDashboard());
                    pause.play();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Erreur : " + e.getMessage());
                    btnCapture.setDisable(false);
                    btnCapture.setText("📸  Prendre la photo");
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ── Passer cette étape ───────────────────────────────────
    @FXML
    private void handleSkip() {
        stopCamera();
        goToDashboard();
    }

    private void goToDashboard() {
        stopCamera();
        try {
            Stage stage = (Stage) webcamView.getScene().getWindow();
            if (currentUser != null &&
                    "ROLE_ADMIN".equals(currentUser.getRoleClean())) {
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

    private void stopCamera() {
        if (timer != null) timer.stop();
        webcamService.closeCamera();
    }

    private void clearMessages() {
        statusLabel.setText(""); errorLabel.setText("");
    }
    private void showError(String msg) {
        errorLabel.setText(msg); statusLabel.setText("");
    }
}