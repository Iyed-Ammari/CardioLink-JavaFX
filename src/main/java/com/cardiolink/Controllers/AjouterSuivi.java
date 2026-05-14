package com.cardiolink.Controllers;

import com.cardiolink.Models.Suivi;
import com.cardiolink.Services.SuiviService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class AjouterSuivi {

    @FXML
    private Label lblMessage;

    @FXML
    private ListView<String> listSuivis;

    private final SuiviService suiviService = new SuiviService();

    @FXML
    public void initialize() {
        chargerSuivis();
    }

    // ✅ RETOUR VERS LE DASHBOARD PATIENT
    @FXML
    public void retourPagePrecedente() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_patient.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);

            Stage stage = (Stage) listSuivis.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CardioLink - Dashboard");
            stage.show();

            PatientDashboardController ctrl = loader.getController();
            ctrl.init();

        } catch (Exception e) {
            lblMessage.setText("Erreur retour dashboard : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirFenetreAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EnregistrerSuivi.fxml"));

            if (loader.getLocation() == null) {
                lblMessage.setText("Fichier EnregistrerSuivi.fxml introuvable.");
                return;
            }

            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Enregistrer un Nouveau Suivi");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            chargerSuivis();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture fenêtre ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirConsulterSuivi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConsulterSuivi.fxml"));

            if (loader.getLocation() == null) {
                lblMessage.setText("Fichier ConsulterSuivi.fxml introuvable.");
                return;
            }

            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Consulter les suivis");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture consultation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void chargerSuivis() {
        listSuivis.getItems().clear();

        try {
            List<Suivi> suivis = suiviService.getAll();

            if (suivis == null || suivis.isEmpty()) {
                lblMessage.setText("Aucun suivi enregistré.");
                listSuivis.getItems().add("Aucun suivi enregistré.");
                return;
            }

            lblMessage.setText("Nombre de suivis enregistrés : " + suivis.size());

            for (Suivi s : suivis) {
                String ligne = "ID: " + s.getId()
                        + " | Type: " + s.getTypeDonnee()
                        + " | Valeur: " + s.getValeur() + " " + s.getUnite()
                        + " | Urgence: " + s.getNiveauUrgence()
                        + " | Patient ID: " + s.getPatientId();

                listSuivis.getItems().add(ligne);
            }

        } catch (Exception e) {
            lblMessage.setText("Impossible de charger les suivis : " + e.getMessage());
            listSuivis.getItems().add("Erreur de chargement des suivis.");
            e.printStackTrace();
        }
    }
}