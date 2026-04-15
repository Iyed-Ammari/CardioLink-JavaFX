package com.cardiolink;

import com.cardiolink.utils.NavigationUtil;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("CardioLink — Every Pulse Counts");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        //NavigationUtil.navigate(stage, "/fxml/patient/produit-list-patient.fxml");
        NavigationUtil.navigate(stage, "/fxml/admin/dashboard-admin.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}