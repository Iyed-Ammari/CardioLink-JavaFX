package com.cardiolink.Controllers;

import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Services.ServiceRendezvous;
import com.cardiolink.utils.ManagerSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLDataException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PredictionRDVController implements UserAwareController {

    @FXML private Label avatarLabel;
    @FXML private javafx.scene.shape.Circle avatarCircle;

    @FXML private Label lblAvg;
    @FXML private Label lblGrowth;
    @FXML private Label lblPredicted;
    @FXML private LineChart<String, Number> chartPrediction;

    private ServiceRendezvous serviceRV = new ServiceRendezvous();

    @FXML
    public void initialize() {
        try {
            int medecinId = ManagerSession.getInstance().getCurrentUserId();
            List<Rendezvous> allRDV = serviceRV.getByMedecinId(medecinId);
            
            generatePredictions(allRDV);
        } catch (SQLDataException e) {
            e.printStackTrace();
        }

        // Initialisation de l'utilisateur pour la navbar
        setCurrentUser(com.cardiolink.utils.ManagerSession.getInstance().getCurrentUser());
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

    private void generatePredictions(List<Rendezvous> allRDV) {
        if (allRDV.isEmpty()) return;

        // 1. Group by month (Year-Month)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);
        Map<LocalDate, Long> countsByMonth = allRDV.stream()
                .collect(Collectors.groupingBy(
                        rv -> rv.getDateHeure().toLocalDate().withDayOfMonth(1),
                        TreeMap::new,
                        Collectors.counting()
                ));

        // 2. Prepare data for Regression
        List<LocalDate> months = new ArrayList<>(countsByMonth.keySet());
        int n = months.size();
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        XYChart.Series<String, Number> realSeries = new XYChart.Series<>();
        realSeries.setName("Données Réelles");

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = countsByMonth.get(months.get(i));
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            
            realSeries.getData().add(new XYChart.Data<>(months.get(i).format(formatter), y));
        }

        // 3. Simple Linear Regression
        double slope = 0;
        double intercept = sumY / n;
        
        if (n > 1) {
            slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            intercept = (sumY - slope * sumX) / n;
        }

        // 4. Predict next 3 months
        XYChart.Series<String, Number> predictionSeries = new XYChart.Series<>();
        predictionSeries.setName("Prédictions");
        
        // Add last real point to prediction to connect the lines
        if (n > 0) {
            predictionSeries.getData().add(new XYChart.Data<>(months.get(n-1).format(formatter), countsByMonth.get(months.get(n-1))));
        }

        double nextPrediction = 0;
        for (int i = n; i < n + 3; i++) {
            double predictedY = Math.max(0, slope * i + intercept);
            if (i == n) nextPrediction = predictedY;
            
            LocalDate nextMonth = months.get(n-1).plusMonths(i - n + 1);
            predictionSeries.getData().add(new XYChart.Data<>(nextMonth.format(formatter), predictedY));
        }

        // 5. Update UI
        chartPrediction.getData().addAll(realSeries, predictionSeries);
        
        lblAvg.setText(String.format("%.1f", sumY / n));
        lblPredicted.setText(String.valueOf((int) Math.round(nextPrediction)));
        
        if (slope > 0) {
            lblGrowth.setText(String.format("+%.1f%%", (slope / (sumY/n)) * 100));
            lblGrowth.setStyle("-fx-text-fill: #10b981;");
        } else {
            lblGrowth.setText(String.format("%.1f%%", (slope / (sumY/n)) * 100));
            lblGrowth.setStyle("-fx-text-fill: #f43f5e;");
        }
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AfficherRDVMedecin.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
