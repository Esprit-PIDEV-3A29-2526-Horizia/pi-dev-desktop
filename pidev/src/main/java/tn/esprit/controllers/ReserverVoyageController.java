package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.api.exchange.ExchangeRateService;
import tn.esprit.entites.Voyage;
import tn.esprit.services.ReservationService;
import tn.esprit.utils.Config;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReserverVoyageController {

    // --- FXML (TON FXML ACTUEL) ---
    @FXML private ImageView imgVoyage;          // ✅ dans ton FXML
    @FXML private Label lblTitre;              // ✅ destination affichée ici (TOZEUR)
    @FXML private Label lblDescription;

    @FXML private Spinner<Integer> spinnerPlaces;

    @FXML private Label lblPrixUnitaire;       // DT
    @FXML private Label lblPrixTotal;          // DT
    @FXML private ChoiceBox<String> cbCurrency;
    @FXML private Label lblPrixUnitaireFx;
    @FXML private Label lblPrixTotalFx;
    @FXML private Label lblRateInfo;

    @FXML private Label lblAIStatus;

    private Voyage selectedVoyage;
    private final ReservationService rs = new ReservationService();

    private final ExchangeRateService exchangeService =
            new ExchangeRateService(Config.get("exchange.apiKey"));

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private double tndToCurrencyRate = -1; // 1 TND -> currency
    private String currentCurrency = "EUR";
    private ZonedDateTime lastUpdated = null;

    private final int CURRENT_USER_ID = 1;

    // =========================
    // INIT
    // =========================
    public void initData(Voyage v) {
        if (v == null) return;
        this.selectedVoyage = v;

        // ✅ destination dans lblTitre (car ton FXML n'a pas lblDestination)
        if (lblTitre != null) {
            lblTitre.setText(safe(v.getDestination()).toUpperCase());
        }

        if (lblDescription != null) {
            lblDescription.setText(safe(v.getDescription()));
        }

        // ✅ image
        if (imgVoyage != null) {
            loadImageSmart(v.getImage_url());
        }

        // Prix unitaire DT
        if (lblPrixUnitaire != null) {
            lblPrixUnitaire.setText(String.format(Locale.US, "%.1f DT", v.getPrix()));
        }

        // Spinner init
        int max = Math.max(1, v.getPlaces_restantes());
        if (spinnerPlaces != null) {
            spinnerPlaces.setDisable(max <= 0);
            spinnerPlaces.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1));
        }

        // Multi devises
        initCurrencyUI();

        // Totaux init
        int nb = (spinnerPlaces != null && spinnerPlaces.getValue() != null) ? spinnerPlaces.getValue() : 1;
        mettreAJourPrixDT(nb);

        // Listener spinner
        if (spinnerPlaces != null) {
            spinnerPlaces.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    mettreAJourPrixDT(newVal);
                    mettreAJourPrixFX(newVal);
                }
            });
        }

        // placeholders + load rate
        initFxPlaceholders();
        loadRateAsync(currentCurrency);
    }

    // =========================
    // CURRENCY
    // =========================
    private void initCurrencyUI() {
        if (cbCurrency == null) return;

        cbCurrency.getItems().setAll("EUR", "USD", "GBP");
        cbCurrency.setValue("EUR");
        currentCurrency = "EUR";

        cbCurrency.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            currentCurrency = newV.toUpperCase(Locale.ROOT);

            initFxPlaceholders();
            loadRateAsync(currentCurrency);

            int nb = (spinnerPlaces != null && spinnerPlaces.getValue() != null) ? spinnerPlaces.getValue() : 1;
            mettreAJourPrixDT(nb);
        });
    }

    // =========================
    // PRICES
    // =========================
    private void mettreAJourPrixDT(int nb) {
        if (selectedVoyage == null) return;
        double total = nb * selectedVoyage.getPrix();

        if (lblPrixTotal != null) {
            lblPrixTotal.setText(String.format(Locale.US, "%.0f DT", total));
        }
    }

    private void initFxPlaceholders() {
        if (lblPrixUnitaireFx != null) lblPrixUnitaireFx.setText("--");
        if (lblPrixTotalFx != null) lblPrixTotalFx.setText("--");
        if (lblRateInfo != null) lblRateInfo.setText("Chargement taux...");
        tndToCurrencyRate = -1;
        lastUpdated = null;
    }

    private void mettreAJourPrixFX(int nb) {
        if (selectedVoyage == null) return;
        if (tndToCurrencyRate <= 0) return;

        double unitTnd = selectedVoyage.getPrix();
        double totalTnd = nb * unitTnd;

        double unitFx = unitTnd * tndToCurrencyRate;
        double totalFx = totalTnd * tndToCurrencyRate;

        if (lblPrixUnitaireFx != null) {
            lblPrixUnitaireFx.setText(String.format(Locale.US, "%.2f %s", unitFx, currentCurrency));
        }
        if (lblPrixTotalFx != null) {
            lblPrixTotalFx.setText(String.format(Locale.US, "%.2f %s", totalFx, currentCurrency));
        }
    }

    private void loadRateAsync(String currency) {
        if (currency == null || currency.isBlank()) currency = "EUR";
        final String wanted = currency.toUpperCase(Locale.ROOT);

        exec.submit(() -> {
            try {
                ExchangeRateService.RateResult rr = exchangeService.getTndTo(wanted);

                tndToCurrencyRate = rr.rate;
                lastUpdated = ZonedDateTime.ofInstant(rr.lastUpdated, ZoneId.systemDefault());

                Platform.runLater(() -> {
                    updateRateInfoUI(wanted, rr.rate, lastUpdated, rr.fromCache);

                    int nb = (spinnerPlaces != null && spinnerPlaces.getValue() != null) ? spinnerPlaces.getValue() : 1;
                    mettreAJourPrixFX(nb);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblRateInfo != null) lblRateInfo.setText("Taux indisponible");
                    if (lblPrixUnitaireFx != null) lblPrixUnitaireFx.setText("Indispo");
                    if (lblPrixTotalFx != null) lblPrixTotalFx.setText("Indispo");
                });
            }
        });
    }

    private void updateRateInfoUI(String currency, double tndToCurrency, ZonedDateTime updatedAt, boolean fromCache) {
        if (lblRateInfo == null) return;

        String dateStr = (updatedAt != null) ? updatedAt.toLocalDateTime().toString() : "--";
        lblRateInfo.setText(String.format(
                Locale.US,
                "1 TND = %.4f %s | Maj: %s%s",
                tndToCurrency,
                currency,
                dateStr,
                fromCache ? " (cache)" : ""
        ));
    }

    // =========================
    // IMAGE (SMART)
    // =========================
    private void loadImageSmart(String path) {
        try {
            if (path != null && !path.isBlank()) {
                String p = path.trim();

                // http(s)
                if (p.startsWith("http://") || p.startsWith("https://")) {
                    imgVoyage.setImage(new Image(p, true));
                    return;
                }

                // file:C:/... -> file:/C:/...
                if (p.startsWith("file:C:/") || p.startsWith("file:D:/") || p.startsWith("file:E:/")) {
                    p = "file:/" + p.substring("file:".length());
                    imgVoyage.setImage(new Image(p, true));
                    return;
                }

                // C:\... -> file:/C:/...
                if (p.matches("^[A-Za-z]:\\\\.*")) {
                    p = "file:/" + p.replace("\\", "/");
                    imgVoyage.setImage(new Image(p, true));
                    return;
                }

                // file:/...
                if (p.startsWith("file:/")) {
                    imgVoyage.setImage(new Image(p, true));
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur image: " + e.getMessage());
        }

        URL fallback = getClass().getResource("/images/default_trip.jpg");
        if (fallback != null) imgVoyage.setImage(new Image(fallback.toExternalForm()));
        else imgVoyage.setImage(null);
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    // =========================
    // ACTIONS
    // =========================
    @FXML
    void confirmerReservation() {
        try {
            if (selectedVoyage == null) {
                new Alert(Alert.AlertType.ERROR, "Aucun voyage sélectionné.").showAndWait();
                return;
            }
            if (spinnerPlaces == null || spinnerPlaces.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Nombre de personnes invalide.").showAndWait();
                return;
            }

            int nbr = spinnerPlaces.getValue();

            rs.effectuerReservation(selectedVoyage.getId(), CURRENT_USER_ID, nbr);

            new Alert(Alert.AlertType.INFORMATION, "Réservation envoyée (EN_ATTENTE).").showAndWait();
            retour();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void retour() {
        try {
            exec.shutdownNow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CatalogueUser.fxml"));
            Parent root = loader.load();

            if (lblTitre != null && lblTitre.getScene() != null) {
                lblTitre.getScene().setRoot(root);
            } else if (imgVoyage != null && imgVoyage.getScene() != null) {
                imgVoyage.getScene().setRoot(root);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}