package com.cardiolink.Controllers;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Services.ServiceOrdonnance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class OrdonnanceMedecinController {

    @FXML private TableView<Ordonnance> tableOrdonnances;
    @FXML private TableColumn<Ordonnance, LocalDateTime> colDate;
    @FXML private TableColumn<Ordonnance, String> colPatient;
    @FXML private TableColumn<Ordonnance, String> colRef;
    @FXML private TableColumn<Ordonnance, String> colDiagnostic;
    @FXML private TableColumn<Ordonnance, Void> colActions;
    @FXML private TextField searchField;

    private ServiceOrdonnance service = new ServiceOrdonnance(); //
    private ObservableList<Ordonnance> liste = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colRef.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));

        configurerBoutonsActions();
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            liste.setAll(service.getAll()); // Utilisation du service réel
            tableOrdonnances.setItems(liste);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurerBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final Button btnCreate = new Button("Créer");
            private final HBox box = new HBox(10, btnEdit, btnDelete, btnCreate);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: orange; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand;");
                btnCreate.setStyle("-fx-background-color: #5b5bff; -fx-text-fill: white; -fx-border-radius: 5;");

                btnEdit.setOnAction(e -> modifierOrdonnance(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> supprimerOrdonnance(getTableView().getItems().get(getIndex())));
                btnCreate.setOnAction(e -> ouvrirFormulaireAjout());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void modifierOrdonnance(Ordonnance o) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrdonnanceCRUD.fxml"));
            Parent root = loader.load();

            // Passer l'objet au contrôleur d'ajout
            AjouterOrdonnance controller = loader.getController();
            controller.preparerModification(o);

            Stage stage = (Stage) tableOrdonnances.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerOrdonnance(Ordonnance o) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + o.getReference() + " ?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            try {
                service.delete(o); // Suppression réelle
                liste.remove(o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ouvrirFormulaireAjout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/OrdonnanceCRUD.fxml"));
            Stage stage = (Stage) tableOrdonnances.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        String filter = searchField.getText().toLowerCase();
        tableOrdonnances.setItems(liste.filtered(o ->
                o.getPatientNom().toLowerCase().contains(filter) ||
                        o.getReference().toLowerCase().contains(filter)
        ));
    }
}