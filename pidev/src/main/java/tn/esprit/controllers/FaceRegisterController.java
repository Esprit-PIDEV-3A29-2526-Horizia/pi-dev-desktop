package tn.esprit.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import tn.esprit.entities.User;
import tn.esprit.services.FaceRecognitionService;
import tn.esprit.services.AuthService;

public class FaceRegisterController {

    @FXML private VBox webcamContainer;
    @FXML private Label statusLabel;
    @FXML private Button btnSave;
    @FXML private Button btnSkip;

    private Webcam webcam;
    private FaceRecognitionService faceService;
    private AuthService authService = new AuthService();
    private User currentUser;
    private boolean isCapturing = false;

    @FXML
    public void initialize() {
        faceService = new FaceRecognitionService();
        initializeWebcam();

        // ✅ Gestionnaire de fermeture
        Platform.runLater(() -> {
            Stage stage = (Stage) webcamContainer.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("Fermeture de la fenêtre - Arrêt de la webcam");
                stopWebcam();
            });
        });
    }

    private void stopWebcam() {
        if (webcam != null && webcam.isOpen()) {
            try {
                webcam.close();
                System.out.println("✅ Webcam fermée");
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture de la webcam: " + e.getMessage());
            }
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            statusLabel.setText("Enregistrement pour: " + user.getEmail());
        }
    }

    private void initializeWebcam() {
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                statusLabel.setText("❌ Aucune webcam détectée");
                btnSave.setDisable(true);
                return;
            }

            webcam.open();

            WebcamPanel panel = new WebcamPanel(webcam);
            panel.setMirrored(true);

            SwingNode swingNode = new SwingNode();
            swingNode.setContent(panel);

            webcamContainer.getChildren().add(swingNode);

            statusLabel.setText("📸 Regardez la caméra et cliquez sur 'Enregistrer'");

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur d'initialisation de la webcam");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSkip() {
        stopWebcam();
        redirectToAccueil();
    }

    @FXML
    private void handleSaveFace() {
        if (currentUser == null) {
            statusLabel.setText("❌ Erreur: utilisateur non connecté");
            return;
        }

        if (isCapturing) return;

        try {
            isCapturing = true;
            btnSave.setDisable(true);
            statusLabel.setText("📸 Capture en cours...");

            java.awt.image.BufferedImage bufferedImage = webcam.getImage();
            if (bufferedImage == null) {
                statusLabel.setText("❌ Erreur de capture");
                resetButtons();
                return;
            }

            Mat frame = faceService.bufferedImageToMat(bufferedImage);
            Rect faceRect = faceService.detectFace(frame);

            if (faceRect == null) {
                statusLabel.setText("❌ Aucun visage détecté! Veuillez réessayer.");
                resetButtons();
                return;
            }

            Mat face = new Mat(frame, faceRect);
            Mat processedFace = faceService.preprocessFace(face);
            boolean registered = faceService.registerFace(currentUser.getId(), processedFace);

            if (registered) {
                statusLabel.setText("✅ Visage enregistré avec succès!");

                // ✅ FERMER LA WEBCAM AVANT DE REDIRIGER
                stopWebcam();

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(this::redirectToAccueil);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                statusLabel.setText("❌ Échec de l'enregistrement");
                resetButtons();
            }

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            resetButtons();
        }
    }


    private void redirectToAccueil() {
        try {
            if (webcam != null) {
                webcam.close();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accueil.fxml"));
            Parent root = loader.load();

            AccueilController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) webcamContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur de redirection");
        }
    }

    private void resetButtons() {
        Platform.runLater(() -> {
            btnSave.setDisable(false);
            isCapturing = false;
        });
    }
}