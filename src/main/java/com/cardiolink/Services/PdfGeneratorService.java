package com.cardiolink.Services;

import com.cardiolink.Models.Ordonnance;
import com.cardiolink.Models.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PdfGeneratorService {

    public void genererOrdonnancePDF(String destPath, Ordonnance ordonnance, String patientName, String medecinName) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(destPath));

        document.open();

        // Polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.DARK_GRAY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);

        // En-tête (Informations Médecin)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        String docName = (medecinName != null) ? "Dr. " + medecinName : "Dr. CardioLink";
        leftCell.addElement(new Paragraph(docName, titleFont));
        leftCell.addElement(new Paragraph("Spécialiste en Cardiologie", subtitleFont));
        leftCell.addElement(new Paragraph("CardioLink Médical", normalFont));
        
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph datePara = new Paragraph("Date : " + dateStr, normalFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(datePara);
        
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Ligne de séparation
        document.add(new Chunk(new LineSeparator(1f, 100f, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -10)));
        document.add(new Paragraph("\n"));

        // Informations Patient
        String pName = (patientName != null) ? patientName : "Non renseigné";
        Paragraph patientPara = new Paragraph("Patient : " + pName, boldFont);
        patientPara.setSpacingBefore(10);
        patientPara.setSpacingAfter(20);
        document.add(patientPara);

        // Titre ORDONNANCE
        Paragraph titreOrd = new Paragraph("ORDONNANCE MÉDICALE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(130, 108, 253)));
        titreOrd.setAlignment(Element.ALIGN_CENTER);
        titreOrd.setSpacingAfter(5);
        document.add(titreOrd);
        
        Paragraph refOrd = new Paragraph("Réf : " + ordonnance.getReference(), subtitleFont);
        refOrd.setAlignment(Element.ALIGN_CENTER);
        refOrd.setSpacingAfter(30);
        document.add(refOrd);

        // Diagnostic (Optionnel)
        if (ordonnance.getDiagnostic() != null && !ordonnance.getDiagnostic().isEmpty()) {
            PdfPTable diagTable = new PdfPTable(1);
            diagTable.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell(new Phrase("DIAGNOSTIC", headerFont));
            hCell.setBackgroundColor(new BaseColor(52, 73, 94)); // Gris foncé/bleu
            hCell.setPadding(8);
            diagTable.addCell(hCell);
            
            PdfPCell dCell = new PdfPCell(new Phrase(ordonnance.getDiagnostic(), normalFont));
            dCell.setPadding(10);
            diagTable.addCell(dCell);
            
            document.add(diagTable);
            document.add(new Paragraph("\n"));
        }

        // Médicaments / Prescription
        PdfPTable prescrTable = new PdfPTable(1);
        prescrTable.setWidthPercentage(100);
        PdfPCell pHeader = new PdfPCell(new Phrase("PRESCRIPTION", headerFont));
        pHeader.setBackgroundColor(new BaseColor(130, 108, 253)); // Couleur du thème CardioLink
        pHeader.setPadding(8);
        prescrTable.addCell(pHeader);
        
        PdfPCell pCell = new PdfPCell(new Phrase(ordonnance.getNotes(), normalFont));
        pCell.setPadding(15);
        pCell.setMinimumHeight(150f); // Espace pour donner un effet pro
        prescrTable.addCell(pCell);
        
        document.add(prescrTable);

        // Pied de page (Signature)
        Paragraph signature = new Paragraph("Signature & Cachet du Médecin", subtitleFont);
        signature.setAlignment(Element.ALIGN_RIGHT);
        signature.setSpacingBefore(50);
        document.add(signature);

        document.close();
    }
}
