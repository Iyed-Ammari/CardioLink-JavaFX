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

    @FXML
    void goToAdd(ActionEvent event) throws IOException {
        changeScene(event, "/AjouterRDV.fxml");
    }

    @FXML
    void goToList(ActionEvent event) throws IOException {
        changeScene(event, "/AfficherRDV.fxml");
    }

    private void changeScene(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}