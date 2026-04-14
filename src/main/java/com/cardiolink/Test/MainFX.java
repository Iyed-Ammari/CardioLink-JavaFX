package com.cardiolink.Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cette ligne dit à Java de lancer le MENU en premier
        Parent root = FXMLLoader.load(getClass().getResource("/MenuRDV.fxml"));
        primaryStage.setTitle("CardioLink - Accueil");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}