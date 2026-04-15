package com.cardiolink.Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Lance l'accueil CardioLink
        Parent root = FXMLLoader.load(getClass().getResource("/MenuRDV.fxml"));

        primaryStage.setTitle("CardioLink - Système de Gestion Cardiologique");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // Empêche la fenêtre d'être trop petite au démarrage
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}