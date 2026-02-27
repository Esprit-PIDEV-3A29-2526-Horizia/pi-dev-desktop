package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service de reconnaissance optique de caractères (OCR) pour scanner les CIN tunisiennes
 * API utilisée : OCR.space (25,000 requêtes/mois gratuites)
 */
public class OCRService {

    // ═══════════════════════════════════════════════════════
    // CONFIGURATION API
    // ═══════════════════════════════════════════════════════

    private static final String API_URL = "https://api.ocr.space/parse/image";

    // ⚠️ IMPORTANT : Remplacez cette clé par votre propre clé API OCR.space
    // Inscription gratuite sur : https://ocr.space/ocrapi
    private static final String API_KEY = "K89712018688957";  // Clé d'exemple - À REMPLACER !

    private final OkHttpClient client;

    public OCRService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    /**
     * Scanner une image de CIN et extraire les informations
     * @param imageFile Fichier image de la CIN (JPG, PNG, etc.)
     * @return Map contenant les données extraites (nom, cin, adresse)
     */
    public Map<String, String> scannerCIN(File imageFile) {
        Map<String, String> resultats = new HashMap<>();

        try {
            System.out.println("→ Scan de la CIN en cours...");

            // Appel API OCR.space
            String texteExtrait = appelOCRSpace(imageFile);

            if (texteExtrait == null || texteExtrait.trim().isEmpty()) {
                System.err.println("✗ Aucun texte extrait de l'image");
                resultats.put("erreur", "Impossible de lire l'image. Assurez-vous qu'elle est claire.");
                return resultats;
            }

            System.out.println("✓ Texte extrait avec succès");
            System.out.println("Texte brut : " + texteExtrait);

            // Parser les informations de la CIN tunisienne
            parserCINTunisienne(texteExtrait, resultats);

            if (resultats.isEmpty()) {
                resultats.put("texte_brut", texteExtrait);
                resultats.put("info", "Données non reconnues automatiquement. Veuillez saisir manuellement.");
            }

        } catch (Exception e) {
            System.err.println("✗ Erreur lors du scan OCR : " + e.getMessage());
            e.printStackTrace();
            resultats.put("erreur", "Erreur technique : " + e.getMessage());
        }

        return resultats;
    }

    /**
     * Appel à l'API OCR.space
     */
    private String appelOCRSpace(File imageFile) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apikey", API_KEY)
                .addFormDataPart("language", "fre")  // Français
                .addFormDataPart("isOverlayRequired", "false")
                .addFormDataPart("detectOrientation", "true")
                .addFormDataPart("scale", "true")
                .addFormDataPart("OCREngine", "2")  // Engine 2 = meilleur pour le français
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API : " + response.code());
            }

            String jsonResponse = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Vérifier si l'OCR a réussi
            if (jsonObject.has("IsErroredOnProcessing") &&
                    jsonObject.get("IsErroredOnProcessing").getAsBoolean()) {
                String errorMessage = jsonObject.has("ErrorMessage") ?
                        jsonObject.get("ErrorMessage").getAsString() : "Erreur inconnue";
                throw new IOException("Erreur OCR : " + errorMessage);
            }

            // Extraire le texte reconnu
            if (jsonObject.has("ParsedResults") &&
                    jsonObject.getAsJsonArray("ParsedResults").size() > 0) {
                return jsonObject.getAsJsonArray("ParsedResults")
                        .get(0).getAsJsonObject()
                        .get("ParsedText").getAsString();
            }
        }

        return null;
    }

    /**
     * Parser les informations d'une CIN tunisienne
     * Format CIN tunisienne : 8 chiffres
     */
    private void parserCINTunisienne(String texte, Map<String, String> resultats) {
        // Nettoyer le texte
        String texteNettoye = texte.replaceAll("\\r\\n", " ").replaceAll("\\n", " ");

        // 1. Extraire le numéro CIN (8 chiffres)
        Pattern patternCIN = Pattern.compile("\\b(\\d{8})\\b");
        Matcher matcherCIN = patternCIN.matcher(texteNettoye);
        if (matcherCIN.find()) {
            resultats.put("cin", matcherCIN.group(1));
            System.out.println("✓ CIN trouvé : " + matcherCIN.group(1));
        }

        // 2. Extraire le nom complet
        // Généralement après "Nom" ou avant la date de naissance
        Pattern patternNom = Pattern.compile("(?:Nom[:\\s]+)?([A-ZÀÂÄÇÈÉÊËÎÏÔÙÛÜ][a-zàâäçèéêëîïôùûü]+(?:[\\s-][A-ZÀÂÄÇÈÉÊËÎÏÔÙÛÜ][a-zàâäçèéêëîïôùûü]+)+)", Pattern.CASE_INSENSITIVE);
        Matcher matcherNom = patternNom.matcher(texteNettoye);
        if (matcherNom.find()) {
            String nomComplet = matcherNom.group(1).trim();
            resultats.put("nom_complet", nomComplet);
            System.out.println("✓ Nom trouvé : " + nomComplet);
        }

        // 3. Extraire l'adresse (généralement après "Adresse" ou "Domicile")
        Pattern patternAdresse = Pattern.compile("(?:Adresse|Domicile)[:\\s]+([^\\d]+(?:\\d+[^\\d]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcherAdresse = patternAdresse.matcher(texteNettoye);
        if (matcherAdresse.find()) {
            String adresse = matcherAdresse.group(1).trim();
            // Nettoyer l'adresse
            adresse = adresse.replaceAll("\\s{2,}", " ").trim();
            resultats.put("adresse", adresse);
            System.out.println("✓ Adresse trouvée : " + adresse);
        }

        // 4. Extraire la ville (mots après l'adresse ou codes postaux tunisiens)
        if (resultats.containsKey("adresse")) {
            String adresse = resultats.get("adresse");
            // Villes tunisiennes communes
            String[] villesTunisiennes = {"Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte",
                    "Gabès", "Ariana", "Gafsa", "Monastir", "Ben Arous", "Kasserine",
                    "Médenine", "Nabeul", "Tataouine", "Béja", "Jendouba", "Mahdia",
                    "Siliana", "Kébili", "Zaghouan", "Manouba", "Tozeur", "Sidi Bouzid"};

            for (String ville : villesTunisiennes) {
                if (adresse.toUpperCase().contains(ville.toUpperCase())) {
                    resultats.put("ville", ville);
                    System.out.println("✓ Ville trouvée : " + ville);
                    break;
                }
            }
        }

        // 5. Extraire le code postal (4 chiffres pour la Tunisie)
        Pattern patternCodePostal = Pattern.compile("\\b(\\d{4})\\b");
        Matcher matcherCP = patternCodePostal.matcher(texteNettoye);
        if (matcherCP.find()) {
            String codePostal = matcherCP.group(1);
            // Vérifier que ce n'est pas une date ou la CIN
            if (!resultats.containsKey("cin") || !resultats.get("cin").contains(codePostal)) {
                resultats.put("code_postal", codePostal);
                System.out.println("✓ Code postal trouvé : " + codePostal);
            }
        }
    }

    /**
     * Test de la clé API
     */
    public boolean testerCleAPI() {
        try {
            // Créer un fichier de test simple
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("apikey", API_KEY)
                    .addFormDataPart("url", "https://via.placeholder.com/150")
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            System.err.println("Erreur test API : " + e.getMessage());
            return false;
        }
    }
}