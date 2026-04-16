module com.cardiolink {

    // JavaFX Modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;

    // Database and External Libraries
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jbcrypt;
    requires com.almasb.fxgl.all;
    requires org.java_websocket;
    requires org.json;

    // Package Exports
    exports com.cardiolink;
    exports com.cardiolink.Controllers;
    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
    exports com.cardiolink.Test;

    // Reflection Opens (Required for JavaFX FXML and Property access)
    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.Controllers to javafx.fxml;
    opens com.cardiolink.Models to javafx.fxml, javafx.base;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;
    opens com.cardiolink.Test to javafx.fxml;
}