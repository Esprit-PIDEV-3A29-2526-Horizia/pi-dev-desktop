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

        // Logo (à placer dans src/main/resources/images/logo.png)
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
            e.printStackTrace(); // si logo absent, on continue
        }

        Paragraph title = new Paragraph("Confirmation de réservation")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(title);

        Paragraph appName = new Paragraph(APP_NAME)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setFontColor(secondaryColor)
                .setMarginBottom(20);
        document.add(appName);

        document.add(new Paragraph("Bonjour " + clientPrenom + " " + clientNom + ",")
                .setFontSize(12).setMarginBottom(5));
        document.add(new Paragraph("Nous vous confirmons votre réservation chez " + APP_NAME + ".")
                .setFontSize(12).setMarginBottom(20));

        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        table.setMarginBottom(20);
        addRow(table, "Logement", logementNom, primaryColor);
        if (logementAdresse != null && !logementAdresse.isEmpty()) {
            addRow(table, "Adresse", logementAdresse, primaryColor);
        }
        addRow(table, "Date d'arrivée", dateArrivee.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), primaryColor);
        addRow(table, "Date de départ", dateDepart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), primaryColor);
        addRow(table, "Prix total", String.format("%.3f DT", montant), primaryColor);
        addRow(table, "Modalité", modalite, primaryColor);
        addRow(table, "Statut", status, primaryColor);
        document.add(table);

        if ("Sur place".equals(modalite)) {
            document.add(new Paragraph("Veuillez régler le montant sur place.")
                    .setFontSize(12).setItalic().setFontColor(secondaryColor).setMarginBottom(20));
        } else {
            document.add(new Paragraph("Paiement en ligne effectué avec succès.")
                    .setFontSize(12).setItalic().setFontColor(secondaryColor).setMarginBottom(20));
        }

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
}