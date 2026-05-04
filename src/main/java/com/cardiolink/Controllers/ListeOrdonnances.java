package com.cardiolink.Controllers;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Services.ServiceOrdonnance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node; // Import manquant
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class ListeOrdonnances {

    @FXML private TableView<Ordonnance> tableOrdonnances;
    @FXML private TableColumn<Ordonnance, String> colRef, colPatient, colDiag;
    @FXML private TableColumn<Ordonnance, LocalDateTime> colDate;
    @FXML private Label lblTotal;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private ServiceOrdonnance service = new ServiceOrdonnance();
    private ObservableList<Ordonnance> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colRef.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colDiag.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));

        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            masterData.setAll(service.getAll());
            tableOrdonnances.setItems(masterData);
            lblTotal.setText(masterData.size() + " ordonnances trouvées");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/dashboard_patient.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToAdd(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterOrdonnance.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterOrdonnance.fxml"));
                Parent root = loader.load();

                AjouterOrdonnance controller = loader.getController();
                controller.preparerModification(selected);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une ordonnance à modifier.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'ordonnance " + selected.getReference() + " ?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().get() == ButtonType.YES) {
                try {
                    service.delete(selected);
                    masterData.remove(selected);
                    lblTotal.setText(masterData.size() + " ordonnances trouvées");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}