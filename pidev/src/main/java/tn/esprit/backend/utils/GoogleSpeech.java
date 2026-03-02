package tn.esprit.backend.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class GoogleSpeech {

    private static final String API_KEY = "AIzaSyAF0L0s7W-NXDx3otedBsXiYU_LZyEFMaQ"; // Remplacez par votre clé
    private static final String API_URL = "https://speech.googleapis.com/v1/speech:recognize?key=" + API_KEY;

    public static CompletableFuture<String> recognize(String languageCode) {
        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                // 1. Enregistrement audio
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    throw new LineUnavailableException("Format audio non supporté");
                }
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                System.out.println("🔴 Enregistrement de 5 secondes... parlez maintenant !");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < 5000) {
                    int bytesRead = line.read(buffer, 0, buffer.length);
                    out.write(buffer, 0, bytesRead);
                }
                line.stop();
                line.close();

                byte[] audioData = out.toByteArray();
                System.out.println("✅ Audio capturé : " + audioData.length + " bytes");

                // 2. Encoder en Base64
                String base64Audio = Base64.getEncoder().encodeToString(audioData);

                // 3. Construire la requête JSON
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode requestJson = mapper.createObjectNode();
                ObjectNode config = mapper.createObjectNode();
                config.put("encoding", "LINEAR16");
                config.put("sampleRateHertz", 16000);
                config.put("languageCode", languageCode);
                requestJson.set("config", config);

                ObjectNode audio = mapper.createObjectNode();
                audio.put("content", base64Audio);
                requestJson.set("audio", audio);

                String jsonRequest = mapper.writeValueAsString(requestJson);

                // 4. Appel HTTP POST
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpPost httpPost = new HttpPost(API_URL);
                    httpPost.setEntity(new ByteArrayEntity(jsonRequest.getBytes(), ContentType.APPLICATION_JSON));

                    var response = client.execute(httpPost);
                    String jsonResponse = EntityUtils.toString(response.getEntity());

                    JsonNode root = mapper.readTree(jsonResponse);
                    if (root.has("results") && root.get("results").size() > 0) {
                        String transcript = root.get("results").get(0).get("alternatives").get(0).get("transcript").asText();
                        System.out.println("📝 Texte reconnu : " + transcript);
                        future.complete(transcript);
                    } else {
                        System.out.println("⚠️ Aucune parole reconnue");
                        future.complete("");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                future.complete("");
            }
        }).start();

        return future;
    }
}