package tn.esprit.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import tn.esprit.entities.Participation;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

import static tn.esprit.utils.QRCodeService.*;

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
                    participation.getNombrePlaces(),
                    participation.getMontantTotal(),
                    sdf.format(participation.getDateParticipation())
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
    public static Image genererQRCodeLocation(int locationId, String nomClient,
                                              String vehicule, String dateDebut,
                                              String dateFin) {
        String contenu = buildQRContent(locationId, nomClient, vehicule, dateDebut, dateFin);
        try {
            BufferedImage qrImage = genererQRCodeImage(contenu);
            BufferedImage qrFinal = ajouterCadreHorizia(qrImage, locationId);
            // FIX : conversion sans SwingFXUtils
            return bufferedImageToFXImage(qrFinal);
        } catch (WriterException e) {
            System.err.println("[QRCodeService] Erreur génération QR : " + e.getMessage());
            return null;
        }
    }
}