package tn.esprit.services;

import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class EmailService {

    private static final String GMAIL_ADDRESS = "dolce.lallouna@gmail.com";
    private static final String GMAIL_APP_PASSWORD = "upbn vnnw dmpu lxjz";

    public static void sendConfirmationEmail(String toEmail, String toName,
                                             Events event, Participation participation) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_ADDRESS, GMAIL_APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_ADDRESS));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("✅ Confirmation de réservation - " + event.getTitre());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String htmlContent = String.format(
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<meta charset='UTF-8'>" +
                            "<style>" +
                            "body { font-family: 'Segoe UI', Arial, sans-serif; padding: 20px; background-color: #f5f7fb; }" +
                            ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 20px; padding: 30px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }" +
                            "h1 { color: #23779C; margin-top: 0; }" +
                            ".header { background-color: #23779C; color: white; padding: 20px; border-radius: 15px; text-align: center; margin-bottom: 25px; }" +
                            ".details { background-color: #f8f9fa; padding: 20px; border-radius: 15px; margin: 20px 0; border-left: 5px solid #81AE8D; }" +
                            ".event-title { color: #23779C; font-size: 22px; font-weight: bold; margin: 10px 0; }" +
                            ".info-row { margin: 10px 0; padding: 8px; border-bottom: 1px solid #DACEB6; }" +
                            ".label { font-weight: bold; color: #666; width: 120px; display: inline-block; }" +
                            ".value { color: #333; }" +
                            ".total { font-size: 20px; color: #81AE8D; font-weight: bold; text-align: right; margin-top: 15px; }" +
                            ".footer { margin-top: 30px; text-align: center; color: #999; font-size: 12px; }" +
                            ".badge { background-color: #81AE8D; color: white; padding: 8px 20px; border-radius: 25px; display: inline-block; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='container'>" +
                            "<div class='header'>" +
                            "<h1>✅ Réservation confirmée !</h1>" +
                            "</div>" +
                            "<p>Bonjour <strong>%s</strong>,</p>" +
                            "<p>Votre réservation pour l'événement ci-dessous a bien été enregistrée.</p>" +
                            "<div class='event-title'>🎫 %s</div>" +
                            "<div class='details'>" +
                            "<div class='info-row'><span class='label'>📅 Date :</span> <span class='value'>%s</span></div>" +
                            "<div class='info-row'><span class='label'>📍 Lieu :</span> <span class='value'>%s</span></div>" +
                            "<div class='info-row'><span class='label'>🎟️ Places :</span> <span class='value'>%d</span></div>" +
                            "<div class='info-row'><span class='label'>💰 Prix unitaire :</span> <span class='value'>%.0f DT</span></div>" +
                            "<div class='total'>💰 Total : %.0f DT</div>" +
                            "</div>" +
                            "<p>Votre QR code vous a été généré dans l'application. Présentez-le à l'entrée.</p>" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<span class='badge'>Réservation #%d</span>" +
                            "</div>" +
                            "<p>Merci de votre confiance et à bientôt !</p>" +
                            "<hr style='border: 1px solid #DACEB6; margin: 20px 0;' />" +
                            "<div class='footer'>" +
                            "<p>EventHub - Votre plateforme d'événements</p>" +
                            "<p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                            "</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>",
                    toName,
                    event.getTitre(),
                    sdf.format(event.getDateDebut()),
                    event.getLocation(),
                    participation.getNombrePlaces(),
                    event.getPrix(),
                    participation.getMontantTotal(),
                    participation.getId_participation()
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}