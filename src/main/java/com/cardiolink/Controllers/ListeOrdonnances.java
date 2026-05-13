package com.cardiolink.Controllers;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Services.ServiceOrdonnance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node; // Import manquant
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class ListeOrdonnances implements UserAwareController {

    @FXML private Label avatarLabel;
    @FXML private javafx.scene.shape.Circle avatarCircle;

    @FXML private TableView<Ordonnance> tableOrdonnances;
    @FXML private TableColumn<Ordonnance, String> colRef, colPatient, colDiag;
    @FXML private TableColumn<Ordonnance, LocalDateTime> colDate;
    @FXML private Label lblTotal;
    @FXML private TextField searchField;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private ServiceOrdonnance service = new ServiceOrdonnance();
    private ObservableList<Ordonnance> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colRef.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colDiag.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));

        chargerDonnees();

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

    private void chargerDonnees() {
        try {
            masterData.setAll(service.getAll());
            tableOrdonnances.setItems(masterData);
            lblTotal.setText(masterData.size() + " ordonnances trouvées");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/dashboard_patient.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToAdd(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/OrdonnanceCRUD.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrdonnanceCRUD.fxml"));
                Parent root = loader.load();

                AjouterOrdonnance controller = loader.getController();
                controller.preparerModification(selected);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une ordonnance à modifier.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'ordonnance " + selected.getReference() + " ?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().get() == ButtonType.YES) {
                try {
                    service.delete(selected);
                    masterData.remove(selected);
                    lblTotal.setText(masterData.size() + " ordonnances trouvées");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}