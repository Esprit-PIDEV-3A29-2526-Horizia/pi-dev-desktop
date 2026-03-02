package tn.esprit.backend.utils;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VoskService {
    private static Model model;
    private static boolean initialized = false;
    // Chemin absolu ou relatif vers le modèle Vosk (adapter si besoin)
    private static final String MODEL_PATH = "C:\\Users\\Dell\\pi-dev-desktop\\Horizia\\vosk-model-small-fr-0.22";

    public static void initModel() {
        if (!initialized) {
            try {
                model = new Model(MODEL_PATH);
                initialized = true;
                System.out.println("✅ Modèle Vosk chargé avec succès !");
            } catch (IOException e) {
                System.err.println("❌ Erreur chargement modèle Vosk : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static String recognizeSpeech() {
        if (!initialized) {
            initModel();
            if (!initialized) return "Erreur: modèle non chargé";
        }

        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Microphone non supporté");
                return "";
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("🎙️ Enregistrement de 5 secondes... parlez !");

            Recognizer recognizer = new Recognizer(model, 16000);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            long endTime = System.currentTimeMillis() + 5000; // 5 secondes

            while (System.currentTimeMillis() < endTime) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            line.stop();
            line.close();

            String result = recognizer.getFinalResult();
            recognizer.close();

            // Extraction du texte depuis le JSON
            if (result != null && !result.isEmpty()) {
                if (result.contains("\"text\"")) {
                    int start = result.indexOf("text") + 6;
                    int end = result.lastIndexOf("\"");
                    if (start < end) {
                        result = result.substring(start, end);
                    }
                }
                System.out.println("✅ Texte reconnu : " + result);
                return result.trim();
            }
            return "";
        } catch (Exception e) {
            System.err.println("Erreur reconnaissance vocale: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
}