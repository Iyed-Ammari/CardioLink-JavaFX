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
    requires jdk.httpserver;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    requires jbcrypt;
    requires org.java_websocket;
    requires org.slf4j;
    requires org.json;

    // --- AJOUTÉS depuis ton module ---
    requires stripe.java;
    requires org.apache.pdfbox;
    requires java.mail;

    // --- Package Exports ---
    exports com.cardiolink;
    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
    exports com.cardiolink.utils;
    exports com.cardiolink.Controllers;
    exports com.cardiolink.Test;

    // --- Reflection Opens ---
    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.Controllers to javafx.fxml;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;
    opens com.cardiolink.Models to javafx.base, javafx.fxml;
    opens com.cardiolink.Test to javafx.fxml, javafx.graphics;
}