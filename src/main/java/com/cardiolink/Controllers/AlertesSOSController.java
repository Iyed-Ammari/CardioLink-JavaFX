package com.cardiolink.Controllers;

import com.cardiolink.Models.Intervention;
import com.cardiolink.Services.InterventionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AlertesSOSController {

    @FXML
    private Label lblResume;

    @FXML
    private VBox containerAlertes;

    private final InterventionService interventionService = new InterventionService();

    @FXML
    public void initialize() {
        chargerAlertesSOS();
    }

    private void chargerAlertesSOS() {
        try {
            List<Intervention> toutes = interventionService.getAll();

            List<Intervention> alertes = toutes.stream()
                    .filter(i -> i.getType() != null && i.getType().toLowerCase().contains("sos"))
                    .collect(Collectors.toList());

            lblResume.setText(alertes.size() + " intervention(s) d'urgence en attente d'attention immédiate.");

            containerAlertes.getChildren().clear();

            for (Intervention i : alertes) {
                VBox card = new VBox(6);
                card.setStyle("-fx-padding: 10 0 14 0;");

                Label type = new Label(i.getType());
                type.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

                Label descTitle = new Label("Description");
                descTitle.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

                Label desc = new Label(i.getDescription());
                desc.setWrapText(true);
                desc.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

                Label statutTitle = new Label("Statut");
                statutTitle.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

                Label statut = new Label(i.getStatut());
                statut.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");

                Label urgenceTitle = new Label("Urgence");
                urgenceTitle.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

                Label urgence = new Label("URGENT");
                urgence.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");

                Label origineTitle = new Label("Origine du déclenchement");
                origineTitle.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

                String origineTexte = "Intervention planifiée : " +
                        (i.getDatePlanifiee() != null
                                ? i.getDatePlanifiee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                : "-");

                Label origine = new Label(origineTexte);
                origine.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

                Hyperlink voirDetails = new Hyperlink("Voir les détails");
                voirDetails.setStyle("-fx-text-fill: #2f5fff; -fx-font-size: 15px;");
                voirDetails.setOnAction(e -> afficherDetails(i));

                Separator separator = new Separator();

                card.getChildren().addAll(
                        type,
                        descTitle,
                        desc,
                        statutTitle,
                        statut,
                        urgenceTitle,
                        urgence,
                        origineTitle,
                        origine,
                        voirDetails,
                        separator
                );

                containerAlertes.getChildren().add(card);
            }

        } catch (Exception e) {
            lblResume.setText("Erreur de chargement des alertes SOS.");
            e.printStackTrace();
        }
    }

    private void afficherDetails(Intervention intervention) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'alerte SOS");
        alert.setHeaderText(intervention.getType());
        alert.setContentText(
                "Description : " + intervention.getDescription() + "\n\n" +
                        "Statut : " + intervention.getStatut() + "\n" +
                        "Médecin ID : " + intervention.getMedecinId() + "\n" +
                        "Date planifiée : " +
                        (intervention.getDatePlanifiee() != null
                                ? intervention.getDatePlanifiee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                : "-")
        );
        alert.showAndWait();
    }
}