package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class MenuRDV {

    UserService userService = new UserService();
    @FXML
    private ComboBox<String> roleSelector; // L'ID doit correspondre dans le FXML

    @FXML
    public void initialize() {
        // Initialisation du menu déroulant
        if (roleSelector != null) {
            roleSelector.getItems().addAll("Patient", "Médecin");
            roleSelector.setValue("Patient"); // Valeur par défaut
        }
    }

    // Méthode liée au bouton "Entrer" ou "Valider"
    @FXML
    void handleNavigation(ActionEvent event) throws IOException {
        String selectedRole = roleSelector.getValue();

        if ("Médecin".equals(selectedRole)) {
            // Redirection vers l'interface des rendez-vous du médecin
            loadScene(event, "/AfficherRDVMedecin.fxml");
        } else {
            // Redirection vers l'interface classique pour le patient
            loadScene(event, "/AfficherRDV.fxml");
        }
    }

    // Boutons existants que vous pouvez adapter ou supprimer selon vos besoins
    @FXML
    void goToAdd(ActionEvent event) throws IOException {
        loadScene(event, "/AjouterRDV.fxml");
    }

    @FXML
    void goToList(ActionEvent event) throws IOException {
        loadScene(event, "/AfficherRDV.fxml");
    }

    // Fonction utilitaire pour changer de page
    private void loadScene(ActionEvent event, String fxmlFile) throws IOException {
        int userId = ManagerSession.getInstance().getCurrentUserId();
        try {
            User user  = userService.getUserById(userId);
            System.out.println(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    void goToOrdonnances(ActionEvent event) throws IOException {
        // Si c'est pour le médecin, on charge l'interface OrdonnanceMedecin (ou AfficherRDVMedecin)
        loadScene(event, "/AfficherRDVMedecin.fxml");
    }
}