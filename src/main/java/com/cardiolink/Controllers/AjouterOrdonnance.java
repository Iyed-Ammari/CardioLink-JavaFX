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
        try {
            if (isUpdateMode) {
                ordToUpdate.setReference(txtReference.getText());
                ordToUpdate.setDiagnostic(txtDiagnostic.getText());
                ordToUpdate.setNotes(txtPrescription.getText());
                service.update(ordToUpdate);
            } else {
                Ordonnance newOrd = new Ordonnance(
                        txtReference.getText(),
                        LocalDateTime.now(),
                        0,
                        txtDiagnostic.getText(),
                        txtPrescription.getText(),
                        "Dr. Ahmed",
                        "Patient Test"
                );
                service.add(newOrd);
            }
            goToAfficher(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Parent root = FXMLLoader.load(getClass().getResource("/ListeOrdonnances.fxml"));
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
}