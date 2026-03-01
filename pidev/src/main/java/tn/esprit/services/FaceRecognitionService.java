package tn.esprit.services;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
// 🔴 AJOUTER CES IMPORTS STATIQUES
import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_UNCHANGED;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FaceRecognitionService {

    private CascadeClassifier faceDetector;
    private FaceRecognizer faceRecognizer;
    private static final String MODEL_PATH = "data/face_model.yml";

    public FaceRecognitionService() {
        try {
            // Charger le fichier comme un flux
            java.io.InputStream cascadeStream = getClass().getResourceAsStream("/fxml/haarcascade_frontalface_default.xml");
            if (cascadeStream == null) {
                System.err.println("ERREUR: fichier haarcascade_frontalface_default.xml non trouvé!");
                return;
            }

            // Créer un fichier temporaire
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("cascade_", ".xml");
            java.nio.file.Files.copy(cascadeStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String cascadePath = tempFile.toString();
            System.out.println("Chargement du cascade classifier depuis: " + cascadePath);

            faceDetector = new CascadeClassifier(cascadePath);

            if (faceDetector.empty()) {
                System.err.println("Erreur: Impossible de charger le cascade classifier");
                System.err.println("Vérifie que le fichier XML est valide");
            } else {
                System.out.println("✅ Cascade classifier chargé avec succès!");
            }

            faceRecognizer = LBPHFaceRecognizer.create();

            File modelFile = new File(MODEL_PATH);
            if (modelFile.exists()) {
                faceRecognizer.read(MODEL_PATH);
            }

            // Nettoyer le fichier temporaire à la fermeture
            tempFile.toFile().deleteOnExit();

        } catch (Exception e) {
            System.err.println("Erreur d'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Mat preprocessFace(Mat face) {
        if (face == null || face.empty()) {
            return new Mat();
        }

        Mat processed = new Mat();

        if (face.channels() > 1) {
            opencv_imgproc.cvtColor(face, processed, COLOR_BGR2GRAY);
        } else {
            processed = face.clone();
        }

        Mat resized = new Mat();
        opencv_imgproc.resize(processed, resized, new Size(200, 200));

        Mat equalized = new Mat();
        opencv_imgproc.equalizeHist(resized, equalized);

        return equalized;
    }
    public boolean registerFace(int userId, Mat faceImage) {
        try {
            // Créer les dossiers nécessaires
            Files.createDirectories(Paths.get("data/faces"));
            Files.createDirectories(Paths.get("data/captures"));

            // Sauvegarder l'image du visage
            String facePath = "data/faces/user_" + userId + ".jpg";
            opencv_imgcodecs.imwrite(facePath, faceImage);

            // Prétraiter l'image
            Mat processedFace = preprocessFace(faceImage);

            // Créer le vecteur d'images (une seule image pour l'instant)
            MatVector imageVector = new MatVector(1);
            imageVector.put(0, processedFace);

            // Créer la matrice d'étiquettes (labels)
            Mat labelMat = new Mat(1, 1, CV_32SC1);
            labelMat.ptr().putInt(userId);  // Cette ligne peut causer des problèmes
            // Vérifier si le modèle existe déjà
            File modelFile = new File(MODEL_PATH);
            if (modelFile.exists()) {
                // Mettre à jour le modèle existant
                faceRecognizer.update(imageVector, labelMat);
                System.out.println("✅ Modèle mis à jour pour l'utilisateur " + userId);
            } else {
                // Créer un nouveau modèle
                faceRecognizer.train(imageVector, labelMat);
                System.out.println("✅ Nouveau modèle créé pour l'utilisateur " + userId);
            }

            // Sauvegarder le modèle
            faceRecognizer.write(MODEL_PATH);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'enregistrement du visage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int recognizeFace(Mat faceImage) {
        try {
            Mat processedFace = preprocessFace(faceImage);
            IntPointer labelPtr = new IntPointer(1);
            DoublePointer confidencePtr = new DoublePointer(1);
            faceRecognizer.predict(processedFace, labelPtr, confidencePtr);
            int predictedLabel = labelPtr.get(0);
            double confidence = confidencePtr.get(0);
            // 🔴 AJOUTE CES LIGNES
            System.out.println("=== DÉBOGAGE RECONNAISSANCE ===");
            System.out.println("Label prédit: " + predictedLabel);
            System.out.println("Confiance: " + confidence);
            System.out.println("Seuil: 70.0");
            System.out.println("Résultat: " + (confidence < 70.0 ? "✅ Accepté" : "❌ Rejeté"));
            if (confidence < 90.0) {
                return predictedLabel;
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Mat bufferedImageToMat(java.awt.image.BufferedImage bi) {
        if (bi == null) {
            return null;
        }

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(bi, "jpg", baos);
            baos.flush();
            byte[] bytes = baos.toByteArray();
            baos.close();

            return opencv_imgcodecs.imdecode(new Mat(bytes), IMREAD_UNCHANGED);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Rect detectFace(Mat image) {
        if (image == null || image.empty()) {
            return null;
        }

        Mat grayImage = new Mat();
        opencv_imgproc.cvtColor(image, grayImage, opencv_imgproc.COLOR_BGR2GRAY);

        RectVector faceDetections = new RectVector();
        faceDetector.detectMultiScale(grayImage, faceDetections);

        if (faceDetections.size() > 0) {
            return faceDetections.get(0);
        }
        return null;
    }
}