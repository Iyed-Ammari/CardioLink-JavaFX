package com.cardiolink.Controllers;

import com.cardiolink.Models.Suivi;
import com.cardiolink.Services.SuiviService;
import com.cardiolink.utils.ManagerSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SuiviArchiveController {

    @FXML
    private TableView<Suivi> tableArchives;

    @FXML
    private TableColumn<Suivi, String> colType;

    @FXML
    private TableColumn<Suivi, String> colValeur;

    @FXML
    private TableColumn<Suivi, String> colDate;

    @FXML
    private TableColumn<Suivi, String> colUrgence;

    @FXML
    private Label lblMessage;

    private final SuiviService suiviService = new SuiviService();
    private final ObservableList<Suivi> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerArchives();
    }

    private void configurerColonnes() {
        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeDonnee()));

        colValeur.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getValeur() + " " + cellData.getValue().getUnite()));

        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateSaisie() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getDateSaisie().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new SimpleStringProperty("-");
        });

        colUrgence.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNiveauUrgence()));
    }

    private void chargerArchives() {
        try {
            int patientId = ManagerSession.getInstance().getCurrentUser().getId();
            List<Suivi> archives = suiviService.getArchivedByPatientId(patientId);

            data.setAll(archives);
            tableArchives.setItems(data);
            lblMessage.setText("Nombre de suivis archivés : " + archives.size());

        } catch (Exception e) {
            lblMessage.setText("Erreur de chargement des archives : " + e.getMessage());
            e.printStackTrace();
        }
    }
}