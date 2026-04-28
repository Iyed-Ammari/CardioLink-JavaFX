package com.cardiolink.Controllers;

public class PredictionDayPeak {

    private String date;
    private double prediction;

    public PredictionDayPeak() {
    }

    public PredictionDayPeak(String date, double prediction) {
        this.date = date;
        this.prediction = prediction;
    }

    public String getDate() {
        return date;
    }

    public double getPrediction() {
        return prediction;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPrediction(double prediction) {
        this.prediction = prediction;
    }
}