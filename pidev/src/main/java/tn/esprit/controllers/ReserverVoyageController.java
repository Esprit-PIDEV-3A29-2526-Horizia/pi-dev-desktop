package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.api.exchange.ExchangeRateService;
import tn.esprit.entites.Voyage;
import tn.esprit.services.ReservationService;
import tn.esprit.services.VoyageService;
import tn.esprit.utils.Config;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;
import tn.esprit.entities.User;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReserverVoyageController {

    // --- Navbar partagée ---
    @FXML private NavbarController navbarController;

    // --- FXML ---
    @FXML private ImageView imgVoyage;
    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Spinner<Integer> spinnerPlaces;
    @FXML private Spinner<Integer> spinnerAdultes;
    @FXML private Spinner<Integer> spinnerEnfants;
    @FXML private Label lblPrixUnitaire;
    @FXML private Label lblPrixTotal;
    @FXML private ChoiceBox<String> cbCurrency;
    @FXML private Label lblPrixUnitaireFx;
    @FXML private Label lblPrixTotalFx;
    @FXML private Label lblRateInfo;
    @FXML private Label lblAIStatus;
    @FXML private Label errorPersonnes;
    @FXML private Label errorRepartition;

    private Voyage selectedVoyage;
    private final ReservationService rs = new ReservationService();
    private final VoyageService vs = new VoyageService();
    private User currentUser;

    private final ExchangeRateService exchangeService =
            new ExchangeRateService(Config.get("exchange.apiKey"));

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private double tndToCurrencyRate = -1;
    private String currentCurrency = "EUR";
    private ZonedDateTime lastUpdated = null;

    // =========================
    // INIT
    // =========================
    public void initData(Voyage v) {
        if (v == null) return;
        this.selectedVoyage = v;
        this.currentUser = SessionManager.getCurrentUser();

        // Mettre à jour la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
        }

        if (lblTitre != null) {
            lblTitre.setText(safe(v.getDestination()).toUpperCase());
        }

        if (lblDescription != null) {
            lblDescription.setText(safe(v.getDescription()));
        }

        if (imgVoyage != null) {
            loadImageSmart(v.getImage_url());
        }

        if (lblPrixUnitaire != null) {
            lblPrixUnitaire.setText(String.format(Locale.US, "%.1f DT", v.getPrix()));
        }

        // Configurer les spinners
        int maxPlaces = Math.max(1, v.getPlaces_restantes());

        if (spinnerPlaces != null) {
            spinnerPlaces.setDisable(maxPlaces <= 0);
            spinnerPlaces.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxPlaces, 1));
        }

        if (spinnerAdultes != null) {
            spinnerAdultes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPlaces, 1));
        }

        if (spinnerEnfants != null) {
            spinnerEnfants.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxPlaces, 0));
        }

        initCurrencyUI();

        int nb = (spinnerPlaces != null && spinnerPlaces.getValue() != null) ? spinnerPlaces.getValue() : 1;
        mettreAJourPrixDT(nb);

        // Listeners
        if (spinnerPlaces != null) {
            spinnerPlaces.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    ajusterRepartition(newVal);
                    mettreAJourPrixDT(newVal);
                    mettreAJourPrixFX(newVal);
                    validerSaisie();
                }
            });
        }

        if (spinnerAdultes != null) {
            spinnerAdultes.valueProperty().addListener((obs, oldVal, newVal) -> {
                verifierCoherence();
                calculerTotal();
                validerSaisie();
            });
        }

        if (spinnerEnfants != null) {
            spinnerEnfants.valueProperty().addListener((obs, oldVal, newVal) -> {
                verifierCoherence();
                calculerTotal();
                validerSaisie();
            });
        }

        initFxPlaceholders();
        loadRateAsync(currentCurrency);
    }

    private void ajusterRepartition(int totalPersonnes) {
        int adultesActuels = spinnerAdultes != null ? spinnerAdultes.getValue() : 1;
        int enfantsActuels = spinnerEnfants != null ? spinnerEnfants.getValue() : 0;
        int sommeActuelle = adultesActuels + enfantsActuels;

        if (sommeActuelle > totalPersonnes) {
            int difference = sommeActuelle - totalPersonnes;
            int nouveauxEnfants = Math.max(0, enfantsActuels - difference);
            if (spinnerEnfants != null) {
                spinnerEnfants.getValueFactory().setValue(nouveauxEnfants);
            }

            int nouvelleSomme = adultesActuels + nouveauxEnfants;
            if (nouvelleSomme > totalPersonnes) {
                int nouveauxAdultes = Math.max(1, adultesActuels - (nouvelleSomme - totalPersonnes));
                if (spinnerAdultes != null) {
                    spinnerAdultes.getValueFactory().setValue(nouveauxAdultes);
                }
            }
        } else if (sommeActuelle < totalPersonnes && totalPersonnes > 0) {
            int difference = totalPersonnes - sommeActuelle;
            if (spinnerAdultes != null) {
                spinnerAdultes.getValueFactory().setValue(adultesActuels + difference);
            }
        }
    }

    private void verifierCoherence() {
        int adultes = spinnerAdultes != null ? spinnerAdultes.getValue() : 1;
        int enfants = spinnerEnfants != null ? spinnerEnfants.getValue() : 0;
        int total = adultes + enfants;
        int maxPlaces = selectedVoyage != null ? selectedVoyage.getPlaces_restantes() : 0;

        if (total > maxPlaces) {
            int difference = total - maxPlaces;
            if (enfants >= difference) {
                if (spinnerEnfants != null) {
                    spinnerEnfants.getValueFactory().setValue(enfants - difference);
                }
            } else {
                if (spinnerEnfants != null) {
                    spinnerEnfants.getValueFactory().setValue(0);
                }
                if (spinnerAdultes != null) {
                    spinnerAdultes.getValueFactory().setValue(maxPlaces);
                }
            }
        }

        int nouveauTotal = (spinnerAdultes != null ? spinnerAdultes.getValue() : 1) +
                (spinnerEnfants != null ? spinnerEnfants.getValue() : 0);
        if (spinnerPlaces != null && nouveauTotal != spinnerPlaces.getValue()) {
            spinnerPlaces.getValueFactory().setValue(nouveauTotal);
        }
    }

    private void calculerTotal() {
        if (selectedVoyage == null) return;

        int adultes = spinnerAdultes != null ? spinnerAdultes.getValue() : 1;
        int enfants = spinnerEnfants != null ? spinnerEnfants.getValue() : 0;
        int totalPersonnes = adultes + enfants;
        double prixUnitaire = selectedVoyage.getPrix();
        double total = totalPersonnes * prixUnitaire;

        if (lblPrixTotal != null) {
            lblPrixTotal.setText(String.format(Locale.US, "%.0f DT", total));
        }
        if (spinnerPlaces != null) {
            spinnerPlaces.getValueFactory().setValue(totalPersonnes);
        }
    }

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

    private void loadImageSmart(String path) {
        try {
            if (path != null && !path.isBlank()) {
                String p = path.trim();

                if (p.startsWith("http://") || p.startsWith("https://")) {
                    imgVoyage.setImage(new Image(p, true));
                    return;
                }

                if (p.startsWith("file:C:/") || p.startsWith("file:D:/") || p.startsWith("file:E:/")) {
                    p = "file:/" + p.substring("file:".length());
                    imgVoyage.setImage(new Image(p, true));
                    return;
                }

                if (p.matches("^[A-Za-z]:\\\\.*")) {
                    p = "file:/" + p.replace("\\", "/");
                    imgVoyage.setImage(new Image(p, true));
                    return;
                }

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

    private boolean validerSaisie() {
        boolean isValid = true;

        if (errorPersonnes != null) {
            errorPersonnes.setVisible(false);
            errorPersonnes.setText("");
        }
        if (errorRepartition != null) {
            errorRepartition.setVisible(false);
            errorRepartition.setText("");
        }

        if (selectedVoyage == null) {
            return false;
        }

        int adultes = spinnerAdultes != null ? spinnerAdultes.getValue() : 1;
        int enfants = spinnerEnfants != null ? spinnerEnfants.getValue() : 0;
        int totalPersonnes = adultes + enfants;
        int maxPlaces = selectedVoyage.getPlaces_restantes();

        if (totalPersonnes < 1) {
            if (errorPersonnes != null) {
                errorPersonnes.setText("Au moins 1 personne est requise");
                errorPersonnes.setVisible(true);
            }
            isValid = false;
        }

        if (adultes < 1 && totalPersonnes > 0) {
            if (errorRepartition != null) {
                errorRepartition.setText("Au moins 1 adulte est requis pour le voyage");
                errorRepartition.setVisible(true);
            }
            isValid = false;
        }

        if (totalPersonnes > maxPlaces) {
            if (errorPersonnes != null) {
                errorPersonnes.setText("Maximum " + maxPlaces + " personnes disponibles");
                errorPersonnes.setVisible(true);
            }
            isValid = false;
        }

        return isValid;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    // =========================
    // ACTIONS
    // =========================
    @FXML
    void confirmerReservation() {
        // Valider la saisie
        if (!validerSaisie()) {
            showAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de confirmer.", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (selectedVoyage == null) {
                showAlert("Erreur", "Aucun voyage sélectionné.", Alert.AlertType.ERROR);
                return;
            }

            if (currentUser == null) {
                showAlert("Connexion requise", "Veuillez vous connecter pour effectuer une réservation.", Alert.AlertType.ERROR);
                NavigationManager.showLogin();
                return;
            }

            int adultes = spinnerAdultes != null ? spinnerAdultes.getValue() : 1;
            int enfants = spinnerEnfants != null ? spinnerEnfants.getValue() : 0;
            int totalPersonnes = adultes + enfants;

            if (totalPersonnes > selectedVoyage.getPlaces_restantes()) {
                showAlert("Erreur", "Nombre de personnes supérieur aux places disponibles (" + selectedVoyage.getPlaces_restantes() + ")", Alert.AlertType.ERROR);
                return;
            }

            double prixTotal = totalPersonnes * selectedVoyage.getPrix();

            // Utiliser la méthode avec tous les paramètres (adultes et enfants)
            boolean success = rs.effectuerReservation(
                    selectedVoyage.getId(),
                    currentUser.getId(),
                    totalPersonnes,
                    adultes,
                    enfants,
                    prixTotal
            );

            if (success) {
                // Mettre à jour les places restantes dans l'objet voyage
                selectedVoyage.setPlaces_restantes(selectedVoyage.getPlaces_restantes() - totalPersonnes);

                showAlert("Succès", "✅ Réservation confirmée !\n\n" +
                                "Détails:\n" +
                                "Voyage: " + selectedVoyage.getDestination() + "\n" +
                                "Adultes: " + adultes + "\n" +
                                "Enfants: " + enfants + "\n" +
                                "Total personnes: " + totalPersonnes + "\n" +
                                "Total: " + String.format("%.0f", prixTotal) + " DT",
                        Alert.AlertType.INFORMATION);
                NavigationManager.loadView("/fxml/MesReservationsvoy.fxml", "Mes Réservations");
            } else {
                showAlert("Erreur", "Impossible d'effectuer la réservation.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void retour() {
        exec.shutdownNow();
        NavigationManager.loadView("/fxml/CatalogueUser.fxml", "Catalogue");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        alert.showAndWait();
    }
}