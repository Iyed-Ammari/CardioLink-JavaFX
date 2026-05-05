package com.cardiolink.Controllers;

import com.cardiolink.Models.Produit;
import com.cardiolink.Models.User;
import com.cardiolink.Services.ProduitService;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import com.cardiolink.utils.NavigationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProduitListAdminController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categorieFilterBox;
    @FXML private VBox produitContainer;
    @FXML private ScrollPane produitScrollPane;
    @FXML private Label messageLabel;
    @FXML private Label countLabel;
    @FXML private Label sidebarInitial;
    @FXML private Label sidebarNom;
    @FXML private Label sidebarRole;

    private final ProduitService produitService = new ProduitService();
    private final UserService userService = new UserService();
    private List<Produit> tousLesProduits = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        int userId = ManagerSession.getInstance().getCurrentUserId();
        try {
            User user = userService.getById(userId);
            System.out.println("userId: " + userId);
            if (user != null) {
                String initial = user.getNom() != null && !user.getNom().isEmpty()
                        ? String.valueOf(user.getNom().charAt(0)).toUpperCase() : "?";
                if (sidebarInitial != null) sidebarInitial.setText(initial);
                if (sidebarNom != null) sidebarNom.setText(
                        (user.getNom() != null ? user.getNom() : "") + " " +
                        (user.getPrenom() != null ? user.getPrenom() : ""));
                if (sidebarRole != null) sidebarRole.setText(
                        user.getRoleClean() != null ? user.getRoleClean() : "—");
            }
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        chargerProduits();
    }

    private void chargerProduits() {
        try {
            tousLesProduits = produitService.getall();

            if (categorieFilterBox != null) {
                List<String> cats = new ArrayList<>();
                cats.add("Toutes catégories");

                tousLesProduits.stream()
                        .map(Produit::getCategorie)
                        .filter(c -> c != null && !c.isBlank())
                        .distinct()
                        .sorted()
                        .forEach(cats::add);

                categorieFilterBox.setItems(FXCollections.observableArrayList(cats));

                if (categorieFilterBox.getValue() == null) {
                    categorieFilterBox.setValue("Toutes catégories");
                }

                categorieFilterBox.setOnAction(e -> appliquerFiltres());
            }

            afficherProduits(tousLesProduits);
            showInfo("✅ " + tousLesProduits.size() + " produit(s) chargé(s).");
        } catch (Exception e) {
            showError("Erreur de chargement : " + e.getMessage());
        }
    }

    private void afficherProduits(List<Produit> produits) {
        if (produitContainer == null) return;
        produitContainer.getChildren().clear();

        if (produits == null || produits.isEmpty()) {
            if (countLabel != null) countLabel.setText("0 produit");

            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));

            Label ico = new Label("📦");
            ico.setStyle("-fx-font-size: 48px;");

            Label msg = new Label("Aucun produit trouvé.");
            msg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 700;");

            empty.getChildren().addAll(ico, msg);
            produitContainer.getChildren().add(empty);
            resetScroll();
            return;
        }

        if (countLabel != null) countLabel.setText(produits.size() + " produit(s)");

        for (Produit p : produits) {
            produitContainer.getChildren().add(creerCarteProduit(p));
        }

        resetScroll();
    }

    private HBox creerCarteProduit(Produit produit) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        boolean isPromo = produit.isPromoAuto();

        // Charte graphique : bordure normale #E5E7EB, promo = rouge #F82239
        String borderColor = isPromo ? "rgba(248,34,57,0.45)" : "#E5E7EB";
        String borderWidth = isPromo ? "2" : "1.5";

        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16;" +
                        "-fx-border-color: " + borderColor + "; -fx-border-radius: 16; -fx-border-width: " + borderWidth + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);"
        );

        StackPane imagePane = new StackPane();
        imagePane.setPrefWidth(80);
        imagePane.setMinWidth(80);
        imagePane.setMaxWidth(80);
        imagePane.setPrefHeight(80);
        imagePane.setMinHeight(80);
        imagePane.setMaxHeight(80);

        if (produit.getImageUrl() != null && !produit.getImageUrl().isBlank()) {
            try {
                String imageUrl = produit.getImageUrl();
                // Corriger le format file: pour Windows
                if (imageUrl.startsWith("file:") && !imageUrl.startsWith("file:///")) {
                    String withoutScheme = imageUrl.substring(5).replaceAll("^/+", "");
                    imageUrl = "file:///" + withoutScheme;
                }
                ImageView iv = new ImageView(new Image(imageUrl, true));
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                iv.setPreserveRatio(false);
                imagePane.setStyle("-fx-background-color: #F6F7FB; -fx-background-radius: 12;");
                imagePane.getChildren().add(iv);
            } catch (Exception e) {
                afficherIconeCategorie(imagePane, produit);
            }
        } else {
            afficherIconeCategorie(imagePane, produit);
        }

        boolean dispo = produit.getStock() != null && produit.getStock() > 0;
        VBox infos = new VBox(5);
        HBox.setHgrow(infos, Priority.ALWAYS);

        // Nom + badge promo sur la même ligne
        HBox nomRow = new HBox(8);
        nomRow.setAlignment(Pos.CENTER_LEFT);

        Label nomLbl = new Label(produit.getNom() != null ? produit.getNom() : "—");
        nomLbl.setStyle("-fx-text-fill: #111827; -fx-font-size: 15px; -fx-font-weight: 800;");
        nomRow.getChildren().add(nomLbl);

        // Badge PROMO AUTO — rouge selon charte
        if (isPromo) {
            Label promoBadge = new Label("⏰ PROMO AUTO");
            promoBadge.setStyle(
                    "-fx-background-color: rgba(248,34,57,0.10); -fx-text-fill: #F82239;" +
                            "-fx-font-size: 10px; -fx-font-weight: 900;" +
                            "-fx-padding: 3 8; -fx-background-radius: 6;"
            );
            nomRow.getChildren().add(promoBadge);
        }

        String desc = produit.getDescription() != null && !produit.getDescription().isBlank()
                ? (produit.getDescription().length() > 80
                   ? produit.getDescription().substring(0, 80) + "…"
                   : produit.getDescription())
                : "Produit médical CardioLink";

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        descLbl.setWrapText(true);

        Label stockBadge = new Label(
                dispo ? "✓ " + produit.getStock() + " en stock" : "⚠ Rupture de stock"
        );
        stockBadge.setStyle(
                dispo
                        ? "-fx-background-color: rgba(16,185,129,0.10); -fx-text-fill: #0F766E; " +
                          "-fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 3 8; -fx-background-radius: 6;"
                        : "-fx-background-color: rgba(248,34,57,0.10); -fx-text-fill: #DC2626; " +
                          "-fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 3 8; -fx-background-radius: 6;"
        );

        infos.getChildren().addAll(nomRow, descLbl, stockBadge);

        // Étoiles en lecture seule côté admin
        if (produit.getNbAvis() > 0) {
            HBox etoilesAdmin = new HBox(4);
            etoilesAdmin.setAlignment(Pos.CENTER_LEFT);
            double note = produit.getNoteMoyenne();
            for (int i = 1; i <= 5; i++) {
                Label star = new Label(i <= Math.round(note) ? "★" : "☆");
                star.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 12px;");
                etoilesAdmin.getChildren().add(star);
            }
            Label noteInfo = new Label(
                    String.format("%.1f", note) + " (" + produit.getNbAvis() + " avis)"
            );
            noteInfo.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-weight: 700;");
            etoilesAdmin.getChildren().add(noteInfo);
            infos.getChildren().add(etoilesAdmin);
        }

        // Zone prix — avec ou sans promo
        VBox prixBox = new VBox(2);
        prixBox.setAlignment(Pos.CENTER_RIGHT);
        prixBox.setMinWidth(130);
        prixBox.setPrefWidth(130);

        BigDecimal prix = produit.getPrix() != null ? produit.getPrix() : BigDecimal.ZERO;

        if (isPromo) {
            Label prixBarre = new Label(prix.toPlainString() + " DT");
            prixBarre.setStyle(
                    "-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: 700;" +
                            "-fx-strikethrough: true; -fx-alignment: CENTER_RIGHT;"
            );
            Label prixPromoLbl = new Label(produit.getPrixPromo().toPlainString() + " DT");
            prixPromoLbl.setStyle("-fx-text-fill: #F82239; -fx-font-size: 18px; -fx-font-weight: 900; -fx-alignment: CENTER_RIGHT;");
            prixBox.getChildren().addAll(prixBarre, prixPromoLbl);
        } else {
            Label prixLbl = new Label(prix.toPlainString() + " DT");
            prixLbl.setAlignment(Pos.CENTER_RIGHT);
            prixLbl.setStyle("-fx-text-fill: #F82239; -fx-font-size: 20px; -fx-font-weight: 900;");
            prixBox.getChildren().add(prixLbl);
        }

        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setMinWidth(140);
        actions.setPrefWidth(140);
        actions.setMaxWidth(140);

        Button btnEdit = new Button("✏  Modifier");
        btnEdit.setPrefWidth(130);
        btnEdit.setMinWidth(130);
        btnEdit.setStyle(
                "-fx-background-color: white; -fx-text-fill: #2F60F5; -fx-font-weight: 800;" +
                        "-fx-border-color: #2F60F5; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-cursor: hand; -fx-padding: 8 12;"
        );
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(
                "-fx-background-color: #2F60F5; -fx-text-fill: white; -fx-font-weight: 800;" +
                        "-fx-border-color: #2F60F5; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-cursor: hand; -fx-padding: 8 12;"
        ));
        btnEdit.setOnMouseExited(e -> btnEdit.setStyle(
                "-fx-background-color: white; -fx-text-fill: #2F60F5; -fx-font-weight: 800;" +
                        "-fx-border-color: #2F60F5; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-cursor: hand; -fx-padding: 8 12;"
        ));
        btnEdit.setOnAction(e -> modifierProduit(produit));

        Button btnDelete = new Button("🗑  Supprimer");
        btnDelete.setPrefWidth(130);
        btnDelete.setMinWidth(130);
        btnDelete.setStyle(
                "-fx-background-color: rgba(248,34,57,0.08); -fx-text-fill: #DC2626;" +
                        "-fx-font-weight: 800; -fx-border-color: rgba(248,34,57,0.3);" +
                        "-fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-cursor: hand; -fx-padding: 8 12;"
        );
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(
                "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 800;" +
                        "-fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-cursor: hand; -fx-padding: 8 12;"
        ));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle(
                "-fx-background-color: rgba(248,34,57,0.08); -fx-text-fill: #DC2626;" +
                        "-fx-font-weight: 800; -fx-border-color: rgba(248,34,57,0.3);" +
                        "-fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-cursor: hand; -fx-padding: 8 12;"
        ));
        btnDelete.setOnAction(e -> supprimerProduit(produit));

        actions.getChildren().addAll(btnEdit, btnDelete);
        card.getChildren().addAll(imagePane, infos, prixBox, actions);

        String hoverBorder = isPromo ? "rgba(248,34,57,0.5)" : "rgba(47,96,245,0.3)";
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16;" +
                        "-fx-border-color: " + hoverBorder + "; -fx-border-radius: 16; -fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(16,24,40,0.10), 14, 0.15, 0, 4);" +
                        "-fx-translate-y: -1;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16;" +
                        "-fx-border-color: " + borderColor + "; -fx-border-radius: 16; -fx-border-width: " + borderWidth + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);" +
                        "-fx-translate-y: 0;"
        ));

        return card;
    }

    private void afficherIconeCategorie(StackPane pane, Produit produit) {
        pane.setStyle("-fx-background-color: " + getCatColor(produit.getCategorie()) + "; -fx-background-radius: 12;");
        Label ico = new Label(getCatIcon(produit.getCategorie()));
        ico.setStyle("-fx-font-size: 28px;");
        pane.getChildren().add(ico);
    }

    private String getCatColor(String cat) {
        if (cat == null) return "linear-gradient(to bottom, #6B7280, #9CA3AF)";
        return switch (cat.toUpperCase()) {
            case "MEDICAL" -> "linear-gradient(to bottom, #F82239, #DC2626)";
            case "ACCESSOIRE" -> "linear-gradient(to bottom, #2F60F5, #1D4ED8)";
            case "AUTRE" -> "linear-gradient(to bottom, #6B7280, #4B5563)";
            default -> "linear-gradient(to bottom, #0F766E, #0D9488)";
        };
    }

    private String getCatIcon(String cat) {
        if (cat == null) return "📦";
        return switch (cat.toUpperCase()) {
            case "MEDICAL" -> "🏥";
            case "ACCESSOIRE" -> "🔧";
            case "AUTRE" -> "📦";
            default -> "💊";
        };
    }

    @FXML
    private void handleSearch() {
        appliquerFiltres();
    }

    @FXML
    private void handleReset() {
        if (searchField != null) searchField.clear();
        if (categorieFilterBox != null) categorieFilterBox.setValue("Toutes catégories");
        afficherProduits(tousLesProduits);
    }

    private void appliquerFiltres() {
        String q = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        String cat = categorieFilterBox != null ? categorieFilterBox.getValue() : null;

        List<Produit> result = tousLesProduits.stream()
                .filter(p -> {
                    if (cat != null && !cat.equals("Toutes catégories")) {
                        if (p.getCategorie() == null || !p.getCategorie().equalsIgnoreCase(cat)) {
                            return false;
                        }
                    }

                    if (!q.isEmpty()) {
                        boolean matchNom = p.getNom() != null && p.getNom().toLowerCase().contains(q);
                        boolean matchDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(q);
                        return matchNom || matchDesc;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        afficherProduits(result);

        if (result.isEmpty()) showError("Aucun résultat.");
        else showInfo(result.size() + " produit(s) trouvé(s).");
    }

    private void supprimerProduit(Produit produit) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Confirmation");
        dlg.setHeaderText("Supprimer « " + produit.getNom() + " »");
        dlg.setContentText("Cette action est irréversible. Confirmer ?");

        dlg.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    produitService.delete(produit);
                    chargerProduits();
                    showInfo("✅ Produit supprimé.");
                } catch (Exception e) {
                    showError("❌ " + e.getMessage());
                }
            }
        });
    }

    private void modifierProduit(Produit produit) {
        try {
            Stage stage = (Stage) produitContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/produit-form-admin.fxml")
            );
            Parent root = loader.load();
            ProduitFormAdminController ctrl = loader.getController();
            ctrl.setProduit(produit);

            Scene scene = new Scene(root, 1400, 850);
            NavigationUtil.applyGlobalCss(scene);

            stage.setTitle("CardioLink — Modifier : " + produit.getNom());
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            showError("❌ Impossible d'ouvrir le formulaire.");
        }
    }

    @FXML
    private void goToProduits() {
        chargerProduits();
    }

    @FXML
    private void goToDashboard() {
        try {
            NavigationUtil.navigate(
                    (Stage) produitContainer.getScene().getWindow(),
                    "/fxml/admin/dashboard-admin.fxml"
            );
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToAjouterProduit() {
        try {
            NavigationUtil.navigate(
                    (Stage) produitContainer.getScene().getWindow(),
                    "/fxml/admin/produit-form-admin.fxml"
            );
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToCommandes() {
        try {
            NavigationUtil.navigate(
                    (Stage) produitContainer.getScene().getWindow(),
                    "/fxml/admin/commande-list-admin.fxml"
            );
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToPredictionIA() {
        try {
            NavigationUtil.navigate(
                    (Stage) produitContainer.getScene().getWindow(),
                    "/fxml/admin/prediction-ia.fxml"
            );
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    private void resetScroll() {
        if (produitScrollPane != null) {
            Platform.runLater(() -> {
                produitScrollPane.setVvalue(0);
                produitScrollPane.setHvalue(0);
            });
        }
    }

    private void showError(String m) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill: #B42318; -fx-font-size: 13px; -fx-font-weight: 700;");
        messageLabel.setText(m);
    }

    private void showInfo(String m) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill: #0F766E; -fx-font-size: 13px; -fx-font-weight: 700;");
        messageLabel.setText(m);
    }
}