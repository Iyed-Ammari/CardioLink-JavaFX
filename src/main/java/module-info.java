module com.cardiolink {

    // --- JavaFX Modules ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.base;

    // --- Core Java Modules ---
    requires java.sql;
    requires java.net.http;
    requires jdk.httpserver;
    requires java.desktop; // Added from File 1

    // --- UI & External JavaFX Libraries ---
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    // --- Security, WebSockets, JSON, Logging & HTTP ---
    requires jbcrypt;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires org.java_websocket;
    requires org.slf4j; // Added from File 1

    // --- Payment APIs ---
    requires stripe.java; // Added from File 1

    // --- Google Calendar & Auth APIs ---
    // These JARs are not proper JPMS modules (unnamed module / classpath).
    // Access is granted via --add-reads in pom.xml compiler args.

    // --- Media & Processing ---
    requires cloudinary.http44;
    requires cloudinary.core;
    requires opencv;

    // --- Mail ---
    requires jakarta.mail;
    requires jakarta.activation;
    // requires java.mail; // REMOVED to prevent conflict. Tell the team to use jakarta.mail!

    // --- Documents (PDF) ---
    // iText 7 (from File 2)
    requires kernel;
    requires layout;
    requires io;
    requires commons;
    // Apache PDFBox (from File 1)
    requires org.apache.pdfbox;

    // --- Package Exports ---
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
    opens com.cardiolink.WebSocket to javafx.fxml;

    // Models need 'javafx.base' for TableView/TableColumn property mapping
    opens com.cardiolink.Models to javafx.base, javafx.fxml;

    // Opening Test to graphics allows the Application class to launch properly
    opens com.cardiolink.Test to javafx.fxml, javafx.graphics;
}