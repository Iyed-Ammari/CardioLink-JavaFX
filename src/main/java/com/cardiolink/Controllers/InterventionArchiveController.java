package com.cardiolink.Controllers;

import com.cardiolink.Models.Intervention;
import com.cardiolink.Services.InterventionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class InterventionArchiveController {

    @FXML
    private TableView<Intervention> tableArchives;

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
    private Label lblMessage;

    private final InterventionService interventionService = new InterventionService();
    private final ObservableList<Intervention> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerArchives();
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

        colMedecin.setCellValueFactory(cell -> {
            if (cell.getValue().getMedecinId() > 0) {
                return new SimpleStringProperty("Dr. #" + cell.getValue().getMedecinId());
            }
            return new SimpleStringProperty("Non assignée");
        });
    }

    private void chargerArchives() {
        try {
            List<Intervention> archives = interventionService.getArchived();
            data.setAll(archives);
            tableArchives.setItems(data);
            lblMessage.setText("Nombre d'interventions archivées : " + archives.size());
        } catch (Exception e) {
            lblMessage.setText("Erreur de chargement des archives : " + e.getMessage());
            e.printStackTrace();
        }
    }
}