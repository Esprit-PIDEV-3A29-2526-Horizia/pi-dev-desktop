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
 * Service OCR – CIN tunisienne
 *
 * STRATÉGIE SIMPLIFIÉE :
 * ─────────────────────
 * • OCR.space Free Tier ne supporte PAS "ara" → erreur E201
 * • On utilise "eng" (Engine 2) qui lit parfaitement les chiffres
 * • Extraction : uniquement le numéro CIN (8 chiffres)
 * • Le nom, adresse, etc. sont saisis manuellement
 */
public class OCRService {

    private static final String API_URL = "https://api.ocr.space/parse/image";
    private static final String API_KEY = "K89712018688957";

    private final OkHttpClient client;

    public OCRService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    // ═══════════════════════════════════════════════════════
    // SCAN RECTO – extrait uniquement le CIN (8 chiffres)
    // ═══════════════════════════════════════════════════════

    public Map<String, String> scannerCINRecto(File imageFile) {
        Map<String, String> res = new HashMap<>();
        try {
            // Engine 2 + eng : meilleure lecture des chiffres
            System.out.println("→ [RECTO] Scan Engine 2, langue=eng...");
            String texte = appelOCRSpace(imageFile, "eng", "2");

            if (texte != null && !texte.trim().isEmpty()) {
                System.out.println("[RECTO] Texte brut :\n" + texte);
                extraireCIN(texte, res);
            }

            // Fallback Engine 1 si CIN non trouvé
            if (!res.containsKey("cin")) {
                System.out.println("→ [RECTO] Fallback Engine 1, langue=eng...");
                String texte2 = appelOCRSpace(imageFile, "eng", "1");
                if (texte2 != null && !texte2.trim().isEmpty()) {
                    System.out.println("[RECTO-E1] Texte brut :\n" + texte2);
                    extraireCIN(texte2, res);
                }
            }

            if (!res.containsKey("cin")) {
                res.put("info", "CIN non detectee – verifiez la qualite de l'image.");
                System.out.println("⚠ CIN non trouvée dans l'image.");
            } else {
                System.out.println("✓ [RECTO] CIN extraite : " + res.get("cin"));
            }

        } catch (Exception e) {
            System.err.println("✗ Erreur recto : " + e.getMessage());
            res.put("erreur", "Erreur OCR : " + e.getMessage());
        }
        return res;
    }

    // ═══════════════════════════════════════════════════════
    // SCAN VERSO – gardé pour compatibilité controllers
    // ═══════════════════════════════════════════════════════

    public Map<String, String> scannerCINVerso(File imageFile) {
        Map<String, String> res = new HashMap<>();
        try {
            System.out.println("→ [VERSO] Scan Engine 2, langue=eng...");
            String texte = appelOCRSpace(imageFile, "eng", "2");

            if (texte != null && !texte.trim().isEmpty()) {
                System.out.println("[VERSO] Texte brut :\n" + texte);
                extraireCIN(texte, res);
            }

            System.out.println("✓ [VERSO] Résultat : " + res);

        } catch (Exception e) {
            System.err.println("✗ Erreur verso : " + e.getMessage());
            res.put("erreur", "Erreur OCR verso : " + e.getMessage());
        }
        return res;
    }

    /** @deprecated Utiliser scannerCINRecto() */
    @Deprecated
    public Map<String, String> scannerCIN(File imageFile) {
        return scannerCINRecto(imageFile);
    }

    // ═══════════════════════════════════════════════════════
    // EXTRACTION CIN (8 chiffres consécutifs)
    // ═══════════════════════════════════════════════════════

    private void extraireCIN(String texte, Map<String, String> res) {
        if (res.containsKey("cin")) return; // déjà trouvé

        String[] lignes = texte.split("[\\r\\n]+");

        // Priorité 1 : ligne contenant EXACTEMENT 8 chiffres (après nettoyage)
        for (String ligne : lignes) {
            String l = ligne.trim().replaceAll("[^0-9]", "");
            if (l.length() == 8) {
                res.put("cin", l);
                System.out.println("✓ CIN (ligne exacte 8 chiffres) : " + l);
                return;
            }
        }

        // Priorité 2 : séquence de 8 chiffres (word boundary)
        Matcher m = Pattern.compile("\\b(\\d{8})\\b").matcher(texte);
        if (m.find()) {
            res.put("cin", m.group(1));
            System.out.println("✓ CIN (regex \\b) : " + m.group(1));
            return;
        }

        // Priorité 3 : n'importe quelle séquence de 8 chiffres
        Matcher m2 = Pattern.compile("(\\d{8})").matcher(texte.replaceAll("\\s", ""));
        if (m2.find()) {
            res.put("cin", m2.group(1));
            System.out.println("✓ CIN (regex simple) : " + m2.group(1));
        }
    }

    // ═══════════════════════════════════════════════════════
    // APPEL API OCR.SPACE
    // ═══════════════════════════════════════════════════════

    private String appelOCRSpace(File imageFile, String language, String ocrEngine) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apikey", API_KEY)
                .addFormDataPart("language", language)
                .addFormDataPart("isOverlayRequired", "false")
                .addFormDataPart("detectOrientation", "true")
                .addFormDataPart("scale", "true")
                .addFormDataPart("OCREngine", ocrEngine)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder().url(API_URL).post(requestBody).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Erreur API HTTP : " + response.code());

            String json = response.body().string();
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            if (obj.has("IsErroredOnProcessing") && obj.get("IsErroredOnProcessing").getAsBoolean()) {
                throw new IOException("OCR error: " +
                        (obj.has("ErrorMessage") ? obj.get("ErrorMessage").getAsString() : "inconnu"));
            }

            if (obj.has("ParsedResults") && obj.getAsJsonArray("ParsedResults").size() > 0) {
                return obj.getAsJsonArray("ParsedResults")
                        .get(0).getAsJsonObject().get("ParsedText").getAsString();
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════
    // TEST CLÉ API
    // ═══════════════════════════════════════════════════════

    public boolean testerCleAPI() {
        try {
            RequestBody rb = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("apikey", API_KEY)
                    .addFormDataPart("url", "https://via.placeholder.com/150")
                    .build();
            Request req = new Request.Builder().url(API_URL).post(rb).build();
            try (Response r = client.newCall(req).execute()) { return r.isSuccessful(); }
        } catch (Exception e) { return false; }
    }
}