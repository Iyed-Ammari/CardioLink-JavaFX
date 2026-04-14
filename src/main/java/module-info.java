module com.cardiolink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.controllers to javafx.fxml;
    opens com.cardiolink.Models to javafx.base, javafx.fxml;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;

    exports com.cardiolink;
    exports com.cardiolink.controllers;
    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
    exports com.cardiolink.utils;
}