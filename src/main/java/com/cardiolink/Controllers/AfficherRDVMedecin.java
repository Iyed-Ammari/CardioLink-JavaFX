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

public class AfficherRDVMedecin implements UserAwareController {

    @FXML private Label avatarLabel;
    @FXML private javafx.scene.shape.Circle avatarCircle;

    @FXML private TableView<Rendezvous> tableRDV;
    @FXML private TableColumn<Rendezvous, LocalDateTime> colDate;
    @FXML private TableColumn<Rendezvous, String> colPatient;
    @FXML private TableColumn<Rendezvous, String> colType;
    @FXML private TableColumn<Rendezvous, String> colStatut;
    @FXML private TableColumn<Rendezvous, Void> colOrdonnance;
    @FXML private TableColumn<Rendezvous, Void> colActions;
    @FXML private TableColumn<Rendezvous, Void> colLienVisio;

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
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Rendezvous rv = getTableView().getItems().get(getIndex());
                    setText(rv.getStatut());
                    
                    // Style basé sur le statut
                    getStyleClass().removeAll("statut-attente", "statut-confirme", "statut-annule", "statut-termine");
                    if ("En attente".equals(rv.getStatut())) getStyleClass().add("statut-attente");
                    else if ("Confirmé".equals(rv.getStatut())) getStyleClass().add("statut-confirme");
                    else if ("Annulé".equals(rv.getStatut())) getStyleClass().add("statut-annule");
                    else if ("Terminé".equals(rv.getStatut())) getStyleClass().add("statut-termine");
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
                        btn.getStyleClass().setAll("button", "button-in-table", "success-button");
                    } else {
                        btn.setText("Créer ordonnance");
                        btn.getStyleClass().setAll("button", "button-in-table", "primary-button");
                    }
                    setGraphic(btn);
                }
            }
        });

        // Suppression de la colonne actions pour les médecins (seuls les patients modifient)
        tableRDV.getColumns().remove(colActions);

        colLienVisio.setCellFactory(tc -> new TableCell<Rendezvous, Void>() {
            private final Button btnVisio = new Button("Rejoindre Visio");
            {
                btnVisio.getStyleClass().addAll("button-in-table", "info-button");
                btnVisio.setOnAction(e -> {
                    Rendezvous rv = getTableView().getItems().get(getIndex());
                    if (rv.getLienVisio() != null && !rv.getLienVisio().isEmpty()) {
                        openUrl(rv.getLienVisio());
                    } else {
                        showAlert("Info", "Aucun lien de visio disponible pour ce rendez-vous.", Alert.AlertType.INFORMATION);
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
                    if ("Téléconsultation".equals(rv.getType())) {
                        setGraphic(btnVisio);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        filterStatut.getItems().addAll("Tous", "En attente", "Confirmé", "Annulé", "Terminé");
        filterStatut.setValue("Tous");

        loadData();
        setupFilterAndSort();

        // Initialisation de l'utilisateur pour la navbar
        setCurrentUser(ManagerSession.getInstance().getCurrentUser());
    }

    @Override
    public void setCurrentUser(com.cardiolink.Models.User user) {
        if (user != null && avatarLabel != null) {
            String initial = user.getNom() != null && !user.getNom().isEmpty()
                    ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
            avatarLabel.setText(initial);
        }
    }

    // --- NAVIGATION METHODS FOR NAVBAR ---
    @FXML void goHome(ActionEvent event) throws IOException {
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), "/dashboard_patient.fxml");
    }
    @FXML void goCommunity(ActionEvent event) throws IOException {
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), "/post_view.fxml");
    }
    @FXML void goSuivis(ActionEvent event) throws IOException {
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), "/AjouterSuivi.fxml");
    }
    @FXML void goRDV(ActionEvent event) throws IOException {
        com.cardiolink.Models.User user = com.cardiolink.utils.ManagerSession.getInstance().getCurrentUser();
        String path = (user != null && "ROLE_MEDECIN".equals(user.getRoleClean())) 
                ? "/AfficherRDVMedecin.fxml" 
                : "/AfficherRDV.fxml";
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), path);
    }
    @FXML void goDossier(ActionEvent event) throws IOException {
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), "/dossier_medical.fxml");
    }
    @FXML void goProfil(ActionEvent event) throws IOException {
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), "/profil_patient.fxml");
    }
    @FXML void handleLogout(ActionEvent event) throws IOException {
        com.cardiolink.utils.ManagerSession.getInstance().logout();
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), "/login.fxml");
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

    @FXML
    void showPrediction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PredictionRDV.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de prédiction.", Alert.AlertType.ERROR);
        }
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
    void syncWithGoogleCalendar(ActionEvent event) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Synchronisation Google Calendar");
        infoAlert.setHeaderText("Synchronisation en cours...");
        infoAlert.setContentText("Veuillez patienter pendant que vos rendez-vous sont synchronisés. Si c'est la première fois, une fenêtre de navigateur va s'ouvrir pour vous demander l'autorisation.");
        infoAlert.show();

        // Exécuter la synchronisation en arrière-plan pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                com.cardiolink.Services.GoogleCalendarService calendarService = new com.cardiolink.Services.GoogleCalendarService();
                
                if (!calendarService.isInitialized()) {
                    javafx.application.Platform.runLater(() -> {
                        infoAlert.close();
                        showAlert("Erreur d'initialisation", calendarService.getInitError(), Alert.AlertType.ERROR);
                    });
                    return;
                }

                int successCount = 0;
                int failCount = 0;

                for (Rendezvous rv : rendezvousList) {
                    try {
                        String patientName = "Inconnu";
                        User patient = userService.getUserById(rv.getPatientId());
                        if (patient != null) {
                            patientName = patient.getNom() + " " + patient.getPrenom();
                        }
                        calendarService.pushRendezvousToCalendar(rv, patientName);
                        successCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        failCount++;
                    }
                }

                final int s = successCount;
                final int f = failCount;
                javafx.application.Platform.runLater(() -> {
                    infoAlert.close();
                    showAlert("Synchronisation Terminée", s + " rendez-vous synchronisés.\n" + f + " échecs.", Alert.AlertType.INFORMATION);
                    
                    // Ouvrir Google Calendar après la synchronisation (en vue Mois pour voir les événements)
                    try {
                        String url = "https://calendar.google.com/calendar/r/month";
                        if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                        } else {
                            String os = System.getProperty("os.name").toLowerCase();
                            if (os.contains("win")) {
                                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                            } else if (os.contains("mac")) {
                                Runtime.getRuntime().exec(new String[]{"open", url});
                            } else if (os.contains("nix") || os.contains("nux")) {
                                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    infoAlert.close();
                    showAlert("Erreur", "Une erreur est survenue lors de la synchronisation : " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    void goToMenu(ActionEvent event) throws IOException {
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), "/dashboard_patient.fxml");
    }

    private void openUrl(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                } else if (os.contains("nix") || os.contains("nux")) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le lien : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
