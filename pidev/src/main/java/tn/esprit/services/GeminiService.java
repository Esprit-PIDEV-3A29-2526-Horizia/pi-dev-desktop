package tn.esprit.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import tn.esprit.entities.logement;
import tn.esprit.entities.User;
import tn.esprit.entities.reservationlog;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class GeminiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Service pour récupérer l'historique des réservations
    private final Servicereservationlog reservationService = new Servicereservationlog();

    // URL de l'API, la clé sera injectée dynamiquement depuis config.properties
    private String apiKey;
    private String apiUrl;

    public GeminiService() {
        loadApiKey();
    }

    /** Charge la clé API depuis config.properties */
    private void loadApiKey() {
        Properties props = new Properties();
        String path = "C:\\Users\\khali\\integration\\pidev\\src\\main\\resources\\config.properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            apiKey = props.getProperty("gemini.api.key");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("La clé Gemini API est manquante dans config.properties !");
            }
            apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le fichier config.properties à : " + path, e);
        }
    }

    /**
     * Appelle Gemini et retourne une liste d'IDs de logements recommandés.
     */
    public List<Map<String, Object>> getRecommendations(User user, List<logement> logements) {
        String prompt = buildPrompt(user, logements);

        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonObject parts = new JsonObject();
        parts.addProperty("text", prompt);
        content.add("parts", gson.toJsonTree(List.of(parts)));
        requestBody.add("contents", gson.toJsonTree(List.of(content)));

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("response_mime_type", "application/json");
        requestBody.add("generationConfig", generationConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseRecommendations(response.body());
            } else {
                System.err.println("Erreur API Gemini : " + response.statusCode() + " - " + response.body());
                return List.of();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private String buildPrompt(User user, List<logement> logements) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un assistant de recommandation de logements pour une agence de voyage.\n");

        if (user != null) {
            sb.append("Profil de l'utilisateur :\n");
            sb.append("- Prénom : ").append(user.getPrenom()).append("\n");
            sb.append("- Nom : ").append(user.getNom()).append("\n");

            List<reservationlog> historique = getHistoriqueReservations(user);
            if (!historique.isEmpty()) {
                sb.append("- Réservations passées :\n");
                for (reservationlog r : historique) {
                    logement log = getLogementById(r.getId_l(), logements);
                    if (log != null) {
                        sb.append("  * ").append(log.getNom())
                                .append(" (").append(log.getType()).append(")")
                                .append(" à ").append(log.getAdresse())
                                .append(" - équipements : ").append(log.getEquipement())
                                .append(" (réservé du ").append(r.getDate_debut()).append(" au ").append(r.getDate_fin()).append(")\n");
                    }
                }
            } else {
                sb.append("- Aucune réservation passée.\n");
            }
        } else {
            sb.append("Utilisateur non connecté (anonyme).\n");
        }

        List<Map<String, ? extends Serializable>> logementsSimplifies = logements.stream()
                .map(l -> Map.of(
                        "id", l.getId(),
                        "nom", l.getNom(),
                        "type", l.getType(),
                        "prix_nuit", l.getTarif_nuit(),
                        "adresse", l.getAdresse(),
                        "equipement", l.getEquipement(),
                        "disponible", l.isDisponibilite()
                ))
                .collect(Collectors.toList());
        sb.append("\nVoici la liste des logements disponibles (au format JSON) :\n");
        sb.append(gson.toJson(logementsSimplifies));

        sb.append("\n\nTâche : Sélectionne 5 logements parmi cette liste qui pourraient le plus intéresser cet utilisateur. ");
        sb.append("Pour chaque recommandation, fournis une brève explication personnalisée.\n");
        sb.append("Retourne uniquement un objet JSON avec une clé \"recommandations\" contenant une liste d'objets. ");
        sb.append("Chaque objet doit avoir les clés \"id_logement\" (int), \"titre\" (string, le nom du logement), et \"raison\" (string).\n");

        return sb.toString();
    }

    private List<reservationlog> getHistoriqueReservations(User user) {
        try {
            return reservationService.getReservationsByClientId(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private logement getLogementById(int id, List<logement> logements) {
        return logements.stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private List<Map<String, Object>> parseRecommendations(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        String text = json.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        JsonObject recosJson = JsonParser.parseString(text).getAsJsonObject();
        return gson.fromJson(recosJson.getAsJsonArray("recommandations"), new TypeToken<List<Map<String, Object>>>(){}.getType());
    }
}