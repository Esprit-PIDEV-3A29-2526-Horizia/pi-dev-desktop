package tn.esprit.api.chatbot;

import tn.esprit.utils.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestGemini {
    public static void main(String[] args) {
        try {
            String apiKey = Config.get("gemini.api.key");
            System.out.println("🔑 Clé: " + apiKey);

            HttpClient client = HttpClient.newHttpClient();

            String json = "{\"contents\":[{\"parts\":[{\"text\":\"Bonjour\"}]}]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            System.out.println("📤 Envoi requête...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Code: " + response.statusCode());
            System.out.println("📦 Réponse: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}