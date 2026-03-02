package tn.esprit.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de géolocalisation utilisant OpenStreetMap et Nominatim
 * 100% Gratuit et illimité (respecter 1 requête/seconde)
 */
public class GeolocationService {

    // ═══════════════════════════════════════════════════════
    // CONFIGURATION API
    // ═══════════════════════════════════════════════════════

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "Horizia-CarRental/1.0";

    private final OkHttpClient client;
    private long dernierAppel = 0;
    private static final long DELAI_MIN_MS = 1000; // 1 seconde entre les requêtes

    public GeolocationService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    /**
     * Géocoder une adresse (Adresse → Coordonnées GPS)
     * @param adresse Adresse complète ou partielle
     * @return Map contenant latitude, longitude, adresse_formatee, ville, code_postal
     */
    public Map<String, String> geocoderAdresse(String adresse) {
        Map<String, String> resultats = new HashMap<>();

        try {
            // Respecter le délai entre les requêtes
            attendreDelaiMinimum();

            System.out.println("→ Géocodage de l'adresse : " + adresse);

            // Encoder l'adresse pour l'URL
            String adresseEncodee = URLEncoder.encode(adresse, StandardCharsets.UTF_8);

            // Construire l'URL de la requête
            String url = NOMINATIM_URL + "/search?q=" + adresseEncodee +
                    "&format=json&addressdetails=1&limit=1&countrycodes=tn";

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Erreur API : " + response.code());
                }

                String jsonResponse = response.body().string();
                JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();

                if (jsonArray.size() == 0) {
                    System.out.println("⚠ Aucun résultat trouvé pour cette adresse");
                    resultats.put("erreur", "Adresse non trouvée");
                    return resultats;
                }

                JsonObject premier = jsonArray.get(0).getAsJsonObject();

                // Extraire les coordonnées
                String latitude = premier.get("lat").getAsString();
                String longitude = premier.get("lon").getAsString();
                String adresseFormatee = premier.get("display_name").getAsString();

                resultats.put("latitude", latitude);
                resultats.put("longitude", longitude);
                resultats.put("adresse_formatee", adresseFormatee);

                // Extraire les détails de l'adresse
                if (premier.has("address")) {
                    JsonObject adresseObj = premier.getAsJsonObject("address");

                    if (adresseObj.has("city")) {
                        resultats.put("ville", adresseObj.get("city").getAsString());
                    } else if (adresseObj.has("town")) {
                        resultats.put("ville", adresseObj.get("town").getAsString());
                    } else if (adresseObj.has("village")) {
                        resultats.put("ville", adresseObj.get("village").getAsString());
                    }

                    if (adresseObj.has("postcode")) {
                        resultats.put("code_postal", adresseObj.get("postcode").getAsString());
                    }

                    if (adresseObj.has("road")) {
                        resultats.put("rue", adresseObj.get("road").getAsString());
                    }
                }

                System.out.println("✓ Géocodage réussi : " + latitude + ", " + longitude);

            }
        } catch (Exception e) {
            System.err.println("✗ Erreur géocodage : " + e.getMessage());
            e.printStackTrace();
            resultats.put("erreur", "Erreur technique : " + e.getMessage());
        }

        return resultats;
    }

    /**
     * Géocodage inverse (Coordonnées GPS → Adresse)
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Map contenant l'adresse complète et ses composants
     */
    public Map<String, String> geocoderInverse(double latitude, double longitude) {
        Map<String, String> resultats = new HashMap<>();

        try {
            // Respecter le délai entre les requêtes
            attendreDelaiMinimum();

            System.out.println("→ Géocodage inverse : " + latitude + ", " + longitude);

            // Construire l'URL de la requête
            String url = NOMINATIM_URL + "/reverse?lat=" + latitude +
                    "&lon=" + longitude + "&format=json&addressdetails=1";

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Erreur API : " + response.code());
                }

                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                if (jsonObject.has("error")) {
                    System.out.println("⚠ Aucune adresse trouvée pour ces coordonnées");
                    resultats.put("erreur", "Coordonnées invalides");
                    return resultats;
                }

                String adresseFormatee = jsonObject.get("display_name").getAsString();
                resultats.put("adresse_formatee", adresseFormatee);

                // Extraire les détails de l'adresse
                if (jsonObject.has("address")) {
                    JsonObject adresseObj = jsonObject.getAsJsonObject("address");

                    if (adresseObj.has("city")) {
                        resultats.put("ville", adresseObj.get("city").getAsString());
                    } else if (adresseObj.has("town")) {
                        resultats.put("ville", adresseObj.get("town").getAsString());
                    }

                    if (adresseObj.has("postcode")) {
                        resultats.put("code_postal", adresseObj.get("postcode").getAsString());
                    }

                    if (adresseObj.has("road")) {
                        resultats.put("rue", adresseObj.get("road").getAsString());
                    }
                }

                System.out.println("✓ Géocodage inverse réussi");

            }
        } catch (Exception e) {
            System.err.println("✗ Erreur géocodage inverse : " + e.getMessage());
            e.printStackTrace();
            resultats.put("erreur", "Erreur technique : " + e.getMessage());
        }

        return resultats;
    }

    /**
     * Calculer la distance entre deux points GPS (en kilomètres)
     * Utilise la formule de Haversine
     * @param lat1 Latitude point 1
     * @param lon1 Longitude point 1
     * @param lat2 Latitude point 2
     * @param lon2 Longitude point 2
     * @return Distance en kilomètres
     */
    public double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int RAYON_TERRE_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RAYON_TERRE_KM * c;
    }

    /**
     * Formater une distance en texte lisible
     * @param distanceKm Distance en kilomètres
     * @return Texte formaté (ex: "2.5 km" ou "750 m")
     */
    public String formaterDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }

    /**
     * Obtenir les coordonnées de l'agence Horizia
     * @return Map avec latitude et longitude de l'agence
     */
    public Map<String, Double> getCoordoneesAgence() {
        Map<String, Double> coords = new HashMap<>();
        // Coordonnées de Tunis centre (à adapter selon votre agence)
        coords.put("latitude", 36.90123398692758);
        coords.put("longitude", 10.19090383100016);
        return coords;
    }

    /**
     * Calculer la distance entre un client et l'agence
     * @param clientLat Latitude du client
     * @param clientLon Longitude du client
     * @return Distance en km
     */
    public double calculerDistanceDepuisAgence(double clientLat, double clientLon) {
        Map<String, Double> agence = getCoordoneesAgence();
        return calculerDistance(
                agence.get("latitude"),
                agence.get("longitude"),
                clientLat,
                clientLon
        );
    }

    /**
     * Respecter le délai minimum entre les requêtes (1 seconde)
     */
    private void attendreDelaiMinimum() {
        long maintenant = System.currentTimeMillis();
        long tempsEcoule = maintenant - dernierAppel;

        if (tempsEcoule < DELAI_MIN_MS) {
            try {
                Thread.sleep(DELAI_MIN_MS - tempsEcoule);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        dernierAppel = System.currentTimeMillis();
    }

    /**
     * Valider des coordonnées GPS
     * @param latitude Latitude à valider
     * @param longitude Longitude à valider
     * @return true si valide
     */
    public boolean validerCoordonnees(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    /**
     * Vérifier si une adresse est en Tunisie
     * @param adresse Adresse à vérifier
     * @return true si l'adresse contient des indices tunisiens
     */
    public boolean estAdresseTunisienne(String adresse) {
        if (adresse == null) return false;

        String adresseLower = adresse.toLowerCase();

        // Villes tunisiennes principales
        String[] villesTunisiennes = {
                "tunis", "sfax", "sousse", "kairouan", "bizerte", "gabès", "ariana",
                "gafsa", "monastir", "ben arous", "kasserine", "médenine", "nabeul",
                "tataouine", "béja", "jendouba", "mahdia", "siliana", "kébili",
                "zaghouan", "manouba", "tozeur", "sidi bouzid"
        };

        for (String ville : villesTunisiennes) {
            if (adresseLower.contains(ville)) {
                return true;
            }
        }

        // Vérifier si "tunisie" ou "tunisia" est présent
        return adresseLower.contains("tunisie") || adresseLower.contains("tunisia");
    }
}