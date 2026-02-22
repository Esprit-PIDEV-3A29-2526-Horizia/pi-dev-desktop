package tn.esprit.utils;

import org.vosk.*;
import org.vosk.Recognizer;
import java.io.IOException;

public class VoskService {

    private static Model model;
    private static boolean initialized = false;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void initModel(String modelPath) throws IOException {
        if (!initialized) {
            // La gestion de log n'est pas disponible dans cette version
            // Log.setLogLevel(Log.WARNINGS);  // ← à supprimer
            model = new Model(modelPath);
            initialized = true;
        }
    }

    public static String recognize(byte[] audioData, int sampleRate) throws IOException {
        if (!initialized) {
            throw new IllegalStateException("Vosk model not initialized. Call initModel first.");
        }

        try (Recognizer recognizer = new Recognizer(model, sampleRate)) {
            recognizer.acceptWaveForm(audioData, audioData.length);
            String result = recognizer.getFinalResult(); // au format JSON
            return extractTextFromJson(result);
        }
    }

    private static String extractTextFromJson(String json) {
        // Extraction simple : on cherche le champ "text"
        int start = json.indexOf("\"text\" : \"");
        if (start < 0) return "";
        start += 10; // longueur de "\"text\" : \""
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end);
    }
}