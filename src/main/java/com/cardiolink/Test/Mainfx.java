package com.cardiolink.Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Mainfx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterSuivi.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("CardioLink - Suivi");
        stage.setScene(scene);
        stage.setWidth(1050);
        stage.setHeight(700);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}