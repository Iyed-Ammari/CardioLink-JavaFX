package com.cardiolink.Controllers;

import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Services.ServiceRendezvous;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class AjouterRDV {

    @FXML private DatePicker date;
    @FXML private ComboBox<String> heure;
    @FXML private ComboBox<String> medecin;
    @FXML private ComboBox<String> type;
    @FXML private TextArea remarques;
    @FXML private Label titleLabel;
    @FXML private Button btnSave;

    private ServiceRendezvous serviceRV = new ServiceRendezvous();
    private com.cardiolink.Services.UserService userService = new com.cardiolink.Services.UserService();
    private boolean modeModification = false;
    private int idAModifier;

    @FXML
    public void initialize() {
        // Initialisation des listes
        type.getItems().addAll("Consultation au cabinet", "Téléconsultation", "Urgence");
        heure.getItems().addAll("08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00");
        medecin.getItems().addAll("Dr. Ahmed Mansour", "Dr. Sonia Ben Ali", "Dr. Marc Durand");
    }

    /**
     * Appelé par AfficherRDV pour passer en mode édition
     */
    public void preparerModification(Rendezvous rv) {
        this.modeModification = true;
        this.idAModifier = rv.getId();

        titleLabel.setText("Modifier le Rendez-vous");
        btnSave.setText("Mettre à jour le rendez-vous");

        date.setValue(rv.getDateHeure().toLocalDate());
        heure.setValue(rv.getDateHeure().toLocalTime().toString().substring(0, 5));
        type.setValue(rv.getType());
        remarques.setText(rv.getRemarques());
        // Optionnel : medecin.setValue(...) si vous stockez le nom
    }

    @FXML
    void save(ActionEvent event) {
        try {
            // Validation simple
            if (date.getValue() == null || heure.getValue() == null || type.getValue() == null) {
                throw new Exception("Veuillez remplir les champs obligatoires !");
            }

            Rendezvous rv = new Rendezvous();
            LocalTime time = LocalTime.parse(heure.getValue());
            rv.setDateHeure(date.getValue().atTime(time));
            rv.setType(type.getValue());
            rv.setRemarques(remarques.getText());
            rv.setStatut("En attente");
            
            int currentUserId = com.cardiolink.utils.ManagerSession.getInstance().getCurrentUserId();
            com.cardiolink.Models.User currentUser = userService.getUserById(currentUserId);
            
            if (currentUser != null && currentUser.getRoleClean().contains("MEDECIN")) {
                rv.setMedecinId(currentUserId);
                rv.setPatientId(1); // Par défaut pour le moment
            } else {
                rv.setPatientId(currentUserId);
                rv.setMedecinId(1); // Par défaut pour le moment
            }

            // Génération du lien Jitsi si c'est une téléconsultation
            if ("Téléconsultation".equals(rv.getType())) {
                // Utilisation d'un format plus pro : CardioLink-DoctorName-UUID
                String roomName = "CardioLink-" + java.util.UUID.randomUUID().toString().substring(0, 12);
                rv.setLienVisio("https://meet.jit.si/" + roomName);
            } else {
                rv.setLienVisio(null);
            }

            if (modeModification) {
                rv.setId(idAModifier);
                serviceRV.update(rv);
            } else {
                serviceRV.add(rv);
            }

            retourListe(event);

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        try {
            retourListe(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AfficherRDV.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    void goToMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/dashboard_patient.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}