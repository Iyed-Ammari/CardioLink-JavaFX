package com.cardiolink.Controllers;

import com.cardiolink.Models.Ordonnance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;

public class OrdonnanceMedecinController {

    @FXML private TableView<Ordonnance> tableOrdonnances;
    @FXML private TableColumn<Ordonnance, LocalDateTime> colDate;
    @FXML private TableColumn<Ordonnance, String> colPatient;
    @FXML private TableColumn<Ordonnance, String> colRef;
    @FXML private TableColumn<Ordonnance, String> colDiagnostic;
    @FXML private TableColumn<Ordonnance, Void> colActions;
    @FXML private TextField searchField;

    private ObservableList<Ordonnance> liste = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colRef.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));

        // ✅ Ajout des boutons
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final Button btnCreate = new Button("Créer");

            private final HBox box = new HBox(10, btnEdit, btnDelete, btnCreate);

            {
                box.setAlignment(Pos.CENTER);

                // Style
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: orange;");
                btnDelete.setStyle("-fx-background-color: white; -fx-text-fill: red;");
                btnCreate.setStyle("-fx-background-color: #5b5bff; -fx-text-fill: white;");

                // Actions
                btnEdit.setOnAction(e -> {
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    modifierOrdonnance(o);
                });

                btnDelete.setOnAction(e -> {
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    supprimerOrdonnance(o);
                });

                btnCreate.setOnAction(e -> {
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    creerOrdonnance(o);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // 🔥 Données test
        liste.add(new Ordonnance("ORD-001", LocalDateTime.now(), 1, "Hypertension", "", "Dr A", "Ahmed"));
        liste.add(new Ordonnance("ORD-002", LocalDateTime.now(), 2, "Diabète", "", "Dr B", "Sonia"));
        liste.add(new Ordonnance("ORD-003", LocalDateTime.now(), 3, "Infection", "", "Dr C", "Youssef"));

        tableOrdonnances.setItems(liste);
    }

    // 🔍 Recherche
    @FXML
    private void onSearch() {
        String filter = searchField.getText().toLowerCase();

        tableOrdonnances.setItems(liste.filtered(o ->
                o.getPatientNom().toLowerCase().contains(filter) ||
                        o.getReference().toLowerCase().contains(filter)
        ));
    }

    // ✏ Modifier
    private void modifierOrdonnance(Ordonnance o) {
        System.out.println("Modifier : " + o.getReference());
    }

    // 🗑 Supprimer
    private void supprimerOrdonnance(Ordonnance o) {
        liste.remove(o);
    }

    // ➕ Créer ordonnance
    private void creerOrdonnance(Ordonnance o) {
        System.out.println("Créer ordonnance pour : " + o.getPatientNom());
    }
}