package com.cardiolink.Controllers;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Services.ServiceOrdonnance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;
    @FXML private TextField searchField;

    private ServiceOrdonnance service = new ServiceOrdonnance();
    private ObservableList<Ordonnance> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colRef.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colDiag.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));
        refreshUI();
    }

    public void setRole(String role, String nom) {
        if ("PATIENT".equals(role)) {
            if (btnAjouter != null) btnAjouter.setVisible(false);
            if (btnModifier != null) btnModifier.setVisible(false);
            if (btnSupprimer != null) btnSupprimer.setVisible(false);
        }
    }

    private void refreshUI() {
        try {
            masterData.setAll(service.getAll());
            tableOrdonnances.setItems(masterData);
            lblTotal.setText(masterData.size() + " ordonnances affichées");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.delete(selected);
                refreshUI();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    void handleEdit(ActionEvent event) throws IOException {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterOrdonnance.fxml"));
            Parent root = loader.load();
            AjouterOrdonnance controller = loader.getController();
            controller.preparerModification(selected);
            showScene(event, root);
        }
    }

    @FXML
    void goToAdd(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AjouterOrdonnance.fxml"));
        showScene(event, root);
    }

    @FXML
    void goToMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/MenuRDV.fxml"));
        showScene(event, root);
    }

    private void showScene(ActionEvent event, Parent root) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}