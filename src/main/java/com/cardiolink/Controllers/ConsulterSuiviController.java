package com.cardiolink.Controllers;

import com.cardiolink.Models.Suivi;
import com.cardiolink.Services.SuiviService;
import com.cardiolink.utils.ManagerSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConsulterSuiviController {

    @FXML
    private TextField txtRecherche;

    @FXML
    private TableView<Suivi> tableSuivis;

    @FXML
    private TableColumn<Suivi, String> colType;

    @FXML
    private TableColumn<Suivi, String> colValeur;

    @FXML
    private TableColumn<Suivi, String> colDate;

    @FXML
    private TableColumn<Suivi, String> colUrgence;

    @FXML
    private TableColumn<Suivi, Void> colActions;

    @FXML
    private Label lblMessage;

    private final SuiviService suiviService = new SuiviService();
    private final ObservableList<Suivi> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerSuivis();
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

        colUrgence.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String urgence, boolean empty) {
                super.updateItem(urgence, empty);

                if (empty || urgence == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(urgence);
                badge.setStyle(styleUrgence(urgence));
                setGraphic(badge);
                setText(null);
            }
        });

        ajouterBoutonsActions();
    }

    private String styleUrgence(String urgence) {
        return switch (urgence) {
            case "Normal" -> "-fx-background-color: #dff3e6; -fx-text-fill: #26a65b; -fx-padding: 8 18; -fx-background-radius: 10;";
            case "Stable" -> "-fx-background-color: #f7eadf; -fx-text-fill: #ff7a00; -fx-padding: 8 18; -fx-background-radius: 10;";
            case "Critique" -> "-fx-background-color: #f8dede; -fx-text-fill: #e74c3c; -fx-padding: 8 18; -fx-background-radius: 10;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-padding: 8 18; -fx-background-radius: 10;";
        };
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁");
            private final Button btnModifier = new Button("✏");
            private final Button btnSupprimer = new Button("🗑");
            private final Button btnArchiver = new Button("📦");
            private final HBox box = new HBox(8, btnVoir, btnModifier, btnSupprimer, btnArchiver);

            {
                btnVoir.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
                btnModifier.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: linear-gradient(to right, #d9416c, #5b6fff); -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
                btnArchiver.setStyle("-fx-background-color: linear-gradient(to right, #ff9f43, #f6c667); -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");

                btnVoir.setOnAction(event -> {
                    Suivi suivi = getTableView().getItems().get(getIndex());
                    afficherDetails(suivi);
                });

                btnModifier.setOnAction(event -> {
                    Suivi suivi = getTableView().getItems().get(getIndex());
                    ouvrirFenetreModification(suivi);
                });

                btnSupprimer.setOnAction(event -> {
                    Suivi suivi = getTableView().getItems().get(getIndex());
                    supprimerSuivi(suivi);
                });

                btnArchiver.setOnAction(event -> {
                    Suivi suivi = getTableView().getItems().get(getIndex());
                    archiverSuivi(suivi);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void chargerSuivis() {
        try {
            int patientId = ManagerSession.getInstance().getCurrentUser().getId();
            List<Suivi> suivis = suiviService.getByPatientId(patientId);
            data.setAll(suivis);
            tableSuivis.setItems(data);
            lblMessage.setText("Nombre de suivis actifs : " + suivis.size());
        } catch (Exception e) {
            lblMessage.setText("Erreur de chargement des suivis : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void rechercherSuivis() {
        try {
            int patientId = ManagerSession.getInstance().getCurrentUser().getId();
            String recherche = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim().toLowerCase();

            List<Suivi> suivis = suiviService.getByPatientId(patientId);

            if (recherche.isEmpty()) {
                data.setAll(suivis);
            } else {
                List<Suivi> filtres = suivis.stream()
                        .filter(s -> s.getTypeDonnee() != null &&
                                s.getTypeDonnee().toLowerCase().contains(recherche))
                        .collect(Collectors.toList());

                data.setAll(filtres);
            }

            tableSuivis.setItems(data);
            lblMessage.setText("Résultats : " + data.size());

        } catch (Exception e) {
            lblMessage.setText("Erreur lors de la recherche.");
            e.printStackTrace();
        }
    }

    private void afficherDetails(Suivi suivi) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du suivi");
        alert.setHeaderText("Informations du suivi ID " + suivi.getId());
        alert.setContentText(
                "Type : " + suivi.getTypeDonnee() + "\n" +
                        "Valeur : " + suivi.getValeur() + " " + suivi.getUnite() + "\n" +
                        "Urgence : " + suivi.getNiveauUrgence() + "\n" +
                        "Patient ID : " + suivi.getPatientId() + "\n" +
                        "Date : " + (suivi.getDateSaisie() != null
                        ? suivi.getDateSaisie().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "-")
        );
        alert.showAndWait();
    }

    private void ouvrirFenetreModification(Suivi suivi) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierSuivi.fxml"));
            Scene scene = new Scene(loader.load());

            ModifierSuiviController controller = loader.getController();
            controller.setSuivi(suivi);

            Stage stage = new Stage();
            stage.setTitle("Modifier Suivi");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerSuivis();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerSuivi(Suivi suivi) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le suivi");
        alert.setContentText("Voulez-vous vraiment supprimer ce suivi ?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                suiviService.delete(suivi);
                chargerSuivis();
                lblMessage.setText("Suivi supprimé avec succès.");
            } catch (Exception e) {
                lblMessage.setText("Erreur suppression : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void archiverSuivi(Suivi suivi) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Archivage");
            confirmation.setHeaderText("Archiver le suivi");
            confirmation.setContentText("Voulez-vous vraiment archiver ce suivi ?");

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                suiviService.archive(suivi);
                lblMessage.setText("Suivi archivé avec succès.");
                chargerSuivis();
            }
        } catch (Exception e) {
            lblMessage.setText("Erreur archivage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirArchives() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SuiviArchive.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Suivis Archivés");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture archives : " + e.getMessage());
            e.printStackTrace();
        }
    }
}