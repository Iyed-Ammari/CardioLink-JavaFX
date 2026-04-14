package com.cardiolink.Controllers;

import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Services.ServiceRendezvous;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AfficherRDV {

    @FXML private TableView<Rendezvous> tableRDV;
    @FXML private TableColumn<Rendezvous, LocalDateTime> colDate;
    @FXML private TableColumn<Rendezvous, String> colMedecin;
    @FXML private TableColumn<Rendezvous, String> colStatut;
    @FXML private TableColumn<Rendezvous, String> colType;
    @FXML private TableColumn<Rendezvous, String> colMotif;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterType;

    private ServiceRendezvous serviceRV = new ServiceRendezvous();
    private ObservableList<Rendezvous> rendezvousList = FXCollections.observableArrayList();
    private FilteredList<Rendezvous> filteredData;

    @FXML
    public void initialize() {
        // 1. Liaison des colonnes
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecinId"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("remarques"));

        // 2. Initialisation des menus de filtrage
        filterStatut.getItems().addAll("Tous", "En attente", "Confirmé", "Annulé", "Terminé");
        filterType.getItems().addAll("Tous", "Consultation au cabinet", "Téléconsultation", "Urgence");
        filterStatut.setValue("Tous");
        filterType.setValue("Tous");

        // 3. Chargement des données
        loadData();

        // 4. Configuration de la logique de filtrage et tri
        setupFilterAndSort();
    }

    private void loadData() {
        try {
            rendezvousList.clear();
            List<Rendezvous> data = serviceRV.getAll();
            rendezvousList.addAll(data);
        } catch (SQLDataException e) {
            showAlert("Erreur", "Base de données inaccessible", Alert.AlertType.ERROR);
        }
    }

    private void setupFilterAndSort() {
        // Créer la liste filtrable
        filteredData = new FilteredList<>(rendezvousList, p -> true);

        // Écouteurs sur les changements (Recherche + ComboBox)
        searchField.textProperty().addListener((obs, old, val) -> updateFilter());
        filterStatut.valueProperty().addListener((obs, old, val) -> updateFilter());
        filterType.valueProperty().addListener((obs, old, val) -> updateFilter());

        // Créer la liste triable liée à la TableView
        SortedList<Rendezvous> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableRDV.comparatorProperty());
        tableRDV.setItems(sortedData);

        // Appliquer le tri par date par défaut
        applyDefaultSort();
    }

    private void updateFilter() {
        filteredData.setPredicate(rv -> {
            // Filtre Recherche
            String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            boolean matchesSearch = rv.getType().toLowerCase().contains(searchText) ||
                    (rv.getRemarques() != null && rv.getRemarques().toLowerCase().contains(searchText));

            // Filtre Statut
            String stat = filterStatut.getValue();
            boolean matchesStatut = (stat == null || stat.equals("Tous") || rv.getStatut().equals(stat));

            // Filtre Type
            String type = filterType.getValue();
            boolean matchesType = (type == null || type.equals("Tous") || rv.getType().equals(type));

            return matchesSearch && matchesStatut && matchesType;
        });
    }

    private void applyDefaultSort() {
        tableRDV.getSortOrder().clear();
        colDate.setSortType(TableColumn.SortType.DESCENDING);
        tableRDV.getSortOrder().add(colDate);
    }

    @FXML
    void handleSortDate(ActionEvent event) {
        applyDefaultSort();
    }

    @FXML
    void resetFilters(ActionEvent event) {
        searchField.clear();
        filterStatut.setValue("Tous");
        filterType.setValue("Tous");
        applyDefaultSort();
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Rendezvous selected = tableRDV.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le rendez-vous ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    serviceRV.delete(selected);
                    loadData();
                } catch (SQLDataException e) {
                    showAlert("Erreur", "Action impossible", Alert.AlertType.ERROR);
                }
            }
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        Rendezvous selected = tableRDV.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRDV.fxml"));
                Parent root = loader.load();
                AjouterRDV controller = loader.getController();
                controller.preparerModification(selected);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML
    void goToAdd(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AjouterRDV.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    void goToMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/MenuRDV.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}