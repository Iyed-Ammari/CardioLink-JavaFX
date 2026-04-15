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
    requires org.java_websocket;
    requires org.json;

    opens com.cardiolink.Test to javafx.fxml;
    exports com.cardiolink.Test;
    opens com.cardiolink.Controllers to javafx.fxml;
    exports com.cardiolink.Controllers;
    opens com.cardiolink.Models to javafx.base;
}