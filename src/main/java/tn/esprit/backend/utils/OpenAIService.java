package tn.esprit.backend.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIService {
    // Remplace par ta vraie clé API OpenAI
    private static final String API_KEY = "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * Génère une description de publication à partir d'un titre.
     */
    public static String genererDescription(String titre) {
        String prompt = "Rédige une description détaillée et attrayante pour une publication de voyage ayant pour titre : \"" + titre + "\". " +
                "La description doit être en français, comporter environ 150 à 200 mots, et donner envie de lire la suite.";
        return appelerGPT(prompt);
    }

    /**
     * Traduit un texte vers une langue cible (ex: "anglais").
     */
    public static String traduire(String texte, String langueCible) {
        String prompt = "Traduis le texte suivant en " + langueCible + " :\n" + texte;
        return appelerGPT(prompt);
    }

    private static String appelerGPT(String prompt) {
        try {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Tu es un assistant spécialisé dans la rédaction de descriptions de voyages.");

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(systemMessage);
            messages.put(userMessage);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String content = message.getString("content");
                    return content.trim();
                }
            } else {
                System.err.println("Erreur API OpenAI: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Exception lors de l'appel OpenAI: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}