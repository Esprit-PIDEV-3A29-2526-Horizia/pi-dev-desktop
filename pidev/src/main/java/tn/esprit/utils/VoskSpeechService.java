package tn.esprit.utils;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class VoskSpeechService {

    // Chemin absolu vers le modèle (à adapter si votre dossier est différent)
    private static final String MODEL_PATH = "C:/Users/Dell/pi-dev-desktop/Horizia/vosk-model-small-fr-0.22";

    private static Model model;

    static {
        LibVosk.setLogLevel(LogLevel.WARNINGS);
        try {
            Path modelPath = Paths.get(MODEL_PATH);
            if (!Files.exists(modelPath)) {
                System.err.println("❌ Dossier modèle introuvable : " + modelPath.toAbsolutePath());
                System.err.println("Vérifiez que le chemin est correct et que le dossier contient les fichiers.");
            } else {
                System.out.println("🔍 Chargement du modèle Vosk depuis : " + modelPath);
                model = new Model(modelPath.toString());
                System.out.println("✅ Modèle Vosk chargé avec succès !");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement du modèle Vosk : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static CompletableFuture<String> recognize(int seconds) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (model == null) {
            System.err.println("❌ Modèle Vosk non chargé, impossible de démarrer la reconnaissance.");
            future.complete("");
            return future;
        }

        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    throw new LineUnavailableException("Format audio non supporté");
                }
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                System.out.println("🔴 Enregistrement de " + seconds + " secondes... parlez !");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < seconds * 1000L) {
                    int bytesRead = line.read(buffer, 0, buffer.length);
                    out.write(buffer, 0, bytesRead);
                }
                line.stop();
                line.close();

                byte[] audioData = out.toByteArray();

                Recognizer recognizer = new Recognizer(model, 16000);
                recognizer.acceptWaveForm(audioData, audioData.length);
                String result = recognizer.getFinalResult();
                recognizer.close();

                // Extraction du texte
                String text = "";
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(result);
                    text = root.path("text").asText();
                } catch (Exception e) {
                    // Fallback regex
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"");
                    java.util.regex.Matcher matcher = pattern.matcher(result);
                    if (matcher.find()) {
                        text = matcher.group(1);
                    }
                }

                if (text.isEmpty()) {
                    System.out.println("⚠️ Aucun texte reconnu (résultat brut : " + result + ")");
                    future.complete("");
                } else {
                    System.out.println("📝 Texte reconnu : " + text);
                    future.complete(text);
                }

            } catch (LineUnavailableException e) {
                e.printStackTrace();
                future.complete("");
            } catch (Exception e) {
                e.printStackTrace();
                future.complete("");
            }
        }).start();

        return future;
    }
}