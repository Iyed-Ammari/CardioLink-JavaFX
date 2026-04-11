module com.cardiolink {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    // requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;

    opens com.cardiolink.Models to javafx.fxml;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;
    opens com.cardiolink.Test to javafx.fxml;

    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
    exports com.cardiolink.utils;
    exports com.cardiolink.Test;
}