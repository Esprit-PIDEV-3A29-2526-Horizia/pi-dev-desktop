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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReserverVoyageController {

    // ===== UI (DT) =====
    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblPrixUnitaire;
    @FXML private Label lblPrixTotal;

    @FXML private ImageView imgVoyage;
    @FXML private Spinner<Integer> spinnerPlaces;

    // ===== ✅ UI (EUR) =====
    @FXML private Label lblPrixUnitaireEur; // fx:id à ajouter dans FXML
    @FXML private Label lblPrixTotalEur;    // fx:id à ajouter dans FXML
    @FXML private Label lblRateInfo;        // fx:id à ajouter dans FXML

    private Voyage selectedVoyage;
    private final ReservationService rs = new ReservationService();

    // ===== ✅ Exchange API =====
    private final ExchangeRateService exchangeService =
            new ExchangeRateService(Config.get("exchange.apiKey"));

    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private double eurToTndRate = -1; // cache

    public void initData(Voyage v) {
        if (v == null) return;
        this.selectedVoyage = v;

        // --- Texte
        if (lblTitre != null) {
            lblTitre.setText("Voyage à " + safe(v.getDestination()).toUpperCase());
        }
        if (lblDescription != null) {
            lblDescription.setText(safe(v.getDescription()));
        }

        // --- Prix unit DT
        if (lblPrixUnitaire != null) {
            lblPrixUnitaire.setText(String.format(Locale.US, "%.1f DT", v.getPrix()));
        }

        // --- Image
        if (imgVoyage != null) {
            loadImage(v.getImage_url());
        }

        // --- Spinner
        int max = Math.max(0, v.getPlaces_restantes());
        int maxSafe = Math.max(1, max);

        if (spinnerPlaces != null) {
            if (max > 0) {
                spinnerPlaces.setDisable(false);
                spinnerPlaces.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxSafe, 1)
                );
            } else {
                spinnerPlaces.setDisable(true);
                spinnerPlaces.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1)
                );
            }
        }

        // --- Initial totals
        mettreAJourPrixDT(1);

        // listener spinner (DT + EUR)
        if (spinnerPlaces != null) {
            spinnerPlaces.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    mettreAJourPrixDT(newVal);
                    if (eurToTndRate > 0) mettreAJourPrixEUR(newVal);
                }
            });
        }

        // --- placeholders EUR + lancer API
        initEurPlaceholders();
        loadRateAsync();
    }

    private void mettreAJourPrixDT(int nb) {
        if (selectedVoyage == null || lblPrixTotal == null) return;
        double total = nb * selectedVoyage.getPrix();
        lblPrixTotal.setText(String.format(Locale.US, "%.0f DT", total));
    }

    private void initEurPlaceholders() {
        if (lblPrixUnitaireEur != null) lblPrixUnitaireEur.setText("-- €");
        if (lblPrixTotalEur != null) lblPrixTotalEur.setText("-- €");
        if (lblRateInfo != null) lblRateInfo.setText("Chargement taux...");
    }

    private void mettreAJourPrixEUR(int nb) {
        if (selectedVoyage == null || eurToTndRate <= 0) return;

        double unitTnd = selectedVoyage.getPrix();
        double totalTnd = nb * unitTnd;

        double unitEur = unitTnd / eurToTndRate;
        double totalEur = totalTnd / eurToTndRate;

        if (lblPrixUnitaireEur != null) {
            lblPrixUnitaireEur.setText(String.format(Locale.US, "%.2f €", unitEur));
        }
        if (lblPrixTotalEur != null) {
            lblPrixTotalEur.setText(String.format(Locale.US, "%.2f €", totalEur));
        }
    }

    private void loadRateAsync() {
        // déjà en cache
        if (eurToTndRate > 0) {
            updateRateInfoUI(eurToTndRate);
            int nb = (spinnerPlaces != null && spinnerPlaces.getValue() != null) ? spinnerPlaces.getValue() : 1;
            mettreAJourPrixEUR(nb);
            return;
        }

        exec.submit(() -> {
            try {
                double rate = exchangeService.getEurToTndRate(); // 1€ = X DT
                eurToTndRate = rate;

                Platform.runLater(() -> {
                    updateRateInfoUI(rate);
                    int nb = (spinnerPlaces != null && spinnerPlaces.getValue() != null) ? spinnerPlaces.getValue() : 1;
                    mettreAJourPrixEUR(nb);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblRateInfo != null) lblRateInfo.setText("Taux indisponible");
                    if (lblPrixUnitaireEur != null) lblPrixUnitaireEur.setText("Indispo");
                    if (lblPrixTotalEur != null) lblPrixTotalEur.setText("Indispo");
                });
            }
        });
    }

    private void updateRateInfoUI(double rate) {
        if (lblRateInfo != null) {
            lblRateInfo.setText(String.format(Locale.US, "1€ = %.3f DT", rate));
        }
    }

    private void loadImage(String url) {
        try {
            if (url != null && !url.isBlank()) {
                imgVoyage.setImage(new Image(url, true));
                return;
            }
        } catch (Exception ignored) {}

        // fallback local (si tu l'as)
        URL fallback = getClass().getResource("/images/default_trip.jpg");
        if (fallback != null) imgVoyage.setImage(new Image(fallback.toExternalForm()));
        else imgVoyage.setImage(null);
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    @FXML
    void confirmerReservation() {
        System.out.println("Clic sur Confirmer détecté !");
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

            // ⚠️ tu utilises 1 comme idUser pour le moment
            rs.effectuerReservation(selectedVoyage.getId(), 1, nbr);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Réservation réussie !");
            alert.showAndWait();
            retour();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur base de données : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CatalogueUser.fxml"));
            Parent root = loader.load();
            lblTitre.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}