
module com.cardiolink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jbcrypt;


    opens com.cardiolink to javafx.fxml;
    opens com.cardiolink.controllers to javafx.fxml;
    opens com.cardiolink.Models to javafx.fxml;
    opens com.cardiolink.Services to javafx.fxml;
    opens com.cardiolink.utils to javafx.fxml;

    exports com.cardiolink;
}
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
