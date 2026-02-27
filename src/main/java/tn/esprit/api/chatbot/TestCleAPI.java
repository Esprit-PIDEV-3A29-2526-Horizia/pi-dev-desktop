package tn.esprit.api.chatbot;

import tn.esprit.utils.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestCleAPI {
    public static void main(String[] args) {
        String apiKey = Config.get("gemini.api.key");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("ERREUR: Clé API non trouvée dans config.properties");
            return;
        }
        System.out.println("Clé API trouvée: " + apiKey.substring(0, 10) + "...");
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Clé API valide !");
                System.out.println("Modèles disponibles: " + response.body().substring(0, 200) + "...");
            } else {
                System.out.println("Clé API invalide. Code: " + response.statusCode());
                System.out.println("Réponse: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}