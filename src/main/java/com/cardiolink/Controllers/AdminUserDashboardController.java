package com.cardiolink.Controllers;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.Models.User;
import com.cardiolink.Services.DossierMedicalService;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminUserDashboardController implements UserAwareController {

    @FXML private ScrollPane  welcomeView;
    @FXML private Label       welcomeName;
    @FXML private BorderPane  dashboardPane;

    @FXML private Label  sidebarInitial;
    @FXML private Label  sidebarName;
    @FXML private Button btnHome;
    @FXML private Button btnUsers;
    @FXML private Button btnDossiers;

    @FXML private ScrollPane homeView;
    @FXML private ScrollPane usersView;
    @FXML private ScrollPane dossiersView;

    @FXML private Label countPatients;
    @FXML private Label countMedecins;
    @FXML private Label countSignins;

    @FXML private TableView<User>           usersTable;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, Void>   colActions;
    @FXML private TextField                 searchField;
    @FXML private ComboBox<String>          roleFilter;

    @FXML private TableView<DossierMedical>           dossiersTable;
    @FXML private TableColumn<DossierMedical, String> dColPatient;
    @FXML private TableColumn<DossierMedical, String> dColGroupe;
    @FXML private TableColumn<DossierMedical, String> dColPoids;
    @FXML private TableColumn<DossierMedical, String> dColTaille;
    @FXML private TableColumn<DossierMedical, String> dColTenSys;
    @FXML private TableColumn<DossierMedical, String> dColTenDia;
    @FXML private TableColumn<DossierMedical, String> dColFreq;
    @FXML private TableColumn<DossierMedical, Void>   dColActions;

    private User       currentUser;
    private List<User> allUsers = new ArrayList<>();

    private boolean usersTableReady    = false;
    private boolean dossiersTableReady = false;

    private final UserService           userService    = new UserService();
    private final DossierMedicalService dossierService = new DossierMedicalService();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        try {
            allUsers = userService.getAllUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            allUsers = new ArrayList<>();
        }
        if (user != null) {
            sidebarName.setText(user.getPrenom() + " " + user.getNom());
            String initial = user.getNom() != null && !user.getNom().isEmpty()
                    ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "A";
            sidebarInitial.setText(initial);
            welcomeName.setText("Welcome to CardioLink");
        }
        roleFilter.setItems(FXCollections.observableArrayList(
                "-- Tous les rôles --", "ROLE_PATIENT", "ROLE_MEDECIN", "ROLE_ADMIN"));
        roleFilter.setValue("-- Tous les rôles --");
        setupUsersTable();
        usersTable.setItems(FXCollections.observableArrayList(allUsers));
        loadStats();
        welcomeView.setVisible(true);
        dashboardPane.setVisible(false);
    }

    // ── Récupère l'user depuis ManagerSession ─────────────────
    public void init() {
        try {
            int userId = ManagerSession.getInstance().getCurrentUserId();
            User user  = userService.getUserById(userId);
            if (user != null) setCurrentUser(user);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Welcome ↔ Dashboard ──────────────────────────────────
    @FXML private void goToDashboard() {
        welcomeView.setVisible(false);
        dashboardPane.setVisible(true);
        showHome();
    }

    @FXML public void showWelcomePage() {
        dashboardPane.setVisible(false);
        welcomeView.setVisible(true);
    }

    // ── Stats ─────────────────────────────────────────────────
    private void loadStats() {
        try {
            long patients  = allUsers.stream()
                    .filter(u -> u.getRoleClean().equals("ROLE_PATIENT")).count();
            long medecins  = allUsers.stream()
                    .filter(u -> u.getRoleClean().equals("ROLE_MEDECIN")).count();
            long thisMonth = userService.countRegistrationsThisMonth();
            countPatients.setText(String.valueOf(patients));
            countMedecins.setText(String.valueOf(medecins));
            countSignins.setText(String.valueOf(thisMonth));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Users Table ───────────────────────────────────────────
    private void setupUsersTable() {
        if (usersTableReady) return;
        usersTableReady = true;

        colNom.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getEmail()));
        colRole.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getRoleClean()));
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isActive() ? "✅ Actif" : "🔒 Bloqué"));

        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item.replace("ROLE_", ""));
                setStyle(item.equals("ROLE_ADMIN")   ? "-fx-text-fill: #E24B4A; -fx-font-weight: bold;" :
                        item.equals("ROLE_MEDECIN") ? "-fx-text-fill: #7F77DD; -fx-font-weight: bold;" :
                                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btnVoir     = new Button("👁 Voir");
            final Button btnModifier = new Button("✏ Modifier");
            final Button btnToggle   = new Button();
            final Button btnSuppr    = new Button("🗑 Supprimer");
            final HBox   box         = new HBox(6, btnVoir, btnModifier, btnToggle, btnSuppr);
            {
                String base = "-fx-font-size: 11px; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-cursor: hand;";
                btnVoir.setStyle(base     + "-fx-background-color: #e8e8ff; -fx-text-fill: #7F77DD;");
                btnModifier.setStyle(base + "-fx-background-color: #fff3e0; -fx-text-fill: #e67e22;");
                btnSuppr.setStyle(base    + "-fx-background-color: #ffe8e8; -fx-text-fill: #E24B4A;");
                btnVoir.setOnAction(e     -> showUserDetail(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(e -> goEditUser(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e   -> toggleUser(getTableView().getItems().get(getIndex())));
                btnSuppr.setOnAction(e    -> deleteUser(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                if (u.isActive()) {
                    btnToggle.setText("🔒 Bloquer");
                    btnToggle.setStyle("-fx-font-size: 11px; -fx-padding: 4 10; " +
                            "-fx-background-radius: 6; -fx-cursor: hand; " +
                            "-fx-background-color: #e8f5e9; -fx-text-fill: #27ae60;");
                } else {
                    btnToggle.setText("🔓 Activer");
                    btnToggle.setStyle("-fx-font-size: 11px; -fx-padding: 4 10; " +
                            "-fx-background-radius: 6; -fx-cursor: hand; " +
                            "-fx-background-color: #e8e8ff; -fx-text-fill: #7F77DD;");
                }
                setGraphic(box);
            }
        });
    }

    private void loadUsers() {
        try {
            allUsers = userService.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(allUsers));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String role  = roleFilter.getValue();
        List<User> filtered = allUsers.stream().filter(u -> {
            boolean matchText = query.isEmpty()
                    || u.getNom().toLowerCase().contains(query)
                    || u.getPrenom().toLowerCase().contains(query)
                    || u.getEmail().toLowerCase().contains(query);
            boolean matchRole = role == null || role.equals("-- Tous les rôles --")
                    || u.getRoleClean().equals(role);
            return matchText && matchRole;
        }).collect(Collectors.toList());
        usersTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showUserDetail(User u) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profil utilisateur");
        alert.setHeaderText(u.getPrenom() + " " + u.getNom());
        alert.setContentText(
                "Email   : " + u.getEmail()     + "\n" +
                        "Rôle    : " + u.getRoleClean() + "\n" +
                        "Tél     : " + (u.getTel()     != null ? u.getTel()     : "-") + "\n" +
                        "Adresse : " + (u.getAdresse() != null ? u.getAdresse() : "-") + "\n" +
                        "Statut  : " + (u.isActive() ? "Actif" : "Bloqué"));
        alert.showAndWait();
    }

    private void goEditUser(User u) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/admin_edit_user.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            AdminEditUserController ctrl = loader.getController();
            ctrl.setData(u);
            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void toggleUser(User u) {
        try {
            userService.setActive(u.getId(), !u.isActive());
            loadUsers();
            loadStats();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void deleteUser(User u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer l'utilisateur");
        alert.setHeaderText("Supprimer " + u.getPrenom() + " " + u.getNom() + " ?");
        alert.setContentText("Cette action est irréversible.");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    userService.deleteUser(u.getId());
                    loadUsers();
                    loadStats();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    // ── Dossiers Table ────────────────────────────────────────
    private void setupDossiersTable() {
        if (dossiersTableReady) return;
        dossiersTableReady = true;

        dColPatient.setCellValueFactory(c -> {
            int uid = c.getValue().getUserId();
            if (allUsers != null) {
                return allUsers.stream().filter(u -> u.getId() == uid).findFirst()
                        .map(u -> new SimpleStringProperty(u.getNom() + " " + u.getPrenom()))
                        .orElse(new SimpleStringProperty("Inconnu"));
            }
            return new SimpleStringProperty(String.valueOf(uid));
        });
        dColGroupe.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGroupeSanguin() != null ? c.getValue().getGroupeSanguin() : "-"));
        dColPoids.setCellValueFactory(c  -> new SimpleStringProperty(
                c.getValue().getPoids()  != null ? c.getValue().getPoids()  + " kg"   : "-"));
        dColTaille.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTaille() != null ? c.getValue().getTaille() + " cm"   : "-"));
        dColTenSys.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTensionSystolique()  != null
                        ? c.getValue().getTensionSystolique()  + " mmHg" : "-"));
        dColTenDia.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTensionDiastolique() != null
                        ? c.getValue().getTensionDiastolique() + " mmHg" : "-"));
        dColFreq.setCellValueFactory(c   -> new SimpleStringProperty(
                c.getValue().getFrequenceCardiaque() != null
                        ? c.getValue().getFrequenceCardiaque() + " bpm"  : "-"));

        dColActions.setCellFactory(col -> new TableCell<>() {
            final Button btnVoir     = new Button("👁 Voir");
            final Button btnModifier = new Button("✏ Modifier");
            final Button btnSuppr    = new Button("🗑 Supprimer");
            final HBox   box         = new HBox(8, btnVoir, btnModifier, btnSuppr);
            {
                String base = "-fx-font-size: 11px; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; " +
                        "-fx-border-radius: 6; -fx-border-width: 1;";
                btnVoir.setStyle(base +
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #7F77DD; -fx-border-color: #7F77DD;");
                btnModifier.setStyle(base +
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #e67e22; -fx-border-color: #e67e22;");
                btnSuppr.setStyle(base +
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #E24B4A; -fx-border-color: #E24B4A; " +
                        "-fx-font-weight: bold;");
                btnVoir.setOnAction(e ->
                        showDossierDetail(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(e ->
                        goEditDossier(getTableView().getItems().get(getIndex())));
                btnSuppr.setOnAction(e ->
                        deleteDossier(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadDossiers() {
        try {
            allUsers = userService.getAllUsers();
            ObservableList<DossierMedical> list = FXCollections.observableArrayList();
            for (User u : allUsers) {
                DossierMedical d = dossierService.getByUserId(u.getId());
                if (d != null) list.add(d);
            }
            dossiersTable.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showDossierDetail(DossierMedical d) {
        String patient = allUsers == null ? String.valueOf(d.getUserId()) :
                allUsers.stream().filter(u -> u.getId() == d.getUserId()).findFirst()
                        .map(u -> u.getNom() + " " + u.getPrenom()).orElse("Inconnu");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dossier Médical");
        alert.setHeaderText("Dossier de " + patient);
        alert.setContentText(
                "Groupe Sanguin      : " + nvl(d.getGroupeSanguin())   + "\n" +
                        "Poids               : " + (d.getPoids()              != null ? d.getPoids()              + " kg"   : "-") + "\n" +
                        "Taille              : " + (d.getTaille()             != null ? d.getTaille()             + " cm"   : "-") + "\n" +
                        "Tension Systolique  : " + (d.getTensionSystolique()  != null ? d.getTensionSystolique()  + " mmHg" : "-") + "\n" +
                        "Tension Diastolique : " + (d.getTensionDiastolique() != null ? d.getTensionDiastolique() + " mmHg" : "-") + "\n" +
                        "Fréquence Cardiaque : " + (d.getFrequenceCardiaque() != null ? d.getFrequenceCardiaque() + " bpm"  : "-") + "\n" +
                        "Antécédents         : " + nvl(d.getAntecedents())     + "\n" +
                        "Allergies           : " + nvl(d.getAllergies()));
        alert.showAndWait();
    }

    private void goEditDossier(DossierMedical d) {
        try {
            User patient = allUsers.stream()
                    .filter(u -> u.getId() == d.getUserId())
                    .findFirst().orElse(null);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/admin_edit_dossier.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            AdminEditDossierController ctrl = loader.getController();
            ctrl.setData(currentUser, d, patient);
            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void deleteDossier(DossierMedical d) {
        String patient = allUsers == null ? String.valueOf(d.getUserId()) :
                allUsers.stream().filter(u -> u.getId() == d.getUserId()).findFirst()
                        .map(u -> u.getNom() + " " + u.getPrenom()).orElse("Inconnu");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le dossier");
        alert.setHeaderText("Supprimer le dossier de " + patient + " ?");
        alert.setContentText("Cette action est irréversible.");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    dossierService.deleteByUserId(d.getUserId());
                    loadDossiers();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private String nvl(String s) { return s != null ? s : "-"; }

    @FXML private void goAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/admin_add_user.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 650);
            AdminAddUserController ctrl = loader.getController();
            ctrl.setCurrentAdmin(currentUser);
            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setTitle("CardioLink - Ajouter Utilisateur");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void showHome() {
        homeView.setVisible(true);
        usersView.setVisible(false);
        dossiersView.setVisible(false);
        setActiveBtn(btnHome);
        loadStats();
    }

    @FXML public void showUsers() {
        homeView.setVisible(false);
        usersView.setVisible(true);
        dossiersView.setVisible(false);
        setActiveBtn(btnUsers);
        setupUsersTable();
        loadUsers();
    }

    @FXML public void showDossiers() {
        homeView.setVisible(false);
        usersView.setVisible(false);
        dossiersView.setVisible(true);
        setActiveBtn(btnDossiers);
        setupDossiersTable();
        loadDossiers();
    }

    private void setActiveBtn(Button active) {
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #ccc;" +
                "-fx-font-size: 13px; -fx-padding: 12 16; -fx-cursor: hand;" +
                "-fx-background-radius: 8; -fx-alignment: CENTER_LEFT;";
        String activeStyle = "-fx-background-color: #7F77DD; -fx-text-fill: white;" +
                "-fx-font-size: 13px; -fx-padding: 12 16; -fx-cursor: hand;" +
                "-fx-background-radius: 8; -fx-alignment: CENTER_LEFT;";
        btnHome.setStyle(inactive);
        btnUsers.setStyle(inactive);
        btnDossiers.setStyle(inactive);
        active.setStyle(activeStyle);
    }

    @FXML private void handleLogout() {
        // ── Effacer la session ──
        ManagerSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setTitle("CardioLink - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goToMarketplace() {
        try {
            // Initialiser ManagerSession pour la marketplace admin
            com.cardiolink.utils.ManagerSession.getInstance().setCurrentUser(currentUser);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/dashboard-admin.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 850);
            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setTitle("CardioLink - Marketplace Admin");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void initAdmin(User admin, String section) {
        this.currentUser = admin;
        try {
            allUsers = userService.getAllUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            allUsers = new ArrayList<>();
        }
        if (admin != null) {
            sidebarName.setText(admin.getPrenom() + " " + admin.getNom());
            String initial = admin.getNom() != null && !admin.getNom().isEmpty()
                    ? String.valueOf(admin.getNom().charAt(0)).toUpperCase() : "A";
            sidebarInitial.setText(initial);
            welcomeName.setText("Welcome to CardioLink");
        }
        roleFilter.setItems(FXCollections.observableArrayList(
                "-- Tous les rôles --", "ROLE_PATIENT", "ROLE_MEDECIN", "ROLE_ADMIN"));
        roleFilter.setValue("-- Tous les rôles --");
        setupUsersTable();
        usersTable.setItems(FXCollections.observableArrayList(allUsers));
        loadStats();
        welcomeView.setVisible(false);
        dashboardPane.setVisible(true);
        switch (section) {
            case "dossiers" -> showDossiers();
            case "users"    -> showUsers();
            default         -> showHome();
        }
    }
}