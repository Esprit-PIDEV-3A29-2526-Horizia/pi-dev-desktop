package tn.esprit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class WeatherService {

    // 🔑 REMPLACE PAR TA VRAIE CLÉ API
    private static final String API_KEY = "6458f579de45aad125899cabb9e97552";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    /**
     * Récupère la météo pour une ville
     */
    public WeatherInfo getWeatherForCity(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, "UTF-8");
            String urlStr = BASE_URL + "?q=" + encodedCity + "&appid=" + API_KEY + "&units=metric&lang=fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Vérifier le code de réponse
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Erreur API: " + responseCode);
                return getDefaultWeather();
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(conn.getInputStream());

            // Extraire les données
            String description = json.get("weather").get(0).get("description").asText();
            double temperature = json.get("main").get("temp").asDouble();
            double feelsLike = json.get("main").get("feels_like").asDouble();
            int humidity = json.get("main").get("humidity").asInt();
            double windSpeed = json.get("wind").get("speed").asDouble();
            String icon = json.get("weather").get(0).get("icon").asText();

            // Heure du coucher/lever du soleil
            long sunrise = json.get("sys").get("sunrise").asLong();
            long sunset = json.get("sys").get("sunset").asLong();

            LocalDateTime sunriseTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(sunrise), ZoneId.systemDefault());
            LocalDateTime sunsetTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(sunset), ZoneId.systemDefault());

            return new WeatherInfo(
                    description, temperature, feelsLike, humidity,
                    windSpeed, icon, sunriseTime, sunsetTime
            );

        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultWeather();
        }
    }

    /**
     * Météo par défaut si l'API échoue
     */
    private WeatherInfo getDefaultWeather() {
        return new WeatherInfo(
                "Informations non disponibles",
                20.0, 20.0, 50, 5.0, "01d",
                LocalDateTime.now(), LocalDateTime.now().plusHours(12)
        );
    }

    /**
     * Classe interne pour stocker les infos météo
     */
    public static class WeatherInfo {
        private String description;
        private double temperature;
        private double feelsLike;
        private int humidity;
        private double windSpeed;
        private String iconCode;
        private LocalDateTime sunrise;
        private LocalDateTime sunset;

        public WeatherInfo(String description, double temperature, double feelsLike,
                           int humidity, double windSpeed, String iconCode,
                           LocalDateTime sunrise, LocalDateTime sunset) {
            this.description = description;
            this.temperature = temperature;
            this.feelsLike = feelsLike;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.iconCode = iconCode;
            this.sunrise = sunrise;
            this.sunset = sunset;
        }

        public String getDescription() { return description; }
        public double getTemperature() { return temperature; }
        public double getFeelsLike() { return feelsLike; }
        public int getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public String getIconCode() { return iconCode; }
        public LocalDateTime getSunrise() { return sunrise; }
        public LocalDateTime getSunset() { return sunset; }

        public String getFormattedTemp() {
            return String.format("%.0f°C", temperature);
        }

        public String getIconUrl() {
            return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        }

        @Override
        public String toString() {
            return String.format("%s, %.0f°C (ressenti %.0f°C) | 💧%d%% | 💨%.0f km/h",
                    description, temperature, feelsLike, humidity, windSpeed * 3.6);
        }
    }
}