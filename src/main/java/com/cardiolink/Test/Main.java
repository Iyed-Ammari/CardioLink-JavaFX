package com.cardiolink.Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chargement du fichier FXML depuis le dossier resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatView.fxml"));
            Parent root = loader.load();

            // Configuration de la scène
            Scene scene = new Scene(root);

            primaryStage.setTitle("CardioLink - Messagerie");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}