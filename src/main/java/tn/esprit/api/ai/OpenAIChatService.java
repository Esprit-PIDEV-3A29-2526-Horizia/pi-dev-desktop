package tn.esprit.api.ai;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenAIChatService {

    private final String apiKey;
    private final String model;
    private final HttpClient client = HttpClient.newHttpClient();

    public OpenAIChatService(String apiKey, String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OPENAI apiKey is missing");
        }
        this.apiKey = apiKey.trim();
        this.model = (model == null || model.isBlank()) ? "gpt-4.1-mini" : model.trim();
    }

    public String ask(String userMessage) throws Exception {
        if (userMessage == null || userMessage.isBlank()) return "";
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        JSONArray input = new JSONArray();
        input.put(new JSONObject()
                .put("role", "system")
                .put("content", "Tu es un assistant de voyage pour l'application Horizia. "
                        + "Réponds en français, de façon claire, courte et utile. "
                        + "Propose des itinéraires, conseils, budget, sécurité, et astuces locales."));
        input.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));
        payload.put("input", input);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("OpenAI error " + res.statusCode() + " : " + res.body());
        }
        JSONObject json = new JSONObject(res.body());
        if (json.has("output")) {
            JSONArray out = json.getJSONArray("output");
            for (int i = 0; i < out.length(); i++) {
                JSONObject item = out.getJSONObject(i);
                if (item.has("content")) {
                    JSONArray content = item.getJSONArray("content");
                    for (int j = 0; j < content.length(); j++) {
                        JSONObject c = content.getJSONObject(j);
                        if ("output_text".equals(c.optString("type")) && c.has("text")) {
                            return c.getString("text");
                        }
                        if (c.has("text")) {
                            return c.getString("text");
                        }
                    }
                }
            }
        }

        // fallback
        return json.optString("text", "");
    }
}