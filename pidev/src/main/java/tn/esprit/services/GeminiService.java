package tn.esprit.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import tn.esprit.entities.logement;
import tn.esprit.entities.User;
import tn.esprit.entities.reservationlog;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeminiService {

    // Remplacez par votre vraie clé API (à ne pas commiter !)
    private static final String API_KEY = "AIzaSyBPFBct72SYy6Yb1M5lKLwtLd2G0qx1D5g";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + API_KEY;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Service pour récupérer l'historique des réservations
    private final Servicereservationlog reservationService = new Servicereservationlog();

    /**
     * Appelle Gemini et retourne une liste d'IDs de logements recommandés.
     *
     * @param user       l'utilisateur connecté (peut être null)
     * @param logements  la liste complète des logements disponibles
     * @return une liste de Map contenant "id_logement" (int) et "raison" (String)
     */
    public List<Map<String, Object>> getRecommendations(User user, List<logement> logements) {
        // 1. Construire le prompt
        String prompt = buildPrompt(user, logements);

        // 2. Créer le corps de la requête
        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonObject parts = new JsonObject();
        parts.addProperty("text", prompt);
        content.add("parts", gson.toJsonTree(List.of(parts)));
        requestBody.add("contents", gson.toJsonTree(List.of(content)));

        // Demander une réponse JSON structurée
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("response_mime_type", "application/json");
        requestBody.add("generationConfig", generationConfig);

        // 3. Envoyer la requête
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
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

    /**
     * Construit le prompt en incluant le profil utilisateur et l'historique.
     */
    private String buildPrompt(User user, List<logement> logements) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un assistant de recommandation de logements pour une agence de voyage.\n");

        if (user != null) {
            sb.append("Profil de l'utilisateur :\n");
            sb.append("- Prénom : ").append(user.getPrenom()).append("\n");
            sb.append("- Nom : ").append(user.getNom()).append("\n");

            // Récupérer l'historique des réservations de l'utilisateur
            List<reservationlog> historique = getHistoriqueReservations(user);
            if (!historique.isEmpty()) {
                sb.append("- Réservations passées :\n");
                for (reservationlog r : historique) {
                    // Pour chaque réservation, récupérer le logement associé
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

        // Liste des logements disponibles (format JSON simplifié)
        sb.append("\nVoici la liste des logements disponibles (au format JSON) :\n");
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
        sb.append(gson.toJson(logementsSimplifies));

        // Tâche demandée
        sb.append("\n\nTâche : Sélectionne 5 logements parmi cette liste qui pourraient le plus intéresser cet utilisateur. ");
        sb.append("Pour chaque recommandation, fournis une brève explication personnalisée.\n");
        sb.append("Retourne uniquement un objet JSON avec une clé \"recommandations\" contenant une liste d'objets. ");
        sb.append("Chaque objet doit avoir les clés \"id_logement\" (int), \"titre\" (string, le nom du logement), et \"raison\" (string).\n");

        return sb.toString();
    }

    /**
     * Récupère l'historique des réservations pour un utilisateur.
     */
    private List<reservationlog> getHistoriqueReservations(User user) {
        try {
            return reservationService.getReservationsByClientId(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cherche un logement par son ID dans la liste complète.
     */
    private logement getLogementById(int id, List<logement> logements) {
        return logements.stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Extrait la liste des recommandations depuis la réponse JSON de Gemini.
     */
    private List<Map<String, Object>> parseRecommendations(String responseBody) {
        // La réponse contient "candidates" -> "content" -> "parts" -> "text"
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        String text = json.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        // Le texte est lui-même un JSON (grâce à response_mime_type)
        JsonObject recosJson = JsonParser.parseString(text).getAsJsonObject();
        return gson.fromJson(recosJson.getAsJsonArray("recommandations"), new TypeToken<List<Map<String, Object>>>(){}.getType());
    }
}