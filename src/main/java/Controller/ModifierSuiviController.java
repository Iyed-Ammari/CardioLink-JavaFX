package Controller;

import com.cardiolink.Models.Suivi;
import com.cardiolink.Services.SuiviService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ModifierSuiviController {

    @FXML
    private ComboBox<String> cbType;

    @FXML
    private TextField txtValeur;

    @FXML
    private TextField txtUnite;

    @FXML
    private Label lblInfo;

    private final SuiviService suiviService = new SuiviService();
    private Suivi suivi;

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList(
                "Fréquence Cardiaque",
                "SpO2",
                "Température",
                "Glycémie",
                "Tension"
        ));
    }

    public void setSuivi(Suivi suivi) {
        this.suivi = suivi;
        cbType.setValue(suivi.getTypeDonnee());
        txtValeur.setText(String.valueOf(suivi.getValeur()));
        txtUnite.setText(suivi.getUnite());
    }

    @FXML
    public void modifierSuivi() {
        try {
            suivi.setTypeDonnee(cbType.getValue());
            suivi.setValeur(Float.parseFloat(txtValeur.getText()));
            suivi.setUnite(txtUnite.getText());

            suiviService.update(suivi);

            lblInfo.setText("Modification réussie.");
            fermerFenetre();

        } catch (Exception e) {
            lblInfo.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void fermerFenetre() {
        Stage stage = (Stage) cbType.getScene().getWindow();
        stage.close();
    }
}
