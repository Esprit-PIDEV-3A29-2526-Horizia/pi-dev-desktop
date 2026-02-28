package tn.esprit.api.weather;

public class WeatherInfo {
    private final double temp;
    private final String description;
    private final String icon;

    public WeatherInfo(double temp, String description, String icon) {
        this.temp = temp;
        this.description = description;
        this.icon = icon;
    }

    public double getTemp() { return temp; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }

    public String getIconUrl() {
        return "https://openweathermap.org/img/wn/" + icon + "@2x.png";
    }
}