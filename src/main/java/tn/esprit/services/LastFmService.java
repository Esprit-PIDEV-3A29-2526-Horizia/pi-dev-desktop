package tn.esprit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LastFmService {

    // 🔑 REMPLACE PAR TA VRAIE CLÉ API
    private static final String API_KEY = "b29c0609a5002b308fc82c7028eb5f35";
    private static final String BASE_URL = "https://ws.audioscrobbler.com/2.0/";

    /**
     * Récupère les informations d'un artiste
     */
    public ArtistInfo getArtistInfo(String artistName) {
        try {
            String encodedArtist = URLEncoder.encode(artistName, "UTF-8");
            String urlStr = BASE_URL + "?method=artist.getinfo&artist=" + encodedArtist
                    + "&api_key=" + API_KEY + "&format=json&lang=fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Vérifier le code de réponse
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Erreur API Last.fm: " + responseCode);
                return getDefaultArtistInfo(artistName);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(conn.getInputStream());
            JsonNode artist = json.get("artist");

            if (artist == null) {
                return getDefaultArtistInfo(artistName);
            }

            // Extraire les données
            String name = artist.get("name").asText();
            String bio = artist.has("bio") ? artist.get("bio").get("summary").asText() : "Aucune biographie disponible";
            String url_ = artist.get("url").asText();

            // Nettoyer la bio (enlever les tags HTML)
            bio = bio.replaceAll("<.*?>", "");

            // Récupérer l'image
            String imageUrl = "";
            JsonNode images = artist.get("image");
            if (images != null && images.size() > 0) {
                // Prendre la plus grande image
                for (JsonNode img : images) {
                    if (img.get("size").asText().equals("extralarge") ||
                            img.get("size").asText().equals("mega")) {
                        imageUrl = img.get("#text").asText();
                        break;
                    }
                }
            }

            // Récupérer les tags (genres)
            List<String> tags = new ArrayList<>();
            JsonNode tagsNode = artist.get("tags").get("tag");
            if (tagsNode != null && tagsNode.isArray()) {
                for (JsonNode tag : tagsNode) {
                    tags.add(tag.get("name").asText());
                }
            }

            // Récupérer le nombre d'auditeurs
            int listeners = artist.get("stats").get("listeners").asInt();

            return new ArtistInfo(name, bio, imageUrl, tags, listeners, url_);

        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultArtistInfo(artistName);
        }
    }

    /**
     * Récupère les top titres d'un artiste
     */
    public List<TrackInfo> getTopTracks(String artistName, int limit) {
        List<TrackInfo> tracks = new ArrayList<>();

        try {
            String encodedArtist = URLEncoder.encode(artistName, "UTF-8");
            String urlStr = BASE_URL + "?method=artist.gettoptracks&artist=" + encodedArtist
                    + "&api_key=" + API_KEY + "&format=json&limit=" + limit;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return tracks;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(conn.getInputStream());
            JsonNode tracksNode = json.get("toptracks").get("track");

            if (tracksNode != null && tracksNode.isArray()) {
                for (JsonNode track : tracksNode) {
                    String name = track.get("name").asText();
                    int playCount = track.get("playcount").asInt();
                    String url_ = track.get("url").asText();

                    tracks.add(new TrackInfo(name, playCount, url_));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tracks;
    }

    /**
     * Recherche des artistes par mot-clé
     */
    public List<ArtistSearchResult> searchArtist(String query) {
        List<ArtistSearchResult> results = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = BASE_URL + "?method=artist.search&artist=" + encodedQuery
                    + "&api_key=" + API_KEY + "&format=json&limit=5";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return results;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(conn.getInputStream());
            JsonNode artists = json.get("results").get("artistmatches").get("artist");

            if (artists != null && artists.isArray()) {
                for (JsonNode artist : artists) {
                    String name = artist.get("name").asText();
                    String url_ = artist.get("url").asText();
                    String imageUrl = "";

                    JsonNode images = artist.get("image");
                    if (images != null && images.size() > 0) {
                        for (JsonNode img : images) {
                            if (img.get("size").asText().equals("large")) {
                                imageUrl = img.get("#text").asText();
                                break;
                            }
                        }
                    }

                    results.add(new ArtistSearchResult(name, imageUrl, url_));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Informations par défaut si l'API échoue
     */
    private ArtistInfo getDefaultArtistInfo(String artistName) {
        return new ArtistInfo(
                artistName,
                "Informations non disponibles pour cet artiste.",
                "",
                List.of(),
                0,
                "https://www.last.fm/music/" + artistName.replace(" ", "+")
        );
    }

    // Classes internes
    public static class ArtistInfo {
        private String name;
        private String bio;
        private String imageUrl;
        private List<String> tags;
        private int listeners;
        private String url;

        public ArtistInfo(String name, String bio, String imageUrl, List<String> tags, int listeners, String url) {
            this.name = name;
            this.bio = bio;
            this.imageUrl = imageUrl;
            this.tags = tags;
            this.listeners = listeners;
            this.url = url;
        }

        public String getName() { return name; }
        public String getBio() { return bio; }
        public String getImageUrl() { return imageUrl; }
        public List<String> getTags() { return tags; }
        public int getListeners() { return listeners; }
        public String getUrl() { return url; }

        public String getFormattedListeners() {
            if (listeners >= 1_000_000) {
                return String.format("%.1fM", listeners / 1_000_000.0);
            } else if (listeners >= 1_000) {
                return String.format("%.1fK", listeners / 1000.0);
            } else {
                return String.valueOf(listeners);
            }
        }
    }

    public static class TrackInfo {
        private String name;
        private int playCount;
        private String url;

        public TrackInfo(String name, int playCount, String url) {
            this.name = name;
            this.playCount = playCount;
            this.url = url;
        }

        public String getName() { return name; }
        public int getPlayCount() { return playCount; }
        public String getUrl() { return url; }
    }

    public static class ArtistSearchResult {
        private String name;
        private String imageUrl;
        private String url;

        public ArtistSearchResult(String name, String imageUrl, String url) {
            this.name = name;
            this.imageUrl = imageUrl;
            this.url = url;
        }

        public String getName() { return name; }
        public String getImageUrl() { return imageUrl; }
        public String getUrl() { return url; }
    }
}