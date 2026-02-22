package tn.esprit.utils;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class AudioRecorder {

    public static byte[] recordAudio(int durationSeconds) throws LineUnavailableException {
        // Format : PCM signé, 16kHz, 16 bits, mono, little-endian
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone non supporté");
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < durationSeconds * 1000) {
            bytesRead = line.read(buffer, 0, buffer.length);
            out.write(buffer, 0, bytesRead);
        }

        line.stop();
        line.close();
        return out.toByteArray();
    }
}