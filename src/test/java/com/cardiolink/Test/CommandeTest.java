package com.cardiolink.Test;

import com.cardiolink.Models.Commande;
import com.cardiolink.Models.LigneCommande;
import com.cardiolink.Models.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class CommandeTest {

    private Commande commande;

    @BeforeEach
    void setUp() {
        commande = new Commande();
        commande.setUserId(4);
    }

    @Test
    void testStatutInitialEstPanier() {
        assertEquals(Commande.Statut.PANIER, commande.getStatut());
    }

    @Test
    void testMontantInitialZero() {
        assertEquals(0, commande.getMontantTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testLignesInitialesVides() {
        assertNotNull(commande.getLignes());
        assertTrue(commande.getLignes().isEmpty());
    }

    @Test
    void testCanEditPanierTrueQuandPanier() {
        assertTrue(commande.canEditPanier());
    }

    @Test
    void testCanEditPanierFalseQuandAnnulee() {
        commande.setStatut(Commande.Statut.ANNULEE);
        assertFalse(commande.canEditPanier());
    }

    @Test
    void testCanEditPanierFalseQuandPayee() {
        commande.setStatut(Commande.Statut.PAYEE);
        assertFalse(commande.canEditPanier());
    }

    @Test
    void testValiderCommandePanierVideEchoue() {
        assertThrows(IllegalStateException.class,
                () -> commande.validerCommande());
    }

    @Test
    void testValiderCommandeAvecLigneReussit() {
        commande.addLigne(creerLigneTest(1, new BigDecimal("29.99")));
        commande.validerCommande();
        assertEquals(Commande.Statut.EN_ATTENTE_PAIEMENT, commande.getStatut());
    }

    @Test
    void testValiderCommandeDejaValideeEchoue() {
        commande.addLigne(creerLigneTest(1, new BigDecimal("29.99")));
        commande.validerCommande();
        assertThrows(IllegalStateException.class,
                () -> commande.validerCommande());
    }

    @Test
    void testAnnulerDepuisPanier() {
        commande.annuler();
        assertEquals(Commande.Statut.ANNULEE, commande.getStatut());
    }

    @Test
    void testAnnulerDepuisEnAttente() {
        commande.setStatut(Commande.Statut.EN_ATTENTE_PAIEMENT);
        commande.annuler();
        assertEquals(Commande.Statut.ANNULEE, commande.getStatut());
    }

    @Test
    void testAnnulerCommandePayeeImpossible() {
        commande.setStatut(Commande.Statut.PAYEE);
        assertThrows(IllegalStateException.class,
                () -> commande.annuler());
    }

    @Test
    void testAnnulerCommandeLivreeImpossible() {
        commande.setStatut(Commande.Statut.LIVREE);
        assertThrows(IllegalStateException.class,
                () -> commande.annuler());
    }

    @Test
    void testMarquerPayeeDepuisEnAttentePaiement() {
        commande.setStatut(Commande.Statut.EN_ATTENTE_PAIEMENT);
        commande.marquerPayee();
        assertEquals(Commande.Statut.PAYEE, commande.getStatut());
    }

    @Test
    void testMarquerPayeeDepuisPanierEchoue() {
        assertThrows(IllegalStateException.class,
                () -> commande.marquerPayee());
    }

    @Test
    void testMarquerLivreeDepuisPayee() {
        commande.setStatut(Commande.Statut.PAYEE);
        commande.marquerLivree();
        assertEquals(Commande.Statut.LIVREE, commande.getStatut());
    }

    @Test
    void testMarquerLivreeSansPaiementEchoue() {
        assertThrows(IllegalStateException.class,
                () -> commande.marquerLivree());
    }

    @Test
    void testRecalculateTotalUneLigne() {
        commande.addLigne(creerLigneTest(2, new BigDecimal("19.99")));
        assertEquals(new BigDecimal("39.98"), commande.getMontantTotal());
    }

    @Test
    void testRecalculateTotalDeuxLignes() {
        commande.addLigne(creerLigneTest(1, new BigDecimal("100.00")));
        commande.addLigne(creerLigneTest(3, new BigDecimal("10.00")));
        assertEquals(new BigDecimal("130.00"), commande.getMontantTotal());
    }

    @Test
    void testMontantTotalApresViderLignes() {
        commande.addLigne(creerLigneTest(2, new BigDecimal("50.00")));
        commande.setLignes(new java.util.ArrayList<>());
        assertEquals(BigDecimal.ZERO.setScale(2), commande.getMontantTotal());
    }

    @Test
    void testUserIdValide() {
        commande.setUserId(10);
        assertEquals(10, commande.getUserId());
    }

    @Test
    void testUserIdZeroRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> commande.setUserId(0));
    }

    @Test
    void testUserIdNegatifRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> commande.setUserId(-5));
    }

    @Test
    void testUserIdNullRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> commande.setUserId(null));
    }

    @Test
    void testMontantTotalNegatifRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> commande.setMontantTotal(new BigDecimal("-1")));
    }

    @Test
    void testMontantTotalNullRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> commande.setMontantTotal(null));
    }

    private LigneCommande creerLigneTest(int quantite, BigDecimal prixUnitaire) {
        Produit p = new Produit();
        p.setId(1);
        p.setNom("Produit Test");
        p.setPrix(prixUnitaire);
        p.setStock(100);
        p.setCategorie("MEDICAL");

        LigneCommande l = new LigneCommande();
        l.setCommandeId(1);
        l.setQuantite(quantite);
        l.setPrixUnitaire(prixUnitaire);
        l.setProduit(p);
        return l;
    }
}