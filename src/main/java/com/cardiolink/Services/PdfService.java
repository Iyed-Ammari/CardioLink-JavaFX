package com.cardiolink.Services;

import com.cardiolink.Models.DossierMedical;
import com.cardiolink.Models.User;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PdfService {

    // ✅ PAS de constantes statiques iText ici !

    public File generateDossierPdf(User patient,
                                   DossierMedical dossier,
                                   String outputPath) throws Exception {

        // ✅ Créer les couleurs DANS la méthode
        DeviceRgb primary = new DeviceRgb(127, 119, 221);
        DeviceRgb red     = new DeviceRgb(226, 75,  74);
        DeviceRgb bgRow   = new DeviceRgb(240, 240, 251);

        PdfWriter   writer   = new PdfWriter(outputPath);
        PdfDocument pdf      = new PdfDocument(writer);
        Document    document = new Document(pdf);

        PdfFont fontBold   = PdfFontFactory.createFont(
                StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(
                StandardFonts.HELVETICA);

        // ── En-tete ───────────────────────────────────────────
        document.add(new Paragraph("CardioLink")
                .setFont(fontBold)
                .setFontSize(26)
                .setFontColor(red)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Dossier Medical")
                .setFont(fontBold)
                .setFontSize(18)
                .setFontColor(primary)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Date : " +
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(fontNormal)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph(" "));

        // ── Infos patient ─────────────────────────────────────
        document.add(new Paragraph("Informations du Patient")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(primary));

        Table infoTable = new Table(
                UnitValue.createPercentArray(new float[]{40, 60}))
                .useAllAvailableWidth();

        addRow(infoTable, "Nom",
                nvl(patient.getNom()), fontBold, fontNormal, bgRow);
        addRow(infoTable, "Prenom",
                nvl(patient.getPrenom()), fontBold, fontNormal, bgRow);
        addRow(infoTable, "Email",
                nvl(patient.getEmail()), fontBold, fontNormal, bgRow);
        addRow(infoTable, "Tel",
                patient.getTel() != null ? patient.getTel() : "-",
                fontBold, fontNormal, bgRow);
        addRow(infoTable, "Adresse",
                patient.getAdresse() != null ? patient.getAdresse() : "-",
                fontBold, fontNormal, bgRow);

        document.add(infoTable);
        document.add(new Paragraph(" "));

        // ── Donnees medicales ─────────────────────────────────
        document.add(new Paragraph("Donnees Medicales")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(primary));

        Table medTable = new Table(
                UnitValue.createPercentArray(new float[]{40, 60}))
                .useAllAvailableWidth();

        addRow(medTable, "Groupe Sanguin",
                nvl(dossier.getGroupeSanguin()),
                fontBold, fontNormal, bgRow);
        addRow(medTable, "Poids",
                dossier.getPoids() != null ?
                        dossier.getPoids() + " kg" : "-",
                fontBold, fontNormal, bgRow);
        addRow(medTable, "Taille",
                dossier.getTaille() != null ?
                        dossier.getTaille() + " cm" : "-",
                fontBold, fontNormal, bgRow);
        addRow(medTable, "Tension Systolique",
                dossier.getTensionSystolique() != null ?
                        dossier.getTensionSystolique() + " mmHg" : "-",
                fontBold, fontNormal, bgRow);
        addRow(medTable, "Tension Diastolique",
                dossier.getTensionDiastolique() != null ?
                        dossier.getTensionDiastolique() + " mmHg" : "-",
                fontBold, fontNormal, bgRow);
        addRow(medTable, "Frequence Cardiaque",
                dossier.getFrequenceCardiaque() != null ?
                        dossier.getFrequenceCardiaque() + " bpm" : "-",
                fontBold, fontNormal, bgRow);

        document.add(medTable);
        document.add(new Paragraph(" "));

        // ── IMC ───────────────────────────────────────────────
        if (dossier.getPoids()  != null &&
                dossier.getTaille() != null &&
                dossier.getTaille() > 0) {

            double tailleM = dossier.getTaille() / 100.0;
            double imc     = dossier.getPoids() / (tailleM * tailleM);

            document.add(new Paragraph("IMC Calcule")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setFontColor(primary));

            Table imcTable = new Table(
                    UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth();

            addRow(imcTable, "IMC",
                    String.format("%.2f", imc),
                    fontBold, fontNormal, bgRow);
            addRow(imcTable, "Categorie",
                    getImcCategorie(imc),
                    fontBold, fontNormal, bgRow);

            document.add(imcTable);
            document.add(new Paragraph(" "));
        }

        // ── Antecedents ───────────────────────────────────────
        if (dossier.getAntecedents() != null &&
                !dossier.getAntecedents().isEmpty()) {
            document.add(new Paragraph("Antecedents")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setFontColor(primary));
            document.add(new Paragraph(dossier.getAntecedents())
                    .setFont(fontNormal)
                    .setFontSize(11));
            document.add(new Paragraph(" "));
        }

        // ── Allergies ─────────────────────────────────────────
        if (dossier.getAllergies() != null &&
                !dossier.getAllergies().isEmpty()) {
            document.add(new Paragraph("Allergies")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setFontColor(primary));
            document.add(new Paragraph(dossier.getAllergies())
                    .setFont(fontNormal)
                    .setFontSize(11));
            document.add(new Paragraph(" "));
        }

        // ── Pied de page ──────────────────────────────────────
        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph(
                "Document genere par CardioLink - Confidentiel")
                .setFont(fontNormal)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        return new File(outputPath);
    }

    // ✅ bgRow passé en paramètre au lieu d'être statique
    private void addRow(Table table,
                        String label,
                        String value,
                        PdfFont fontBold,
                        PdfFont fontNormal,
                        DeviceRgb bgRow) {
        table.addCell(new Cell()
                .add(new Paragraph(label)
                        .setFont(fontBold)
                        .setFontSize(11))
                .setBackgroundColor(bgRow));
        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "-")
                        .setFont(fontNormal)
                        .setFontSize(11)));
    }

    private String getImcCategorie(double imc) {
        if (imc < 18.5) return "Insuffisance ponderale";
        if (imc < 25.0) return "Poids normal";
        if (imc < 30.0) return "Surpoids";
        return "Obesite";
    }

    private String nvl(String s) {
        return s != null && !s.isEmpty() ? s : "-";
    }
}