package tn.esprit.api.chatbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import tn.esprit.entites.Voyage;
import tn.esprit.services.VoyageService;
import tn.esprit.utils.Config;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GeminiService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private final String apiKey;
    private final OkHttpClient client;
    private final VoyageService voyageService;

    public GeminiService() {
        this.apiKey = Config.get("gemini.api.key");
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.voyageService = new VoyageService();
        System.out.println("Clé API Gemini chargée : " + (apiKey != null ? "OK" : "NULL"));
        System.out.println("URL utilisée : " + API_URL);
    }
    private String getContexteVoyages() {
        List<Voyage> voyages = voyageService.afficher();
        StringBuilder contexte = new StringBuilder();
        contexte.append("Tu es un assistant virtuel pour une agence de voyages. ");
        contexte.append("Voici la liste complète de nos voyages :\n\n");
        if (voyages == null || voyages.isEmpty()) {
            contexte.append("Aucun voyage disponible pour le moment.\n");
        } else {
            for (Voyage v : voyages) {
                String statut = v.getPlaces_restantes() > 0 ? "DISPONIBLE" : "COMPLET";
                contexte.append(String.format(
                        "• DESTINATION : %s\n" +
                                "  - Titre : %s\n" +
                                "  - Prix : %.2f DT\n" +
                                "  - Dates : %s au %s\n" +
                                "  - Places restantes : %d\n" +
                                "  - Statut : %s\n" +
                                "  - Description : %s\n\n",
                        v.getDestination(),
                        v.getTitre(),
                        v.getPrix(),
                        v.getDate_depart(),
                        v.getDate_retour(),
                        v.getPlaces_restantes(),
                        statut,
                        v.getDescription() != null ? v.getDescription() : "Non spécifiée"
                ));
            }
        }
        contexte.append("RÈGLES IMPORTANTES :\n");
        contexte.append("1. Réponds UNIQUEMENT aux questions concernant CES destinations\n");
        contexte.append("2. Utilise les prix en DT (Dinars Tunisiens)\n");
        contexte.append("3. Mentionne toujours la disponibilité\n");
        contexte.append("4. Sois amical et utilise des émojis 🌍✈️\n");
        return contexte.toString();
    }
    public void askQuestion(String question, ChatbotCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("Clé API non configurée dans config.properties");
            return;
        }
        String prompt = getContexteVoyages() + "\n\nQuestion du client : " + question + "\n\nRéponse : ";
        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        JsonObject[] parts = {part};
        content.add("parts", JsonParser.parseString("[{\"text\":\"" + escapeJson(prompt) + "\"}]"));
        JsonObject[] contents = {content};
        requestBody.add("contents", JsonParser.parseString("[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}]"));
        Request request = new Request.Builder()
                .url(API_URL + "?key=" + apiKey)
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        requestBody.toString()
                ))
                .build();
        System.out.println("Envoi de la question à Gemini...");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onError("Erreur de connexion : " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseStr = responseBody != null ? responseBody.string() : "";

                    if (!response.isSuccessful()) {
                        System.err.println("❌ Erreur HTTP " + response.code() + ": " + responseStr);

                        // Analyser l'erreur pour aider au diagnostic
                        if (response.code() == 404) {
                            callback.onError("❌ Modèle non trouvé. Vérifiez l'URL de l'API.");
                        } else if (response.code() == 403) {
                            callback.onError("❌ Clé API invalide ou désactivée.");
                        } else if (response.code() == 429) {
                            callback.onError("❌ Trop de requêtes. Limite de débit atteinte.");
                        } else {
                            callback.onError("❌ Erreur API (" + response.code() + ")");
                        }
                        return;
                    }

                    try {
                        JsonObject jsonResponse = JsonParser.parseString(responseStr).getAsJsonObject();

                        if (jsonResponse.has("candidates")) {
                            String reply = jsonResponse
                                    .getAsJsonArray("candidates")
                                    .get(0).getAsJsonObject()
                                    .getAsJsonObject("content")
                                    .getAsJsonArray("parts")
                                    .get(0).getAsJsonObject()
                                    .get("text").getAsString();
                            callback.onSuccess(reply);
                        } else {
                            callback.onError("❌ Réponse inattendue de l'API");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError("❌ Erreur de parsing de la réponse");
                    }
                }
            }
        });
    }
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}