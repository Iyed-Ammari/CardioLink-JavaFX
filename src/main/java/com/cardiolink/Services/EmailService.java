package com.cardiolink.Services;

import com.cardiolink.Models.Commande;
import com.cardiolink.Models.LigneCommande;
import com.cardiolink.Models.Produit;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service d'envoi d'email de confirmation de paiement via Gmail SMTP.
 * Fonctionne avec n'importe quel email destinataire.
 */
public class EmailService {

    private static final String GMAIL_USER     = "hammamikhadija57@gmail.com";
    private static final String GMAIL_APP_PASS = "ezlklyglgncfdleu"; // mot de passe app Google

    private static final String SENDER_NAME    = "CardioLink Marketplace";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /**
     * Envoie un email de confirmation de paiement au patient.
     *
     * @param destinataireEmail email du patient
     * @param destinataireNom   prénom + nom du patient
     * @param commande          commande payée (avec lignes chargées)
     */
    public void envoyerConfirmationPaiement(String destinataireEmail,
                                            String destinataireNom,
                                            Commande commande) {
        if (destinataireEmail == null || destinataireEmail.isBlank()) {
            System.err.println("⚠ EmailService : email destinataire vide, envoi annulé.");
            return;
        }

        try {
            // ── Configuration Gmail SMTP ──────────────────────────
            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            "smtp.gmail.com");
            props.put("mail.smtp.port",            "587");
            props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(GMAIL_USER, GMAIL_APP_PASS);
                }
            });

            // ── Construction du message ───────────────────────────
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_USER, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(destinataireEmail));
            message.setSubject("✅ Confirmation de votre commande #"
                    + commande.getId() + " — CardioLink");

            // Partie HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(construireHtml(destinataireNom, commande), "text/html; charset=UTF-8");

            // Partie texte brut (fallback)
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(construireTexte(destinataireNom, commande), "UTF-8");

            MimeMultipart multipart = new MimeMultipart("alternative");
            multipart.addBodyPart(textPart); // texte en premier (fallback)
            multipart.addBodyPart(htmlPart); // HTML en dernier (prioritaire)

            message.setContent(multipart);

            // ── Envoi ─────────────────────────────────────────────
            Transport.send(message);

            System.out.println("✅ Email de confirmation envoyé à : " + destinataireEmail);

        } catch (Exception e) {
            // Non bloquant — le paiement reste confirmé même si l'email échoue
            System.err.println("⚠ EmailService : échec envoi email → " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Construction du contenu HTML de l'email
    // ─────────────────────────────────────────────────────────
    private String construireHtml(String nom, Commande commande) {
        StringBuilder sb = new StringBuilder();

        String date = commande.getDateCommande() != null
                ? commande.getDateCommande().format(DATE_FMT)
                : "—";

        BigDecimal montant = commande.getMontantTotal() != null
                ? commande.getMontantTotal()
                : BigDecimal.ZERO;

        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>")
                .append("<body style='font-family:Arial,sans-serif;background:#F9FAFB;margin:0;padding:0;'>")

                // Conteneur
                .append("<div style='max-width:600px;margin:30px auto;background:white;")
                .append("border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>")

                // Header dégradé rouge → bleu (charte CardioLink)
                .append("<div style='background:linear-gradient(to right,#F82239,#2F60F5);padding:28px 32px;'>")
                .append("<h1 style='color:white;margin:0;font-size:24px;'>Cardio")
                .append("<span style='color:#FFD700;'>Link</span></h1>")
                .append("<p style='color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:13px;'>")
                .append("Marketplace Médical</p>")
                .append("</div>")

                // Corps
                .append("<div style='padding:32px;'>")

                // Salutation
                .append("<p style='font-size:16px;color:#111827;'>Bonjour <strong>")
                .append(escapeHtml(nom)).append("</strong>,</p>")
                .append("<p style='color:#374151;font-size:15px;'>")
                .append("Votre paiement a bien été reçu. Merci pour votre commande sur CardioLink !</p>")

                // Badge confirmation
                .append("<div style='background:#D1FAE5;border:1.5px solid #10B981;border-radius:12px;")
                .append("padding:14px 20px;margin:20px 0;display:inline-block;'>")
                .append("<span style='color:#065F46;font-weight:800;font-size:15px;'>")
                .append("&#10003; Paiement confirmé</span></div>")

                // Infos commande
                .append("<div style='background:#F9FAFB;border-radius:12px;padding:18px 20px;margin:20px 0;'>")
                .append("<p style='margin:0 0 8px;color:#6B7280;font-size:12px;font-weight:700;'>")
                .append("DÉTAILS DE LA COMMANDE</p>")
                .append("<table style='width:100%;border-collapse:collapse;'>")
                .append("<tr><td style='color:#374151;padding:4px 0;'>Numéro de commande</td>")
                .append("<td style='text-align:right;font-weight:800;color:#111827;'>#")
                .append(commande.getId()).append("</td></tr>")
                .append("<tr><td style='color:#374151;padding:4px 0;'>Date</td>")
                .append("<td style='text-align:right;color:#111827;'>").append(date).append("</td></tr>")
                .append("<tr><td style='color:#374151;padding:4px 0;'>Statut</td>")
                .append("<td style='text-align:right;'>")
                .append("<span style='background:#D1FAE5;color:#065F46;padding:3px 10px;")
                .append("border-radius:999px;font-size:12px;font-weight:800;'>PAYÉE</span>")
                .append("</td></tr></table></div>");

        // Tableau des produits
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            sb.append("<p style='font-weight:800;color:#111827;margin:20px 0 10px;'>")
                    .append("Produits commandés</p>")
                    .append("<table style='width:100%;border-collapse:collapse;'>")
                    .append("<thead><tr style='background:#F3F4F6;'>")
                    .append("<th style='text-align:left;padding:10px 12px;color:#6B7280;font-size:12px;'>PRODUIT</th>")
                    .append("<th style='text-align:center;padding:10px 12px;color:#6B7280;font-size:12px;'>QTÉ</th>")
                    .append("<th style='text-align:right;padding:10px 12px;color:#6B7280;font-size:12px;'>TOTAL</th>")
                    .append("</tr></thead><tbody>");

            for (LigneCommande ligne : commande.getLignes()) {
                Produit p        = ligne.getProduit();
                String nomProd   = (p != null && p.getNom() != null) ? p.getNom() : "Produit";
                String categorie = (p != null && p.getCategorie() != null) ? p.getCategorie() : "";
                BigDecimal total = ligne.getTotalLigne() != null ? ligne.getTotalLigne() : BigDecimal.ZERO;

                sb.append("<tr style='border-bottom:1px solid #F3F4F6;'>")
                        .append("<td style='padding:12px;'>")
                        .append("<span style='font-weight:800;color:#111827;font-size:14px;'>")
                        .append(escapeHtml(nomProd)).append("</span><br>")
                        .append("<span style='background:#DBEAFE;color:#1D4ED8;font-size:10px;")
                        .append("font-weight:800;padding:2px 8px;border-radius:999px;'>")
                        .append(escapeHtml(categorie)).append("</span></td>")
                        .append("<td style='text-align:center;color:#374151;font-weight:700;'>")
                        .append(ligne.getQuantite()).append("</td>")
                        .append("<td style='text-align:right;font-weight:800;color:#F82239;'>")
                        .append(total.toPlainString()).append(" DT</td></tr>");
            }

            sb.append("</tbody></table>");
        }

        // Total final
        sb.append("<div style='background:linear-gradient(to right,#F82239,#2F60F5);")
                .append("border-radius:12px;padding:16px 20px;margin:20px 0;'>")
                .append("<table style='width:100%;'><tr>")
                .append("<td style='color:white;font-weight:800;font-size:15px;'>TOTAL PAYÉ</td>")
                .append("<td style='text-align:right;color:white;font-weight:900;font-size:22px;'>")
                .append(montant.toPlainString()).append(" DT</td></tr></table></div>")

                // Message fin
                .append("<p style='color:#6B7280;font-size:13px;margin-top:24px;'>")
                .append("Votre commande sera traitée dans les plus brefs délais.<br>")
                .append("Pour toute question : <strong>contact@cardiolink.tn</strong></p>")
                .append("</div>")

                // Footer
                .append("<div style='background:#F9FAFB;padding:16px 32px;text-align:center;")
                .append("border-top:1px solid #E5E7EB;'>")
                .append("<p style='color:#9CA3AF;font-size:11px;margin:0;'>")
                .append("CardioLink Tunisia • Marketplace Médical • Tunis, Tunisie</p>")
                .append("<p style='color:#9CA3AF;font-size:11px;margin:4px 0 0;'>")
                .append("Cet email est généré automatiquement.</p>")
                .append("</div>")
                .append("</div></body></html>");

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────
    // Version texte brut (fallback)
    // ─────────────────────────────────────────────────────────
    private String construireTexte(String nom, Commande commande) {
        StringBuilder sb = new StringBuilder();

        String date = commande.getDateCommande() != null
                ? commande.getDateCommande().format(DATE_FMT)
                : "—";

        BigDecimal montant = commande.getMontantTotal() != null
                ? commande.getMontantTotal()
                : BigDecimal.ZERO;

        sb.append("Bonjour ").append(nom).append(",\n\n")
                .append("Votre paiement a bien été reçu.\n\n")
                .append("Commande #").append(commande.getId()).append("\n")
                .append("Date     : ").append(date).append("\n")
                .append("Statut   : PAYÉE\n\n");

        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            sb.append("Produits commandés :\n");
            for (LigneCommande ligne : commande.getLignes()) {
                Produit p        = ligne.getProduit();
                String nomProd   = (p != null && p.getNom() != null) ? p.getNom() : "Produit";
                BigDecimal total = ligne.getTotalLigne() != null ? ligne.getTotalLigne() : BigDecimal.ZERO;
                sb.append("  • ").append(nomProd)
                        .append("  x").append(ligne.getQuantite())
                        .append("  →  ").append(total.toPlainString()).append(" DT\n");
            }
            sb.append("\n");
        }

        sb.append("TOTAL PAYÉ : ").append(montant.toPlainString()).append(" DT\n\n")
                .append("Merci pour votre confiance.\n")
                .append("— L'équipe CardioLink\n")
                .append("contact@cardiolink.tn");

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────
    // Utilitaire HTML
    // ─────────────────────────────────────────────────────────
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
