package com.cardiolink.Services;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    // IMPORTANT: Remplacez par votre adresse Gmail
    private static final String SENDER_EMAIL = "chaymakamel3@gmail.com";
    // IMPORTANT: Remplacez par votre Mot de passe d'application généré par Google (16 caractères, sans espaces)
    private static final String SENDER_APP_PASSWORD = "kghedulqvoohstxz";

    public boolean sendReminder(String recipientEmail, String patientName, String doctorName, LocalDateTime appointmentDate) {
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

            String htmlContent = "<h2>Bonjour " + patientName + ",</h2>"
                    + "<p>Nous vous rappelons que vous avez un rendez-vous prévu demain le <strong>" + formattedDate + "</strong> "
                    + "avec le <strong>" + doctorName + "</strong>.</p>"
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
