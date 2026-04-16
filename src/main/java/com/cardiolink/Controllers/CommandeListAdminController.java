package com.cardiolink.Controllers;

import com.cardiolink.Models.Commande;
import com.cardiolink.Services.CommandeService;
import com.cardiolink.utils.NavigationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CommandeListAdminController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statutFilterBox;
    @FXML private TilePane commandeContainer;
    @FXML private ScrollPane commandeScrollPane;
    @FXML private Label messageLabel;
    @FXML private Label totalCommandesLabel;
    @FXML private Label totalCALabel;
    @FXML private Label nbPayeesLabel;
    @FXML private Label countLabel;

    private final CommandeService commandeService = new CommandeService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    private List<Commande> toutesLesCommandes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (statutFilterBox != null) {
            statutFilterBox.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE_PAIEMENT", "PAYEE", "LIVREE", "ANNULEE"));
            statutFilterBox.setValue("Tous");
            statutFilterBox.setOnAction(e -> appliquerFiltres());
        }

        if (commandeContainer != null) {
            commandeContainer.setPrefColumns(1);
            commandeContainer.setHgap(14);
            commandeContainer.setVgap(14);
            commandeContainer.setTileAlignment(Pos.TOP_LEFT);
        }

        if (commandeScrollPane != null) {
            commandeScrollPane.setFocusTraversable(false);
        }

        chargerCommandes();
    }

    private void chargerCommandes() {
        try {
            toutesLesCommandes = commandeService.getAllNonPanier();
            mettreAJourStats(toutesLesCommandes);
            afficherCommandes(toutesLesCommandes);
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void mettreAJourStats(List<Commande> liste) {
        if (totalCommandesLabel != null) totalCommandesLabel.setText(String.valueOf(liste.size()));
        if (totalCALabel != null) totalCALabel.setText(commandeService.getChiffreAffaires().toPlainString() + " DT");
        if (nbPayeesLabel != null) nbPayeesLabel.setText(String.valueOf(commandeService.countByStatut(Commande.Statut.PAYEE)));
    }

    private void afficherCommandes(List<Commande> commandes) {
        if (commandeContainer == null) return;
        commandeContainer.getChildren().clear();

        if (commandes == null || commandes.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));

            Label ico = new Label("📋");
            ico.setStyle("-fx-font-size: 48px;");

            Label msg = new Label("Aucune commande trouvée.");
            msg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 700;");

            empty.getChildren().addAll(ico, msg);
            commandeContainer.getChildren().add(empty);

            if (countLabel != null) countLabel.setText("0 commande");

            Platform.runLater(() -> {
                if (commandeScrollPane != null) {
                    commandeScrollPane.setVvalue(0);
                    commandeScrollPane.setHvalue(0);
                }
            });
            return;
        }

        if (countLabel != null) countLabel.setText(commandes.size() + " commande(s)");

        for (Commande c : commandes) {
            commandeContainer.getChildren().add(creerCarteCommande(c));
        }

        Platform.runLater(() -> {
            if (commandeScrollPane != null) {
                commandeScrollPane.setVvalue(0);
                commandeScrollPane.setHvalue(0);
            }
        });
    }

    private HBox creerCarteCommande(Commande commande) {
        HBox card = new HBox(18);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setFocusTraversable(false);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-radius: 16; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);");

        VBox numBox = new VBox(4);
        numBox.setAlignment(Pos.CENTER);
        numBox.setPrefWidth(80);
        numBox.setMinWidth(80);
        numBox.setFocusTraversable(false);
        numBox.setStyle("-fx-background-color: " + getGradientForStatut(commande.getStatut()) + "; -fx-background-radius: 14; -fx-padding: 10 6;");

        Label numIco = new Label(getIconForStatut(commande.getStatut()));
        numIco.setStyle("-fx-font-size: 20px;");

        Label numLbl = new Label("#" + commande.getId());
        numLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 900;");

        numBox.getChildren().addAll(numIco, numLbl);

        VBox infos = new VBox(5);
        HBox.setHgrow(infos, Priority.ALWAYS);
        infos.setFocusTraversable(false);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label userLbl = new Label("👤 Patient #" + commande.getUserId());
        userLbl.setStyle("-fx-text-fill: #111827; -fx-font-size: 15px; -fx-font-weight: 800;");

        Label statutBadge = creerStatutBadge(commande.getStatut());
        topRow.getChildren().addAll(userLbl, statutBadge);

        String dateTxt = commande.getDateCommande() != null
                ? "📅 " + commande.getDateCommande().format(FORMATTER)
                : "📅 —";

        Label dateLbl = new Label(dateTxt);
        dateLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        int nbLignes = commande.getLignes() != null ? commande.getLignes().size() : 0;
        Label articlesLbl = new Label("🛍 " + nbLignes + " article(s)");
        articlesLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-font-weight: 700;");

        infos.getChildren().addAll(topRow, dateLbl, articlesLbl);

        BigDecimal montant = commande.getMontantTotal() != null ? commande.getMontantTotal() : BigDecimal.ZERO;
        Label montantLbl = new Label(montant.toPlainString() + " DT");
        montantLbl.setStyle("-fx-text-fill: #F82239; -fx-font-size: 22px; -fx-font-weight: 900; -fx-pref-width: 130; -fx-alignment: CENTER_RIGHT;");

        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setPrefWidth(130);
        actions.setFocusTraversable(false);

        if (commande.getStatut() == Commande.Statut.PAYEE) {
            Button livrerBtn = new Button("📦 Livrer");
            livrerBtn.setPrefWidth(120);
            livrerBtn.setFocusTraversable(false);
            livrerBtn.setStyle("-fx-background-color: linear-gradient(to right, #F82239, #2F60F5); -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 8 12;");
            livrerBtn.setOnAction(e -> marquerCommeLivree(commande));
            actions.getChildren().add(livrerBtn);
        } else if (commande.getStatut() == Commande.Statut.LIVREE) {
            Label doneLbl = new Label("✅ Livrée");
            doneLbl.setStyle("-fx-text-fill: #0F766E; -fx-font-weight: 800; -fx-font-size: 13px;");
            actions.getChildren().add(doneLbl);
        } else if (commande.getStatut() == Commande.Statut.ANNULEE) {
            Label cancelLbl = new Label("❌ Annulée");
            cancelLbl.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 800; -fx-font-size: 13px;");
            actions.getChildren().add(cancelLbl);
        } else {
            Label waitLbl = new Label("⏳ En attente");
            waitLbl.setStyle("-fx-text-fill: #B45309; -fx-font-weight: 800; -fx-font-size: 12px;");
            actions.getChildren().add(waitLbl);
        }

        card.getChildren().addAll(numBox, infos, montantLbl, actions);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: rgba(47,96,245,0.3); -fx-border-radius: 16; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(16,24,40,0.10), 14, 0.15, 0, 4); -fx-translate-y: -1;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-radius: 16; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2); -fx-translate-y: 0;"));

        return card;
    }

    private String getGradientForStatut(Commande.Statut s) {
        if (s == null) return "linear-gradient(to bottom, #6B7280, #9CA3AF)";
        return switch (s) {
            case EN_ATTENTE_PAIEMENT -> "linear-gradient(to bottom, #F59E0B, #D97706)";
            case PAYEE -> "linear-gradient(to bottom, #10B981, #0F766E)";
            case LIVREE -> "linear-gradient(to bottom, #2F60F5, #1D4ED8)";
            case ANNULEE -> "linear-gradient(to bottom, #F82239, #B91C1C)";
            default -> "linear-gradient(to bottom, #6B7280, #9CA3AF)";
        };
    }

    private String getIconForStatut(Commande.Statut s) {
        if (s == null) return "❓";
        return switch (s) {
            case EN_ATTENTE_PAIEMENT -> "⏳";
            case PAYEE -> "💳";
            case LIVREE -> "📦";
            case ANNULEE -> "❌";
            default -> "📋";
        };
    }

    private Label creerStatutBadge(Commande.Statut statut) {
        String txt;
        String style;

        if (statut == null) {
            txt = "Inconnu";
            style = "-fx-background-color: rgba(107,114,128,0.10); -fx-text-fill: #6B7280;";
        } else switch (statut) {
            case EN_ATTENTE_PAIEMENT -> {
                txt = "⏳ En attente";
                style = "-fx-background-color: rgba(245,158,11,0.12); -fx-text-fill: #B45309;";
            }
            case PAYEE -> {
                txt = "💳 Payée";
                style = "-fx-background-color: rgba(16,185,129,0.10); -fx-text-fill: #0F766E;";
            }
            case LIVREE -> {
                txt = "📦 Livrée";
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
        badge.setStyle(style + " -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 5 10; -fx-background-radius: 999;");
        return badge;
    }

    @FXML
    private void handleSearch() {
        appliquerFiltres();
    }

    @FXML
    private void handleReset() {
        if (searchField != null) searchField.clear();
        if (statutFilterBox != null) statutFilterBox.setValue("Tous");
        afficherCommandes(toutesLesCommandes);
        if (messageLabel != null) messageLabel.setText("");
    }

    private void appliquerFiltres() {
        if (toutesLesCommandes == null) return;

        String q = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        String statut = statutFilterBox != null ? statutFilterBox.getValue() : "Tous";

        List<Commande> result = toutesLesCommandes.stream()
                .filter(c -> {
                    if (statut != null && !statut.equals("Tous")) {
                        try {
                            if (c.getStatut() != Commande.Statut.valueOf(statut)) return false;
                        } catch (Exception ignored) {
                        }
                    }

                    if (!q.isEmpty()) {
                        boolean matchUser = String.valueOf(c.getUserId()).contains(q);
                        boolean matchMontant = c.getMontantTotal() != null && c.getMontantTotal().toPlainString().contains(q);
                        boolean matchId = String.valueOf(c.getId()).contains(q);
                        return matchUser || matchMontant || matchId;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        afficherCommandes(result);

        if (result.isEmpty()) showError("Aucun résultat.");
        else showInfo(result.size() + " commande(s) trouvée(s).");
    }

    private void marquerCommeLivree(Commande commande) {
        if (commande.getStatut() != Commande.Statut.PAYEE) {
            showError("Seules les commandes PAYÉES peuvent être livrées.");
            return;
        }

        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Confirmation de livraison");
        dlg.setHeaderText("Livraison commande #" + commande.getId());
        dlg.setContentText("Confirmer la livraison pour le patient #" + commande.getUserId() + " ?");

        dlg.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    commandeService.marquerLivree(commande);
                    chargerCommandes();
                    showInfo("✅ Commande #" + commande.getId() + " marquée LIVRÉE.");
                } catch (Exception e) {
                    showError("Impossible de livrer : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        chargerCommandes();
    }

    @FXML
    private void goToDashboard() {
        try {
            NavigationUtil.navigate(
                    (Stage) commandeContainer.getScene().getWindow(),
                    "/fxml/admin/dashboard-admin.fxml"
            );
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToProduits() {
        try {
            NavigationUtil.navigate(
                    (Stage) commandeContainer.getScene().getWindow(),
                    "/fxml/admin/produit-list-admin.fxml"
            );
        } catch (IOException e) {
            showError("Navigation impossible.");
        }
    }

    @FXML
    private void goToCommandes() {
        chargerCommandes();
    }

    private void showError(String msg) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill: #B42318; -fx-font-size: 13px; -fx-font-weight: 700; -fx-background-color: rgba(248,34,57,0.06); -fx-background-radius: 8; -fx-padding: 8 12;");
        messageLabel.setText(msg);
    }

    private void showInfo(String msg) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill: #0F766E; -fx-font-size: 13px; -fx-font-weight: 700; -fx-background-color: rgba(16,185,129,0.10); -fx-background-radius: 8; -fx-padding: 8 12;");
        messageLabel.setText(msg);
    }
}