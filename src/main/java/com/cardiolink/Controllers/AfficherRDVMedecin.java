package com.cardiolink.Controllers;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Models.User;
import com.cardiolink.Services.ServiceOrdonnance;
import com.cardiolink.Services.ServiceRendezvous;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AfficherRDVMedecin {

    @FXML private TableView<Rendezvous> tableRDV;
    @FXML private TableColumn<Rendezvous, LocalDateTime> colDate;
    @FXML private TableColumn<Rendezvous, String> colPatient;
    @FXML private TableColumn<Rendezvous, String> colType;
    @FXML private TableColumn<Rendezvous, String> colStatut;
    @FXML private TableColumn<Rendezvous, Void> colOrdonnance;
    @FXML private TableColumn<Rendezvous, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;

    private ServiceRendezvous serviceRV = new ServiceRendezvous();
    private ServiceOrdonnance serviceOrd = new ServiceOrdonnance();
    private UserService userService = new UserService();
    private ObservableList<Rendezvous> rendezvousList = FXCollections.observableArrayList();
    private FilteredList<Rendezvous> filteredData;

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        
        colPatient.setCellValueFactory(cellData -> {
            int patientId = cellData.getValue().getPatientId();
            try {
                User patient = userService.getUserById(patientId);
                if (patient != null) {
                    return new SimpleStringProperty(patient.getNom() + " " + patient.getPrenom());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("Inconnu (" + patientId + ")");
        });
        
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        colStatut.setCellFactory(tc -> new TableCell<Rendezvous, String>() {
            private final ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(
                    "En attente", "Confirmé", "Annulé", "Terminé"
            ));

            {
                combo.setOnAction(e -> {
                    Rendezvous rv = getTableView().getItems().get(getIndex());
                    String newStatut = combo.getValue();
                    if (newStatut != null && !newStatut.equals(rv.getStatut())) {
                        rv.setStatut(newStatut);
                        try {
                            serviceRV.update(rv);
                            loadData(); 
                        } catch (SQLDataException ex) {
                            showAlert("Erreur", "Mise à jour du statut impossible", Alert.AlertType.ERROR);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Rendezvous rv = getTableView().getItems().get(getIndex());
                    combo.setValue(rv.getStatut());
                    setGraphic(combo);
                }
            }
        });

        colOrdonnance.setCellFactory(tc -> new TableCell<Rendezvous, Void>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Rendezvous rv = getTableView().getItems().get(getIndex());
                    Ordonnance ord = serviceOrd.getByConsultationId(rv.getId());
                    if (ord != null) {
                        consulterOrdonnance(ord, e);
                    } else {
                        creerOrdonnance(rv, e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Rendezvous rv = getTableView().getItems().get(getIndex());
                    Ordonnance ord = serviceOrd.getByConsultationId(rv.getId());
                    if (ord != null) {
                        btn.setText("Consulter");
                        btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
                    } else {
                        btn.setText("Créer ordonnance");
                        btn.setStyle("-fx-background-color: #826cfd; -fx-text-fill: white; -fx-cursor: hand;");
                    }
                    setGraphic(btn);
                }
            }
        });

        colActions.setCellFactory(tc -> new TableCell<Rendezvous, Void>() {
            private final Button btnModif = new Button("Modifier");
            private final Button btnSuppr = new Button("Supprimer");
            private final HBox pane = new HBox(10, btnModif, btnSuppr);

            {
                btnModif.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                btnSuppr.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                btnModif.setOnAction(e -> modifierRendezvous(getTableView().getItems().get(getIndex()), e));
                btnSuppr.setOnAction(e -> supprimerRendezvous(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        filterStatut.getItems().addAll("Tous", "En attente", "Confirmé", "Annulé", "Terminé");
        filterStatut.setValue("Tous");

        loadData();
        setupFilterAndSort();
    }

    private void loadData() {
        try {
            rendezvousList.clear();
            int userId = ManagerSession.getInstance().getCurrentUserId();
            List<Rendezvous> data = serviceRV.getByMedecinId(userId);
            rendezvousList.addAll(data);
        } catch (SQLDataException e) {
            showAlert("Erreur", "Base de données inaccessible", Alert.AlertType.ERROR);
        }
    }

    private void setupFilterAndSort() {
        filteredData = new FilteredList<>(rendezvousList, p -> true);

        searchField.textProperty().addListener((obs, old, val) -> updateFilter());
        filterStatut.valueProperty().addListener((obs, old, val) -> updateFilter());

        SortedList<Rendezvous> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableRDV.comparatorProperty());
        tableRDV.setItems(sortedData);
    }

    private void updateFilter() {
        filteredData.setPredicate(rv -> {
            String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            String patientName = "";
            try {
                User patient = userService.getUserById(rv.getPatientId());
                if (patient != null) {
                    patientName = (patient.getNom() + " " + patient.getPrenom()).toLowerCase();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            boolean matchesSearch = rv.getType().toLowerCase().contains(searchText) || patientName.contains(searchText);

            String stat = filterStatut.getValue();
            boolean matchesStatut = (stat == null || stat.equals("Tous") || rv.getStatut().equals(stat));

            return matchesSearch && matchesStatut;
        });
    }

    @FXML
    void handleSortDate(ActionEvent event) {
        tableRDV.getSortOrder().clear();
        colDate.setSortType(TableColumn.SortType.DESCENDING);
        tableRDV.getSortOrder().add(colDate);
    }

    @FXML
    void resetFilters(ActionEvent event) {
        searchField.clear();
        filterStatut.setValue("Tous");
    }

    private void modifierRendezvous(Rendezvous selected, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRDV.fxml"));
            Parent root = loader.load();
            AjouterRDV controller = loader.getController();
            controller.preparerModification(selected);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void supprimerRendezvous(Rendezvous selected) {
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

    private void consulterOrdonnance(Ordonnance ord, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrdonnanceCRUD.fxml"));
            Parent root = loader.load();
            AjouterOrdonnance controller = loader.getController();
            controller.preparerModification(ord); 
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void creerOrdonnance(Rendezvous rv, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrdonnanceCRUD.fxml"));
            Parent root = loader.load();
            AjouterOrdonnance controller = loader.getController();
            controller.preparerCreation(rv);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
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
        Parent root = FXMLLoader.load(getClass().getResource("/dashboard_patient.fxml"));
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
