package com.cardiolink.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MapViewController {

    @FXML
    private WebView webView;

    @FXML
    private Label lblTitre;

    public void setLocation(Double latitude, Double longitude, String titreIntervention) {
        try {
            System.out.println("setLocation appelé -> lat=" + latitude + ", lng=" + longitude + ", titre=" + titreIntervention);

            if (latitude == null || longitude == null) {
                if (lblTitre != null) {
                    lblTitre.setText("Aucune localisation disponible");
                }
                return;
            }

            if (lblTitre != null) {
                lblTitre.setText("Localisation - " + titreIntervention);
            }

            WebEngine webEngine = webView.getEngine();

            String url = "https://www.openstreetmap.org/export/embed.html?bbox="
                    + (longitude - 0.01) + "%2C" + (latitude - 0.01) + "%2C"
                    + (longitude + 0.01) + "%2C" + (latitude + 0.01)
                    + "&layer=mapnik&marker=" + latitude + "%2C" + longitude;

            System.out.println("Chargement URL carte : " + url);
            webEngine.load(url);

        } catch (Exception e) {
            e.printStackTrace();
            if (lblTitre != null) {
                lblTitre.setText("Erreur chargement carte : " + e.getMessage());
            }
        }
    }
}