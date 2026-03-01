package tn.esprit.backend.utils;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class AudioRecorder {

    public static CompletableFuture<Path> record(int seconds) {
        CompletableFuture<Path> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                // Configuration audio
                AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                System.out.println("🔴 Enregistrement de " + seconds + " secondes...");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < seconds * 1000) {
                    int bytesRead = line.read(buffer, 0, buffer.length);
                    out.write(buffer, 0, bytesRead);
                }
                line.stop();
                line.close();

                byte[] audioData = out.toByteArray();

                // Sauvegarde dans un fichier temporaire
                Path tempFile = Files.createTempFile("horizia_audio_", ".wav");
                Files.write(tempFile, audioData);
                System.out.println("✅ Audio sauvegardé : " + tempFile);
                future.complete(tempFile);

            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }
}