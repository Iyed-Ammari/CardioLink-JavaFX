package com.cardiolink.Services;

import com.cardiolink.utils.DatabaseConnection;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

public class PasswordResetService {

    private static final String FROM_EMAIL    = "sarra.hajjeji37@gmail.com";
    private static final String FROM_PASSWORD = "xxxn jiih cwgd czlc";
    // ✅ Générer token et sauvegarder en base
    public String generateToken(int userId) throws SQLException {
        deleteOldTokens(userId);
        String token  = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        String sql = "INSERT INTO password_reset_token " +
                "(user_id, token, expiry_date, used) VALUES (?,?,?,0)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setString(2, token);
        ps.setTimestamp(3, Timestamp.valueOf(expiry));
        ps.executeUpdate();
        return token;
    }

    // ✅ Valider par les 8 premiers caractères
    public int validateTokenByCode(String code) throws SQLException {
        String sql = "SELECT user_id, expiry_date, used " +
                "FROM password_reset_token " +
                "WHERE UPPER(LEFT(token, 8)) = UPPER(?) " +
                "ORDER BY expiry_date DESC LIMIT 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            if (rs.getBoolean("used")) return -1;
            if (rs.getTimestamp("expiry_date")
                    .before(new Timestamp(System.currentTimeMillis())))
                return -2;
            return rs.getInt("user_id");
        }
        return -3;
    }

    // ✅ Marquer token utilisé
    public void markTokenUsedByCode(String code) throws SQLException {
        String sql = "UPDATE password_reset_token SET used = 1 " +
                "WHERE UPPER(LEFT(token, 8)) = UPPER(?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, code);
        ps.executeUpdate();
    }

    private void deleteOldTokens(int userId) throws SQLException {
        String sql = "DELETE FROM password_reset_token WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    // ✅ Envoyer email avec le code
    public void sendResetEmail(String toEmail,
                               String token) throws MessagingException {
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

        String code = token.substring(0, 8).toUpperCase();

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM_EMAIL));
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
        msg.setSubject("CardioLink - Réinitialisation de mot de passe");
        msg.setContent(
                "<html><body style='font-family:Arial;'>" +
                        "<h2 style='color:#7F77DD'>❤ CardioLink</h2>" +
                        "<p>Vous avez demandé la réinitialisation de votre mot de passe.</p>" +
                        "<p>Votre code de réinitialisation :</p>" +
                        "<div style='background:#f0f0fb;padding:20px;" +
                        "border-radius:8px;text-align:center;'>" +
                        "<h1 style='color:#E24B4A;letter-spacing:8px;'>" +
                        code + "</h1></div>" +
                        "<p>Ce code expire dans <strong>1 heure</strong>.</p>" +
                        "<p style='color:#888'>— L'équipe CardioLink</p>" +
                        "</body></html>",
                "text/html; charset=utf-8");

        Transport.send(msg);
    }
}