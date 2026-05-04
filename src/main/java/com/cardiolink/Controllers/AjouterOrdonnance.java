package com.cardiolink.Controllers;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Services.ServiceOrdonnance;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDateTime;

public class AjouterOrdonnance {

    @FXML private TextField txtReference;
    @FXML private TextArea txtDiagnostic, txtPrescription;
    @FXML private Label lblPreviewRef, lblPreviewDiag, lblPreviewPrescr;
    @FXML private Button btnSave;

    private ServiceOrdonnance service = new ServiceOrdonnance();
    private boolean isUpdateMode = false;
    private Ordonnance ordToUpdate;

    @FXML
    public void initialize() {
        txtReference.textProperty().addListener((obs, old, val) ->
                lblPreviewRef.setText("REF: " + (val.isEmpty() ? "----" : val.toUpperCase())));
        txtDiagnostic.textProperty().addListener((obs, old, val) ->
                lblPreviewDiag.setText(val.isEmpty() ? "En attente..." : val));
        txtPrescription.textProperty().addListener((obs, old, val) ->
                lblPreviewPrescr.setText(val.isEmpty() ? "..." : val));
    }

    @FXML
    void handleSave(ActionEvent event) {
        String ref = txtReference.getText();
        String diag = txtDiagnostic.getText();
        String prescr = txtPrescription.getText();

        if (ref.isEmpty() || diag.isEmpty()) {
            showError("Veuillez remplir les champs obligatoires.");
            return;
        }

        try {
            if (isUpdateMode) {
                // ... logique update ...
            } else {
                // CORRECTION : S'assurer que l'ID de consultation 1 existe ou en trouver un autre
                // Pour le test, on va supposer que vous avez lié l'ordonnance à un RDV existant
                Ordonnance ord = new Ordonnance(
                        ref,
                        LocalDateTime.now(),
                        4, // Assurez-vous que cet ID existe dans la table rendez_vous !
                        diag,
                        prescr,
                        "Dr. Ahmed", // Devrait idéalement venir de la session utilisateur
                        "Patient Test"
                );

                service.add(ord);
                showSuccess("Ordonnance enregistrée avec succès !");
                goToAfficher(event);
            }
        } catch (Exception e) {
            // Cela affichera l'erreur précise dans l'interface en cas de problème SQL
            showError("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    void clearFields(ActionEvent event) { // <-- CORRIGÉ : Paramètre ajouté
        txtReference.clear();
        txtDiagnostic.clear();
        txtPrescription.clear();
        txtReference.setEditable(true);
        isUpdateMode = false;
        if (btnSave != null) btnSave.setText("Générer & Enregistrer");
    }

    @FXML
    void goToAfficher(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/OrdonnanceMedecin.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    public void preparerModification(Ordonnance ord) {
        this.isUpdateMode = true;
        this.ordToUpdate = ord;
        txtReference.setText(ord.getReference());
        txtReference.setEditable(false);
        txtDiagnostic.setText(ord.getDiagnostic());
        txtPrescription.setText(ord.getNotes());
        if (btnSave != null) btnSave.setText("Mettre à jour");
    }
    @FXML
    void goToMenu(ActionEvent event) {
        try {
            // Chargement de la page MenuRDV
            Parent root = FXMLLoader.load(getClass().getResource("/ListeOrdonnances.fxml"));

            // Récupération de la fenêtre actuelle (Stage)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Changement de la scène
            stage.setScene(new Scene(root));
            stage.setTitle("CardioLink - Menu Principal");
            stage.show();
        } catch (IOException e) {
            showError("Impossible de charger le menu : " + e.getMessage());
            e.printStackTrace();
        }
    }
}