package tn.esprit.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "khalilbenlahmer@gmail.com";
    private static final String PASSWORD = "eodrpvucmjtvwiwo"; // Sans espaces

    public static boolean sendResetPasswordEmail(String toEmail, String resetCode) {
        // Propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false"); // Désactiver le debug pour éviter les erreurs

        // Session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🔐 Code de réinitialisation");

            // Contenu texte simple (pas de HTML pour éviter les problèmes)
            String content = "Bonjour,\n\n"
                    + "Voici votre code de réinitialisation : " + resetCode + "\n\n"
                    + "Ce code est valable pendant 15 minutes.\n\n"
                    + "Application de Réservation";

            message.setText(content);

            System.out.println("📧 Envoi à: " + toEmail);
            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès!");
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean testConfiguration() {
        return sendResetPasswordEmail(FROM_EMAIL, "123456");
    }
}