package com.cardiolink.Controllers;

import com.cardiolink.utils.ManagerSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MedecinDashboardController {

    @FXML
    public void ouvrirInterventions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Intervention.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);

            Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
            stage.setScene(scene);
            stage.setTitle("CardioLink - Interventions Médecin");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirAlertesSOS() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AlertesSOS.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 850);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("CardioLink - Alertes SOS");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        try {
            ManagerSession.getInstance().setCurrentUser(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);

            Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
            stage.setScene(scene);
            stage.setTitle("CardioLink - Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}