package tn.esprit.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import tn.esprit.entities.Participation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;

public class QRCodeService {

    public static Image generateQRCode(Participation participation) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String qrData = String.format(
                    "RÉSERVATION EVENTHUB\n" +
                            "══════════════════════\n" +
                            "ID Réservation: %d\n" +
                            "ID Événement: %d\n" +
                            "Places réservées: %d\n" +
                            "Montant total: %.0f DT\n" +
                            "Date réservation: %s\n" +
                            "══════════════════════\n" +
                            "Présentez ce QR code à l'entrée",
                    participation.getId_participation(),
                    participation.getId_event(),
                    participation.getNombre_places(),
                    participation.getMontant_total(),
                    sdf.format(participation.getDate_participation())
            );

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateQRCodeBase64(Participation participation) {
        Image qrImage = generateQRCode(participation);
        if (qrImage == null) return null;

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(qrImage, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bufferedImage, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode utilitaire pour générer le contenu du QR code
    private static String buildQRContent(int locationId, String nomClient, String vehicule, String dateDebut, String dateFin) {
        return String.format(
                "LOCATION HORIZIA\n" +
                        "══════════════════════\n" +
                        "ID Location: %d\n" +
                        "Client: %s\n" +
                        "Véhicule: %s\n" +
                        "Date début: %s\n" +
                        "Date fin: %s\n" +
                        "══════════════════════\n" +
                        "Présentez ce QR code pour récupérer votre véhicule",
                locationId, nomClient, vehicule, dateDebut, dateFin
        );
    }

    private static BufferedImage genererQRCodeImage(String contenu) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(contenu, BarcodeFormat.QR_CODE, 300, 300);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private static BufferedImage ajouterCadreHorizia(BufferedImage qr, int locationId) {
        // Version simple: retourne le QR sans modification
        return qr;
    }

    private static Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static Image genererQRCodeLocation(int locationId, String nomClient,
                                              String vehicule, String dateDebut,
                                              String dateFin) {
        String contenu = buildQRContent(locationId, nomClient, vehicule, dateDebut, dateFin);
        try {
            BufferedImage qrImage = genererQRCodeImage(contenu);
            BufferedImage qrFinal = ajouterCadreHorizia(qrImage, locationId);
            return bufferedImageToFXImage(qrFinal);
        } catch (WriterException e) {
            System.err.println("[QRCodeService] Erreur génération QR : " + e.getMessage());
            return null;
        }
    }
}