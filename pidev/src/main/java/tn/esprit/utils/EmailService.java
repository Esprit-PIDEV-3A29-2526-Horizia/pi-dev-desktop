package tn.esprit.utils;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.activation.DataHandler;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    // 🔐 Paramètres SMTP (remplacer par vos identifiants Gmail + mot de passe d'application)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "khadijaderbel123@gmail.com";
    private static final String PASSWORD = "ebfj wsnb wkek tbku"; // mot de passe d'application

    // Nom de l'application pour l'expéditeur
    private static final String APP_NAME = "Horizia";

    public static void sendReservationEmailWithPDF(String to, String subject,
                                                   String clientNom, String clientPrenom,
                                                   String logementNom, String logementAdresse,
                                                   LocalDate dateArrivee, LocalDate dateDepart,
                                                   double montant, String modalite, String status,
                                                   String qrContent, String nomFichier) throws MessagingException, IOException, WriterException {

        byte[] pdfBytes = createReservationPDF(clientNom, clientPrenom, logementNom, logementAdresse,
                dateArrivee, dateDepart, montant, modalite, status, qrContent);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        // Expéditeur avec nom personnalisé (encodé pour éviter les problèmes)
        message.setFrom(new InternetAddress(USERNAME, MimeUtility.encodeText(APP_NAME, "utf-8", null)));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        // Corps de l'email en HTML
        String emailBody = buildEmailBody(clientPrenom, logementNom, logementAdresse,
                dateArrivee, dateDepart, montant, modalite, status);
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(emailBody, "text/html; charset=utf-8");

        // Pièce jointe PDF
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(pdfBytes, "application/pdf")));
        attachmentPart.setFileName(nomFichier);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        Transport.send(message);
    }

    private static String buildEmailBody(String prenom, String logementNom, String logementAdresse,
                                         LocalDate arrivee, LocalDate depart,
                                         double montant, String modalite, String status) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                ".header { background-color: #1A3C5A; color: white; padding: 20px; text-align: center; }" +
                ".header h2 { margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; }" +
                ".footer { background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777; }" +
                "table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                "td { padding: 10px; border-bottom: 1px solid #eee; }" +
                ".label { font-weight: bold; color: #1A3C5A; width: 40%; }" +
                ".value { color: #333; }" +
                ".note { margin-top: 20px; padding: 10px; background-color: #e8f4fd; border-left: 4px solid #2ECC71; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Confirmation de réservation</h2></div>" +
                "<div class='content'>" +
                "<p>Bonjour " + prenom + ",</p>" +
                "<p>Nous vous confirmons votre réservation chez <strong>" + APP_NAME + "</strong>.</p>" +
                "<table>" +
                "<tr><td class='label'>Logement</td><td class='value'>" + logementNom + "</td></tr>" +
                (logementAdresse != null && !logementAdresse.isEmpty() ? "<tr><td class='label'>Adresse</td><td class='value'>" + logementAdresse + "</td></tr>" : "") +
                "<tr><td class='label'>Arrivée</td><td class='value'>" + arrivee.format(dtf) + "</td></tr>" +
                "<tr><td class='label'>Départ</td><td class='value'>" + depart.format(dtf) + "</td></tr>" +
                "<tr><td class='label'>Montant total</td><td class='value'>" + String.format("%.3f DT", montant) + "</td></tr>" +
                "<tr><td class='label'>Modalité</td><td class='value'>" + modalite + "</td></tr>" +
                "<tr><td class='label'>Statut</td><td class='value'>" + status + "</td></tr>" +
                "</table>" +
                "<div class='note'>" +
                (modalite.equals("Sur place") ? "Veuillez régler le montant sur place." : "Paiement en ligne effectué avec succès.") +
                "</div>" +
                "<p>Veuillez trouver en pièce jointe votre QR code et le récapitulatif détaillé.</p>" +
                "<p>Merci de votre confiance !</p>" +
                "</div>" +
                "<div class='footer'>" +
                "Tunis, Tunisie | 📞 +216 5 861 445 | 📧 info@horizia.com" +
                "</div></div></body></html>";
    }

    private static byte[] createReservationPDF(String clientNom, String clientPrenom,
                                               String logementNom, String logementAdresse,
                                               LocalDate dateArrivee, LocalDate dateDepart,
                                               double montant, String modalite, String status,
                                               String qrContent) throws IOException, WriterException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        Color primaryColor = new DeviceRgb(26, 60, 90);   // #1A3C5A
        Color secondaryColor = new DeviceRgb(46, 204, 113); // #2ECC71

        // Logo (optionnel)
        try {
            InputStream logoStream = EmailService.class.getResourceAsStream("/images/logo.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                Image logo = new Image(ImageDataFactory.create(logoBytes));
                logo.setWidth(100);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Titre
        Paragraph title = new Paragraph("Confirmation de réservation")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginBottom(20);
        document.add(title);

        // Message de bienvenue
        document.add(new Paragraph("Bonjour " + clientPrenom + " " + clientNom + ",")
                .setFontSize(12).setMarginBottom(5));
        document.add(new Paragraph("Nous vous confirmons votre réservation chez Horizia.")
                .setFontSize(12).setMarginBottom(20));

        // Tableau des détails
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        table.setMarginBottom(20);

        // Ajout des lignes
        addRow(table, "Logement", logementNom, primaryColor);
        if (logementAdresse != null && !logementAdresse.isEmpty()) {
            addRow(table, "Adresse", logementAdresse, primaryColor);
        }
        addRow(table, "Arrivée", dateArrivee.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), primaryColor);
        addRow(table, "Départ", dateDepart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), primaryColor);
        addRow(table, "Montant total", String.format("%.3f DT", montant), primaryColor);
        addRow(table, "Modalité", modalite, primaryColor);
        addRow(table, "Statut", status, primaryColor);

        document.add(table);

        // Note selon modalité
        if ("Sur place".equals(modalite)) {
            document.add(new Paragraph("Veuillez régler le montant sur place.")
                    .setFontSize(12).setItalic().setFontColor(secondaryColor).setMarginBottom(20));
        } else {
            document.add(new Paragraph("Paiement en ligne effectué avec succès.")
                    .setFontSize(12).setItalic().setFontColor(secondaryColor).setMarginBottom(20));
        }

        // QR Code
        Paragraph qrTitle = new Paragraph("Votre QR Code")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginTop(20);
        document.add(qrTitle);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrBaos);
        Image qrImage = new Image(ImageDataFactory.create(qrBaos.toByteArray()));
        qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
        qrImage.setWidth(150);
        qrImage.setHeight(150);
        document.add(qrImage);

        // Pied de page
        document.add(new Paragraph("Merci de votre confiance !")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(secondaryColor)
                .setMarginTop(30));
        document.add(new Paragraph("Tunis, Tunisie | 📞 +216 5 861 445 | 📧 info@horizia.com")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(primaryColor));

        document.close();
        return baos.toByteArray();
    }

    private static void addRow(Table table, String label, String value, Color labelColor) {
        Cell labelCell = new Cell().add(new Paragraph(label).setBold()).setBorder(Border.NO_BORDER).setPadding(5);
        Cell valueCell = new Cell().add(new Paragraph(value)).setBorder(Border.NO_BORDER).setPadding(5);
        labelCell.setFontColor(labelColor);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    public static void sendSimpleEmail(String to, String subject, String body) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME, MimeUtility.encodeText(APP_NAME, "utf-8", null)));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }
    public static void sendCancellationEmail(String to, String clientNom, String clientPrenom,
                                             String logementNom, String dates, double montant) throws MessagingException, UnsupportedEncodingException {
        String subject = "Annulation de votre réservation";
        String body = "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                ".container { max-width: 600px; margin: auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                ".header { background-color: #e74c3c; color: white; padding: 20px; text-align: center; }" +
                ".header h2 { margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; }" +
                ".footer { background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777; }" +
                "table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                "td { padding: 10px; border-bottom: 1px solid #eee; }" +
                ".label { font-weight: bold; color: #e74c3c; width: 40%; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Annulation de réservation</h2></div>" +
                "<div class='content'>" +
                "<p>Bonjour " + clientPrenom + " " + clientNom + ",</p>" +
                "<p>Votre réservation pour <strong>" + logementNom + "</strong> a été annulée.</p>" +
                "<table>" +
                "<tr><td class='label'>Période</td><td>" + dates + "</td></tr>" +
                "<tr><td class='label'>Montant</td><td>" + String.format("%.3f DT", montant) + "</td></tr>" +
                "</table>" +
                "<p>Nous restons à votre disposition pour toute question.</p>" +
                "<p>Cordialement,<br>L'équipe " + APP_NAME + "</p>" +
                "</div>" +
                "<div class='footer'>" +
                "Tunis, Tunisie | 📞 +216 5 861 445 | 📧 info@horizia.com" +
                "</div></div></body></html>";

        sendStyledEmail(to, subject, body);
    }

    public static void sendReminderEmail(String to, String clientPrenom, String logementNom,
                                         long heuresRestantes, long minutesRestantes) throws MessagingException, UnsupportedEncodingException {
        String tempsRestant;
        if (heuresRestantes > 0) {
            tempsRestant = heuresRestantes + " heure" + (heuresRestantes > 1 ? "s" : "");
        } else {
            tempsRestant = minutesRestantes + " minute" + (minutesRestantes > 1 ? "s" : "");
        }

        String subject = "Rappel : Finalisez votre paiement";
        String body = "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                ".container { max-width: 600px; margin: auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                ".header { background-color: #E8B156; color: white; padding: 20px; text-align: center; }" +
                ".header h2 { margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; }" +
                ".footer { background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777; }" +
                ".timer { font-size: 24px; font-weight: bold; color: #e74c3c; text-align: center; margin: 20px 0; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Rappel de paiement</h2></div>" +
                "<div class='content'>" +
                "<p>Bonjour " + clientPrenom + ",</p>" +
                "<p>Vous avez réservé <strong>" + logementNom + "</strong> et choisi de payer plus tard.</p>" +
                "<p>Il vous reste <span class='timer'>" + tempsRestant + "</span> pour finaliser votre paiement, sinon votre réservation sera automatiquement annulée.</p>" +
                "<p>Connectez-vous à votre espace pour effectuer le paiement.</p>" +
                "<p>Merci de votre confiance.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "Tunis, Tunisie | 📞 +216 5 861 445 | 📧 info@horizia.com" +
                "</div></div></body></html>";

        sendStyledEmail(to, subject, body);
    }

    // Méthode utilitaire pour envoyer un email HTML avec le bon expéditeur
    private static void sendStyledEmail(String to, String subject, String htmlBody) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME, MimeUtility.encodeText(APP_NAME, "utf-8", null)));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=utf-8");
        Transport.send(message);
    }
    public static void sendPaymentDeferredEmail(String to, String clientPrenom, String logementNom,
                                                 String dates, double montant,
                                                String paymentUrl) throws MessagingException, UnsupportedEncodingException {
        String subject = "Confirmation de votre demande de paiement différé";
        String body = "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                ".container { max-width: 600px; margin: auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                ".header { background-color: #1A3C5A; color: white; padding: 20px; text-align: center; }" +
                ".header h2 { margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; text-align: center; }" +
                ".timer { font-size: 48px; font-weight: bold; color: #e74c3c; margin: 20px 0; }" +
                ".details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                ".details p { margin: 5px 0; }" +
                ".button { background-color: #2ECC71; color: white; padding: 15px 40px; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 18px; display: inline-block; margin-top: 20px; border: none; cursor: pointer; }" +
                ".button:hover { background-color: #27ae60; }" +
                ".footer { background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Horiza</h2></div>" +
                "<div class='content'>" +
                "<p>Bonjour " + clientPrenom + ",</p>" +
                "<p>Vous avez choisi de payer plus tard pour votre réservation <strong>" + logementNom + "</strong>.</p>" +
                "<div class='timer'>24:00:00</div>" +
                "<p>Il vous reste ce temps pour finaliser votre paiement.</p>" +
                "<div class='details'>" +
                "<p><strong>Dates :</strong> " + dates + "</p>" +
                "<p><strong>Montant à régler :</strong> " + String.format("%.3f DT", montant) + "</p>" +
                "</div>" +
                "<a href='" + paymentUrl + "' class='button'>PAYER MAINTENANT</a>" +
                "<p style='margin-top:20px; font-size:12px; color:#777;'>Ce lien expire dans 24h.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "Tunis, Tunisie | 📞 +216 5 861 445 | 📧 info@horizia.com" +
                "</div></div></body></html>";

        sendStyledEmail(to, subject, body);
    }
   // Sans espaces

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
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
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
        return sendResetPasswordEmail(USERNAME, "123456");
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
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // Créer le message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
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
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // Créer le message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
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