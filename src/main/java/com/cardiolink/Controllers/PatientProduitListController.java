package com.cardiolink.Controllers;

import com.cardiolink.Models.Produit;
import com.cardiolink.Models.User;
import com.cardiolink.Services.CommandeService;
import com.cardiolink.Services.ProduitService;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import com.cardiolink.utils.NavigationUtil;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PatientProduitListController implements Initializable {

    @FXML private TextField  searchField;
    @FXML private TilePane   productContainer;
    @FXML private ScrollPane productScrollPane;
    @FXML private Label      messageLabel;
    @FXML private Label      countLabel;
    @FXML private Button     btnFavoris;
    @FXML private Button     btnProduits;
    @FXML private Button     btnPanier;
    @FXML private Button     btnCommandes;

    private final ProduitService  produitService  = new ProduitService();
    private final CommandeService commandeService = new CommandeService();
    private final UserService     userService     = new UserService();
    private final List<Produit>   produitsCourants = new ArrayList<>();

    private double  savedScrollV  = 0.0;
    private boolean modeFavoris   = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int userId = ManagerSession.getInstance().getCurrentUserId();
        try {
            User user = userService.getUserById(userId);
            System.out.println(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        initialiserVue();
        configurerRecherche();
        chargerProduits();
    }

    private void initialiserVue() {
        if (messageLabel != null) messageLabel.setText("");
        if (countLabel   != null) countLabel.setText("0 produit");
        if (productContainer != null) {
            productContainer.setPrefColumns(2);
            productContainer.setHgap(22);
            productContainer.setVgap(22);
            productContainer.setTileAlignment(Pos.TOP_LEFT);
        }
        if (productScrollPane != null) productScrollPane.setFocusTraversable(false);
        mettreAJourBtnFavoris();
    }

    private void configurerRecherche() {
        if (searchField != null) searchField.setOnAction(e -> handleSearch());
    }

    private void chargerProduits() {
        try {
            List<Produit> liste = produitService.getall();
            produitsCourants.clear();
            produitsCourants.addAll(liste);
            afficherProduits(produitsCourants);
        } catch (Exception e) {
            showError("Erreur chargement : " + e.getMessage());
        }
    }

    private void afficherProduits(List<Produit> produits) {
        if (productContainer == null) return;
        productContainer.getChildren().clear();

        if (produits == null || produits.isEmpty()) {
            if (countLabel != null) countLabel.setText("0 produit");
            Label empty = new Label("Aucun produit disponible.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 15px; -fx-font-weight: 700;");
            productContainer.getChildren().add(empty);
            Platform.runLater(() -> { if (productScrollPane != null) productScrollPane.setVvalue(0); });
            return;
        }

        if (countLabel != null) countLabel.setText(produits.size() + " produit(s)");
        for (Produit p : produits)
            productContainer.getChildren().add(creerCarteProduit(p));

        Platform.runLater(() -> {
            if (productScrollPane != null) productScrollPane.setVvalue(savedScrollV);
        });
    }

    private VBox creerCarteProduit(Produit produit) {
        boolean dispo   = produit.getStock() != null && produit.getStock() > 0;
        boolean isPromo = produit.isPromoAuto();

        // ── Hauteur dynamique selon promo ──
        double cardHeight = isPromo ? 540 : 510;

        VBox card = new VBox(0);
        card.setPrefWidth(360);
        card.setMinWidth(360);
        card.setMaxWidth(360);
        card.setPrefHeight(cardHeight);
        card.setMinHeight(cardHeight);
        card.setMaxHeight(cardHeight);
        card.setFocusTraversable(false);

        String borderColor = isPromo ? "rgba(248,34,57,0.45)" : "#E5E7EB";
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 22;" +
                        "-fx-border-color: " + borderColor + "; -fx-border-radius: 22; -fx-border-width: " + (isPromo ? "2" : "1") + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2);"
        );

        // ── Zone image ──
        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(160);
        imagePane.setMinHeight(160);
        imagePane.setMaxHeight(160);
        imagePane.setFocusTraversable(false);
        imagePane.setStyle(
                "-fx-background-color: " + (dispo
                        ? "linear-gradient(to bottom right, rgba(248,34,57,0.06), rgba(47,96,245,0.06))"
                        : "rgba(229,231,235,0.5)")
                        + "; -fx-background-radius: 22 22 0 0;"
        );

        if (produit.getImageUrl() != null && !produit.getImageUrl().isBlank()) {
            try {
                ImageView iv = new ImageView(new Image(produit.getImageUrl(), true));
                iv.setFitWidth(360);
                iv.setFitHeight(160);
                iv.setPreserveRatio(false);
                imagePane.getChildren().add(iv);
            } catch (Exception ex) {
                Label ico = new Label("🖼");
                ico.setStyle("-fx-font-size: 40px;");
                imagePane.getChildren().add(ico);
            }
        } else {
            Label ico = new Label("🖼");
            ico.setStyle("-fx-font-size: 40px;");
            imagePane.getChildren().add(ico);
        }

        // Badge stock (haut droite)
        Label stockBadge = new Label(dispo ? "✓ " + produit.getStock() : "Rupture");
        stockBadge.setFocusTraversable(false);
        stockBadge.setStyle(dispo
                ? "-fx-background-color: rgba(16,185,129,0.92); -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 5 10; -fx-background-radius: 8;"
                : "-fx-background-color: rgba(248,34,57,0.92); -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 5 10; -fx-background-radius: 8;");
        StackPane.setAlignment(stockBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(stockBadge, new Insets(10, 10, 0, 0));
        imagePane.getChildren().add(stockBadge);

        // Bouton cœur favori — rouge #F82239 charte graphique
        boolean estFavori = produit.isFavoriPour(ManagerSession.getInstance().getCurrentUserId());
        Label coeur = new Label(estFavori ? "♥" : "♡");
        coeur.setFocusTraversable(false);
        coeur.setStyle(
                "-fx-font-size: 22px; -fx-cursor: hand; -fx-text-fill: " +
                        (estFavori ? "#F82239" : "white") + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 1);"
        );
        coeur.setOnMouseClicked(e -> {
            try {
                int userId = ManagerSession.getInstance().getCurrentUserId();
                produitService.toggleFavori(produit.getId(), userId);
                // Recharger ce produit depuis la base pour avoir l'état à jour
                Produit updated = produitService.getById(produit.getId());
                boolean nowFavori = updated != null && updated.isFavoriPour(userId);
                coeur.setText(nowFavori ? "♥" : "♡");
                coeur.setStyle(
                        "-fx-font-size: 22px; -fx-cursor: hand; -fx-text-fill: " +
                                (nowFavori ? "#F82239" : "white") + ";" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 1);"
                );
                // Mettre à jour la liste en mémoire
                if (updated != null) {
                    produitsCourants.replaceAll(p2 ->
                            p2.getId().equals(produit.getId()) ? updated : p2
                    );
                }
                mettreAJourBtnFavoris();
                if (modeFavoris && !nowFavori) goToFavoris();
                showInfo(nowFavori ? "♥ Ajouté aux favoris" : "♡ Retiré des favoris");
            } catch (Exception ex) {
                showError("❌ " + ex.getMessage());
            }
        });

        if (!isPromo) {
            StackPane.setAlignment(coeur, Pos.TOP_LEFT);
            StackPane.setMargin(coeur, new Insets(8, 0, 0, 10));
            imagePane.getChildren().add(coeur);
        } else {
            StackPane.setAlignment(coeur, Pos.BOTTOM_LEFT);
            StackPane.setMargin(coeur, new Insets(0, 0, 8, 10));
            imagePane.getChildren().add(coeur);
        }

        // Badge PROMO (haut gauche) — affiché seulement si promo active
        if (isPromo) {
            Label promoBadge = new Label("🔥 -20%");
            promoBadge.setFocusTraversable(false);
            promoBadge.setStyle(
                    "-fx-background-color: #F82239; -fx-text-fill: white;" +
                            "-fx-font-size: 12px; -fx-font-weight: 900;" +
                            "-fx-padding: 5 10; -fx-background-radius: 8;"
            );
            StackPane.setAlignment(promoBadge, Pos.TOP_LEFT);
            StackPane.setMargin(promoBadge, new Insets(10, 0, 0, 10));
            imagePane.getChildren().add(promoBadge);
        }

        // ── Header (catégorie + nom + description) ──
        VBox header = new VBox(6);
        header.setPadding(new Insets(14, 18, 10, 18));
        header.setFocusTraversable(false);

        Label catBadge = new Label(produit.getCategorie() != null ? produit.getCategorie() : "AUTRE");
        catBadge.setFocusTraversable(false);
        catBadge.setStyle("-fx-background-color: rgba(47,96,245,0.10); -fx-text-fill: #2F60F5; -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 5 10; -fx-background-radius: 8;");

        Label nomLabel = new Label(produit.getNom() != null ? produit.getNom() : "Produit");
        nomLabel.setFocusTraversable(false);
        nomLabel.setWrapText(true);
        nomLabel.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: 900;");

        String descTxt = (produit.getDescription() != null && !produit.getDescription().isBlank())
                ? (produit.getDescription().length() > 55
                   ? produit.getDescription().substring(0, 55) + "…"
                   : produit.getDescription())
                : "Produit médical CardioLink.";
        Label descLabel = new Label(descTxt);
        descLabel.setFocusTraversable(false);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        header.getChildren().addAll(catBadge, nomLabel, descLabel);

        // ── Body (prix + quantité + bouton) ──
        VBox body = new VBox(10);
        body.setPadding(new Insets(8, 18, 18, 18));
        body.setFocusTraversable(false);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Zone prix — avec ou sans promo
        VBox prixBox = new VBox(2);
        prixBox.setFocusTraversable(false);

        if (isPromo && produit.getPrix() != null) {
            // Prix barré
            Label prixBarre = new Label(produit.getPrix().toPlainString() + " DT");
            prixBarre.setFocusTraversable(false);
            prixBarre.setStyle(
                    "-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 700;" +
                            "-fx-strikethrough: true;"
            );

            // Nouveau prix promo
            BigDecimal prixPromo = produit.getPrixPromo();
            Label prixPromoLabel = new Label(prixPromo.toPlainString() + " DT");
            prixPromoLabel.setFocusTraversable(false);
            prixPromoLabel.setStyle("-fx-text-fill: #F82239; -fx-font-size: 24px; -fx-font-weight: 900;");

            // Badge économie
            BigDecimal economie = produit.getPrix().subtract(prixPromo);
            Label economieBadge = new Label("Économisez " + economie.toPlainString() + " DT");
            economieBadge.setFocusTraversable(false);
            economieBadge.setStyle(
                    "-fx-background-color: rgba(248,34,57,0.08); -fx-text-fill: #F82239;" +
                            "-fx-font-size: 11px; -fx-font-weight: 800;" +
                            "-fx-padding: 3 8; -fx-background-radius: 6;"
            );

            prixBox.getChildren().addAll(prixBarre, prixPromoLabel, economieBadge);
        } else {
            Label prixLabel = new Label(produit.getPrix() != null ? produit.getPrix().toPlainString() + " DT" : "—");
            prixLabel.setFocusTraversable(false);
            prixLabel.setStyle("-fx-text-fill: #F82239; -fx-font-size: 24px; -fx-font-weight: 900;");
            prixBox.getChildren().add(prixLabel);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Zone étoiles + bouton noter
        HBox zonesEtoiles = creerZoneEtoiles(produit);
        Button btnNoter = creerBoutonNoter(produit);

        if (dispo) {
            HBox qteSelector = new HBox(0);
            qteSelector.setAlignment(Pos.CENTER_LEFT);
            qteSelector.setFocusTraversable(false);
            qteSelector.setStyle("-fx-background-color: #F6F7FB; -fx-background-radius: 12; -fx-border-color: #E5E7EB; -fx-border-radius: 12; -fx-border-width: 1.5;");

            Label qteLbl = new Label("Qté :");
            qteLbl.setFocusTraversable(false);
            qteLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 0 8 0 12;");

            Button btnMoins = creerBoutonQte("−", "#374151");
            Label qteVal = new Label("1");
            qteVal.setFocusTraversable(false);
            qteVal.setStyle("-fx-text-fill: #111827; -fx-font-size: 15px; -fx-font-weight: 900; -fx-pref-width: 32; -fx-alignment: CENTER; -fx-text-alignment: center;");
            Button btnPlus = creerBoutonQte("+", "#2F60F5");

            int maxStock = produit.getStock();

            btnMoins.setOnAction(e -> {
                sauvegarderScroll();
                int current = Integer.parseInt(qteVal.getText());
                if (current > 1) { qteVal.setText(String.valueOf(current - 1)); animer(qteVal); }
                if (searchField != null) Platform.runLater(searchField::requestFocus);
                Platform.runLater(() -> { if (productScrollPane != null) productScrollPane.setVvalue(savedScrollV); });
            });

            btnPlus.setOnAction(e -> {
                sauvegarderScroll();
                int current = Integer.parseInt(qteVal.getText());
                if (current < maxStock) { qteVal.setText(String.valueOf(current + 1)); animer(qteVal); }
                else showError("Stock max atteint (" + maxStock + ").");
                if (searchField != null) Platform.runLater(searchField::requestFocus);
                Platform.runLater(() -> { if (productScrollPane != null) productScrollPane.setVvalue(savedScrollV); });
            });

            qteSelector.getChildren().addAll(qteLbl, btnMoins, qteVal, btnPlus);

            // Bouton ajouter au panier — toujours gradient rouge-bleu charte
            String btnStyle = "-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800; -fx-background-radius: 14; -fx-cursor: hand;";
            String btnHoverStyle = "-fx-background-color: linear-gradient(to right, #D01E32, #1D4ED8); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800; -fx-background-radius: 14; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(248,34,57,0.4), 12, 0.2, 0, 3);";

            Button addBtn = new Button("🛒  Ajouter au panier");
            addBtn.setMaxWidth(Double.MAX_VALUE);
            addBtn.setPrefHeight(46);
            addBtn.setFocusTraversable(false);
            addBtn.setStyle(btnStyle);
            addBtn.setOnMouseEntered(e -> addBtn.setStyle(btnHoverStyle));
            addBtn.setOnMouseExited(e -> addBtn.setStyle(btnStyle));
            addBtn.setOnAction(e -> {
                sauvegarderScroll();
                int qte = Integer.parseInt(qteVal.getText());
                ajouterAuPanier(produit, qte, addBtn, btnStyle);
            });

            body.getChildren().addAll(prixBox, spacer, zonesEtoiles, btnNoter, qteSelector, addBtn);
        } else {
            Button disabledBtn = new Button("Rupture de stock");
            disabledBtn.setMaxWidth(Double.MAX_VALUE);
            disabledBtn.setPrefHeight(46);
            disabledBtn.setDisable(true);
            disabledBtn.setFocusTraversable(false);
            disabledBtn.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-background-radius: 14;");
            body.getChildren().addAll(prixBox, spacer, zonesEtoiles, btnNoter, disabledBtn);
        }

        card.getChildren().addAll(imagePane, header, body);

        // Hover effect
        String hoverBorder = isPromo ? "rgba(248,34,57,0.5)" : "rgba(47,96,245,0.35)";
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 22;" +
                        "-fx-border-color: " + hoverBorder + "; -fx-border-radius: 22; -fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(16,24,40,0.14), 20, 0.2, 0, 5); -fx-translate-y: -2;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 22;" +
                        "-fx-border-color: " + borderColor + "; -fx-border-radius: 22; -fx-border-width: " + (isPromo ? "2" : "1") + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2); -fx-translate-y: 0;"
        ));

        return card;
    }

    // ─────────────────────────────────────────────────────────
    // AVIS ÉTOILES
    // ─────────────────────────────────────────────────────────

    private HBox creerZoneEtoiles(Produit produit) {
        HBox zone = new HBox(8);
        zone.setAlignment(Pos.CENTER_LEFT);
        zone.setFocusTraversable(false);

        double note = produit.getNoteMoyenne();
        int nbAvis  = produit.getNbAvis();

        // Étoiles visuelles
        HBox etoiles = new HBox(2);
        etoiles.setAlignment(Pos.CENTER_LEFT);
        etoiles.setFocusTraversable(false);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= Math.round(note) ? "★" : "☆");
            star.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 14px;");
            star.setFocusTraversable(false);
            etoiles.getChildren().add(star);
        }

        // Note moyenne + nb avis
        String noteStr = nbAvis > 0
                ? String.format("%.1f", note) + " (" + nbAvis + " avis)"
                : "Aucun avis";
        Label noteLbl = new Label(noteStr);
        noteLbl.setFocusTraversable(false);
        noteLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px; -fx-font-weight: 700;");

        zone.getChildren().addAll(etoiles, noteLbl);
        return zone;
    }

    private Button creerBoutonNoter(Produit produit) {
        boolean dejaNote = ManagerSession.getInstance().aDejaNote(produit.getId());

        Button btn = new Button(dejaNote ? "✓ Noté" : "⭐ Noter");
        btn.setFocusTraversable(false);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(36);

        if (dejaNote) {
            btn.setDisable(true);
            btn.setStyle(
                    "-fx-background-color: #F3F4F6; -fx-text-fill: #9CA3AF;" +
                            "-fx-font-size: 12px; -fx-font-weight: 800;" +
                            "-fx-background-radius: 10;"
            );
        } else {
            btn.setStyle(
                    "-fx-background-color: rgba(47,96,245,0.10); -fx-text-fill: #2F60F5;" +
                            "-fx-font-size: 12px; -fx-font-weight: 800;" +
                            "-fx-border-color: rgba(47,96,245,0.30); -fx-border-radius: 10;" +
                            "-fx-background-radius: 10; -fx-cursor: hand;"
            );
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: #2F60F5; -fx-text-fill: white;" +
                            "-fx-font-size: 12px; -fx-font-weight: 800;" +
                            "-fx-background-radius: 10; -fx-cursor: hand;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: rgba(47,96,245,0.10); -fx-text-fill: #2F60F5;" +
                            "-fx-font-size: 12px; -fx-font-weight: 800;" +
                            "-fx-border-color: rgba(47,96,245,0.30); -fx-border-radius: 10;" +
                            "-fx-background-radius: 10; -fx-cursor: hand;"
            ));
            btn.setOnAction(e -> ouvrirDialogNotation(produit, btn));
        }

        return btn;
    }

    private void ouvrirDialogNotation(Produit produit, Button btnNoter) {
        // Créer un dialog custom respectant la charte graphique
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Noter le produit");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(380);

        // Titre
        Label titre = new Label("⭐ Votre avis");
        titre.setStyle("-fx-text-fill: #111827; -fx-font-size: 20px; -fx-font-weight: 900;");

        // Nom produit
        Label nomProduit = new Label(produit.getNom());
        nomProduit.setStyle(
                "-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-weight: 700;" +
                        "-fx-background-color: #F6F7FB; -fx-background-radius: 8; -fx-padding: 6 12;"
        );

        // Question
        Label question = new Label("Quelle note donnez-vous à ce produit ?");
        question.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px; -fx-font-weight: 700;");
        question.setWrapText(true);

        // 5 boutons étoiles interactifs
        HBox etoilesBox = new HBox(8);
        etoilesBox.setAlignment(Pos.CENTER);

        final int[] noteChoisie = {0};
        Label[] stars = new Label[5];

        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 36px; -fx-text-fill: #D1D5DB; -fx-cursor: hand;");
            star.setFocusTraversable(false);

            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].setStyle("-fx-font-size: 36px; -fx-text-fill: " +
                            (j < val ? "#F59E0B" : "#D1D5DB") + "; -fx-cursor: hand;");
                }
            });

            star.setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].setStyle("-fx-font-size: 36px; -fx-text-fill: " +
                            (j < noteChoisie[0] ? "#F59E0B" : "#D1D5DB") + "; -fx-cursor: hand;");
                }
            });

            star.setOnMouseClicked(e -> {
                noteChoisie[0] = val;
                for (int j = 0; j < 5; j++) {
                    stars[j].setStyle("-fx-font-size: 36px; -fx-text-fill: " +
                            (j < val ? "#F59E0B" : "#D1D5DB") + "; -fx-cursor: hand;");
                }
            });

            stars[i] = star;
            etoilesBox.getChildren().add(star);
        }

        // Label description de la note
        Label descNote = new Label("Cliquez sur une étoile");
        descNote.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: 700;");

        // Message erreur/succès
        Label msgLabel = new Label("");
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700;");

        // Boutons action
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER);

        // Bouton valider — gradient rouge-bleu charte
        Button btnValider = new Button("Valider ma note");
        btnValider.setPrefWidth(160);
        btnValider.setPrefHeight(42);
        btnValider.setStyle(
                "-fx-background-color: linear-gradient(to right, #F82239, #2F60F5);" +
                        "-fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 12; -fx-cursor: hand;"
        );

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setPrefWidth(100);
        btnAnnuler.setPrefHeight(42);
        btnAnnuler.setStyle(
                "-fx-background-color: white; -fx-text-fill: #6B7280; -fx-font-weight: 700;" +
                        "-fx-border-color: #E5E7EB; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;"
        );

        // Mettre à jour description selon note
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            stars[i].setOnMouseClicked(stars[i].getOnMouseClicked() == null ? e -> {} : stars[i].getOnMouseClicked());
        }
        // Recréer les handlers avec descNote
        String[] descriptions = {"😞 Très mauvais", "😐 Mauvais", "🙂 Moyen", "😊 Bien", "🤩 Excellent"};
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            final String desc = descriptions[i];
            stars[i].setOnMouseClicked(e -> {
                noteChoisie[0] = val;
                descNote.setText(desc);
                descNote.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 13px; -fx-font-weight: 800;");
                for (int j = 0; j < 5; j++) {
                    stars[j].setStyle("-fx-font-size: 36px; -fx-text-fill: " +
                            (j < val ? "#F59E0B" : "#D1D5DB") + "; -fx-cursor: hand;");
                }
            });
        }

        btnValider.setOnAction(e -> {
            if (noteChoisie[0] == 0) {
                msgLabel.setText("⚠ Veuillez choisir une note.");
                msgLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-font-weight: 700;");
                return;
            }
            try {
                produitService.noterProduit(produit.getId(), noteChoisie[0]);
                ManagerSession.getInstance().marquerCommeNote(produit.getId());

                msgLabel.setText("✅ Merci pour votre avis !");
                msgLabel.setStyle("-fx-text-fill: #065F46; -fx-font-size: 12px; -fx-font-weight: 700;");
                btnValider.setDisable(true);
                btnNoter.setText("✓ Noté");
                btnNoter.setDisable(true);
                btnNoter.setStyle(
                        "-fx-background-color: #F3F4F6; -fx-text-fill: #9CA3AF;" +
                                "-fx-font-size: 12px; -fx-font-weight: 800; -fx-background-radius: 10;"
                );

                // Fermer après 1.5 secondes et recharger
                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                pause.setOnFinished(ev -> {
                    dialog.close();
                    chargerProduits();
                });
                pause.play();

            } catch (Exception ex) {
                msgLabel.setText("❌ " + ex.getMessage());
                msgLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-font-weight: 700;");
            }
        });

        btnAnnuler.setOnAction(e -> dialog.close());

        btnBox.getChildren().addAll(btnValider, btnAnnuler);
        root.getChildren().addAll(titre, nomProduit, question, etoilesBox, descNote, msgLabel, btnBox);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private Button creerBoutonQte(String text, String color) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: 900; -fx-cursor: hand; -fx-padding: 4 10; -fx-border-color: transparent;");
        return btn;
    }

    private void sauvegarderScroll() {
        if (productScrollPane != null) savedScrollV = productScrollPane.getVvalue();
    }

    private void animer(Label label) {
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(label.scaleXProperty(), 1.0), new KeyValue(label.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(80),  new KeyValue(label.scaleXProperty(), 1.4), new KeyValue(label.scaleYProperty(), 1.4)),
                new KeyFrame(Duration.millis(160), new KeyValue(label.scaleXProperty(), 1.0), new KeyValue(label.scaleYProperty(), 1.0))
        );
        tl.play();
    }

    private void ajouterAuPanier(Produit produit, int quantite, Button addBtn, String originalStyle) {
        if (searchField != null) searchField.requestFocus();
        try {
            addBtn.setText("✅  Ajouté !");
            addBtn.setStyle("-fx-background-color: #0F766E; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800; -fx-background-radius: 14; -fx-cursor: hand;");
            addBtn.setDisable(true);

            commandeService.ajouterProduitAuPanier(
                    ManagerSession.getInstance().getCurrentUserId(),
                    produit.getId(),
                    quantite
            );

            showInfo("✅ « " + produit.getNom() + " » × " + quantite + " ajouté(s) au panier !");
            Platform.runLater(() -> { if (productScrollPane != null) productScrollPane.setVvalue(savedScrollV); });

            javafx.animation.PauseTransition t = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            t.setOnFinished(ev -> {
                addBtn.setText("🛒  Ajouter au panier");
                addBtn.setStyle(originalStyle);
                addBtn.setDisable(false);
                if (messageLabel != null) messageLabel.setText("");
            });
            t.play();

        } catch (Exception e) {
            showError("❌ " + e.getMessage());
            Platform.runLater(() -> { if (productScrollPane != null) productScrollPane.setVvalue(savedScrollV); });
        }
    }

    @FXML private void handleSearch() {
        String q = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) { savedScrollV = 0; afficherProduits(produitsCourants); return; }

        List<Produit> res = produitsCourants.stream()
                .filter(p -> (p.getNom() != null && p.getNom().toLowerCase().contains(q)) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(q)) ||
                        (p.getCategorie() != null && p.getCategorie().toLowerCase().contains(q)))
                .toList();

        savedScrollV = 0;
        afficherProduits(res);
        if (res.isEmpty()) showError("Aucun résultat pour « " + q + " ».");
        else showInfo(res.size() + " résultat(s).");
    }

    @FXML private void handleReset() {
        if (searchField != null) searchField.clear();
        savedScrollV = 0;
        afficherProduits(produitsCourants);
    }

    @FXML private void goToProduits() {
        modeFavoris = false;
        savedScrollV = 0;
        activerBoutonSidebar(btnProduits);
        chargerProduits();
    }

    @FXML private void goToFavoris() {
        modeFavoris = true;
        savedScrollV = 0;
        activerBoutonSidebar(btnFavoris);

        try {
            int userId = ManagerSession.getInstance().getCurrentUserId();
            List<Produit> favoris = produitService.getFavorisByUser(userId);

            if (favoris.isEmpty()) {
                if (productContainer != null) productContainer.getChildren().clear();
                if (countLabel != null) countLabel.setText("0 favori");

                VBox empty = new VBox(16);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(60));

                Label ico = new Label("♡");
                ico.setStyle("-fx-font-size: 56px; -fx-text-fill: #F82239;");

                Label titre = new Label("Aucun favori");
                titre.setStyle("-fx-text-fill: #111827; -fx-font-size: 18px; -fx-font-weight: 900;");

                Label msg = new Label("Cliquez sur ♡ sur un produit pour l'ajouter à vos favoris.");
                msg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px; -fx-font-weight: 700; -fx-text-alignment: center;");
                msg.setWrapText(true);

                Button btnVoirTout = new Button("🛍  Voir tous les produits");
                btnVoirTout.setPrefHeight(46);
                btnVoirTout.setStyle(
                        "-fx-background-color: linear-gradient(to right, #F82239, #2F60F5);" +
                                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800;" +
                                "-fx-background-radius: 14; -fx-padding: 10 24; -fx-cursor: hand;"
                );
                btnVoirTout.setOnAction(e -> goToProduits());

                empty.getChildren().addAll(ico, titre, msg, btnVoirTout);
                if (productContainer != null) productContainer.getChildren().add(empty);
                if (messageLabel != null) messageLabel.setText("");
                return;
            }

            afficherProduits(favoris);
            showInfo("♥ " + favoris.size() + " produit(s) en favori.");

        } catch (Exception e) {
            showError("Erreur chargement favoris : " + e.getMessage());
        }
    }

    private static final String BTN_ACTIF   = "-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 800; -fx-background-radius: 18; -fx-cursor: hand;";
    private static final String BTN_INACTIF = "-fx-background-color: #F3F4F6; -fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: 800; -fx-background-radius: 18; -fx-cursor: hand;";

    private void activerBoutonSidebar(Button actif) {
        if (btnProduits  != null) btnProduits.setStyle(BTN_INACTIF);
        if (btnPanier    != null) btnPanier.setStyle(BTN_INACTIF);
        if (btnCommandes != null) btnCommandes.setStyle(BTN_INACTIF);
        if (btnFavoris   != null) btnFavoris.setStyle(BTN_INACTIF);
        if (actif        != null) actif.setStyle(BTN_ACTIF);
    }

    private void mettreAJourBtnFavoris() {
        if (btnFavoris == null) return;
        try {
            int userId = ManagerSession.getInstance().getCurrentUserId();
            long nb = produitsCourants.stream()
                    .filter(p -> p.isFavoriPour(userId))
                    .count();
            btnFavoris.setText(nb > 0 ? "♥  Favoris (" + nb + ")" : "♡  Mes favoris");
        } catch (Exception ignored) {
            btnFavoris.setText("♡  Mes favoris");
        }
    }

    @FXML private void goToPanier() {
        activerBoutonSidebar(btnPanier);
        try {
            NavigationUtil.navigate((Stage) productContainer.getScene().getWindow(), "/fxml/patient/panier-patient.fxml");
        } catch (Exception e) { showError("Impossible d'ouvrir le panier."); }
    }

    @FXML private void goToCommandes() {
        activerBoutonSidebar(btnCommandes);
        try {
            NavigationUtil.navigate((Stage) productContainer.getScene().getWindow(), "/fxml/patient/commande-list-patient.fxml");
        } catch (Exception e) { showError("Impossible d'ouvrir les commandes."); }
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
