package org.example.services;

import org.example.entities.Location;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service d'envoi d'emails automatiques pour les locations Horizia
 *
 * FIX : import jakarta.mail.* (javax.mail.* → jakarta.mail.*)
 *       Compatible avec la dépendance jakarta.mail dans le pom.xml
 *
 * ⚠️ CONFIGURATION REQUISE :
 * 1. Créer un compte Gmail dédié (ex: horizia.agence@gmail.com)
 * 2. Activer "Mots de passe d'application" dans Compte Google > Sécurité
 * 3. Remplacer EMAIL_EXPEDITEUR et EMAIL_PASSWORD ci-dessous
 */
public class EmailService {

    // ─── ⚙️ CONFIGURATION (À MODIFIER) ────────────────────────────
    private static final String EMAIL_EXPEDITEUR = "bishudo597@gmail.com\n"; // votre vrai email
    private static final String EMAIL_PASSWORD    = "gipx hrpd fctc jcrc";
    private static final String NOM_AGENCE        = "Horizia - Agence de Location";
    private static final String TEL_AGENCE        = "+216 53 661 445";
    private static final String ADRESSE_AGENCE    = "Tunis, Tunisie";
    // ──────────────────────────────────────────────────────────────

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Résultat d'un envoi d'email
     */
    public record ResultatEmail(boolean succes, String message) {}

    // ─────────────────────────────────────────────────────────────
    // MÉTHODES PUBLIQUES D'ENVOI
    // ─────────────────────────────────────────────────────────────

    /** Envoie un email de CONFIRMATION de location au client */
    public static ResultatEmail envoyerConfirmationLocation(Location location, String emailClient) {
        if (emailClient == null || emailClient.isBlank())
            return new ResultatEmail(false, "Adresse email du client manquante");

        String sujet = "✅ Confirmation de votre location - Horizia #" + location.getIdLocation();
        return envoyerEmail(emailClient, sujet, buildEmailConfirmation(location));
    }

    /** Envoie un email de RAPPEL 24h avant la location */
    public static ResultatEmail envoyerRappelLocation(Location location, String emailClient) {
        if (emailClient == null || emailClient.isBlank())
            return new ResultatEmail(false, "Adresse email du client manquante");

        return envoyerEmail(emailClient,
                "⏰ Rappel : Votre location commence demain - Horizia",
                buildEmailRappel(location));
    }

    /** Envoie la FACTURE FINALE après retour du véhicule */
    public static ResultatEmail envoyerFactureFinale(Location location, String emailClient,
                                                     double montantTotal, double avancePayee,
                                                     double soldeRestant) {
        if (emailClient == null || emailClient.isBlank())
            return new ResultatEmail(false, "Adresse email du client manquante");

        String sujet = "🧾 Votre facture finale - Horizia Location #" + location.getIdLocation();
        return envoyerEmail(emailClient, sujet,
                buildEmailFacture(location, montantTotal, avancePayee, soldeRestant));
    }

    /** Envoie les détails du CONTRAT de location */
    public static ResultatEmail envoyerContrat(Location location, String emailClient, String qrCodePath) {
        if (emailClient == null || emailClient.isBlank())
            return new ResultatEmail(false, "Adresse email du client manquante");

        String sujet = "📄 Votre contrat de location - Horizia #" + location.getIdLocation();
        return envoyerEmail(emailClient, sujet, buildEmailContrat(location));
    }

    /** Envoie un email d'ANNULATION de location */
    public static ResultatEmail envoyerAnnulation(Location location, String emailClient) {
        if (emailClient == null || emailClient.isBlank())
            return new ResultatEmail(false, "Adresse email du client manquante");

        String sujet = "❌ Annulation de votre location - Horizia #" + location.getIdLocation();
        return envoyerEmail(emailClient, sujet, buildEmailAnnulation(location));
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTHODE D'ENVOI CENTRALE (Gmail SMTP)
    // ─────────────────────────────────────────────────────────────

    private static ResultatEmail envoyerEmail(String destinataire, String sujet, String corpsHtml) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.protocols",   "TLSv1.2");

        // FIX : jakarta.mail.Session, jakarta.mail.Authenticator, jakarta.mail.PasswordAuthentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, EMAIL_PASSWORD);
            }
        });

        try {
            // FIX : jakarta.mail.internet.MimeMessage, jakarta.mail.internet.InternetAddress
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR, NOM_AGENCE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setContent(corpsHtml, "text/html; charset=UTF-8");
            Transport.send(message);

            System.out.println("[EmailService] ✅ Email envoyé à : " + destinataire);
            return new ResultatEmail(true, "Email envoyé avec succès à " + destinataire);

        } catch (Exception e) {
            System.err.println("[EmailService] ❌ Erreur envoi email : " + e.getMessage());
            return new ResultatEmail(false, "Erreur : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TEMPLATES HTML DES EMAILS
    // ─────────────────────────────────────────────────────────────

    private static String buildEmailConfirmation(Location location) {
        return buildEmailBase(
                "✅ Confirmation de Location", "#27ae60",
                """
                <p style="font-size:16px; color:#2c3e50;">Bonjour <strong>%s</strong>,</p>
                <p>Nous vous confirmons votre réservation de véhicule chez <strong>Horizia</strong>.</p>
                """.formatted(safeClient(location)),
                buildTableDetails(location),
                """
                <p style="color:#7f8c8d;">Veuillez vous présenter muni de votre CIN et permis de conduire.</p>
                <p>Merci de votre confiance !</p>
                """
        );
    }

    private static String buildEmailRappel(Location location) {
        return buildEmailBase(
                "⏰ Rappel de Location", "#f39c12",
                """
                <p style="font-size:16px; color:#2c3e50;">Bonjour <strong>%s</strong>,</p>
                <p>Nous vous rappelons que votre location commence <strong>demain</strong>.</p>
                """.formatted(safeClient(location)),
                buildTableDetails(location),
                """
                <p style="color:#7f8c8d;">N'oubliez pas votre CIN et permis de conduire.</p>
                <p>À demain chez Horizia !</p>
                """
        );
    }

    private static String buildEmailFacture(Location location, double total, double avance, double solde) {
        String tableFinance = """
            <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
              <tr style="background:#f8f9fa;">
                <td style="padding:10px; border:1px solid #dee2e6;">Montant total</td>
                <td style="padding:10px; border:1px solid #dee2e6; font-weight:bold;">%.3f TND</td>
              </tr>
              <tr>
                <td style="padding:10px; border:1px solid #dee2e6;">Avance payée</td>
                <td style="padding:10px; border:1px solid #dee2e6; color:#27ae60;">%.3f TND</td>
              </tr>
              <tr style="background:#fff3cd;">
                <td style="padding:10px; border:1px solid #dee2e6; font-weight:bold;">Solde restant</td>
                <td style="padding:10px; border:1px solid #dee2e6; font-weight:bold; color:#e74c3c;">%.3f TND</td>
              </tr>
            </table>
            """.formatted(total, avance, solde);

        return buildEmailBase(
                "🧾 Facture Finale", "#8e44ad",
                """
                <p style="font-size:16px; color:#2c3e50;">Bonjour <strong>%s</strong>,</p>
                <p>Merci d'avoir choisi Horizia. Voici votre facture finale.</p>
                """.formatted(safeClient(location)),
                buildTableDetails(location) + tableFinance,
                "<p>Merci pour votre confiance. À bientôt !</p>"
        );
    }

    private static String buildEmailContrat(Location location) {
        return buildEmailBase(
                "📄 Votre Contrat de Location", "#2980b9",
                """
                <p style="font-size:16px; color:#2c3e50;">Bonjour <strong>%s</strong>,</p>
                <p>Veuillez trouver ci-dessous les détails de votre contrat de location.</p>
                """.formatted(safeClient(location)),
                buildTableDetails(location),
                """
                <p style="color:#7f8c8d;">Ce contrat fait foi de votre accord avec Horizia.</p>
                <p>Conditions générales disponibles en agence.</p>
                """
        );
    }

    private static String buildEmailAnnulation(Location location) {
        return buildEmailBase(
                "❌ Annulation de Location", "#e74c3c",
                """
                <p style="font-size:16px; color:#2c3e50;">Bonjour <strong>%s</strong>,</p>
                <p>Nous vous informons que votre location a été <strong>annulée</strong>.</p>
                """.formatted(safeClient(location)),
                buildTableDetails(location),
                "<p>Pour toute question, contactez-nous au " + TEL_AGENCE + ".</p>"
        );
    }

    private static String buildTableDetails(Location location) {
        String dateDebut = location.getDateDebut() != null ?
                location.getDateDebut().toLocalDateTime().toLocalDate().format(FMT) : "—";
        String dateFin = location.getDateFinPrev() != null ?
                location.getDateFinPrev().toLocalDateTime().toLocalDate().format(FMT) : "—";

        return """
            <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
              <tr style="background:#3498db; color:white;">
                <th colspan="2" style="padding:12px; text-align:left;">📋 Détails de la Location #%d</th>
              </tr>
              <tr><td style="padding:10px; border:1px solid #dee2e6; color:#7f8c8d;">Client</td>
                  <td style="padding:10px; border:1px solid #dee2e6; font-weight:bold;">%s</td></tr>
              <tr style="background:#f8f9fa;">
                  <td style="padding:10px; border:1px solid #dee2e6; color:#7f8c8d;">Téléphone</td>
                  <td style="padding:10px; border:1px solid #dee2e6;">%s</td></tr>
              <tr><td style="padding:10px; border:1px solid #dee2e6; color:#7f8c8d;">Véhicule ID</td>
                  <td style="padding:10px; border:1px solid #dee2e6; font-weight:bold;">%d</td></tr>
              <tr style="background:#f8f9fa;">
                  <td style="padding:10px; border:1px solid #dee2e6; color:#7f8c8d;">Date début</td>
                  <td style="padding:10px; border:1px solid #dee2e6;">%s</td></tr>
              <tr><td style="padding:10px; border:1px solid #dee2e6; color:#7f8c8d;">Date fin prév.</td>
                  <td style="padding:10px; border:1px solid #dee2e6;">%s</td></tr>
              <tr style="background:#f8f9fa;">
                  <td style="padding:10px; border:1px solid #dee2e6; color:#7f8c8d;">Prix/jour</td>
                  <td style="padding:10px; border:1px solid #dee2e6; color:#27ae60; font-weight:bold;">%.3f TND</td></tr>
              <tr><td style="padding:10px; border:1px solid #dee2e6; color:#7f8c8d;">Statut</td>
                  <td style="padding:10px; border:1px solid #dee2e6;">%s</td></tr>
            </table>
            """.formatted(
                location.getIdLocation(),
                safeClient(location),
                location.getClientTelephone() != null ? location.getClientTelephone() : "—",
                location.getIdVehicule(),
                dateDebut,
                dateFin,
                location.getPrixParJour(),
                location.getStatut() != null ? location.getStatut() : "—"
        );
    }

    private static String buildEmailBase(String titre, String couleur,
                                         String intro, String contenu, String footer) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
              <table width="100%%" style="max-width:600px;margin:30px auto;">
                <tr>
                  <td style="background:%s;padding:30px;text-align:center;border-radius:10px 10px 0 0;">
                    <h1 style="color:white;margin:0;font-size:26px;">🚗 HORIZIA</h1>
                    <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;">Agence de Location de Voitures</p>
                    <h2 style="color:white;margin:15px 0 0;font-size:20px;">%s</h2>
                  </td>
                </tr>
                <tr>
                  <td style="background:white;padding:30px;">
                    %s %s %s
                  </td>
                </tr>
                <tr>
                  <td style="background:#2c3e50;padding:20px;text-align:center;border-radius:0 0 10px 10px;">
                    <p style="color:rgba(255,255,255,0.7);margin:0;font-size:13px;">
                      📍 %s | 📞 %s | 📧 %s
                    </p>
                    <p style="color:rgba(255,255,255,0.5);margin:8px 0 0;font-size:11px;">
                      © 2025 Horizia - Tous droits réservés
                    </p>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(couleur, titre, intro, contenu, footer,
                ADRESSE_AGENCE, TEL_AGENCE, EMAIL_EXPEDITEUR);
    }

    private static String safeClient(Location location) {
        return location.getClientNomComplet() != null ? location.getClientNomComplet() : "Client";
    }
}