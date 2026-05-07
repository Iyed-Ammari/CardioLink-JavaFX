package com.cardiolink.Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailServiceChaima {

    // IMPORTANT: Remplacez par votre adresse Gmail
    private static final String SENDER_EMAIL = "chaymakamel3@gmail.com";
    // IMPORTANT: Remplacez par votre Mot de passe d'application généré par Google (16 caractères, sans espaces)
    private static final String SENDER_APP_PASSWORD = "kghedulqvoohstxz";

    public boolean sendReminder(String recipientEmail, String patientName, String doctorName, LocalDateTime appointmentDate, String lienVisio) {
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "CardioLink Notifications"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Rappel : Votre rendez-vous approche");

            String formattedDate = appointmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm"));

            String visioSection = "";
            if (lienVisio != null && !lienVisio.isEmpty()) {
                visioSection = "<div style='background-color: #f0f7ff; padding: 15px; border-radius: 8px; border: 1px solid #007bff; margin-top: 20px;'>"
                        + "<h3 style='color: #007bff; margin-top: 0;'>Consultation en Visio</h3>"
                        + "<p>Votre consultation se déroulera en ligne. Vous pouvez rejoindre la réunion en cliquant sur le bouton ci-dessous :</p>"
                        + "<a href='" + lienVisio + "' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Rejoindre la Visio (Jitsi)</a>"
                        + "<p style='font-size: 0.9em; color: #666; margin-top: 10px;'>Lien direct : <a href='" + lienVisio + "'>" + lienVisio + "</a></p>"
                        + "</div>";
            }

            String htmlContent = "<h2>Bonjour " + patientName + ",</h2>"
                    + "<p>Nous vous rappelons que vous avez un rendez-vous prévu demain le <strong>" + formattedDate + "</strong> "
                    + "avec le <strong>" + doctorName + "</strong>.</p>"
                    + visioSection
                    + "<p>Merci d'être présent 10 minutes à l'avance.</p>"
                    + "<br><p>Cordialement,<br>L'équipe CardioLink</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email envoyé avec succès via Gmail SMTP à " + recipientEmail);
            return true;

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email via Gmail à " + recipientEmail + " : " + e.getMessage());
            return false;
        }
    }
}
