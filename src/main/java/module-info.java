module com.cardiolink {

    // --- JavaFX Modules ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.base;

    // --- Database & External Libraries ---
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires jbcrypt;
    requires org.java_websocket;
    requires org.json;

    // --- Package Exports ---
    // These allow other modules to use your classes directly
    exports com.cardiolink;
    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
    exports com.cardiolink.utils;
    exports com.cardiolink.Controllers;
    exports com.cardiolink.Test;

    // --- Reflection Opens ---
    // Required for FXML to inject fields and for TableView to access Model properties
    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.Controllers to javafx.fxml;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;

    // Models need 'javafx.base' for TableView/TableColumn property mapping
    opens com.cardiolink.Models to javafx.fxml, javafx.base;

    // Opening Test to graphics allows the Application class to launch from here
    opens com.cardiolink.Test to javafx.fxml, javafx.graphics;
}