package com.cardiolink.Controllers;

import com.cardiolink.Models.Intervention;
import com.cardiolink.Services.InterventionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class InterventionController {

    @FXML
    private TextField txtRecherche;

    @FXML
    private TableView<Intervention> tableInterventions;

    @FXML
    private TableColumn<Intervention, String> colType;

    @FXML
    private TableColumn<Intervention, String> colDescription;

    @FXML
    private TableColumn<Intervention, String> colDatePlanifiee;

    @FXML
    private TableColumn<Intervention, String> colStatut;

    @FXML
    private TableColumn<Intervention, String> colMedecin;

    @FXML
    private TableColumn<Intervention, Void> colActions;

    @FXML
    private Label lblMessage;

    private final InterventionService interventionService = new InterventionService();
    private final ObservableList<Intervention> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerInterventions();
    }

    private void configurerColonnes() {
        colType.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getType()));

        colDescription.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescription()));

        colDatePlanifiee.setCellValueFactory(cell -> {
            if (cell.getValue().getDatePlanifiee() != null) {
                return new SimpleStringProperty(
                        cell.getValue().getDatePlanifiee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new SimpleStringProperty("-");
        });

        colStatut.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatut()));

        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(statut);
                badge.setStyle(styleStatut(statut));
                setGraphic(badge);
                setText(null);
            }
        });

        colMedecin.setCellValueFactory(cell -> {
            if (cell.getValue().getMedecinId() > 0) {
                return new SimpleStringProperty("Dr. #" + cell.getValue().getMedecinId());
            }
            return new SimpleStringProperty("Non assignée");
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁 Voir");
            private final HBox box = new HBox(btnVoir);

            {
                btnVoir.setStyle("-fx-background-color: linear-gradient(to right, #5d73f1, #7587ff);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 15px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 12;"
                        + "-fx-cursor: hand;");

                btnVoir.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    afficherDetails(intervention);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private String styleStatut(String statut) {
        return switch (statut) {
            case "En attente" -> "-fx-background-color: #f2e6d6; -fx-text-fill: #ff8a00; -fx-padding: 10 18; -fx-background-radius: 10;";
            case "Acceptée" -> "-fx-background-color: #dff3e6; -fx-text-fill: #26a65b; -fx-padding: 10 18; -fx-background-radius: 10;";
            case "Effectuée" -> "-fx-background-color: #dde8ff; -fx-text-fill: #4b6fff; -fx-padding: 10 18; -fx-background-radius: 10;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-padding: 10 18; -fx-background-radius: 10;";
        };
    }

    private void chargerInterventions() {
        try {
            List<Intervention> interventions = interventionService.getAll();
            data.setAll(interventions);
            tableInterventions.setItems(data);
            lblMessage.setText("Nombre d'interventions : " + interventions.size());
        } catch (Exception e) {
            lblMessage.setText("Erreur de chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void rechercherInterventions() {
        try {
            String recherche = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim().toLowerCase();

            List<Intervention> interventions = interventionService.getAll();

            if (recherche.isEmpty()) {
                data.setAll(interventions);
            } else {
                data.setAll(
                        interventions.stream()
                                .filter(i ->
                                        (i.getType() != null && i.getType().toLowerCase().contains(recherche)) ||
                                                (i.getDescription() != null && i.getDescription().toLowerCase().contains(recherche))
                                )
                                .collect(Collectors.toList())
                );
            }

            tableInterventions.setItems(data);
            lblMessage.setText("Résultats : " + data.size());

        } catch (Exception e) {
            lblMessage.setText("Erreur de recherche.");
            e.printStackTrace();
        }
    }

    private void afficherDetails(Intervention intervention) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails Intervention");
        alert.setHeaderText(intervention.getType());
        alert.setContentText(
                "Description : " + intervention.getDescription() + "\n\n" +
                        "Statut : " + intervention.getStatut() + "\n" +
                        "Date planifiée : " +
                        (intervention.getDatePlanifiee() != null
                                ? intervention.getDatePlanifiee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                : "-") + "\n" +
                        "Médecin : " + intervention.getMedecinId()
        );
        alert.showAndWait();
    }

    @FXML
    public void ouvrirAlertesSOS() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AlertesSOS.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Alertes SOS Urgentes");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture alertes SOS : " + e.getMessage());
            e.printStackTrace();
        }
    }
}