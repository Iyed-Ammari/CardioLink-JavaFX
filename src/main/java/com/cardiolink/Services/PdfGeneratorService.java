package com.cardiolink.Services;

import com.cardiolink.Models.Ordonnance;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PdfGeneratorService {

    public void genererOrdonnancePDF(String destPath, Ordonnance ordonnance, String patientName, String medecinName) throws Exception {
        PdfWriter writer = new PdfWriter(new FileOutputStream(destPath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(50, 50, 50, 50);

        // Polices
        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont subtitleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Color darkGray = ColorConstants.DARK_GRAY;
        Color gray = ColorConstants.GRAY;
        Color black = ColorConstants.BLACK;
        Color white = ColorConstants.WHITE;
        Color themeColor = new DeviceRgb(130, 108, 253);
        Color darkBlue = new DeviceRgb(52, 73, 94);
        Color lightGray = ColorConstants.LIGHT_GRAY;

        // En-tête (Informations Médecin)
        Table headerTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER);
        String docName = (medecinName != null) ? "Dr. " + medecinName : "Dr. CardioLink";
        leftCell.add(new Paragraph(docName).setFont(titleFont).setFontSize(24).setFontColor(darkGray));
        leftCell.add(new Paragraph("Spécialiste en Cardiologie").setFont(subtitleFont).setFontSize(14).setFontColor(gray));
        leftCell.add(new Paragraph("CardioLink Médical").setFont(normalFont).setFontSize(12).setFontColor(black));
        
        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        rightCell.add(new Paragraph("Date : " + dateStr).setFont(normalFont).setFontSize(12).setFontColor(black));
        
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Ligne de séparation
        SolidLine line = new SolidLine(1f);
        line.setColor(lightGray);
        LineSeparator ls = new LineSeparator(line);
        ls.setMarginTop(10);
        ls.setMarginBottom(10);
        document.add(ls);

        // Informations Patient
        String pName = (patientName != null) ? patientName : "Non renseigné";
        Paragraph patientPara = new Paragraph("Patient : " + pName).setFont(boldFont).setFontSize(12).setFontColor(black);
        patientPara.setMarginTop(10);
        patientPara.setMarginBottom(20);
        document.add(patientPara);

        // Titre ORDONNANCE
        Paragraph titreOrd = new Paragraph("ORDONNANCE MÉDICALE").setFont(titleFont).setFontSize(18).setFontColor(themeColor);
        titreOrd.setTextAlignment(TextAlignment.CENTER);
        titreOrd.setMarginBottom(5);
        document.add(titreOrd);
        
        Paragraph refOrd = new Paragraph("Réf : " + ordonnance.getReference()).setFont(subtitleFont).setFontSize(14).setFontColor(gray);
        refOrd.setTextAlignment(TextAlignment.CENTER);
        refOrd.setMarginBottom(30);
        document.add(refOrd);

        // Diagnostic (Optionnel)
        if (ordonnance.getDiagnostic() != null && !ordonnance.getDiagnostic().isEmpty()) {
            Table diagTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            Cell hCell = new Cell().add(new Paragraph("DIAGNOSTIC").setFont(headerFont).setFontSize(11).setFontColor(white));
            hCell.setBackgroundColor(darkBlue);
            hCell.setPadding(8);
            diagTable.addCell(hCell);
            
            Cell dCell = new Cell().add(new Paragraph(ordonnance.getDiagnostic()).setFont(normalFont).setFontSize(12).setFontColor(black));
            dCell.setPadding(10);
            diagTable.addCell(dCell);
            
            document.add(diagTable);
            document.add(new Paragraph("\n"));
        }

        // Médicaments / Prescription
        Table prescrTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
        Cell pHeader = new Cell().add(new Paragraph("PRESCRIPTION").setFont(headerFont).setFontSize(11).setFontColor(white));
        pHeader.setBackgroundColor(themeColor);
        pHeader.setPadding(8);
        prescrTable.addCell(pHeader);
        
        Cell pCell = new Cell().add(new Paragraph(ordonnance.getNotes()).setFont(normalFont).setFontSize(12).setFontColor(black));
        pCell.setPadding(15);
        pCell.setMinHeight(150f);
        prescrTable.addCell(pCell);
        
        document.add(prescrTable);

        // Pied de page (Signature)
        Paragraph signature = new Paragraph("Signature & Cachet du Médecin").setFont(subtitleFont).setFontSize(14).setFontColor(gray);
        signature.setTextAlignment(TextAlignment.RIGHT);
        signature.setMarginTop(50);
        document.add(signature);

        document.close();
    }
}
