package com.cardiolink.Services;

import com.cardiolink.Models.Commande;
import com.cardiolink.Models.LigneCommande;
import com.cardiolink.Models.Produit;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FacturePdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    private static final Color NAVY = new Color(17, 24, 39);
    private static final Color RED = new Color(248, 34, 57);
    private static final Color BLUE = new Color(47, 96, 245);
    private static final Color GREEN = new Color(16, 185, 129);
    private static final Color GREEN_BG = new Color(209, 250, 229);
    private static final Color BLUE_BG = new Color(219, 234, 254);
    private static final Color LIGHT_BG = new Color(249, 250, 251);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color LIGHT_MUTED = new Color(156, 163, 175);
    private static final Color TEXT = new Color(31, 41, 55);

    public void genererFacture(Commande commande, File destination) {
        if (commande == null || commande.getId() == null) {
            throw new IllegalArgumentException("Commande invalide pour génération PDF.");
        }

        if (commande.getStatut() != Commande.Statut.PAYEE && commande.getStatut() != Commande.Statut.LIVREE) {
            throw new IllegalStateException("La facture PDF n'est disponible que pour les commandes PAYEES ou LIVREES.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDFont regular = loadRegularFont(document);
            PDFont bold = loadBoldFont(document);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawFacture(page, content, commande, regular, bold);
            }

            document.save(destination);

        } catch (IOException e) {
            throw new RuntimeException("Erreur génération facture PDF : " + e.getMessage(), e);
        }
    }

    private void drawFacture(PDPage page,
                             PDPageContentStream content,
                             Commande commande,
                             PDFont regular,
                             PDFont bold) throws IOException {

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        float margin = 40f;
        float y = pageHeight - 32f;

        // Bande top dégradée simulée en 2 couleurs
        fillRect(content, 0, pageHeight - 8, pageWidth / 2f, 8, RED);
        fillRect(content, pageWidth / 2f, pageHeight - 8, pageWidth / 2f, 8, BLUE);

        // Header gauche
        writeText(content, "Cardio", margin, y, bold, 28, NAVY);
        writeText(content, "Link", margin + 72, y, bold, 28, RED);
        writeText(content, "Marketplace Médical", margin, y - 18, bold, 11, LIGHT_MUTED);

        writeText(content, "ÉMETTEUR", margin, y - 50, bold, 11, LIGHT_MUTED);
        writeText(content, "CardioLink Tunisia", margin, y - 68, bold, 12, TEXT);
        writeText(content, "Tunis, Tunisie", margin, y - 84, regular, 11, MUTED);
        writeText(content, "contact@cardiolink.tn", margin, y - 100, regular, 11, MUTED);

        // Header droite
        float rightX = pageWidth - 180;
        writeText(content, "FACTURE", rightX, y, bold, 30, NAVY);
        writeText(content, "N° FACT-" + String.format("%06d", commande.getId()), rightX, y - 18, bold, 12, MUTED);

        // date block
        drawFilledRoundedRect(content, rightX - 18, y - 60, 126, 34, 8, LIGHT_BG);
        writeText(content, "DATE D'ÉMISSION", rightX - 8, y - 42, bold, 8, LIGHT_MUTED);
        writeText(
                content,
                commande.getDateCommande() != null ? commande.getDateCommande().format(DATE_FMT) : "—",
                rightX - 8, y - 55, bold, 12, NAVY
        );

        // status badge
        if (commande.getStatut() == Commande.Statut.LIVREE) {
            drawStatusBadge(content, rightX + 25, y - 90, 88, 18, BLUE_BG, BLUE, "LIVREE", regular, bold);
        } else {
            drawStatusBadge(content, rightX + 25, y - 90, 88, 18, GREEN_BG, GREEN, "PAYEE", regular, bold);
        }

        // divider
        drawLine(content, margin, y - 118, pageWidth - margin, y - 118, BORDER);

        // Info section
        float infoY = y - 150;
        drawInfoBlock(content, margin, infoY, "FACTURÉ À", "Utilisateur #" + commande.getUserId(), "Patient CardioLink", regular, bold);
        drawInfoBlock(
                content,
                margin + 185,
                infoY,
                "COMMANDE",
                "#" + commande.getId(),
                commande.getDateCommande() != null ? commande.getDateCommande().format(DATE_TIME_FMT) : "—",
                regular, bold
        );
        drawInfoBlock(
                content,
                margin + 395,
                infoY,
                "RÉFÉRENCE PAIEMENT",
                "PAY-" + commande.getId() + "-CL",
                "Paiement carte bancaire",
                regular, bold
        );

        // divider
        drawLine(content, margin, infoY - 52, pageWidth - margin, infoY - 52, BORDER);

        // Table
        float tableY = infoY - 78;
        float[] cols = {margin, margin + 245, margin + 355, margin + 455, margin + 520, pageWidth - margin};

        drawFilledRect(content, margin, tableY - 16, pageWidth - 2 * margin, 22, LIGHT_BG);
        drawRect(content, margin, tableY - 16, pageWidth - 2 * margin, 22, BORDER);

        writeText(content, "DÉSIGNATION", cols[0] + 10, tableY - 2, bold, 10, MUTED);
        writeText(content, "CATÉGORIE", cols[1] + 10, tableY - 2, bold, 10, MUTED);
        writeText(content, "PRIX UNIT.", cols[2] + 10, tableY - 2, bold, 10, MUTED);
        writeText(content, "QTÉ", cols[3] + 10, tableY - 2, bold, 10, MUTED);
        writeText(content, "TOTAL HT", cols[4] + 10, tableY - 2, bold, 10, MUTED);

        float rowY = tableY - 40;
        List<LigneCommande> lignes = commande.getLignes();
        if (lignes != null) {
            for (LigneCommande ligne : lignes) {
                Produit produit = ligne.getProduit();
                String nom = (produit != null && produit.getNom() != null) ? produit.getNom() : "Produit";
                String categorie = (produit != null && produit.getCategorie() != null) ? produit.getCategorie() : "AUTRE";
                BigDecimal pu = ligne.getPrixUnitaire() != null ? ligne.getPrixUnitaire() : BigDecimal.ZERO;
                BigDecimal totalLigne = ligne.getTotalLigne() != null ? ligne.getTotalLigne() : BigDecimal.ZERO;

                writeText(content, nom, cols[0] + 10, rowY, bold, 12, NAVY);

                drawCategoryBadge(content, cols[1] + 8, rowY - 12, categorie, regular, bold);

                writeTextRight(content, pu.toPlainString() + " TND", cols[3] - 8, rowY, bold, 11, TEXT);
                writeTextRight(content, String.valueOf(ligne.getQuantite()), cols[4] - 8, rowY, bold, 11, TEXT);
                writeTextRight(content, totalLigne.toPlainString() + " TND", cols[5] - 8, rowY, bold, 11, NAVY);

                drawLine(content, margin, rowY - 12, pageWidth - margin, rowY - 12, new Color(243, 244, 246));
                rowY -= 26;
            }
        }

        // Totals block
        BigDecimal montantTotal = commande.getMontantTotal() != null ? commande.getMontantTotal() : BigDecimal.ZERO;

        float totalBlockX = pageWidth - 260;
        float totalBlockY = rowY - 18;

        drawTotalRow(content, totalBlockX, totalBlockY, "Sous-total", montantTotal.toPlainString() + " TND", regular, bold);
        drawTotalRow(content, totalBlockX, totalBlockY - 20, "TVA (0%)", "0.00 TND", regular, bold);
        drawTotalRow(content, totalBlockX, totalBlockY - 40, "Livraison", "Gratuite", regular, bold);

        drawFilledRoundedRect(content, totalBlockX, totalBlockY - 82, 220, 34, 10, NAVY);
        writeText(content, "TOTAL À PAYER", totalBlockX + 14, totalBlockY - 61, bold, 12, Color.WHITE);
        writeTextRight(content, montantTotal.toPlainString() + " TND", totalBlockX + 205, totalBlockY - 61, bold, 20, Color.WHITE);

        // Paid stamp
        float stampY = totalBlockY - 138;
        drawStampedBox(content, (pageWidth - 190) / 2f, stampY, 190, 34, "PAIEMENT RECU", GREEN, regular, bold);

        // Footer
        drawLine(content, margin, 90, pageWidth - margin, 90, BORDER);
        writeCentered(content, "CardioLink Tunisia • Marketplace Médical • Tunis, Tunisie", pageWidth / 2f, 70, bold, 9, TEXT);
        writeCentered(content, "Cette facture est générée automatiquement et fait foi de paiement.", pageWidth / 2f, 56, regular, 8, LIGHT_MUTED);
        writeCentered(content, "Pour toute question : contact@cardiolink.tn", pageWidth / 2f, 42, bold, 8, TEXT);

        // Footer band
        fillRect(content, 0, 0, pageWidth / 2f, 6, BLUE);
        fillRect(content, pageWidth / 2f, 0, pageWidth / 2f, 6, RED);
    }

    private void drawInfoBlock(PDPageContentStream content, float x, float y, String title, String value, String sub,
                               PDFont regular, PDFont bold) throws IOException {
        writeText(content, title, x, y, bold, 9, LIGHT_MUTED);
        writeText(content, value, x, y - 16, bold, 13, NAVY);
        writeText(content, sub, x, y - 31, regular, 11, MUTED);
    }

    private void drawStatusBadge(PDPageContentStream content, float x, float y, float w, float h,
                                 Color bg, Color stroke, String text, PDFont regular, PDFont bold) throws IOException {
        drawFilledRoundedRect(content, x, y, w, h, 9, bg);
        drawRoundedBorder(content, x, y, w, h, 9, stroke);
        writeCentered(content, text, x + w / 2f, y + 5.5f, bold, 10, stroke);
    }

    private void drawCategoryBadge(PDPageContentStream content, float x, float y, String text,
                                   PDFont regular, PDFont bold) throws IOException {
        float w = Math.max(48, text.length() * 5.6f + 16);
        drawFilledRoundedRect(content, x, y, w, 14, 5, new Color(243, 244, 246));
        writeCentered(content, text, x + w / 2f, y + 3.8f, bold, 8, MUTED);
    }

    private void drawTotalRow(PDPageContentStream content, float x, float y, String label, String value,
                              PDFont regular, PDFont bold) throws IOException {
        drawLine(content, x, y - 6, x + 220, y - 6, new Color(243, 244, 246));
        writeText(content, label, x, y, regular, 11, MUTED);
        writeTextRight(content, value, x + 220, y, bold, 11, TEXT);
    }

    private void drawStampedBox(PDPageContentStream content, float x, float y, float w, float h,
                                String text, Color color, PDFont regular, PDFont bold) throws IOException {
        drawRoundedBorder(content, x, y, w, h, 10, color);
        writeCentered(content, text, x + w / 2f, y + 9f, bold, 15, color);
    }

    private PDFont loadRegularFont(PDDocument document) throws IOException {
        return PDType0Font.load(document, findWindowsFont(
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/segoeui.ttf",
                "C:/Windows/Fonts/calibri.ttf"
        ).toFile());
    }

    private PDFont loadBoldFont(PDDocument document) throws IOException {
        return PDType0Font.load(document, findWindowsFont(
                "C:/Windows/Fonts/arialbd.ttf",
                "C:/Windows/Fonts/segoeuib.ttf",
                "C:/Windows/Fonts/calibrib.ttf"
        ).toFile());
    }

    private Path findWindowsFont(String... candidates) {
        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                return path;
            }
        }
        throw new RuntimeException("Aucune police Windows compatible trouvée pour le PDF.");
    }

    private void writeText(PDPageContentStream content, String text, float x, float y,
                           PDFont font, float size, Color color) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.setNonStrokingColor(color);
        content.newLineAtOffset(x, y);
        content.showText(text != null ? text : "");
        content.endText();
    }

    private void writeTextRight(PDPageContentStream content, String text, float rightX, float y,
                                PDFont font, float size, Color color) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * size;
        writeText(content, text, rightX - textWidth, y, font, size, color);
    }

    private void writeCentered(PDPageContentStream content, String text, float centerX, float y,
                               PDFont font, float size, Color color) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * size;
        writeText(content, text, centerX - textWidth / 2f, y, font, size, color);
    }

    private void fillRect(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
        content.setNonStrokingColor(color);
        content.addRect(x, y, width, height);
        content.fill();
    }

    private void drawFilledRect(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
        fillRect(content, x, y, width, height, color);
    }

    private void drawRect(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
        content.setStrokingColor(color);
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void drawLine(PDPageContentStream content, float x1, float y1, float x2, float y2, Color color) throws IOException {
        content.setStrokingColor(color);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private void drawFilledRoundedRect(PDPageContentStream content, float x, float y, float width, float height,
                                       float radius, Color fillColor) throws IOException {
        content.setNonStrokingColor(fillColor);
        addRoundedRectPath(content, x, y, width, height, radius);
        content.fill();
    }

    private void drawRoundedBorder(PDPageContentStream content, float x, float y, float width, float height,
                                   float radius, Color strokeColor) throws IOException {
        content.setStrokingColor(strokeColor);
        addRoundedRectPath(content, x, y, width, height, radius);
        content.stroke();
    }

    private void addRoundedRectPath(PDPageContentStream content, float x, float y, float width, float height,
                                    float radius) throws IOException {
        float k = 0.552284749831f;
        float c = radius * k;

        content.moveTo(x + radius, y);
        content.lineTo(x + width - radius, y);
        content.curveTo(x + width - radius + c, y, x + width, y + radius - c, x + width, y + radius);
        content.lineTo(x + width, y + height - radius);
        content.curveTo(x + width, y + height - radius + c, x + width - radius + c, y + height, x + width - radius, y + height);
        content.lineTo(x + radius, y + height);
        content.curveTo(x + radius - c, y + height, x, y + height - radius + c, x, y + height - radius);
        content.lineTo(x, y + radius);
        content.curveTo(x, y + radius - c, x + radius - c, y, x + radius, y);
        content.closePath();
    }
}