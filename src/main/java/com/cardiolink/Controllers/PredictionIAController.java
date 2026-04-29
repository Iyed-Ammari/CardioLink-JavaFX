package com.cardiolink.Controllers;

import com.cardiolink.Models.User;
import com.cardiolink.Services.PredictionService;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import com.cardiolink.utils.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PredictionIAController implements Initializable {

    @FXML private ComboBox<String> monthBox;

    @FXML private Label maxLabel;
    @FXML private Label countLabel;
    @FXML private Label monthLabel;
    @FXML private Label messageLabel;

    @FXML private Label heroPeriodLabel;
    @FXML private Label futureTagLabel;
    @FXML private Label resultCountBadge;
    @FXML private Label infoPillLabel;
    @FXML private Label heroModelBadge;

    @FXML private VBox resultsContainer;

    private final PredictionService predictionService = new PredictionService();
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        int userId = ManagerSession.getInstance().getCurrentUserId();

        try {
            User user = userService.getById(userId);
            System.out.println(user);
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        initialiserMois();
        initialiserValeursParDefaut();
    }

    private void initialiserMois() {
        if (monthBox == null) return;

        List<String> months = new ArrayList<>();

        // Commencer au mois prochain (pas le mois actuel, on prédit le futur)
        LocalDate today = LocalDate.now();
        LocalDate debut = today.withDayOfMonth(1).plusMonths(1); // mois prochain
        LocalDate fin   = today.withDayOfMonth(1).plusYears(5);  // 5 ans dans le futur

        LocalDate cursor = debut;
        while (!cursor.isAfter(fin)) {
            months.add(String.format("%d-%02d", cursor.getYear(), cursor.getMonthValue()));
            cursor = cursor.plusMonths(1);
        }

        monthBox.setItems(FXCollections.observableArrayList(months));

        // Valeur par défaut = le mois prochain
        String defaultMonth = String.format("%d-%02d", debut.getYear(), debut.getMonthValue());
        monthBox.setValue(defaultMonth);
    }

    private void initialiserValeursParDefaut() {
        // Mois par défaut = mois prochain
        LocalDate debut = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        String defaultMonth = String.format("%d-%02d", debut.getYear(), debut.getMonthValue());

        if (maxLabel != null) maxLabel.setText("0.00");
        if (countLabel != null) countLabel.setText("0");
        if (monthLabel != null) monthLabel.setText("-");
        if (heroPeriodLabel != null) heroPeriodLabel.setText("📅 " + defaultMonth);
        if (futureTagLabel != null) {
            futureTagLabel.setText("FUTUR");
            futureTagLabel.setVisible(true);
            futureTagLabel.setManaged(true);
        }
        if (resultCountBadge != null) resultCountBadge.setText("0 jour");
        if (infoPillLabel != null)  { infoPillLabel.setText("");  infoPillLabel.setVisible(false);  infoPillLabel.setManaged(false); }
        if (heroModelBadge != null) { heroModelBadge.setText(""); heroModelBadge.setVisible(false); heroModelBadge.setManaged(false); }

        if (messageLabel != null) {
            messageLabel.setText("Sélectionnez un mois puis cliquez sur « Lancer la prédiction ».");
            messageLabel.setStyle(
                    "-fx-text-fill: #64748B;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-background-color: rgba(47,96,245,0.05);" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 10 14;"
            );
        }

        if (resultsContainer != null) {
            resultsContainer.getChildren().clear();
        }

        updateFutureBadge(defaultMonth);
    }

    @FXML
    private void handlePredict() {
        String selectedMonth = monthBox != null ? monthBox.getValue() : null;

        if (selectedMonth == null || selectedMonth.isBlank()) {
            afficherErreur("Veuillez choisir un mois.");
            return;
        }

        // Bloquer si le mois sélectionné est dans le passé ou le mois actuel
        try {
            LocalDate today = LocalDate.now().withDayOfMonth(1);
            LocalDate selected = LocalDate.parse(selectedMonth + "-01");
            if (!selected.isAfter(today)) {
                afficherErreur("⚠ Veuillez sélectionner un mois futur pour la prédiction.");
                return;
            }
        } catch (Exception ignored) {}

        if (monthLabel != null) monthLabel.setText(selectedMonth);
        if (heroPeriodLabel != null) heroPeriodLabel.setText("📅 " + selectedMonth);
        updateFutureBadge(selectedMonth);

        if (resultsContainer != null) resultsContainer.getChildren().clear();

        try {
            PredictionResponse response = predictionService.predict(selectedMonth);
            List<PredictionDayPeak> jours = response.getTopJoursPic();

            if (jours == null || jours.isEmpty()) {
                if (maxLabel != null) maxLabel.setText("0.00");
                if (countLabel != null) countLabel.setText("0");
                if (resultCountBadge != null) resultCountBadge.setText("0 jour");
                afficherErreur("Aucune donnée de pic retournée par l'API.");
                return;
            }

            double max = 0.0;
            for (PredictionDayPeak jour : jours) {
                if (jour.getPrediction() > max) {
                    max = jour.getPrediction();
                }
            }

            if (maxLabel != null) maxLabel.setText(String.format(Locale.US, "%.2f", max));
            if (countLabel != null) countLabel.setText(String.valueOf(jours.size()));
            if (resultCountBadge != null) {
                resultCountBadge.setText(jours.size() + (jours.size() > 1 ? " jours" : " jour"));
            }

            if (monthLabel != null && response.getMois() != null && !response.getMois().isBlank()) {
                monthLabel.setText(response.getMois());
            }

            int rank = 1;
            for (PredictionDayPeak jour : jours) {
                resultsContainer.getChildren().add(creerCarteJour(rank++, jour, max));
            }

            afficherSucces("Prédiction chargée avec succès.");

        } catch (Exception e) {
            afficherErreur("Erreur lors de la prédiction : " + e.getMessage());
        }
    }

    private void updateFutureBadge(String month) {
        if (futureTagLabel == null) return;

        try {
            // Comparer avec le mois actuel (pas juste l'année)
            LocalDate today = LocalDate.now().withDayOfMonth(1);
            LocalDate selected = LocalDate.parse(month + "-01");
            boolean futur = selected.isAfter(today);

            futureTagLabel.setVisible(futur);
            futureTagLabel.setManaged(futur);
        } catch (Exception e) {
            futureTagLabel.setVisible(false);
            futureTagLabel.setManaged(false);
        }
    }

    private HBox creerCarteJour(int rank, PredictionDayPeak jour, double max) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(15,23,42,0.08);" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-width: 1.5;"
        );

        StackPane rankPane = new StackPane();
        rankPane.setPrefSize(42, 42);
        rankPane.setMinSize(42, 42);
        rankPane.setMaxSize(42, 42);
        rankPane.setStyle(getRankStyle(rank));

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        rankPane.getChildren().add(rankLabel);

        VBox infoBox = new VBox(6);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label dateLabel = new Label("🗓 " + jour.getDate());
        dateLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 17px; -fx-font-weight: 900;");

        boolean weekend = isWeekend(jour.getDate());
        Label dayBadge = new Label((weekend ? "🏖 " : "💼 ") + getJourFrancais(jour.getDate()));
        dayBadge.setStyle(
                (weekend
                        ? "-fx-background-color: rgba(248,34,57,0.08); -fx-text-fill: #991B1B;"
                        : "-fx-background-color: rgba(47,96,245,0.08); -fx-text-fill: #1D4ED8;")
                        + "-fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 4 10; -fx-background-radius: 999;"
        );

        infoBox.getChildren().addAll(dateLabel, dayBadge);

        VBox valueBox = new VBox(8);
        valueBox.setAlignment(Pos.CENTER_RIGHT);
        valueBox.setPrefWidth(300);

        Label valueLabel = new Label(String.format(Locale.US, "%.4f", jour.getPrediction()));
        valueLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 18px; -fx-font-weight: 900;");

        double pct = max > 0 ? (jour.getPrediction() / max) * 100.0 : 0.0;

        StackPane barTrack = new StackPane();
        barTrack.setPrefWidth(220);
        barTrack.setPrefHeight(10);
        barTrack.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 999;");

        Region barFill = new Region();
        barFill.setPrefHeight(10);
        barFill.setPrefWidth(Math.max(10, 220 * pct / 100.0));
        barFill.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-background-radius: 999;");
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);

        barTrack.getChildren().add(barFill);

        Label pctLabel = new Label(String.format(Locale.US, "%.0f%%", pct));
        pctLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px; -fx-font-weight: 800;");

        valueBox.getChildren().addAll(valueLabel, barTrack, pctLabel);

        card.getChildren().addAll(rankPane, infoBox, valueBox);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(47,96,245,0.20);" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 18, 0.15, 0, 4);"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(15,23,42,0.08);" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-width: 1.5;"
        ));

        return card;
    }

    private boolean isWeekend(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            int dow = date.getDayOfWeek().getValue();
            return dow >= 6;
        } catch (Exception e) {
            return false;
        }
    }

    private String getRankStyle(int rank) {
        return switch (rank) {
            case 1 -> "-fx-background-color: linear-gradient(to right, #F59E0B, #D97706); -fx-background-radius: 14;";
            case 2 -> "-fx-background-color: linear-gradient(to right, #6B7280, #94A3B8); -fx-background-radius: 14;";
            case 3 -> "-fx-background-color: linear-gradient(to right, #B45309, #92400E); -fx-background-radius: 14;";
            default -> "-fx-background-color: linear-gradient(to right, #2F60F5, #1D4ED8); -fx-background-radius: 14;";
        };
    }

    private String getJourFrancais(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            String day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
            return day.substring(0, 1).toUpperCase() + day.substring(1);
        } catch (Exception e) {
            return "-";
        }
    }

    private void afficherErreur(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle(
                    "-fx-text-fill: #B42318;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-background-color: rgba(248,34,57,0.06);" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 10 14;"
            );
        }
    }

    private void afficherSucces(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle(
                    "-fx-text-fill: #0F766E;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-background-color: rgba(16,185,129,0.10);" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 10 14;"
            );
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            Stage stage = (Stage) monthBox.getScene().getWindow();
            NavigationUtil.navigate(stage, "/fxml/admin/dashboard-admin.fxml");
        } catch (IOException e) {
            afficherErreur("Navigation impossible vers Dashboard.");
        }
    }

    @FXML
    private void goToProduits() {
        try {
            Stage stage = (Stage) monthBox.getScene().getWindow();
            NavigationUtil.navigate(stage, "/fxml/admin/produit-list-admin.fxml");
        } catch (IOException e) {
            afficherErreur("Navigation impossible vers Produits.");
        }
    }

    @FXML
    private void goToCommandes() {
        try {
            Stage stage = (Stage) monthBox.getScene().getWindow();
            NavigationUtil.navigate(stage, "/fxml/admin/commande-list-admin.fxml");
        } catch (IOException e) {
            afficherErreur("Navigation impossible vers Commandes.");
        }
    }

    @FXML
    private void goToPredictionIA() {
        // page actuelle
    }
}