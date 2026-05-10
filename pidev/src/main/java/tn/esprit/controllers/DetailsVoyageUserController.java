package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.entites.Reservation;
import tn.esprit.entites.Voyage;
import tn.esprit.services.ReservationService;
import tn.esprit.services.VoyageService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;
import tn.esprit.entities.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DetailsVoyageUserController {

    @FXML private NavbarController navbarController;

    @FXML private Label lblTitre, lblDescription, lblDates, lblPrix, lblPlaces;
    @FXML private Label lblPrixUnitaire, lblTotal, badgeStatus;
    @FXML private Label imageBadge;
    @FXML private Spinner<Integer> spNbPersonnes;
    @FXML private Spinner<Integer> spAdultes;
    @FXML private Spinner<Integer> spEnfants;
    @FXML private ImageView imgVoyage;
    @FXML private Label errorPersonnes;
    @FXML private Label errorRepartition;

    private Voyage voyage;
    private User currentUser;
    private ReservationService reservationService = new ReservationService();
    private VoyageService voyageService = new VoyageService();

    public void initData(Voyage v) {
        this.voyage = v;
        this.currentUser = SessionManager.getCurrentUser();

        if (navbarController != null) {
            navbarController.updateUserInfo();
        }

        afficherInfosVoyage();
        configurerSpinners();
        configurerListeners();
    }

    private void afficherInfosVoyage() {
        lblTitre.setText("Voyage à " + voyage.getDestination());
        lblDescription.setText(voyage.getDescription());
        lblDates.setText("📅 " + voyage.getDate_depart() + " → " + voyage.getDate_retour());
        lblPrix.setText("💰 " + voyage.getPrix() + " DT / pers");
        lblPlaces.setText("👥 " + voyage.getPlaces_restantes() + " places restantes");
        lblPrixUnitaire.setText(voyage.getPrix() + " DT");
        lblTotal.setText(voyage.getPrix() + " DT");

        if (imageBadge != null) {
            imageBadge.setText(voyage.getDestination());
        }

        if (voyage.getPlaces_restantes() > 0) {
            badgeStatus.setText("DISPONIBLE");
            badgeStatus.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 12;");
        } else {
            badgeStatus.setText("COMPLET");
            badgeStatus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 12;");
        }

        try {
            if (voyage.getImage_url() != null && !voyage.getImage_url().isEmpty()) {
                imgVoyage.setImage(new Image(voyage.getImage_url(), true));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }
    }

    private void configurerSpinners() {
        int placesMax = voyage.getPlaces_restantes();

        spNbPersonnes.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, placesMax, 1)
        );

        spAdultes.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, placesMax, 1)
        );

        spEnfants.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, placesMax, 0)
        );
    }

    private void configurerListeners() {
        spNbPersonnes.valueProperty().addListener((obs, oldVal, newVal) -> {
            ajusterRepartition(newVal);
            calculerTotal();
            validerSaisie();
        });

        spAdultes.valueProperty().addListener((obs, oldVal, newVal) -> {
            verifierCoherence();
            calculerTotal();
            validerSaisie();
        });

        spEnfants.valueProperty().addListener((obs, oldVal, newVal) -> {
            verifierCoherence();
            calculerTotal();
            validerSaisie();
        });
    }

    private void ajusterRepartition(int totalPersonnes) {
        int adultesActuels = spAdultes.getValue();
        int enfantsActuels = spEnfants.getValue();
        int sommeActuelle = adultesActuels + enfantsActuels;

        if (sommeActuelle > totalPersonnes) {
            int difference = sommeActuelle - totalPersonnes;
            int nouveauxEnfants = Math.max(0, enfantsActuels - difference);
            spEnfants.getValueFactory().setValue(nouveauxEnfants);

            int nouvelleSomme = adultesActuels + nouveauxEnfants;
            if (nouvelleSomme > totalPersonnes) {
                int nouveauxAdultes = Math.max(1, adultesActuels - (nouvelleSomme - totalPersonnes));
                spAdultes.getValueFactory().setValue(nouveauxAdultes);
            }
        } else if (sommeActuelle < totalPersonnes && totalPersonnes > 0) {
            int difference = totalPersonnes - sommeActuelle;
            spAdultes.getValueFactory().setValue(adultesActuels + difference);
        }
    }

    private void verifierCoherence() {
        int adultes = spAdultes.getValue();
        int enfants = spEnfants.getValue();
        int total = adultes + enfants;
        int maxPlaces = voyage.getPlaces_restantes();

        if (total > maxPlaces) {
            int difference = total - maxPlaces;
            if (enfants >= difference) {
                spEnfants.getValueFactory().setValue(enfants - difference);
            } else {
                spEnfants.getValueFactory().setValue(0);
                spAdultes.getValueFactory().setValue(maxPlaces);
            }
        }

        int nouveauTotal = spAdultes.getValue() + spEnfants.getValue();
        if (nouveauTotal != spNbPersonnes.getValue()) {
            spNbPersonnes.getValueFactory().setValue(nouveauTotal);
        }
    }

    private void calculerTotal() {
        int adultes = spAdultes.getValue();
        int enfants = spEnfants.getValue();
        int totalPersonnes = adultes + enfants;
        double prixUnitaire = voyage.getPrix();
        double total = totalPersonnes * prixUnitaire;

        lblTotal.setText(String.format("%.0f DT", total));
        spNbPersonnes.getValueFactory().setValue(totalPersonnes);
    }

    private boolean validerSaisie() {
        boolean isValid = true;

        errorPersonnes.setVisible(false);
        errorRepartition.setVisible(false);

        int adultes = spAdultes.getValue();
        int enfants = spEnfants.getValue();
        int totalPersonnes = adultes + enfants;
        int placesMax = voyage.getPlaces_restantes();

        if (totalPersonnes < 1) {
            errorPersonnes.setText("Au moins 1 personne est requise");
            errorPersonnes.setVisible(true);
            isValid = false;
        }

        if (adultes < 1 && totalPersonnes > 0) {
            errorRepartition.setText("Au moins 1 adulte est requis pour le voyage");
            errorRepartition.setVisible(true);
            isValid = false;
        }

        if (totalPersonnes > placesMax) {
            errorPersonnes.setText("Nombre de personnes dépasse les places disponibles (" + placesMax + ")");
            errorPersonnes.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void confirmerReservation() {
        if (currentUser == null) {
            showAlert("Connexion requise", "Veuillez vous connecter pour effectuer une réservation");
            NavigationManager.showLogin();
            return;
        }

        if (!validerSaisie()) {
            showAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de confirmer");
            return;
        }

        int adultes = spAdultes.getValue();
        int enfants = spEnfants.getValue();
        int totalPersonnes = adultes + enfants;
        double prixTotal = totalPersonnes * voyage.getPrix();

        if (voyage.getPlaces_restantes() < totalPersonnes) {
            showAlert("Erreur", "Désolé, il n'y a plus assez de places disponibles pour ce voyage");
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setDate_reservation(new Timestamp(System.currentTimeMillis()));
        reservation.setStatut("CONFIRMEE");
        reservation.setId_voyage(voyage.getId());
        reservation.setId_user(currentUser.getId());
        reservation.setNbr_personnes(totalPersonnes);
        reservation.setNb_adultes(adultes);
        reservation.setNb_enfants(enfants);
        reservation.setPrix_total(prixTotal);
        reservation.setPayment_status("NON_PAYEE");

        try {
            reservationService.ajouter(reservation);

            boolean placesUpdated = voyageService.decrementerPlaces(voyage.getId(), totalPersonnes);

            if (placesUpdated) {
                showAlert("Succès", "✅ Réservation effectuée avec succès !\n\n" +
                        "Détails:\n" +
                        "Voyage: " + voyage.getDestination() + "\n" +
                        "Personnes: " + totalPersonnes + " (" + adultes + " adultes, " + enfants + " enfants)\n" +
                        "Total: " + String.format("%.0f", prixTotal) + " DT\n" +
                        "Statut paiement: En attente");

                NavigationManager.loadView("/fxml/CatalogueUser.fxml", "Catalogue");
            } else {
                showAlert("Erreur", "Impossible de mettre à jour les places disponibles");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la réservation: " + e.getMessage());
        }
    }

    @FXML
    private void retourCatalogue() {
        NavigationManager.loadView("/fxml/CatalogueUser.fxml", "Catalogue");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}