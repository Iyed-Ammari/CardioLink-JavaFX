package com.cardiolink.Controllers;

import com.cardiolink.Services.CommandeService;
import com.cardiolink.Services.ProduitService;
import com.cardiolink.Services.UserService;
import com.cardiolink.Models.Commande;
import com.cardiolink.Models.Produit;
import com.cardiolink.Models.User;
import com.cardiolink.utils.ManagerSession;
import com.cardiolink.utils.NavigationUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label heroTitleLabel;
    @FXML private Label heroSubtitleLabel;
    @FXML private Label totalProduitsLabel;
    @FXML private Label ruptureBadge;
    @FXML private Label caLabel;
    @FXML private Label totalCommandesLabel;
    @FXML private Label nbAttenteLabel;
    @FXML private Label nbPayeesLabel;
    @FXML private Label nbLivreesLabel;
    @FXML private Label nbAnnuleesLabel;

    private final CommandeService commandeService = new CommandeService();
    private final ProduitService produitService = new ProduitService();
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // ✅ LIGNES DEMANDÉES PAR TA COLLÈGUE (respectées)
        int userId = ManagerSession.getInstance().getCurrentUserId();
        try {
            User user = userService.getById(userId);
            System.out.println(user);
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        // ✅ TON CODE ORIGINAL (inchangé)
        chargerStats();
    }

    private void chargerStats() {
        try {
            List<Produit> produits = produitService.getall();
            int totalProduits = produits.size();

            long enRupture = produits.stream()
                    .filter(p -> p.getStock() != null && p.getStock() == 0)
                    .count();

            if (totalProduitsLabel != null) {
                totalProduitsLabel.setText(String.valueOf(totalProduits));
            }

            if (ruptureBadge != null) {
                ruptureBadge.setText(
                        enRupture > 0
                                ? "⚠ " + enRupture + " en rupture de stock"
                                : "✓ Tous disponibles"
                );

                ruptureBadge.setStyle(
                        enRupture > 0
                                ? "-fx-text-fill: #DC2626; -fx-font-size: 11px; -fx-font-weight: 700;"
                                : "-fx-text-fill: #0F766E; -fx-font-size: 11px; -fx-font-weight: 700;"
                );
            }

            int totalCommandes = commandeService.getAllNonPanier().size();
            BigDecimal ca = commandeService.getChiffreAffaires();
            int nbAttente = commandeService.countByStatut(Commande.Statut.EN_ATTENTE_PAIEMENT);
            int nbPayees = commandeService.countByStatut(Commande.Statut.PAYEE);
            int nbLivrees = commandeService.countByStatut(Commande.Statut.LIVREE);
            int nbAnnulees = commandeService.countByStatut(Commande.Statut.ANNULEE);

            if (totalCommandesLabel != null) totalCommandesLabel.setText(String.valueOf(totalCommandes));
            if (caLabel != null) caLabel.setText(ca.toPlainString() + " DT");
            if (nbAttenteLabel != null) nbAttenteLabel.setText(String.valueOf(nbAttente));
            if (nbPayeesLabel != null) nbPayeesLabel.setText(String.valueOf(nbPayees));
            if (nbLivreesLabel != null) nbLivreesLabel.setText(String.valueOf(nbLivrees));
            if (nbAnnuleesLabel != null) nbAnnuleesLabel.setText(String.valueOf(nbAnnulees));

        } catch (Exception e) {
            System.err.println("❌ Dashboard stats : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        chargerStats();
    }

    @FXML
    private void goToDashboard() {
        chargerStats();
    }

    @FXML
    private void goToProduits() {
        try {
            Stage stage = (Stage) heroTitleLabel.getScene().getWindow();
            NavigationUtil.navigate(stage, "/fxml/admin/produit-list-admin.fxml");
        } catch (Exception e) {
            System.err.println("❌ AdminDashboard → Produits : " + e.getMessage());
        }
    }

    @FXML
    private void goToAjouterProduit() {
        try {
            Stage stage = (Stage) heroTitleLabel.getScene().getWindow();
            NavigationUtil.navigate(stage, "/fxml/admin/produit-form-admin.fxml");
        } catch (Exception e) {
            System.err.println("❌ AdminDashboard → AjouterProduit : " + e.getMessage());
        }
    }

    @FXML
    private void goToCommandes() {
        try {
            Stage stage = (Stage) heroTitleLabel.getScene().getWindow();
            NavigationUtil.navigate(stage, "/fxml/admin/commande-list-admin.fxml");
        } catch (Exception e) {
            System.err.println("❌ AdminDashboard → Commandes : " + e.getMessage());
        }
    }

    @FXML
    private void goToPredictionIA() {
        try {
            Stage stage = (Stage) heroTitleLabel.getScene().getWindow();
            NavigationUtil.navigate(stage, "/fxml/admin/prediction-ia.fxml");
        } catch (Exception e) {
            System.err.println("❌ AdminDashboard → Prédiction IA : " + e.getMessage());
        }
    }
}
