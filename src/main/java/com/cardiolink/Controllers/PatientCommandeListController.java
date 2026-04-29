package com.cardiolink.Controllers;

import com.cardiolink.Models.Commande;
import com.cardiolink.Models.User;
import com.cardiolink.Services.CommandeService;
import com.cardiolink.Services.FacturePdfService;
import com.cardiolink.Services.PaiementService;
import com.cardiolink.Services.UserService;
import com.cardiolink.utils.ManagerSession;
import com.cardiolink.utils.NavigationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLDataException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PatientCommandeListController implements Initializable {

    @FXML private TilePane orderContainer;
    @FXML private ScrollPane orderScrollPane;
    @FXML private Label countLabel;
    @FXML private Label messageLabel;

    private final CommandeService commandeService = new CommandeService();
    private final PaiementService paiementService = new PaiementService();
    private final FacturePdfService facturePdfService = new FacturePdfService();
    private final UserService userService = new UserService();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        int userId = ManagerSession.getInstance().getCurrentUserId();

        try {
            User user = userService.getById(userId);
            System.out.println(user);
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        initialiserVue();
        chargerCommandes();
    }

    private void initialiserVue() {
        if (countLabel != null) countLabel.setText("0 commande");
        if (messageLabel != null) messageLabel.setText("");
        if (orderContainer != null) {
            orderContainer.setPrefColumns(1);
            orderContainer.setHgap(18);
            orderContainer.setVgap(18);
            orderContainer.setTileAlignment(Pos.TOP_LEFT);
        }
        if (orderScrollPane != null) orderScrollPane.setFocusTraversable(false);
    }

    private void chargerCommandes() {
        try {
            int userId = ManagerSession.getInstance().getCurrentUserId();

            // Annuler automatiquement les commandes expirées (> 3 jours sans paiement)
            int nbExpirees = commandeService.annulerCommandesExpirees(userId);
            if (nbExpirees > 0) {
                showInfo("⚠ " + nbExpirees + " commande(s) annulée(s) automatiquement — délai de paiement dépassé.");
            }

            List<Commande> commandes = commandeService.findByUser(userId);
            List<Commande> commandesSansPanier = commandes.stream()
                    .filter(c -> c.getStatut() != Commande.Statut.PANIER)
                    .collect(java.util.stream.Collectors.toList());
            afficherCommandes(commandesSansPanier);
        } catch (Exception e) {
            showError("Erreur chargement : " + e.getMessage());
        }
    }

    private void afficherCommandes(List<Commande> commandes) {
        if (orderContainer == null) return;
        orderContainer.getChildren().clear();

        if (commandes == null || commandes.isEmpty()) {
            if (countLabel != null) countLabel.setText("0 commande");

            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));

            Label ico = new Label("📋");
            ico.setStyle("-fx-font-size: 48px;");

            Label msg = new Label("Aucune commande pour le moment.");
            msg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 700;");

            Button btn = new Button("🛍 Voir le catalogue");
            btn.setFocusTraversable(false);
            btn.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 12; -fx-padding: 10 22; -fx-cursor: hand;");
            btn.setOnAction(e -> goToProduits());

            empty.getChildren().addAll(ico, msg, btn);
            orderContainer.getChildren().add(empty);

            Platform.runLater(() -> {
                if (orderScrollPane != null) {
                    orderScrollPane.setVvalue(0);
                    orderScrollPane.setHvalue(0);
                }
            });
            return;
        }

        if (countLabel != null) countLabel.setText(commandes.size() + " commande(s)");

        for (Commande c : commandes) {
            orderContainer.getChildren().add(creerCarteCommande(c));
        }

        Platform.runLater(() -> {
            if (orderScrollPane != null) {
                orderScrollPane.setVvalue(0);
                orderScrollPane.setHvalue(0);
            }
        });
    }

    private VBox creerCarteCommande(Commande commande) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setPrefWidth(980);
        card.setMinWidth(980);
        card.setMaxWidth(980);
        card.setFocusTraversable(false);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Commande.Statut st = commande.getStatut() != null ? commande.getStatut() : Commande.Statut.ANNULEE;

        Label iconeLbl = new Label(getIconStatut(st));
        iconeLbl.setStyle("-fx-font-size: 22px;");

        Label idLbl = new Label("Commande #" + (commande.getId() != null ? commande.getId() : "—"));
        idLbl.setStyle("-fx-text-fill: #111827; -fx-font-size: 18px; -fx-font-weight: 800;");
        HBox.setHgrow(idLbl, Priority.ALWAYS);

        Label statutBadge = creerStatutBadge(st);
        header.getChildren().addAll(iconeLbl, idLbl, statutBadge);

        String dateTxt = commande.getDateCommande() != null ? "📅 " + commande.getDateCommande().format(FMT) : "📅 —";
        Label dateLbl = new Label(dateTxt);
        dateLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");

        int nbLignes = commande.getLignes() != null ? commande.getLignes().size() : 0;
        Label articlesLbl = new Label("🛍 " + nbLignes + " article(s)");
        articlesLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-weight: 700;");

        BigDecimal montant = commande.getMontantTotal() != null ? commande.getMontantTotal() : BigDecimal.ZERO;
        Label montantLbl = new Label(montant.toPlainString() + " DT");
        montantLbl.setStyle("-fx-text-fill: #F82239; -fx-font-size: 20px; -fx-font-weight: 900;");

        card.getChildren().addAll(header, dateLbl, articlesLbl, montantLbl);

        // Badge expiration pour les commandes EN_ATTENTE_PAIEMENT
        if (st == Commande.Statut.EN_ATTENTE_PAIEMENT) {
            long heuresRestantes = commandeService.heuresAvantExpiration(commande);
            if (heuresRestantes > 0 && heuresRestantes <= 24) {
                // Moins de 24h → badge rouge urgent
                Label expireBadge = new Label("⏰ Expire dans " + heuresRestantes + "h — Payez maintenant !");
                expireBadge.setStyle(
                        "-fx-background-color: rgba(248,34,57,0.10); -fx-text-fill: #F82239;" +
                                "-fx-font-size: 12px; -fx-font-weight: 800;" +
                                "-fx-padding: 6 12; -fx-background-radius: 8;"
                );
                card.getChildren().add(expireBadge);
            } else if (heuresRestantes > 24 && heuresRestantes <= 72) {
                // Entre 24h et 72h → badge bleu informatif
                long joursRestants = heuresRestantes / 24;
                Label expireBadge = new Label("⏳ " + joursRestants + " jour(s) restant(s) pour payer");
                expireBadge.setStyle(
                        "-fx-background-color: rgba(47,96,245,0.08); -fx-text-fill: #2F60F5;" +
                                "-fx-font-size: 12px; -fx-font-weight: 800;" +
                                "-fx-padding: 6 12; -fx-background-radius: 8;"
                );
                card.getChildren().add(expireBadge);
            }
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (st == Commande.Statut.EN_ATTENTE_PAIEMENT) {
            Button payerBtn = new Button("💳 Payer maintenant");
            payerBtn.setFocusTraversable(false);
            payerBtn.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand;");
            payerBtn.setOnMouseEntered(e -> payerBtn.setStyle("-fx-background-color: linear-gradient(to right, #D01E32, #1D4ED8); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(248,34,57,0.4), 12, 0.2, 0, 3);"));
            payerBtn.setOnMouseExited(e -> payerBtn.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand;"));
            payerBtn.setOnAction(e -> payerCommande(commande));

            Button annulerBtn = creerBoutonAnnuler(commande);
            actions.getChildren().addAll(annulerBtn, payerBtn);
            card.getChildren().add(actions);

        } else if (st == Commande.Statut.PAYEE || st == Commande.Statut.LIVREE) {
            Button pdfBtn = new Button("📄 Télécharger la facture PDF");
            pdfBtn.setFocusTraversable(false);
            pdfBtn.setStyle("-fx-background-color: linear-gradient(to right, #10B981, #059669); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand;");
            pdfBtn.setOnMouseEntered(e -> pdfBtn.setStyle("-fx-background-color: linear-gradient(to right, #0F9F74, #047857); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand;"));
            pdfBtn.setOnMouseExited(e -> pdfBtn.setStyle("-fx-background-color: linear-gradient(to right, #10B981, #059669); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand;"));
            pdfBtn.setOnAction(e -> telechargerFacturePdf(commande));

            actions.getChildren().add(pdfBtn);
            card.getChildren().add(actions);

        } else if (st == Commande.Statut.PANIER) {
            Button annulerBtn = creerBoutonAnnuler(commande);
            actions.getChildren().add(annulerBtn);
            card.getChildren().add(actions);
        }

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: rgba(47,96,245,0.25); -fx-border-radius: 20; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(16,24,40,0.10), 18, 0.15, 0, 5); -fx-translate-y: -1;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2); -fx-translate-y: 0;"));

        return card;
    }

    private String getIconStatut(Commande.Statut st) {
        if (st == null) return "❓";
        return switch (st) {
            case PANIER -> "🛒";
            case EN_ATTENTE_PAIEMENT -> "⏳";
            case PAYEE -> "💳";
            case LIVREE -> "📦";
            case ANNULEE -> "❌";
        };
    }

    private Button creerBoutonAnnuler(Commande commande) {
        Button btn = new Button("❌ Annuler");
        btn.setFocusTraversable(false);

        String styleNormal = "-fx-background-color: rgba(248,34,57,0.08); -fx-text-fill: #DC2626; -fx-font-weight: 800; -fx-border-color: rgba(248,34,57,0.25); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 16; -fx-cursor: hand;";
        String styleHover  = "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 9 16; -fx-cursor: hand;";

        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));
        btn.setOnAction(e -> annulerCommande(commande));
        return btn;
    }

    private Label creerStatutBadge(Commande.Statut statut) {
        String txt;
        String style;

        switch (statut) {
            case PANIER -> {
                txt = "🛒 Panier";
                style = "-fx-background-color: rgba(47,96,245,0.10); -fx-text-fill: #1D4ED8;";
            }
            case EN_ATTENTE_PAIEMENT -> {
                txt = "⏳ En attente paiement";
                style = "-fx-background-color: rgba(245,158,11,0.12); -fx-text-fill: #B45309;";
            }
            case PAYEE -> {
                txt = "💳 Payée";
                style = "-fx-background-color: rgba(16,185,129,0.10); -fx-text-fill: #0F766E;";
            }
            case LIVREE -> {
                txt = "📦 Livrée ✓";
                style = "-fx-background-color: rgba(47,96,245,0.10); -fx-text-fill: #2F60F5;";
            }
            case ANNULEE -> {
                txt = "❌ Annulée";
                style = "-fx-background-color: rgba(248,34,57,0.10); -fx-text-fill: #DC2626;";
            }
            default -> {
                txt = statut.name();
                style = "-fx-background-color: rgba(107,114,128,0.10); -fx-text-fill: #6B7280;";
            }
        }

        Label badge = new Label(txt);
        badge.setFocusTraversable(false);
        badge.setStyle(style + " -fx-font-size: 12px; -fx-font-weight: 800; -fx-padding: 7 14; -fx-background-radius: 999;");
        return badge;
    }

    private void payerCommande(Commande commande) {
        try {
            paiementService.payerCommandeAvecStripe(
                    commande,
                    () -> {
                        showInfo("✅ Paiement Stripe confirmé pour la commande #" + commande.getId() + ".");
                        chargerCommandes();
                    },
                    this::showInfo,
                    this::showError
            );
        } catch (Exception e) {
            showError("❌ " + e.getMessage());
        }
    }

    private void telechargerFacturePdf(Commande commande) {
        try {
            Stage stage = (Stage) orderContainer.getScene().getWindow();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Enregistrer la facture PDF");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            chooser.setInitialFileName("facture-" + commande.getId() + ".pdf");

            File file = chooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }

            facturePdfService.genererFacture(commande, file);
            showInfo("✅ Facture PDF téléchargée : " + file.getName());

        } catch (Exception e) {
            showError("❌ Erreur génération facture : " + e.getMessage());
        }
    }

    private void annulerCommande(Commande commande) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Annulation");
        dlg.setHeaderText("Annuler la commande #" + commande.getId());
        dlg.setContentText("Cette action est irréversible. Confirmer ?");
        dlg.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    commandeService.annuler(commande);
                    chargerCommandes();
                    showInfo("✅ Commande annulée.");
                } catch (Exception e) {
                    showError("❌ " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void goToProduits() {
        try {
            NavigationUtil.navigate((Stage) orderContainer.getScene().getWindow(), "/fxml/patient/produit-list-patient.fxml");
        } catch (Exception e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToPanier() {
        try {
            NavigationUtil.navigate((Stage) orderContainer.getScene().getWindow(), "/fxml/patient/panier-patient.fxml");
        } catch (Exception e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToCommandes() {
        chargerCommandes();
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