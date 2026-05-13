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

public class AjouterOrdonnance implements UserAwareController {

    @FXML private Label avatarLabel;
    @FXML private javafx.scene.shape.Circle avatarCircle;

    @FXML private TextField txtReference;
    @FXML private TextArea txtDiagnostic, txtPrescription;
    @FXML private Label lblPreviewRef, lblPreviewDiag, lblPreviewPrescr;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnPdf;

    private ServiceOrdonnance service = new ServiceOrdonnance();
    private boolean isUpdateMode = false;
    private Ordonnance ordToUpdate;
    private int currentConsultationId;
    private String currentPatientNom = "Inconnu";
    private String currentMedecinNom = "Inconnu";

    @FXML
    public void initialize() {
        txtReference.textProperty().addListener((obs, old, val) ->
                lblPreviewRef.setText("REF: " + (val.isEmpty() ? "----" : val.toUpperCase())));
        txtDiagnostic.textProperty().addListener((obs, old, val) ->
                lblPreviewDiag.setText(val.isEmpty() ? "En attente..." : val));
        txtPrescription.textProperty().addListener((obs, old, val) ->
                lblPreviewPrescr.setText(val.isEmpty() ? "..." : val));

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
                Ordonnance ord = new Ordonnance(
                        ref,
                        LocalDateTime.now(),
                        currentConsultationId,
                        diag,
                        prescr,
                        currentMedecinNom,
                        currentPatientNom
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
    void clearFields(ActionEvent event) {
        txtDiagnostic.clear();
        txtPrescription.clear();
        txtReference.setEditable(true);
        isUpdateMode = false;
        if (btnSave != null) btnSave.setText("Générer & Enregistrer");
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (ordToUpdate != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer cette ordonnance ?");
            alert.setContentText("Cette action est irréversible.");

            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(ordToUpdate);
                    showSuccess("Ordonnance supprimée avec succès.");
                    goToAfficher(event);
                } catch (Exception e) {
                    showError("Erreur lors de la suppression : " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void goToAfficher(ActionEvent event) throws IOException {
        com.cardiolink.Models.User user = com.cardiolink.utils.ManagerSession.getInstance().getCurrentUser();
        String path = (user != null && "ROLE_MEDECIN".equals(user.getRoleClean())) 
                ? "/AfficherRDVMedecin.fxml" 
                : "/AfficherRDV.fxml";
        com.cardiolink.utils.NavigationUtil.navigate((Stage) ((Node) event.getSource()).getScene().getWindow(), path);
    }

    @FXML
    void handleGeneratePdf(ActionEvent event) {
        if (ordToUpdate == null) return;
        
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer l'ordonnance PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName(ordToUpdate.getReference() + ".pdf");
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                com.cardiolink.Services.PdfGeneratorService pdfService = new com.cardiolink.Services.PdfGeneratorService();
                pdfService.genererOrdonnancePDF(file.getAbsolutePath(), ordToUpdate, ordToUpdate.getPatientNom(), ordToUpdate.getMedecinNom());
                showSuccess("PDF généré avec succès !");
            } catch (Exception e) {
                showError("Erreur lors de la génération du PDF : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void preparerModification(Ordonnance ord) {
        this.isUpdateMode = true;
        this.ordToUpdate = ord;
        txtReference.setText(ord.getReference());
        txtReference.setEditable(false);
        txtReference.setDisable(true);
        txtDiagnostic.setText(ord.getDiagnostic());
        txtPrescription.setText(ord.getNotes());
        if (btnSave != null) btnSave.setText("Mettre à jour");
        if (btnDelete != null) btnDelete.setVisible(true);
        if (btnPdf != null) btnPdf.setVisible(true);
    }

    public void preparerCreation(com.cardiolink.Models.Rendezvous rv) {
        this.isUpdateMode = false;
        this.currentConsultationId = rv.getId();
        if (btnDelete != null) btnDelete.setVisible(false);
        
        try {
            com.cardiolink.Services.UserService userService = new com.cardiolink.Services.UserService();
            com.cardiolink.Models.User patient = userService.getUserById(rv.getPatientId());
            if (patient != null) {
                this.currentPatientNom = patient.getNom() + " " + patient.getPrenom();
            }
            
            com.cardiolink.Models.User medecin = userService.getUserById(rv.getMedecinId());
            if (medecin != null) {
                this.currentMedecinNom = medecin.getNom() + " " + medecin.getPrenom();
            }
        } catch(java.sql.SQLException e) {
            e.printStackTrace();
        }

        String lastRef = service.getLastReference();
        String newRef = "ORD-001";
        if (lastRef != null && lastRef.startsWith("ORD-")) {
            try {
                int num = Integer.parseInt(lastRef.substring(4));
                newRef = String.format("ORD-%03d", num + 1);
            } catch (NumberFormatException e) {
                // Ignore and use default
            }
        }
        txtReference.setText(newRef);
        txtReference.setEditable(false);
        txtReference.setDisable(true);
    }
    @FXML
    void goToMenu(ActionEvent event) {
        try {
            // Chargement de la page MenuRDV
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherRDVMedecin.fxml"));

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