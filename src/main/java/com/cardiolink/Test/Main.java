package com.cardiolink.Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/post_view.fxml")
            );

            Scene scene = new Scene(root);

            stage.setTitle("CardioLink - Forum");
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
