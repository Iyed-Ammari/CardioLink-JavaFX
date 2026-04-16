package com.cardiolink.Test;

import com.cardiolink.Models.LigneCommande;
import com.cardiolink.Models.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class LigneCommandeTest {

    private LigneCommande ligne;

    @BeforeEach
    void setUp() {
        ligne = new LigneCommande();
        ligne.setCommandeId(1);
        ligne.setPrixUnitaire(new BigDecimal("50.00"));
    }

    @Test
    void testQuantiteInitialeEstUn() {
        LigneCommande l = new LigneCommande();
        assertEquals(1, l.getQuantite());
    }

    @Test
    void testPrixUnitaireInitialZero() {
        LigneCommande l = new LigneCommande();
        assertEquals(0, l.getPrixUnitaire().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testQuantiteValide() {
        ligne.setQuantite(5);
        assertEquals(5, ligne.getQuantite());
    }

    @Test
    void testQuantiteUnAcceptee() {
        ligne.setQuantite(1);
        assertEquals(1, ligne.getQuantite());
    }

    @Test
    void testQuantiteZeroRefusee() {
        assertThrows(IllegalArgumentException.class,
                () -> ligne.setQuantite(0));
    }

    @Test
    void testQuantiteNegativeRefusee() {
        assertThrows(IllegalArgumentException.class,
                () -> ligne.setQuantite(-3));
    }

    @Test
    void testPrixUnitaireValide() {
        ligne.setPrixUnitaire(new BigDecimal("199.99"));
        assertEquals(new BigDecimal("199.99"), ligne.getPrixUnitaire());
    }

    @Test
    void testPrixUnitaireZeroAccepte() {
        ligne.setPrixUnitaire(BigDecimal.ZERO);
        assertEquals(0, ligne.getPrixUnitaire().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testPrixUnitaireNegatifRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> ligne.setPrixUnitaire(new BigDecimal("-5")));
    }

    @Test
    void testPrixUnitaireNullRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> ligne.setPrixUnitaire(null));
    }

    @Test
    void testTotalLigneCalculCorrect() {
        ligne.setQuantite(3);
        ligne.setPrixUnitaire(new BigDecimal("20.00"));
        assertEquals(new BigDecimal("60.00"), ligne.getTotalLigne());
    }

    @Test
    void testTotalLigneQuantiteUn() {
        ligne.setQuantite(1);
        ligne.setPrixUnitaire(new BigDecimal("109.99"));
        assertEquals(new BigDecimal("109.99"), ligne.getTotalLigne());
    }

    @Test
    void testTotalLigneDixArticles() {
        ligne.setQuantite(10);
        ligne.setPrixUnitaire(new BigDecimal("5.50"));
        assertEquals(new BigDecimal("55.00"), ligne.getTotalLigne());
    }

    @Test
    void testCommandeIdValide() {
        ligne.setCommandeId(42);
        assertEquals(42, ligne.getCommandeId());
    }

    @Test
    void testCommandeIdZeroRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> ligne.setCommandeId(0));
    }

    @Test
    void testCommandeIdNegatifRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> ligne.setCommandeId(-1));
    }

    @Test
    void testCommandeIdNullRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> ligne.setCommandeId(null));
    }

    @Test
    void testSetProduit() {
        Produit p = new Produit();
        p.setId(5);
        p.setNom("Oxymetre de Pouls");
        p.setPrix(new BigDecimal("29.99"));
        p.setStock(200);
        p.setCategorie("MEDICAL");

        ligne.setProduit(p);
        assertNotNull(ligne.getProduit());
        assertEquals("Oxymetre de Pouls", ligne.getProduit().getNom());
    }

    @Test
    void testProduitNullAccepte() {
        ligne.setProduit(null);
        assertNull(ligne.getProduit());
    }

    @Test
    void testToStringContientProduitNull() {
        String str = ligne.toString();
        assertTrue(str.contains("null"));
    }

    @Test
    void testToStringContientNomProduit() {
        Produit p = new Produit();
        p.setId(1);
        p.setNom("ECG Pro");
        p.setPrix(new BigDecimal("199.99"));
        p.setStock(10);
        p.setCategorie("MEDICAL");
        ligne.setProduit(p);

        String str = ligne.toString();
        assertTrue(str.contains("ECG Pro"));
    }
}