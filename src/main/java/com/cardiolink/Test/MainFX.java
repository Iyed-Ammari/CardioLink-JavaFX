package com.cardiolink.Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Chemin vers ton nouveau fichier FXML pour le médecin
            // Assure-toi que le fichier OrdonnanceMedecin.fxml est bien dans le dossier resources
            Parent root = FXMLLoader.load(getClass().getResource("/OrdonnanceMedecin.fxml"));

            primaryStage.setTitle("CardioLink - Espace Médecin (Gestion des Ordonnances)");

            Scene scene = new Scene(root);

            // Appliquer le CSS si nécessaire
            // scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            primaryStage.setScene(scene);

            // Dimensions adaptées pour un tableau d'ordonnances
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(650);

            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'interface Ordonnance : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}