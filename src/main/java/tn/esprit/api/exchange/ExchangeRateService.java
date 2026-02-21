package tn.esprit.api.exchange;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExchangeRateService {

    private final String apiKey;

    public ExchangeRateService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return taux EUR -> TND
     */
    public double getEurToTndRate() throws Exception {

        String url =
                "https://api.exchangeratesapi.io/v1/latest" +
                        "?access_key=" + apiKey +
                        "&symbols=TND";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());

        if (!json.getBoolean("success")) {
            throw new RuntimeException("API ExchangeRate error: " + response.body());
        }

        return json.getJSONObject("rates").getDouble("TND");
    }
}