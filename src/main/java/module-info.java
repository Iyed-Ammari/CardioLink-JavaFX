module com.cardiolink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql; // Crucial pour JDBC

    // On ouvre les dossiers à JavaFX pour qu'il puisse afficher les données
    opens com.cardiolink.Models to javafx.base, javafx.fxml;

    // On exporte nos dossiers pour qu'ils soient visibles par le projet
    exports com.cardiolink.Models;
    exports com.cardiolink.Services;
    exports com.cardiolink.utils;

    // Si tu as une classe de test ou de lancement dans com.cardiolink.Test
    exports com.cardiolink.Test;
}