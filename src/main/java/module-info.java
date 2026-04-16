module com.cardiolink {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    exports com.cardiolink.Test;
    opens com.cardiolink.Test to javafx.graphics, javafx.fxml;

    exports Controller;
    opens Controller to javafx.fxml;

    exports com.cardiolink.Models;
    opens com.cardiolink.Models to javafx.base, javafx.fxml;

    exports com.cardiolink.Services;
    exports com.cardiolink.utils;
}