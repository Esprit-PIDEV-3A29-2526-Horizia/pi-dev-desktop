package tn.esprit.api.chatbot;

import tn.esprit.utils.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestToutesLesURLs {

    public static void main(String[] args) {
        String apiKey = Config.get("gemini.api.key");
        System.out.println("Clé API: " + (apiKey != null ? apiKey.substring(0, 10) + "..." : "NULL"));
        String[] urlsATester = {
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent",
                "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent",
                "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent",
                "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent"
        };
        HttpClient client = HttpClient.newHttpClient();
        String json = "{\"contents\":[{\"parts\":[{\"text\":\"Bonjour\"}]}]}";
        for (String url : urlsATester) {
            try {
                System.out.println("\nTest URL: " + url);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url + "?key=" + apiKey))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Code: " + response.statusCode());
                if (response.statusCode() == 200) {
                    System.out.println("SUCCÈS ! Cette URL fonctionne !");
                    System.out.println("Réponse: " + response.body().substring(0, Math.min(100, response.body().length())) + "...");
                } else {
                    System.out.println("Échec: " + response.statusCode());
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }
    }
}