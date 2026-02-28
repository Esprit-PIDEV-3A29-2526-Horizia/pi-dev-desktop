package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.api.weather.OpenWeatherService;
import tn.esprit.api.weather.WeatherInfo;
import tn.esprit.entites.Voyage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class VoyageCardUserController {

    @FXML private VBox cardContainer;
    @FXML private ImageView imgVoyage;
    @FXML private Label lblDestination, lblPrix, lblDates, lblPlacesInfo, badgeStatus;
    @FXML private Button btnReserver;
    @FXML private StackPane imageZone;

    private Voyage voyage;
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
    private OpenWeatherService meteoService;
    private ConcurrentHashMap<String, WeatherInfo> cacheMeteo;
    private ExecutorService executor;


    @FXML
    public void initialize() {
        if (imgVoyage != null) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
            clip.setArcWidth(18);
            clip.setArcHeight(18);
            clip.widthProperty().bind(imgVoyage.fitWidthProperty());
            clip.heightProperty().bind(imgVoyage.fitHeightProperty());
            imgVoyage.setClip(clip);
        }
    }
    public void setData(Voyage v) {
        this.voyage = v;
        if (v == null) return;
        if (lblDestination != null) lblDestination.setText(safe(v.getDestination()));
        if (lblPrix != null) lblPrix.setText(nf.format(v.getPrix()) + " DT");
        LocalDate d1 = toLocalDate(v.getDate_depart());
        LocalDate d2 = toLocalDate(v.getDate_retour());
        if (lblDates != null) {
            lblDates.setText("Du " + (d1 != null ? df.format(d1) : "--/--/----")
                    + " au " + (d2 != null ? df.format(d2) : "--/--/----"));
        }
        int restantes = v.getPlaces_restantes();
        if (lblPlacesInfo != null) lblPlacesInfo.setText("Places restantes : " + restantes);
        loadImage(v.getImage_url());
        ajouterChipMeteoSurImage(v.getDestination());
        applyStyles(restantes);
        setupHoverEffects();
    }
    private void ajouterChipMeteoSurImage(String destination) {
        if (imageZone == null) return;
        if (meteoService == null || cacheMeteo == null || executor == null) return; // sécurité
        // éviter de dupliquer à chaque refresh
        imageZone.getChildren().removeIf(n -> "meteo-chip".equals(n.getId()));
        HBox chip = creerChipMeteo(destination);
        chip.setId("meteo-chip");
        StackPane.setAlignment(chip, Pos.TOP_RIGHT);
        StackPane.setMargin(chip, new Insets(8, 8, 0, 0));
        imageZone.getChildren().add(chip);
    }

    private HBox creerChipMeteo(String destination) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 8, 4, 8));
        chip.setStyle("-fx-background-color: rgba(15,23,42,0.65); -fx-background-radius: 14;");
        ImageView icon = new ImageView();
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        icon.setPreserveRatio(true);
        Label lbl = new Label("Météo...");
        lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10;");
        chip.getChildren().addAll(icon, lbl);
        String key = (destination == null) ? "" : destination.trim().toLowerCase();

        // Cache
        WeatherInfo cached = cacheMeteo.get(key);
        if (cached != null) {
            lbl.setText(Math.round(cached.getTemp()) + "°C • " + cached.getDescription());
            icon.setImage(new Image(cached.getIconUrl(), true));
            return chip;
        }
        // Appel API en background
        executor.submit(() -> {
            try {
                WeatherInfo w = meteoService.getWeatherByCity(destination);
                cacheMeteo.put(key, w);
                Platform.runLater(() -> {
                    lbl.setText(Math.round(w.getTemp()) + "°C • " + w.getDescription());
                    icon.setImage(new Image(w.getIconUrl(), true));
                });
            } catch (Exception e) {
                Platform.runLater(() -> lbl.setText("Météo indisponible"));
            }
        });
        return chip;
    }
    private void loadImage(String url) {
        if (imgVoyage == null) return;
        try {
            if (url != null && !url.isBlank()) {
                imgVoyage.setImage(new Image(url, true));
                return;
            }
        } catch (Exception ignored) {}
        URL fallback = getClass().getResource("/images/default.png");
        if (fallback != null) imgVoyage.setImage(new Image(fallback.toExternalForm()));
    }

    private void applyStyles(int restantes) {
        if (restantes <= 0) {
            if (badgeStatus != null) {
                badgeStatus.setText("COMPLET");
                badgeStatus.setStyle("-fx-background-color:#FEE2E2; -fx-text-fill:#EF4444; -fx-background-radius:14; -fx-padding:3 10; -fx-font-weight:bold; -fx-font-size:11;");
            }
            if (btnReserver != null) {
                btnReserver.setText("Plein");
                btnReserver.setDisable(true);
                btnReserver.setStyle("-fx-background-color:#E2E8F0; -fx-text-fill:#94A3B8; -fx-background-radius:12; -fx-font-weight:bold;");
            }
        } else {
            if (badgeStatus != null) {
                badgeStatus.setText("DISPONIBLE");
                badgeStatus.setStyle("-fx-background-color:#DCFCE7; -fx-text-fill:#059669; -fx-background-radius:14; -fx-padding:3 10; -fx-font-weight:bold; -fx-font-size:11;");
            }
            if (btnReserver != null) {
                btnReserver.setText("Réserver");
                btnReserver.setDisable(false);
                btnReserver.setStyle("-fx-background-color:#E8B156; -fx-text-fill:white; -fx-background-radius:12; -fx-font-weight:bold; -fx-cursor: hand;");
            }
        }
    }

    private void setupHoverEffects() {
        if (cardContainer == null) return;
        String normal =
                "-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 14, 0, 0, 6);";
        String hover =
                "-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 18, 0, 0, 10);" +
                        "-fx-translate-y: -3; -fx-cursor: hand;";
        cardContainer.setStyle(normal);
        cardContainer.setOnMouseEntered(e -> cardContainer.setStyle(hover));
        cardContainer.setOnMouseExited(e -> cardContainer.setStyle(normal));
    }

    @FXML
    private void handleReserver() {
        if (voyage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReserverVoyage.fxml"));
            Parent root = loader.load();
            ReserverVoyageController ctrl = loader.getController();
            if (ctrl != null) ctrl.initData(voyage);
            Scene scene = btnReserver.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //garder Details
    @FXML
    private void handleDetails() {
        if (voyage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyageUser.fxml"));
            Parent root = loader.load();
            DetailsVoyageUserController ctrl = loader.getController();
            if (ctrl != null) ctrl.initData(voyage);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + safe(voyage.getDestination()));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LocalDate toLocalDate(Date d) {
        return (d == null) ? null : d.toLocalDate();
    }
    private String safe(String s) { return (s == null) ? "" : s; }

    public void setMeteo(OpenWeatherService meteoService,
                         ConcurrentHashMap<String, WeatherInfo> cacheMeteo,
                         ExecutorService executor) {
        this.meteoService = meteoService;
        this.cacheMeteo = cacheMeteo;
        this.executor = executor;
    }
}