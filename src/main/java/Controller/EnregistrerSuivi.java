package Controller;

import com.cardiolink.Models.Suivi;
import com.cardiolink.Services.SuiviService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EnregistrerSuivi {

    @FXML
    private ComboBox<String> cbTypeDonnee;

    @FXML
    private TextField txtValeur;

    @FXML
    private Label lblInfo;

    private final SuiviService suiviService = new SuiviService();

    @FXML
    public void initialize() {
        cbTypeDonnee.setItems(FXCollections.observableArrayList(
                "Fréquence Cardiaque",
                "SpO2",
                "Température",
                "Glycémie",
                "Tension"
        ));
    }

    @FXML
    public void enregistrerSuivi() {
        try {
            String type = cbTypeDonnee.getValue();
            String valeurText = txtValeur.getText();

            if (type == null || valeurText == null || valeurText.isEmpty()) {
                lblInfo.setText("Veuillez remplir le type et la valeur.");
                lblInfo.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px;");
                return;
            }

            float valeur = Float.parseFloat(valeurText);

            String unite = determinerUnite(type);

            int patientId = 4; // temporaire pour test

            Suivi suivi = new Suivi(0, type, valeur, unite, patientId);
            suiviService.add(suivi);

            lblInfo.setText("Suivi enregistré avec succès. Urgence : " + suivi.getNiveauUrgence());
            lblInfo.setStyle("-fx-text-fill: #7CFC98; -fx-font-size: 14px;");

            fermerFenetre();

        } catch (NumberFormatException e) {
            lblInfo.setText("Veuillez entrer une valeur numérique valide.");
            lblInfo.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px;");
        } catch (Exception e) {
            lblInfo.setText("Erreur lors de l'enregistrement.");
            lblInfo.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px;");
            e.printStackTrace();
        }
    }

    private String determinerUnite(String type) {
        switch (type) {
            case "Fréquence Cardiaque":
                return "bpm";
            case "SpO2":
                return "%";
            case "Température":
                return "°C";
            case "Glycémie":
                return "mg/dL";
            case "Tension":
                return "mmHg";
            default:
                return "";
        }
    }

    @FXML
    public void fermerFenetre() {
        Stage stage = (Stage) cbTypeDonnee.getScene().getWindow();
        stage.close();
    }
}