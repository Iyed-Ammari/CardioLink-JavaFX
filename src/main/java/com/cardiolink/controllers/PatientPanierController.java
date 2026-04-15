package com.cardiolink.controllers;

import com.cardiolink.Models.Commande;
import com.cardiolink.Models.LigneCommande;
import com.cardiolink.Services.CommandeService;
import com.cardiolink.utils.NavigationUtil;
import com.cardiolink.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PatientPanierController implements Initializable {

    @FXML private TilePane   cartContainer;
    @FXML private ScrollPane cartScrollPane;
    @FXML private Label      countLabel;
    @FXML private Label      lineCountValue;
    @FXML private Label      totalValue;
    @FXML private Label      messageLabel;

    private final CommandeService commandeService = new CommandeService();
    private Commande panierCourant;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initialiserVue();
        chargerPanier();
    }

    private void initialiserVue() {
        if (countLabel     != null) countLabel.setText("0 ligne");
        if (lineCountValue != null) lineCountValue.setText("0");
        if (totalValue     != null) totalValue.setText("0.00 DT");
        if (messageLabel   != null) messageLabel.setText("");
        if (cartContainer  != null) {
            cartContainer.getChildren().clear();
            cartContainer.setPrefColumns(1);
            cartContainer.setHgap(16);
            cartContainer.setVgap(18);
            cartContainer.setTileAlignment(Pos.TOP_LEFT);
        }
        if (cartScrollPane != null) cartScrollPane.setFocusTraversable(false);
    }

    private void chargerPanier() {
        try {
            panierCourant = commandeService.getOrCreatePanier(SessionManager.getCurrentUserId());
            commandeService.loadLignes(panierCourant);
            afficherLignes(panierCourant.getLignes());
            mettreAJourResume();
        } catch (Exception e) {
            showError("Erreur chargement panier : " + e.getMessage());
        }
    }

    private void afficherLignes(List<LigneCommande> lignes) {
        if (cartContainer == null) return;
        cartContainer.getChildren().clear();

        if (lignes == null || lignes.isEmpty()) {
            if (countLabel != null) countLabel.setText("Panier vide");

            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label ico = new Label("🛒"); ico.setStyle("-fx-font-size: 48px;");
            Label msg = new Label("Votre panier est vide.\nAjoutez des produits depuis le catalogue.");
            msg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 700; -fx-text-alignment: center;");
            msg.setWrapText(true);
            Button btn = new Button("🛍 Voir le catalogue");
            btn.setFocusTraversable(false);
            btn.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 12; -fx-padding: 10 22; -fx-cursor: hand;");
            btn.setOnAction(e -> goToProduits());
            empty.getChildren().addAll(ico, msg, btn);
            cartContainer.getChildren().add(empty);
            Platform.runLater(() -> { if (cartScrollPane != null) { cartScrollPane.setVvalue(0); cartScrollPane.setHvalue(0); } });
            return;
        }

        if (countLabel != null) countLabel.setText(lignes.size() + " ligne(s)");
        for (LigneCommande l : lignes) cartContainer.getChildren().add(creerCarteLigne(l));
        Platform.runLater(() -> { if (cartScrollPane != null) { cartScrollPane.setVvalue(0); cartScrollPane.setHvalue(0); } });
    }

    private VBox creerCarteLigne(LigneCommande ligne) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(860); card.setMinWidth(860); card.setMaxWidth(860);
        card.setFocusTraversable(false);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);

        String nomTxt = ligne.getProduit() != null && ligne.getProduit().getNom() != null ? ligne.getProduit().getNom() : "Produit";
        Label nom = new Label(nomTxt);
        nom.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: 800;");

        if (ligne.getProduit() != null && ligne.getProduit().getCategorie() != null) {
            Label cat = new Label(ligne.getProduit().getCategorie());
            cat.setStyle("-fx-background-color: rgba(47,96,245,0.10); -fx-text-fill: #2F60F5; -fx-font-size: 10px; -fx-font-weight: 800; -fx-padding: 2 8; -fx-background-radius: 999;");
            infos.getChildren().add(cat);
        }
        infos.getChildren().add(nom);

        BigDecimal totalLigne = ligne.getTotalLigne() != null ? ligne.getTotalLigne() : BigDecimal.ZERO;
        Label total = new Label(totalLigne.toPlainString() + " DT");
        total.setStyle("-fx-text-fill: #F82239; -fx-font-size: 18px; -fx-font-weight: 900;");
        header.getChildren().addAll(infos, total);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        BigDecimal pu = ligne.getPrixUnitaire() != null ? ligne.getPrixUnitaire() : BigDecimal.ZERO;
        Label details = new Label("Qté : " + ligne.getQuantite() + "  •  PU : " + pu.toPlainString() + " DT");
        details.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button suppBtn = new Button("🗑 Supprimer");
        suppBtn.setFocusTraversable(false);
        String styleSupp = "-fx-background-color: rgba(248,34,57,0.08); -fx-text-fill: #DC2626; -fx-font-weight: 800; -fx-border-color: rgba(248,34,57,0.25); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 14; -fx-cursor: hand;";
        String styleSuppHover = "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 8 14; -fx-cursor: hand;";
        suppBtn.setStyle(styleSupp);
        suppBtn.setOnMouseEntered(e -> suppBtn.setStyle(styleSuppHover));
        suppBtn.setOnMouseExited(e -> suppBtn.setStyle(styleSupp));
        suppBtn.setOnAction(e -> supprimerLigne(ligne));

        footer.getChildren().addAll(details, spacer, suppBtn);
        card.getChildren().addAll(header, footer);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: rgba(47,96,245,0.25); -fx-border-radius: 20; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(16,24,40,0.10), 18, 0.15, 0, 5); -fx-translate-y: -1;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2); -fx-translate-y: 0;"));

        return card;
    }

    private void supprimerLigne(LigneCommande ligne) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Confirmation");
        dlg.setHeaderText("Supprimer du panier");
        dlg.setContentText("Retirer « " + (ligne.getProduit() != null ? ligne.getProduit().getNom() : "ce produit") + " » ?");
        dlg.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    commandeService.supprimerLigneDuPanier(ligne.getId(), panierCourant.getId());
                    chargerPanier();
                    showInfo("✅ Produit retiré du panier.");
                } catch (Exception e) { showError("❌ " + e.getMessage()); }
            }
        });
    }

    private void mettreAJourResume() {
        int nb = panierCourant != null && panierCourant.getLignes() != null ? panierCourant.getLignes().size() : 0;
        BigDecimal total = panierCourant != null && panierCourant.getMontantTotal() != null ? panierCourant.getMontantTotal() : BigDecimal.ZERO;
        if (lineCountValue != null) lineCountValue.setText(String.valueOf(nb));
        if (totalValue     != null) totalValue.setText(total.toPlainString() + " DT");
    }

    // ─── Actions

    @FXML
    private void handleViderPanier() {
        if (panierCourant == null || panierCourant.getLignes() == null || panierCourant.getLignes().isEmpty()) {
            showError("Le panier est déjà vide."); return;
        }
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Vider le panier");
        dlg.setHeaderText("Confirmation");
        dlg.setContentText("Supprimer tous les produits du panier ?");
        dlg.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    commandeService.viderPanier(panierCourant.getId());
                    chargerPanier();
                    showInfo("✅ Panier vidé.");
                } catch (Exception e) { showError("❌ " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void handleValiderCommande() {
        if (panierCourant == null) { showError("Panier introuvable."); return; }
        if (panierCourant.getLignes() == null || panierCourant.getLignes().isEmpty()) {
            showError("❌ Le panier est vide."); return;
        }
        try {commandeService.validerCommande(panierCourant);


            showInfo("✅ Commande validée ! Redirection vers vos commandes...");

            // La page commandes affichera la nouvelle commande EN_ATTENTE_PAIEMENT
            javafx.animation.PauseTransition t = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.2));
            t.setOnFinished(e -> goToCommandes());
            t.play();

        } catch (Exception e) {
            showError("❌ " + e.getMessage());
        }
    }

    // ─── Navigation
    @FXML private void goToProduits() {
        try { NavigationUtil.navigate((Stage) cartContainer.getScene().getWindow(), "/fxml/patient/produit-list-patient.fxml"); }
        catch (Exception e) { showError("Navigation impossible."); }
    }

    @FXML private void goToPanier() {

        chargerPanier();
    }

    @FXML private void goToCommandes() {
        try { NavigationUtil.navigate((Stage) cartContainer.getScene().getWindow(), "/fxml/patient/commande-list-patient.fxml"); }
        catch (Exception e) { showError("Navigation impossible."); }
    }

    private void showError(String m) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 13px; -fx-font-weight: 700; -fx-background-color: rgba(248,34,57,0.07); -fx-background-radius: 8; -fx-padding: 8 12;");
        messageLabel.setText(m);
    }
    private void showInfo(String m) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill: #065F46; -fx-font-size: 13px; -fx-font-weight: 700; -fx-background-color: rgba(16,185,129,0.10); -fx-background-radius: 8; -fx-padding: 8 12;");
        messageLabel.setText(m);
    }
}