package com.cardiolink.Controllers;

import com.cardiolink.Models.Rendezvous;
import com.cardiolink.Models.User;
import com.cardiolink.Services.GoogleCalendarService;
import com.cardiolink.Services.ServiceRendezvous;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AjouterRDV {

    @FXML private DatePicker date;
    @FXML private ComboBox<String> heure;
    @FXML private ComboBox<User>   medecin;   // <User> au lieu de <String>
    @FXML private ComboBox<String> type;
    @FXML private TextArea         remarques;
    @FXML private Label            titleLabel;
    @FXML private Button           btnSave;

    private final ServiceRendezvous serviceRV   = new ServiceRendezvous();
    private final UserService       userService = new UserService();

    private boolean modeModification = false;
    private int     idAModifier;

    @FXML
    public void initialize() {
        // ── Vérifier que l'utilisateur connecté est bien un PATIENT ──────────
        try {
            int currentUserId = ManagerSession.getInstance().getCurrentUserId();
            User currentUser  = userService.getUserById(currentUserId);

            if (currentUser == null || !currentUser.getRoleClean().contains("ROLE_PATIENT")) {
                // Afficher un message et désactiver le formulaire
                showAccessDenied();
                return;
            }
        } catch (Exception e) {
            showAccessDenied();
            return;
        }

        // ── Remplir les types et heures ──────────────────────────────────────
        type.getItems().addAll("Consultation au cabinet", "Téléconsultation", "Urgence");
        heure.getItems().addAll("08:00", "09:00", "10:00", "11:00",
                                "14:00", "15:00", "16:00", "17:00");

        // ── Charger la liste des médecins depuis la BDD ─────────────────────
        loadMedecins();
    }

    /**
     * Charge tous les médecins (rôle ROLE_MEDECIN) depuis la base de données
     * et les affiche dans la ComboBox avec "Dr. Prénom Nom".
     */
    private void loadMedecins() {
        try {
            List<User> medecins = userService.getMedecins();

            medecin.getItems().clear();
            medecin.getItems().addAll(medecins);

            // Convertisseur pour afficher "Dr. Prénom Nom" dans la liste
            medecin.setConverter(new StringConverter<>() {
                @Override
                public String toString(User user) {
                    if (user == null) return "";
                    return "Dr. " + user.getPrenom() + " " + user.getNom();
                }
                @Override
                public User fromString(String s) { return null; }
            });

            if (medecins.isEmpty()) {
                medecin.setPromptText("Aucun médecin disponible");
                medecin.setDisable(true);
            } else {
                medecin.setPromptText("Sélectionner un praticien");
            }

        } catch (Exception e) {
            System.err.println("[AjouterRDV] Erreur chargement médecins : " + e.getMessage());
            medecin.setPromptText("Erreur de chargement");
            medecin.setDisable(true);
        }
    }

    /** Désactive le formulaire et affiche un message si l'accès est refusé. */
    private void showAccessDenied() {
        if (titleLabel != null) {
            titleLabel.setText("Accès refusé");
            titleLabel.setStyle("-fx-text-fill: #E24B4A;");
        }
        if (btnSave != null) btnSave.setDisable(true);
        if (medecin  != null) medecin.setDisable(true);
        if (date     != null) date.setDisable(true);
        if (heure    != null) heure.setDisable(true);
        if (type     != null) type.setDisable(true);
        if (remarques != null) remarques.setDisable(true);

        new Alert(Alert.AlertType.WARNING,
                "Seuls les patients peuvent prendre un rendez-vous.").show();
    }

    /**
     * Appelé par AfficherRDV pour passer en mode édition.
     */
    public void preparerModification(Rendezvous rv) {
        this.modeModification = true;
        this.idAModifier      = rv.getId();

        titleLabel.setText("Modifier le Rendez-vous");
        btnSave.setText("Mettre à jour le rendez-vous");

        date.setValue(rv.getDateHeure().toLocalDate());
        heure.setValue(rv.getDateHeure().toLocalTime().toString().substring(0, 5));
        type.setValue(rv.getType());
        remarques.setText(rv.getRemarques());

        // Pré-sélectionner le médecin correspondant
        medecin.getItems().stream()
                .filter(u -> u.getId() == rv.getMedecinId())
                .findFirst()
                .ifPresent(medecin::setValue);
    }

    @FXML
    void save(ActionEvent event) {
        try {
            // ── Validation ───────────────────────────────────────────────────
            if (date.getValue() == null || heure.getValue() == null
                    || type.getValue() == null || medecin.getValue() == null) {
                throw new Exception("Veuillez remplir tous les champs obligatoires !");
            }

            // ── Construction du RDV ──────────────────────────────────────────
            Rendezvous rv = new Rendezvous();
            LocalTime time = LocalTime.parse(heure.getValue());
            rv.setDateHeure(date.getValue().atTime(time));
            rv.setType(rv.getType() == null ? type.getValue() : rv.getType());
            rv.setType(type.getValue());
            rv.setRemarques(remarques.getText());
            rv.setStatut("En attente");

            int currentUserId = ManagerSession.getInstance().getCurrentUserId();
            rv.setPatientId(currentUserId);
            rv.setMedecinId(medecin.getValue().getId());

            // ── Lien Jitsi pour téléconsultation ─────────────────────────────
            if ("Téléconsultation".equals(rv.getType())) {
                String roomName = "CardioLink-" + java.util.UUID.randomUUID().toString().substring(0, 12);
                rv.setLienVisio("https://meet.jit.si/" + roomName);
            } else {
                rv.setLienVisio(null);
            }

            // ── Sauvegarde en BD ─────────────────────────────────────────────
            if (modeModification) {
                rv.setId(idAModifier);
                serviceRV.update(rv);
            } else {
                serviceRV.add(rv);

                // ── Synchronisation Google Calendar du médecin (thread BG) ───
                final Rendezvous rvFinal        = rv;
                final User       selectedMedecin = medecin.getValue();
                final int        patId          = currentUserId;

                new Thread(() -> {
                    try {
                        User patient = userService.getUserById(patId);
                        String nomPatient = (patient != null)
                                ? patient.getPrenom() + " " + patient.getNom()
                                : "Patient #" + patId;

                        GoogleCalendarService calService = new GoogleCalendarService();
                        if (calService.isInitialized()) {
                            calService.pushRendezvousToCalendar(rvFinal, nomPatient);
                            System.out.println("[GoogleCalendar] RDV ajouté au calendrier de Dr. "
                                    + selectedMedecin.getNom());
                        } else {
                            System.err.println("[GoogleCalendar] Non initialisé : " + calService.getInitError());
                        }
                    } catch (Exception ex) {
                        System.err.println("[GoogleCalendar] Erreur lors de l'ajout du RDV : " + ex.getMessage());
                    }
                }, "google-calendar-sync").start();
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