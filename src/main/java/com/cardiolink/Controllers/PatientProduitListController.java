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
import javafx.stage.Stage;
import javafx.util.Duration;

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

    private final ProduitService  produitService  = new ProduitService();
    private final CommandeService commandeService = new CommandeService();
    private final UserService userService = new UserService();
    private final List<Produit>   produitsCourants = new ArrayList<>();

    private double savedScrollV = 0.0;

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

        if (productScrollPane != null) {
            productScrollPane.setFocusTraversable(false);
        }
    }

    private void configurerRecherche() {
        if (searchField != null)
            searchField.setOnAction(e -> handleSearch());
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
        VBox card = new VBox(0);
        card.setPrefWidth(360);
        card.setMinWidth(360);
        card.setMaxWidth(360);
        card.setPrefHeight(440);
        card.setMinHeight(440);
        card.setMaxHeight(440);
        card.setFocusTraversable(false);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 22;" +
                        "-fx-border-color: #E5E7EB; -fx-border-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2);"
        );

        boolean dispo = produit.getStock() != null && produit.getStock() > 0;

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

        Label stockBadge = new Label(dispo ? "✓ " + produit.getStock() : "Rupture");
        stockBadge.setFocusTraversable(false);
        stockBadge.setStyle(dispo
                ? "-fx-background-color: rgba(16,185,129,0.92); -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 5 10; -fx-background-radius: 8;"
                : "-fx-background-color: rgba(248,34,57,0.92); -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 5 10; -fx-background-radius: 8;");
        StackPane.setAlignment(stockBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(stockBadge, new Insets(10, 10, 0, 0));
        imagePane.getChildren().add(stockBadge);

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
                ? (produit.getDescription().length() > 55 ? produit.getDescription().substring(0, 55) + "…" : produit.getDescription())
                : "Produit médical CardioLink.";

        Label descLabel = new Label(descTxt);
        descLabel.setFocusTraversable(false);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        header.getChildren().addAll(catBadge, nomLabel, descLabel);

        VBox body = new VBox(12);
        body.setPadding(new Insets(8, 18, 18, 18));
        body.setFocusTraversable(false);
        VBox.setVgrow(body, Priority.ALWAYS);

        Label prixLabel = new Label(produit.getPrix() != null ? produit.getPrix().toPlainString() + " DT" : "—");
        prixLabel.setFocusTraversable(false);
        prixLabel.setStyle("-fx-text-fill: #F82239; -fx-font-size: 24px; -fx-font-weight: 900;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

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
                if (current > 1) {
                    qteVal.setText(String.valueOf(current - 1));
                    animer(qteVal);
                }
                if (searchField != null) Platform.runLater(searchField::requestFocus);
                Platform.runLater(() -> { if (productScrollPane != null) productScrollPane.setVvalue(savedScrollV); });
            });

            btnPlus.setOnAction(e -> {
                sauvegarderScroll();
                int current = Integer.parseInt(qteVal.getText());
                if (current < maxStock) {
                    qteVal.setText(String.valueOf(current + 1));
                    animer(qteVal);
                } else {
                    showError("Stock max atteint (" + maxStock + ").");
                }
                if (searchField != null) Platform.runLater(searchField::requestFocus);
                Platform.runLater(() -> { if (productScrollPane != null) productScrollPane.setVvalue(savedScrollV); });
            });

            qteSelector.getChildren().addAll(qteLbl, btnMoins, qteVal, btnPlus);

            Button addBtn = new Button("🛒  Ajouter au panier");
            addBtn.setMaxWidth(Double.MAX_VALUE);
            addBtn.setPrefHeight(46);
            addBtn.setFocusTraversable(false);
            addBtn.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800; -fx-background-radius: 14; -fx-cursor: hand;");
            addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color: linear-gradient(to right, #D01E32, #1D4ED8); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800; -fx-background-radius: 14; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(248,34,57,0.4), 12, 0.2, 0, 3);"));
            addBtn.setOnMouseExited(e -> addBtn.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800; -fx-background-radius: 14; -fx-cursor: hand;"));
            addBtn.setOnAction(e -> {
                sauvegarderScroll();
                int qte = Integer.parseInt(qteVal.getText());
                ajouterAuPanier(produit, qte, addBtn);
            });

            body.getChildren().addAll(prixLabel, spacer, qteSelector, addBtn);
        } else {
            Button disabledBtn = new Button("Rupture de stock");
            disabledBtn.setMaxWidth(Double.MAX_VALUE);
            disabledBtn.setPrefHeight(46);
            disabledBtn.setDisable(true);
            disabledBtn.setFocusTraversable(false);
            disabledBtn.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-background-radius: 14;");
            body.getChildren().addAll(prixLabel, spacer, disabledBtn);
        }

        card.getChildren().addAll(imagePane, header, body);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 22; -fx-border-color: rgba(47,96,245,0.35); -fx-border-radius: 22; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(16,24,40,0.14), 20, 0.2, 0, 5); -fx-translate-y: -2;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 22; -fx-border-color: #E5E7EB; -fx-border-radius: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2); -fx-translate-y: 0;"));

        return card;
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

    private void ajouterAuPanier(Produit produit, int quantite, Button addBtn) {
        if (searchField != null) searchField.requestFocus();

        try {
            String originalStyle = addBtn.getStyle();
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
        String q = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) {
            savedScrollV = 0;
            afficherProduits(produitsCourants);
            return;
        }

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

    @FXML private void goToProduits()  {
        savedScrollV = 0;
        chargerProduits();
    }

    @FXML private void goToPanier() {
        try {
            NavigationUtil.navigate((Stage) productContainer.getScene().getWindow(), "/fxml/patient/panier-patient.fxml");
        } catch (Exception e) {
            showError("Impossible d'ouvrir le panier.");
        }
    }

    @FXML private void goToCommandes() {
        try {
            NavigationUtil.navigate((Stage) productContainer.getScene().getWindow(), "/fxml/patient/commande-list-patient.fxml");
        } catch (Exception e) {
            showError("Impossible d'ouvrir les commandes.");
        }
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