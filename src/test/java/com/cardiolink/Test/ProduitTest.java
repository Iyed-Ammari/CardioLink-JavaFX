package com.cardiolink.Test;

import com.cardiolink.Models.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ProduitTest {

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = new Produit();
    }

    @Test
    void testNomValide() {
        produit.setNom("Stéthoscope Pro MAX");
        assertEquals("Stéthoscope Pro MAX", produit.getNom());
    }

    @Test
    void testNomNull() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setNom(null));
    }

    @Test
    void testNomVide() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setNom(""));
    }

    @Test
    void testNomTropCourt() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setNom("A"));
    }

    @Test
    void testNomAvecChiffresAccepte() {
        produit.setNom("ECG 12 derivations");
        assertEquals("ECG 12 derivations", produit.getNom());
    }

    @Test
    void testPrixValide() {
        produit.setPrix(new BigDecimal("109.99"));
        assertEquals(new BigDecimal("109.99"), produit.getPrix());
    }

    @Test
    void testPrixZeroRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setPrix(BigDecimal.ZERO));
    }

    @Test
    void testPrixNegatifRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setPrix(new BigDecimal("-1")));
    }

    @Test
    void testPrixNull() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setPrix(null));
    }

    @Test
    void testPrixMaximumDepasse() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setPrix(new BigDecimal("100000")));
    }

    @Test
    void testPrixMaximumAccepte() {
        produit.setPrix(new BigDecimal("99999.99"));
        assertEquals(new BigDecimal("99999.99"), produit.getPrix());
    }

    @Test
    void testStockValide() {
        produit.setStock(100);
        assertEquals(100, produit.getStock());
    }

    @Test
    void testStockZeroAccepte() {
        produit.setStock(0);
        assertEquals(0, produit.getStock());
    }

    @Test
    void testStockNegatifRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setStock(-1));
    }

    @Test
    void testStockNull() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setStock(null));
    }

    @Test
    void testCategorieValide() {
        produit.setCategorie("medical");
        assertEquals("MEDICAL", produit.getCategorie());
    }

    @Test
    void testCategorieConvertieEnMajuscule() {
        produit.setCategorie("accessoire");
        assertEquals("ACCESSOIRE", produit.getCategorie());
    }

    @Test
    void testCategorieVide() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setCategorie(""));
    }

    @Test
    void testCategorieNull() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setCategorie(null));
    }

    @Test
    void testStockStatusRupture() {
        produit.setStock(0);
        assertEquals("RUPTURE", produit.getStockStatus());
    }

    @Test
    void testStockStatusDisponible() {
        produit.setStock(5);
        assertEquals("DISPONIBLE", produit.getStockStatus());
    }

    @Test
    void testImageUrlHttpsValide() {
        produit.setImageUrl("https://example.com/image.png");
        assertEquals("https://example.com/image.png", produit.getImageUrl());
    }

    @Test
    void testImageUrlNullAcceptee() {
        produit.setImageUrl(null);
        assertNull(produit.getImageUrl());
    }

    @Test
    void testImageUrlVideAcceptee() {
        produit.setImageUrl("");
        assertNull(produit.getImageUrl());
    }

    @Test
    void testImageUrlInvalideRefusee() {
        assertThrows(IllegalArgumentException.class,
                () -> produit.setImageUrl("chemin_invalide"));
    }

    @Test
    void testDescriptionValide() {
        produit.setDescription("Mesure SpO2");
        assertEquals("Mesure SpO2", produit.getDescription());
    }

    @Test
    void testDescriptionNullAcceptee() {
        produit.setDescription(null);
        assertNull(produit.getDescription());
    }

    @Test
    void testDescriptionVideRetourneNull() {
        produit.setDescription("   ");
        assertNull(produit.getDescription());
    }
}