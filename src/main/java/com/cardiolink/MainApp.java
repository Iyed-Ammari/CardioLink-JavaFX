package com.cardiolink;

import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Services.ServiceRendezvous;
import com.cardiolink.utils.ManagerSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {
    
    private com.cardiolink.Services.ReminderScheduler reminderScheduler;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/login.fxml")
        );
        Scene scene = new Scene(loader.load(), 1100, 700);
        stage.setTitle("CardioLink");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();

        // Démarrer le planificateur de rappels
        reminderScheduler = new com.cardiolink.Services.ReminderScheduler();
        reminderScheduler.start();
    }

    @Override
    public void stop() throws Exception {
        if (reminderScheduler != null) {
            reminderScheduler.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}