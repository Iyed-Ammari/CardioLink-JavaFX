package com.cardiolink.Services;

import com.cardiolink.utils.DatabaseConnection;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.sql.*;
import java.util.Properties;

public class EmailVerificationService {

    private static final String FROM_EMAIL    = "sarra.hajjeji37@gmail.com";
    private static final String FROM_PASSWORD = "xxxn jiih cwgd czlc";

    // ── Générer un code à 6 chiffres et l'enregistrer en base ──
    public String generateVerificationToken(int userId) throws SQLException {
        String token = String.format("%06d", (int)(Math.random() * 1000000));
        String sql = "UPDATE user SET verification_token = ?, is_verified = 0 WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, token);
        ps.setInt(2, userId);
        ps.executeUpdate();
        return token;
    }

    // ── Vérifier le code et activer le compte ──
    public boolean verifyToken(String token) throws SQLException {
        String sql = "SELECT id FROM user WHERE verification_token = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, token);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int userId = rs.getInt("id");
            String update = "UPDATE user SET is_verified = 1, " +
                    "verification_token = NULL WHERE id = ?";
            PreparedStatement ps2 = conn.prepareStatement(update);
            ps2.setInt(1, userId);
            ps2.executeUpdate();
            return true;
        }
        return false;
    }

    // ── Envoyer l'email avec le code à 6 chiffres ──
    public void sendVerificationEmail(String toEmail,
                                      String token,
                                      String prenom) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
        message.setSubject("CardioLink - Vérifiez votre adresse email");
        message.setContent(buildEmailHtml(prenom, token),
                "text/html; charset=utf-8");

        Transport.send(message);
    }

    // ── Template email avec code visible ──
    private String buildEmailHtml(String prenom, String token) {
        return "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; " +
                "background-color: #f8f8ff; margin: 0; padding: 0;'>" +
                "<div style='max-width: 600px; margin: 40px auto; " +
                "background-color: white; border-radius: 16px; " +
                "padding: 40px; text-align: center; " +
                "box-shadow: 0 4px 20px rgba(0,0,0,0.08);'>" +

                "<div style='font-size: 48px; margin-bottom: 20px;'>❤️</div>" +

                "<h1 style='color: #1a1a2e; font-size: 24px; margin-bottom: 16px;'>" +
                "Please Confirm Your Email</h1>" +

                "<p style='color: #555; font-size: 15px; line-height: 1.6;'>" +
                "Welcome to <strong>CardioLink</strong>! " +
                "We're excited to have you on board, " + prenom + ".</p>" +

                "<p style='color: #555; font-size: 15px; line-height: 1.6;'>" +
                "Enter this verification code in the app to confirm your account:</p>" +

                // ── Code à 6 chiffres affiché en grand ──
                "<div style='margin: 28px auto; padding: 20px 48px; " +
                "background: linear-gradient(to right, #E24B4A, #7F77DD); " +
                "border-radius: 14px; display: inline-block;'>" +
                "<span style='color: white; font-size: 36px; font-weight: bold; " +
                "letter-spacing: 10px; font-family: monospace;'>" +
                token +
                "</span>" +
                "</div>" +

                "<p style='color: #777; font-size: 14px; margin-top: 8px;'>" +
                "Copy this code and paste it in the CardioLink app.</p>" +

                "<p style='color: #999; font-size: 13px; margin-top: 20px;'>" +
                "This code will expire in 1 hour.</p>" +

                "<hr style='border: none; border-top: 1px solid #eee; margin: 24px 0;'/>" +
                "<p style='color: #bbb; font-size: 12px;'>" +
                "CardioLink - Your Cardiac Health</p>" +
                "</div></body></html>";
    }
}