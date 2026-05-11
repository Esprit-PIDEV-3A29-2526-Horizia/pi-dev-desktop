package tn.esprit.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Random;

public class ApiClient {

    // Mode gratuit : true = utilise des descriptions aléatoires locales, false = utilise OpenAI (payant)
    private static final boolean MODE_GRATUIT = true;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MYMEMORY_URL = "https://api.mymemory.translated.net/get";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static String openAiApiKey;

    // Banque de descriptions pour le mode gratuit
    private static final String[] DESCRIPTIONS = {
            "Un endroit paradisiaque aux eaux cristallines, idéal pour se détendre en famille. Les couchers de soleil y sont inoubliables.",
            "Une aventure inoubliable au cœur de paysages à couper le souffle. Parfait pour les amateurs de randonnée et de nature sauvage.",
            "Découvrez ce lieu chargé d'histoire, avec ses ruelles pittoresques et sa gastronomie locale. Une expérience culturelle unique.",
            "Un havre de paix où le temps s'arrête. Laissez-vous bercer par le chant des oiseaux et la brise légère.",
            "Pour les amateurs de sensations fortes, ce spot offre des activités variées : plongée, escalade, et bien plus.",
            "Un coin secret préservé du tourisme de masse. Authenticité et tranquillité garanties.",
            "Idéal pour une escapade romantique. Les levers de soleil y sont magiques."
    };
    private static final Random RANDOM = new Random();

    static {
        if (!MODE_GRATUIT) {
            // Charger la clé API OpenAI seulement si on utilise le mode payant
            try (InputStream input = ApiClient.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new RuntimeException("Fichier config.properties introuvable dans resources");
                }
                Properties prop = new Properties();
                prop.load(input);
                openAiApiKey = prop.getProperty("openai.api.key");
                if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
                    throw new RuntimeException("Clé API OpenAI manquante dans config.properties");
                }
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors du chargement de config.properties", e);
            }
        }
    }

    /**
     * Génère une description pour une publication
     * @param titre le titre de la publication
     * @param categorie la catégorie
     * @return une description (aléatoire en mode gratuit, ou via OpenAI en mode payant)
     */
    public static String genererDescriptionIA(String titre, String categorie) throws IOException {
        if (MODE_GRATUIT) {
            int index = RANDOM.nextInt(DESCRIPTIONS.length);
            return "🏝️ " + titre + " (" + categorie + ") : " + DESCRIPTIONS[index];
        } else {
            return genererDescriptionAvecOpenAI(titre, categorie);
        }
    }

    // Méthode privée pour l'appel à OpenAI (non utilisé en mode gratuit)
    private static String genererDescriptionAvecOpenAI(String titre, String categorie) throws IOException {
        // Cette méthode n'est pas appelée en mode gratuit, mais on la garde pour compatibilité
        throw new IOException("Mode OpenAI désactivé (MODE_GRATUIT=true)");
    }

    /**
     * Traduit un texte via l'API MyMemory (gratuite)
     * @param texte le texte à traduire
     * @param source code de la langue source (ex: "fr")
     * @param cible code de la langue cible (ex: "en")
     * @return le texte traduit
     */
    public static String traduire(String texte, String source, String cible) throws IOException {
        try {
            URIBuilder builder = new URIBuilder(MYMEMORY_URL)
                    .addParameter("q", texte)
                    .addParameter("langpair", source + "|" + cible);
            HttpGet request = new HttpGet(builder.build());

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                var response = client.execute(request);
                String json = EntityUtils.toString(response.getEntity());
                JsonNode root = mapper.readTree(json);

                if (root.has("responseData")) {
                    String translated = root.path("responseData").path("translatedText").asText();
                    if (translated == null || translated.isEmpty() || translated.equals(texte)) {
                        return texte; // pas de traduction ou identique
                    }
                    return translated;
                } else {
                    return texte; // fallback
                }
            }
        } catch (URISyntaxException e) {
            throw new IOException("Erreur de construction de l'URI", e);
        }
    }
}