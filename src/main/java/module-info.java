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

    // OPEN seulement pour JavaFX (FXML + TableView)
    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.controllers to javafx.fxml;
    opens com.cardiolink.Models to javafx.base;

    // EXPORTS seulement pour API principale
    exports com.cardiolink;
    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
}