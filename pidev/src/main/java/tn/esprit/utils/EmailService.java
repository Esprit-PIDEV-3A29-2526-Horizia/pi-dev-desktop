package tn.esprit.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
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

    public static boolean sendEmailWithAttachment(String toEmail, String subject, String body, String attachmentPath) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            // Créer le message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Créer la partie texte
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body, "UTF-8");

            // Créer la partie pièce jointe
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachmentPath);

            // Assembler
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Envoyer
            Transport.send(message);
            System.out.println("✅ Email avec pièce jointe envoyé à " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie un email simple avec possibilité de pièce jointe
     */
    public static boolean sendEmail(String toEmail, String subject, String body, String attachmentPath) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            // Créer le message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                // Avec pièce jointe (code existant)
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setContent(body, "text/html; charset=utf-8");  // ← HTML

                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachmentPath);

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(attachmentPart);

                message.setContent(multipart);
            } else {
                // Sans pièce jointe - Version HTML aussi
                message.setContent(body, "text/html; charset=utf-8");  // ← HTML
            }
            // Envoyer
            Transport.send(message);
            System.out.println("✅ Email envoyé à " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}