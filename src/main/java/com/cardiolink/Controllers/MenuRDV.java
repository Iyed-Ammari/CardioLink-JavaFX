package com.cardiolink.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuRDV {

    // 1. Bouton "Prendre un RDV" -> Redirige vers AjouterRDV.fxml
    @FXML
    void goToAdd(ActionEvent event) throws IOException {
        loadScene(event, "/AjouterRDV.fxml", "PATIENT");
    }

    // 2. Bouton "Gestion des Ordonnances" -> Reste sur ListeOrdonnances.fxml
    @FXML
    void goToOrdonnances(ActionEvent event) throws IOException {
        loadScene(event, "/ListeOrdonnances.fxml", "MEDECIN");
    }

    // 3. Bouton "Mes Rendez-vous" -> Redirige vers AfficherRDV.fxml
    @FXML
    void goToList(ActionEvent event) throws IOException {
        loadScene(event, "/AfficherRDV.fxml", "PATIENT");
    }

    // MÉTHODE DE CHARGEMENT UNIQUE ET CORRIGÉE
    private void loadScene(ActionEvent event, String fxmlFile, String role) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // On vérifie le type de contrôleur pour passer les paramètres si nécessaire
        Object controller = loader.getController();

        if (controller instanceof ListeOrdonnances) {
            ((ListeOrdonnances) controller).setRole(role, "Patient Test");
        }
        // Vous pouvez ajouter ici des conditions pour d'autres contrôleurs si besoin

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}