package tn.esprit.api.exchange;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeRateService {

    private final String apiKey;

    // Cache en mémoire
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(30);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public ExchangeRateService(String apiKey) {
        this.apiKey = apiKey;
    }

    public static class RateSnapshot {
        private final String base; // "EUR"
        private final Map<String, Double> rates; // EUR->XXX
        private final Instant fetchedAt;
        private final boolean fromCache;

        public RateSnapshot(String base, Map<String, Double> rates, Instant fetchedAt, boolean fromCache) {
            this.base = base;
            this.rates = rates;
            this.fetchedAt = fetchedAt;
            this.fromCache = fromCache;
        }

        public String getBase() { return base; }
        public Map<String, Double> getRates() { return rates; }
        public Instant getFetchedAt() { return fetchedAt; }
        public boolean isFromCache() { return fromCache; }
    }

    private static class CacheEntry {
        RateSnapshot snapshot;
        CacheEntry(RateSnapshot snapshot) { this.snapshot = snapshot; }
    }

    /**
     * Récupère plusieurs rates en 1 appel: EUR -> symbols
     */
    public RateSnapshot getLatestRates(Set<String> symbols) throws Exception {
        // Normaliser
        Set<String> norm = new LinkedHashSet<>();
        for (String s : symbols) norm.add(s.toUpperCase(Locale.ROOT));

        // Clé cache unique selon symbols
        String cacheKey = "EUR->" + String.join(",", norm);

        CacheEntry cached = CACHE.get(cacheKey);
        if (cached != null) {
            Duration age = Duration.between(cached.snapshot.getFetchedAt(), Instant.now());
            if (age.compareTo(TTL) < 0) {
                return new RateSnapshot(
                        cached.snapshot.getBase(),
                        cached.snapshot.getRates(),
                        cached.snapshot.getFetchedAt(),
                        true
                );
            }
        }

        String url = "https://api.exchangeratesapi.io/v1/latest"
                + "?access_key=" + apiKey
                + "&symbols=" + String.join(",", norm);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();

        // Retry simple
        Exception last = null;
        for (int i = 0; i < 2; i++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject json = new JSONObject(response.body());

                if (!json.optBoolean("success", false)) {
                    throw new RuntimeException("API ExchangeRate error: " + response.body());
                }

                String base = json.optString("base", "EUR");
                JSONObject ratesJson = json.getJSONObject("rates");

                Map<String, Double> rates = new HashMap<>();
                for (String k : ratesJson.keySet()) {
                    rates.put(k.toUpperCase(Locale.ROOT), ratesJson.getDouble(k));
                }

                RateSnapshot snap = new RateSnapshot(base, rates, Instant.now(), false);
                CACHE.put(cacheKey, new CacheEntry(snap));
                return snap;

            } catch (Exception e) {
                last = e;
            }
        }

        // Fallback: utiliser cache même expiré si dispo
        if (cached != null) {
            return new RateSnapshot(
                    cached.snapshot.getBase(),
                    cached.snapshot.getRates(),
                    cached.snapshot.getFetchedAt(),
                    true
            );
        }

        throw last;
    }

    // ---- Méthodes pratiques ----

    public double getEurToTndRate() throws Exception {
        RateSnapshot snap = getLatestRates(Set.of("TND"));
        Double eurToTnd = snap.getRates().get("TND");
        if (eurToTnd == null) throw new IllegalStateException("TND rate missing");
        return eurToTnd;
    }

    /**
     * Retourne 1 TND -> target (EUR/USD/GBP...)
     * En utilisant EUR comme base API:
     * 1 TND = (EUR->target) / (EUR->TND)
     */
    public RateResult getTndTo(String targetCurrency) throws Exception {
        targetCurrency = targetCurrency.toUpperCase(Locale.ROOT);

        RateSnapshot snap = getLatestRates(Set.of("TND", targetCurrency));

        double eurToTnd = snap.getRates().get("TND");
        double eurToTarget = snap.getRates().get(targetCurrency);

        double tndToTarget = eurToTarget / eurToTnd;

        return new RateResult(tndToTarget, snap.getFetchedAt(), snap.isFromCache());
    }

    public static class RateResult {
        public final double rate;         // 1 TND = rate TARGET
        public final Instant lastUpdated; // quand on a fetch
        public final boolean fromCache;

        public RateResult(double rate, Instant lastUpdated, boolean fromCache) {
            this.rate = rate;
            this.lastUpdated = lastUpdated;
            this.fromCache = fromCache;
        }
    }
}