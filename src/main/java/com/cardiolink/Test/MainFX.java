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
            // ON DÉMARRE SUR LE MENU DE SÉLECTION
            Parent root = FXMLLoader.load(getClass().getResource("/MenuRDV.fxml"));

            primaryStage.setTitle("CardioLink - Bienvenue");
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // Dimensions standards
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Erreur au démarrage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}