package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entites.Reservation;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

public class ReservationCardUserController {

    @FXML private Label lblDestination, lblDate, lblPersonnes, lblStatut;
    @FXML private Button btnVoirQr;
    @FXML private Button btnAnnuler;

    private Reservation current;

    public void setData(Reservation res) {
        this.current = res;
        String destination = (res.getDestination() == null) ? "" : res.getDestination();
        lblDestination.setText(destination.toUpperCase());
        lblDestination.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        lblDate.setText("Réservée le : " + res.getDate_reservation());
        lblPersonnes.setText(res.getNbr_personnes() + " personnes");
        String statutDb = normalizeStatut(res.getStatut());
        lblStatut.setText(statutDb);
        lblStatut.setStyle(styleStatut(statutDb));
        boolean confirmed = "CONFIRMEE".equals(statutDb);
        boolean pending   = "EN_ATTENTE".equals(statutDb);
        btnVoirQr.setVisible(confirmed);
        btnVoirQr.setManaged(confirmed);
        boolean hasQr = res.getQrCode() != null && !res.getQrCode().isBlank();
        btnVoirQr.setDisable(!hasQr);
        if (!hasQr) {
            btnVoirQr.setStyle("-fx-background-color:#CBD5E1; -fx-text-fill:#475569; -fx-background-radius:10;");
        } else {
            btnVoirQr.setStyle("-fx-background-color:#23779C; -fx-text-fill:white; -fx-background-radius:10; -fx-cursor:hand;");
        }
        btnAnnuler.setVisible(pending);
        btnAnnuler.setManaged(pending);
    }

    private String normalizeStatut(String s) {
        if (s == null || s.isBlank()) return "EN_ATTENTE";
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private String styleStatut(String st) {
        return switch (st) {
            case "CONFIRMEE" -> "-fx-background-color:#DCFCE7; -fx-text-fill:#16A34A; -fx-background-radius:10; -fx-padding:4 12; -fx-font-weight:bold;";
            case "EN_ATTENTE" -> "-fx-background-color:#FEF3C7; -fx-text-fill:#D97706; -fx-background-radius:10; -fx-padding:4 12; -fx-font-weight:bold;";
            case "ANNULEE" -> "-fx-background-color:#FEE2E2; -fx-text-fill:#EF4444; -fx-background-radius:10; -fx-padding:4 12; -fx-font-weight:bold;";
            default -> "-fx-background-color:#E2E8F0; -fx-text-fill:#334155; -fx-background-radius:10; -fx-padding:4 12; -fx-font-weight:bold;";
        };
    }

    @FXML
    private void voirQrCode() {
        if (current == null) return;
        String b64 = current.getQrCode();
        if (b64 == null || b64.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "QR Code non disponible pour cette réservation.").showAndWait();
            return;
        }
        try {
            if (b64.contains(",")) {
                b64 = b64.substring(b64.indexOf(",") + 1);
            }
            b64 = b64.replaceAll("\\s+", "");
            byte[] bytes = Base64.getDecoder().decode(b64.getBytes(StandardCharsets.UTF_8));
            Image img = new Image(new ByteArrayInputStream(bytes));
            if (img.isError()) {
                throw new RuntimeException("Image QR invalide (Base64 incorrect).");
            }
            ImageView iv = new ImageView(img);
            iv.setFitWidth(280);
            iv.setFitHeight(280);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            Label title = new Label("QR Code - Réservation #" + current.getId());
            title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
            VBox root = new VBox(12, title, iv);
            root.setPadding(new Insets(18));
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: #F6F7FB;");
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("QR Code");
            st.setScene(new Scene(root, 360, 380));
            st.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Impossible d’afficher le QR Code.\nCause: " + e.getMessage()).showAndWait();
        }
    }
}