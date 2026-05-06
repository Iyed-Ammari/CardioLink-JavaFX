package com.cardiolink.Services;

import com.cardiolink.utils.DatabaseConnection;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.Properties;

public class PasswordResetService {

    private static final String FROM_EMAIL    = "sarra.hajjeji37@gmail.com";
    private static final String FROM_PASSWORD = "xxxn jiih cwgd czlc";

    // ── Générer token 6 chiffres et envoyer email ────────────
    public String generateAndSendResetToken(String email) throws SQLException {
        String sql = "SELECT id, prenom FROM user WHERE email = ? AND is_verified = 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) throw new SQLException("EMAIL_NOT_FOUND");

        int    userId = rs.getInt("id");
        String prenom = rs.getString("prenom");

        String token = String.format("%06d", (int)(Math.random() * 1000000));

        String update = "UPDATE user SET reset_token = ?, " +
                "reset_token_expiry = DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE id = ?";
        PreparedStatement ps2 = conn.prepareStatement(update);
        ps2.setString(1, token);
        ps2.setInt(2, userId);
        ps2.executeUpdate();

        try {
            sendResetEmail(email, token, prenom);
        } catch (MessagingException e) {
            throw new SQLException("EMAIL_SEND_FAILED");
        }

        return token;
    }

    // ── Vérifier token ───────────────────────────────────────
    public boolean verifyResetToken(String email, String token) throws SQLException {
        String sql = "SELECT id FROM user WHERE email = ? AND reset_token = ? " +
                "AND reset_token_expiry > NOW()";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, token);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    // ── Réinitialiser mot de passe ───────────────────────────
    public void resetPassword(String email, String token,
                              String newPassword) throws SQLException {
        if (!verifyResetToken(email, token))
            throw new SQLException("INVALID_TOKEN");

        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        String sql = "UPDATE user SET password = ?, reset_token = NULL, " +
                "reset_token_expiry = NULL WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, hashed);
        ps.setString(2, email);
        ps.executeUpdate();
    }

    // ── Envoyer email ────────────────────────────────────────
    private void sendResetEmail(String toEmail, String token,
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
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("CardioLink - Réinitialisation de mot de passe");
        message.setContent(buildEmailHtml(prenom, token), "text/html; charset=utf-8");
        Transport.send(message);
    }

    // ── Template email ───────────────────────────────────────
    private String buildEmailHtml(String prenom, String token) {
        return "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; " +
                "background-color: #f8f8ff; margin: 0; padding: 0;'>" +
                "<div style='max-width: 600px; margin: 40px auto; " +
                "background-color: white; border-radius: 16px; " +
                "padding: 40px; text-align: center; " +
                "box-shadow: 0 4px 20px rgba(0,0,0,0.08);'>" +
                "<div style='font-size: 48px; margin-bottom: 20px;'>🔐</div>" +
                "<h1 style='color: #1a1a2e; font-size: 24px; margin-bottom: 16px;'>" +
                "Réinitialisation de mot de passe</h1>" +
                "<p style='color: #555; font-size: 15px; line-height: 1.6;'>" +
                "Bonjour <strong>" + prenom + "</strong>,</p>" +
                "<p style='color: #555; font-size: 15px; line-height: 1.6;'>" +
                "Voici votre code de réinitialisation :</p>" +
                "<div style='margin: 28px auto; padding: 20px 48px; " +
                "background: linear-gradient(to right, #D4537E, #7F77DD); " +
                "border-radius: 14px; display: inline-block;'>" +
                "<span style='color: white; font-size: 36px; font-weight: bold; " +
                "letter-spacing: 10px; font-family: monospace;'>" + token + "</span>" +
                "</div>" +
                "<p style='color: #777; font-size: 14px; margin-top: 8px;'>" +
                "Entrez ce code dans l'application CardioLink.</p>" +
                "<p style='color: #999; font-size: 13px; margin-top: 20px;'>" +
                "Ce code expire dans 1 heure.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 24px 0;'/>" +
                "<p style='color: #bbb; font-size: 12px;'>" +
                "CardioLink - Your Cardiac Health</p>" +
                "</div></body></html>";
    }
}