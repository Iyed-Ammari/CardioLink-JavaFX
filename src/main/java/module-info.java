module com.cardiolink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;
    requires java.net.http;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jakarta.mail;
    requires jakarta.activation;
    requires jbcrypt;
    requires cloudinary.http44;
    requires cloudinary.core;
    requires com.google.api.client;
    requires jdk.httpserver;
    requires org.json;
    requires okhttp3;
    requires opencv;

    // ✅ iText PDF
    requires kernel;
    requires layout;
    requires io;
    requires commons;

    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.Controllers to javafx.fxml;
    opens com.cardiolink.Models to javafx.fxml;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;
    opens com.cardiolink.WebSocket to javafx.fxml;

    exports com.cardiolink;
}