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
    requires java.mail;
    requires java.net.http;
    requires itextpdf;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires jbcrypt;
    requires org.java_websocket;
    requires org.json;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.services.calendar;
    requires jdk.httpserver;

    // --- Package Exports ---
    // These allow other modules to use your classes (e.g., during compilation)
    exports com.cardiolink;
    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
    exports com.cardiolink.utils;
    exports com.cardiolink.Controllers;
    exports com.cardiolink.Test;

    // --- Reflection Opens ---
    // Required for FXML to inject fields (@FXML)
    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.Controllers to javafx.fxml;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;

    // Models need 'javafx.base' for TableView/TableColumn property mapping
    // Also opens to fxml in case you use models inside FXML files (like ChoiceBox)
    opens com.cardiolink.Models to javafx.base, javafx.fxml;

    // Opening Test to graphics allows the Application class to launch properly
    opens com.cardiolink.Test to javafx.fxml, javafx.graphics;
}