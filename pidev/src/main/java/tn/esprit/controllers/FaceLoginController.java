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
import tn.esprit.services.FaceRecognitionService;
import tn.esprit.services.AuthService;
import tn.esprit.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceLoginController {

    @FXML private VBox webcamContainer;
    @FXML private Label statusLabel;
    @FXML private Button btnBack;

    private Webcam webcam;
    private FaceRecognitionService faceService;
    private AuthService authService = new AuthService();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isRunning = false;
    private loginController mainLoginController;

    @FXML
    public void initialize() {
        faceService = new FaceRecognitionService();
        initializeWebcam();

        // ✅ Fermeture manuelle de la fenêtre
        Platform.runLater(() -> {
            Stage stage = (Stage) webcamContainer.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("Fermeture de la fenêtre - Arrêt de la webcam");
                stopWebcam();
            });
        });
    }
    private void stopWebcam() {
        isRunning = false;
        if (webcam != null && webcam.isOpen()) {
            try {
                webcam.close();
                System.out.println("✅ Webcam fermée");
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture de la webcam: " + e.getMessage());
            }
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public void setLoginController(loginController controller) {
        this.mainLoginController = controller;
    }

    private void initializeWebcam() {
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                statusLabel.setText("❌ Aucune webcam détectée");
                return;
            }

            webcam.open();

            WebcamPanel panel = new WebcamPanel(webcam);
            panel.setFPSDisplayed(true);
            panel.setMirrored(true);

            SwingNode swingNode = new SwingNode();
            swingNode.setContent(panel);

            webcamContainer.getChildren().add(swingNode);

            statusLabel.setText("✅ Webcam prête - Placez votre visage devant la caméra");
            isRunning = true;

            startRecognitionLoop();

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: Impossible d'initialiser la webcam");
            e.printStackTrace();
        }
    }

    private void startRecognitionLoop() {
        executor.submit(() -> {
            int attempts = 0;
            final int MAX_ATTEMPTS = 30; // ~15 secondes (30 * 500ms)

            while (isRunning && attempts < MAX_ATTEMPTS) {
                try {
                    java.awt.image.BufferedImage bufferedImage = webcam.getImage();
                    if (bufferedImage != null) {
                        Mat frame = faceService.bufferedImageToMat(bufferedImage);
                        Rect faceRect = faceService.detectFace(frame);

                        if (faceRect != null) {
                            Platform.runLater(() ->
                                    statusLabel.setText("🟢 Visage détecté! Reconnaissance en cours...")
                            );

                            Mat face = new Mat(frame, faceRect);
                            Mat processedFace = faceService.preprocessFace(face);
                            int userId = faceService.recognizeFace(processedFace);
                            System.out.println("🔍 UserId reconnu: " + userId);  // ← AJOUTE

                            if (userId != -1) {
                                // Récupérer l'utilisateur
                                User user1 = authService.getUserById(userId);
                                System.out.println("👤 Utilisateur trouvé: " + (user1 != null ? user1.getEmail() : "null"));  // ← AJOUTE
                                // ✅ SUCCÈS
                                Platform.runLater(() -> {
                                    statusLabel.setText("✅ Visage reconnu! Connexion...");
                                    stopWebcam();  // ← FERMER LA WEBCAM

                                    User user = authService.getUserById(userId);
                                    if (user != null && mainLoginController != null) {
                                        mainLoginController.onFaceRecognized(user.getEmail());
                                    }

                                    Stage stage = (Stage) webcamContainer.getScene().getWindow();
                                    stage.close();
                                });
                                return; // Sortir de la méthode
                            }
                        }
                    }
                    attempts++;
                    Thread.sleep(500);

                } catch (Exception e) {
                    e.printStackTrace();
                    attempts++;
                }
            }

            // ❌ ÉCHEC - Trop de tentatives
            Platform.runLater(() -> {
                statusLabel.setText("❌ Reconnaissance échouée. Veuillez réessayer.");
                stopWebcam();  // ← FERMER LA WEBCAM AUSSI
            });
        });
    }

    @FXML
    private void goBack() {
        stopWebcam();
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}