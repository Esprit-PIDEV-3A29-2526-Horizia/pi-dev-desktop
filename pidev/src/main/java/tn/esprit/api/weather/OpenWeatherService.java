package tn.esprit.api.weather;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenWeatherService {

    private static final String BASE_URL =
            "https://api.openweathermap.org/data/2.5/weather";

    private final HttpClient http = HttpClient.newHttpClient();
    private final String apiKey;

    public OpenWeatherService(String apiKey) {
        this.apiKey = apiKey;
    }

    public WeatherInfo getWeatherByCity(String city) throws Exception {
        String url = BASE_URL
                + "?q=" + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&appid=" + apiKey
                + "&units=metric"
                + "&lang=fr";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new RuntimeException("OpenWeather error: " + res.statusCode() + " -> " + res.body());
        }

        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();

        double temp = json.getAsJsonObject("main").get("temp").getAsDouble();

        JsonArray weatherArr = json.getAsJsonArray("weather");
        JsonObject w0 = weatherArr.get(0).getAsJsonObject();
        String desc = w0.get("description").getAsString();
        String icon = w0.get("icon").getAsString();

        return new WeatherInfo(temp, desc, icon);
    }
}