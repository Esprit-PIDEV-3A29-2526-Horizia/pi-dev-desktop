package org.example.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de génération de QR Codes pour Horizia
 *
 * FIX : Suppression de javafx.embed.swing.SwingFXUtils (nécessite le module javafx-swing).
 *       Remplacement par conversion manuelle BufferedImage → WritableImage (JavaFX natif).
 *       Aucune dépendance supplémentaire requise.
 */
public class QRCodeService {

    private static final int QR_WIDTH  = 300;
    private static final int QR_HEIGHT = 300;

    // Couleurs Horizia
    private static final Color COLOR_DARK  = new Color(26,  35,  50);   // #1a2332
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_BLUE  = new Color(3, 132, 183);    // #0384b7

    // ─────────────────────────────────────────────────────────────
    // MÉTHODES PUBLIQUES
    // ─────────────────────────────────────────────────────────────

    /**
     * Génère un QR Code pour une location et retourne l'image JavaFX
     */
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

    /**
     * Génère un QR Code simple à partir d'un texte
     */
    public static Image genererQRCodeSimple(String texte) {
        try {
            BufferedImage qrImage = genererQRCodeImage(texte);
            return bufferedImageToFXImage(qrImage);
        } catch (WriterException e) {
            System.err.println("[QRCodeService] Erreur génération QR simple : " + e.getMessage());
            return null;
        }
    }

    /**
     * Sauvegarde un QR Code dans un fichier PNG et retourne le chemin
     */
    public static String sauvegarderQRCode(int locationId, String nomClient,
                                           String vehicule, String dateDebut,
                                           String dateFin, String dossier) {
        String contenu = buildQRContent(locationId, nomClient, vehicule, dateDebut, dateFin);
        try {
            BufferedImage qrImage = genererQRCodeImage(contenu);
            BufferedImage qrFinal = ajouterCadreHorizia(qrImage, locationId);

            File dir = new File(dossier);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) System.err.println("[QRCodeService] Impossible de créer le dossier : " + dossier);
            }

            String nomFichier = "QR_Location_" + locationId + ".png";
            File fichier = new File(dossier + File.separator + nomFichier);
            ImageIO.write(qrFinal, "PNG", fichier);

            System.out.println("[QRCodeService] QR Code sauvegardé : " + fichier.getAbsolutePath());
            return fichier.getAbsolutePath();

        } catch (WriterException | IOException e) {
            System.err.println("[QRCodeService] Erreur sauvegarde QR : " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTHODES PRIVÉES
    // ─────────────────────────────────────────────────────────────

    private static String buildQRContent(int locationId, String nomClient,
                                         String vehicule, String dateDebut, String dateFin) {
        return "HORIZIA-LOCATION\n" +
                "ID:" + locationId + "\n" +
                "CLIENT:" + nomClient + "\n" +
                "VEHICULE:" + vehicule + "\n" +
                "DEBUT:" + dateDebut + "\n" +
                "FIN:" + dateFin;
    }

    private static BufferedImage genererQRCodeImage(String contenu) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(contenu, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);

        BufferedImage image = new BufferedImage(QR_WIDTH, QR_HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < QR_WIDTH; x++) {
            for (int y = 0; y < QR_HEIGHT; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? COLOR_DARK.getRGB() : COLOR_WHITE.getRGB());
            }
        }
        return image;
    }

    private static BufferedImage ajouterCadreHorizia(BufferedImage qrImage, int locationId) {
        int padding = 20;
        int headerH = 50;
        int footerH = 40;
        int totalW  = QR_WIDTH  + padding * 2;
        int totalH  = QR_HEIGHT + padding * 2 + headerH + footerH;

        BufferedImage result = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fond blanc
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, totalW, totalH, 20, 20);

        // Bande header bleue Horizia
        g.setColor(COLOR_BLUE);
        g.fillRoundRect(0, 0, totalW, headerH + 10, 20, 20);
        g.fillRect(0, 10, totalW, headerH);

        // Texte header
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        String titre = "HORIZIA";
        g.drawString(titre, (totalW - fm.stringWidth(titre)) / 2, headerH - 10);

        g.setFont(new Font("Arial", Font.PLAIN, 11));
        fm = g.getFontMetrics();
        String sousTitre = "Agence de Location de Voitures";
        g.drawString(sousTitre, (totalW - fm.stringWidth(sousTitre)) / 2, headerH + 5);

        // QR Code
        g.drawImage(qrImage, padding, headerH + padding, null);

        // Ligne séparatrice footer
        g.setColor(new Color(230, 230, 230));
        g.fillRect(padding, headerH + QR_HEIGHT + padding + 5, totalW - 2 * padding, 1);

        // Footer : ID de location
        g.setColor(new Color(100, 100, 100));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        fm = g.getFontMetrics();
        String footer = "Location #" + locationId;
        g.drawString(footer, (totalW - fm.stringWidth(footer)) / 2,
                headerH + QR_HEIGHT + padding + 25);

        g.dispose();
        return result;
    }

    /**
     * Convertit un BufferedImage AWT en Image JavaFX.
     * FIX : Remplace SwingFXUtils.toFXImage() — aucune dépendance javafx-swing requise.
     *
     * @param bufferedImage L'image AWT source
     * @return Image JavaFX correspondante
     */
    private static Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        int width  = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Convertir en TYPE_INT_ARGB si nécessaire
        BufferedImage argbImage;
        if (bufferedImage.getType() == BufferedImage.TYPE_INT_ARGB) {
            argbImage = bufferedImage;
        } else {
            argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = argbImage.createGraphics();
            g2d.drawImage(bufferedImage, 0, 0, null);
            g2d.dispose();
        }

        WritableImage fxImage = new WritableImage(width, height);
        PixelWriter pw = fxImage.getPixelWriter();

        int[] pixels = new int[width * height];
        argbImage.getRGB(0, 0, width, height, pixels, 0, width);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[y * width + x];
                // AWT : ARGB — JavaFX PixelWriter.setArgb attend aussi ARGB → compatible direct
                pw.setArgb(x, y, argb);
            }
        }
        return fxImage;
    }
}