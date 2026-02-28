package tn.esprit.api.weather;

import tn.esprit.utils.Config;

public class TestWeatherAPI {

    public static void main(String[] args) {
        try {
            OpenWeatherService service = new OpenWeatherService(Config.get("openweather.apiKey"));
            WeatherInfo info = service.getWeatherByCity("Tunis");

            System.out.println("Température : " + info.getTemp() + " °C");
            System.out.println("Description : " + info.getDescription());
            System.out.println("Icon URL : " + info.getIconUrl());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}