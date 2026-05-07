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

public class PredictionRDVController {

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
