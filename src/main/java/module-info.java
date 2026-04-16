module com.cardiolink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;

    // 1. Autorise JavaFX à lire tes contrôleurs (pour le FXML)
    opens com.cardiolink.Controllers to javafx.fxml;

    // 2. Autorise JavaFX à lire tes modèles (pour l'affichage dans la TableView)
    opens com.cardiolink.Models to javafx.base;

    // 3. Autorise le lancement de l'application
    opens com.cardiolink.Test to javafx.fxml, javafx.graphics;

    exports com.cardiolink.Test;
    exports com.cardiolink.Controllers;
    exports com.cardiolink.Models;
}