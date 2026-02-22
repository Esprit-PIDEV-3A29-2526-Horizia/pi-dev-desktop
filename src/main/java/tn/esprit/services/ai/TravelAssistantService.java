package tn.esprit.services.ai;

import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.utils.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TravelAssistantService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey = Config.get("openai.apiKey");

    public String askAssistant(String systemContext, String userMessage) throws Exception {

        String body = """
        {
          "model": "gpt-4.1-mini",
          "messages": [
            {"role": "system", "content": "%s"},
            {"role": "user", "content": "%s"}
          ],
          "temperature": 0.7
        }
        """.formatted(
                escape(systemContext),
                escape(userMessage)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return extractAnswer(response.body());
    }

    private String extractAnswer(String json) {
        JSONObject root = new JSONObject(json);
        JSONArray choices = root.getJSONArray("choices");
        if (choices.isEmpty()) {
            return "Aucune réponse reçue.";
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        return message.getString("content");
    }
    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}