package com.cardiolink.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public final class NavigationUtil {

    private static final double DEFAULT_WIDTH = 1400;
    private static final double DEFAULT_HEIGHT = 850;

    private NavigationUtil() {
    }

    public static void navigate(Stage stage, String fxmlPath) throws IOException {
        if (stage == null) {
            throw new IllegalArgumentException("Le stage ne peut pas être null.");
        }

        URL fxmlUrl = NavigationUtil.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML introuvable : " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        applyGlobalCss(scene);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void applyGlobalCss(Scene scene) {
        String css = Objects.requireNonNull(
                NavigationUtil.class.getResource("/css/app.css"),
                "Fichier CSS introuvable : /css/app.css"
        ).toExternalForm();

        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }
}