package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.entities.User;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.*;

import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReservationFormController {

    @FXML private NavbarController navbarController;

    // Images et infos logement
    @FXML private ImageView imageLogement;
    @FXML private Label nomLabel;
    @FXML private Label typeLabel;
    @FXML private Label adresseLabel;
    @FXML private Label capaciteLabel;
    @FXML private Label prixLabel;
    @FXML private Label equipementLabel;
    @FXML private Label disponibiliteLabel;

    // Champs formulaire
    @FXML private DatePicker dateArriveePicker;
    @FXML private DatePicker dateDepartPicker;
    @FXML private Spinner<Integer> adultesSpinner;
    @FXML private Spinner<Integer> enfantsSpinner;
    @FXML private Spinner<Integer> chambresSpinner;
    @FXML private TextArea repartitionChambres;

    // Formules pension
    @FXML private RadioButton petitDejeunerRadio;
    @FXML private RadioButton demiPensionRadio;
    @FXML private RadioButton allInclusiveRadio;
    @FXML private RadioButton allInclusiveSoftRadio;

    // Modalités
    @FXML private RadioButton enLigneRadio;
    @FXML private RadioButton surPlaceRadio;
    @FXML private CheckBox paiementImmediatCheckbox;

    // Conteneurs dynamiques
    @FXML private VBox chambresContainer;
    @FXML private VBox paiementImmediatContainer;

    // Labels résultats
    @FXML private Label nuitsLabel;
    @FXML private Label voyageursLabel;
    @FXML private Label formuleLabel;
    @FXML private Label prixParPersonneLabel;
    @FXML private Label totalLabel;

    // Labels erreur
    @FXML private Label arriveeErrorLabel;
    @FXML private Label departErrorLabel;
    @FXML private Label dureeErrorLabel;
    @FXML private Label modaliteErrorLabel;
    @FXML private Label capaciteErrorLabel;
    @FXML private Label pensionErrorLabel;

    // Boutons
    @FXML private Button confirmerBtn;
    @FXML private Button annulerBtn;
    @FXML private Button accueilBtn;

    private logement selectedLogement;
    private reservationlog reservationToEdit;
    private User currentUser;
    private Servicelogement serviceLogement = new Servicelogement();
    private Servicereservationlog serviceReservation = new Servicereservationlog();

    private int currentNuits = 1;
    private double coefficientPension = 1.0;
    private String formuleSelectionnee = "logement_petit_dejeuner";

    private final Map<String, Double> coefficientsPension = new HashMap<>();
    private final Map<String, String> nomsFormules = new HashMap<>();

    @FXML
    public void initialize() {
        System.out.println("Initialisation de ReservationFormController...");

        initCoefficients();

        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showSweetAlert("Erreur", "Vous devez être connecté pour faire une réservation", Alert.AlertType.ERROR);
            NavigationManager.loadView("/fxml/Login.fxml", "Connexion");
            return;
        }

        if (navbarController != null) {
            navbarController.updateUserInfo();
        }

        chargerReservation();

        if (selectedLogement != null) {
            afficherInfosLogement();
            configurerSpinners();
        }

        configurerListeners();
        setupRadioButtons();

        // Sélectionner les valeurs par défaut
        if (petitDejeunerRadio != null) petitDejeunerRadio.setSelected(true);
        if (enLigneRadio != null) enLigneRadio.setSelected(true);
        if (paiementImmediatCheckbox != null) paiementImmediatCheckbox.setSelected(true);

        resetErrorLabels();
        calculerTotal();
    }

    private void initCoefficients() {
        coefficientsPension.put("logement_petit_dejeuner", 1.0);
        coefficientsPension.put("demi_pension", 1.20);
        coefficientsPension.put("all_inclusive", 1.45);
        coefficientsPension.put("all_inclusive_soft", 1.37);

        nomsFormules.put("logement_petit_dejeuner", "Logement + Petit déjeuner");
        nomsFormules.put("demi_pension", "Demi-pension");
        nomsFormules.put("all_inclusive", "All inclusive");
        nomsFormules.put("all_inclusive_soft", "All inclusive soft");
    }

    private void setupRadioButtons() {
        if (petitDejeunerRadio != null) {
            petitDejeunerRadio.setOnAction(e -> {
                formuleSelectionnee = "logement_petit_dejeuner";
                coefficientPension = 1.0;
                calculerTotal();
            });
        }
        if (demiPensionRadio != null) {
            demiPensionRadio.setOnAction(e -> {
                formuleSelectionnee = "demi_pension";
                coefficientPension = 1.20;
                calculerTotal();
            });
        }
        if (allInclusiveRadio != null) {
            allInclusiveRadio.setOnAction(e -> {
                formuleSelectionnee = "all_inclusive";
                coefficientPension = 1.45;
                calculerTotal();
            });
        }
        if (allInclusiveSoftRadio != null) {
            allInclusiveSoftRadio.setOnAction(e -> {
                formuleSelectionnee = "all_inclusive_soft";
                coefficientPension = 1.37;
                calculerTotal();
            });
        }

        if (enLigneRadio != null) {
            enLigneRadio.setOnAction(e -> {
                if (paiementImmediatContainer != null) {
                    paiementImmediatContainer.setVisible(true);
                    paiementImmediatContainer.setManaged(true);
                }
            });
        }
        if (surPlaceRadio != null) {
            surPlaceRadio.setOnAction(e -> {
                if (paiementImmediatContainer != null) {
                    paiementImmediatContainer.setVisible(false);
                    paiementImmediatContainer.setManaged(false);
                }
            });
        }
    }

    private void chargerReservation() {
        reservationToEdit = SessionManager.getEditingReservation();
        if (reservationToEdit != null) {
            try {
                selectedLogement = serviceLogement.rechercherParId(reservationToEdit.getId_l());
                if (selectedLogement == null) {
                    showSweetAlert("Erreur", "Logement introuvable.", Alert.AlertType.ERROR);
                    NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
                }
            } catch (SQLException e) {
                showSweetAlert("Erreur", "Impossible de charger le logement.", Alert.AlertType.ERROR);
                NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
            }
        } else {
            selectedLogement = SessionManager.getSelectedLogement();
            if (selectedLogement == null) {
                showSweetAlert("Erreur", "Aucun logement sélectionné.", Alert.AlertType.ERROR);
                NavigationManager.loadView("/fxml/accueil.fxml", "Accueil");
            }
        }
    }

    private void afficherInfosLogement() {
        if (selectedLogement == null) return;

        String imagePath = selectedLogement.getImage();
        if (imagePath != null && !imagePath.isEmpty() && imageLogement != null) {
            try {
                if (imagePath.startsWith("http")) {
                    imageLogement.setImage(new Image(imagePath, true));
                } else {
                    InputStream is = getClass().getResourceAsStream(imagePath);
                    if (is != null) imageLogement.setImage(new Image(is));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        if (nomLabel != null) nomLabel.setText(selectedLogement.getNom());
        if (typeLabel != null) typeLabel.setText("Type: " + selectedLogement.getType());
        if (adresseLabel != null) adresseLabel.setText("📍 " + selectedLogement.getAdresse());
        if (capaciteLabel != null) capaciteLabel.setText("👥 Capacité max: " + selectedLogement.getCapacite() + " personnes");
        if (prixLabel != null) prixLabel.setText(String.format("Prix de base: %.0f DT/personne/nuit", selectedLogement.getTarif_nuit()));
        if (equipementLabel != null) equipementLabel.setText("⚙️ " + selectedLogement.getEquipement());
        if (disponibiliteLabel != null) disponibiliteLabel.setText(selectedLogement.isDisponibilite() ? "✓ Disponible" : "✗ Non disponible");

        String type = selectedLogement.getType().toLowerCase();
        boolean isHotel = type.equals("hôtel") || type.equals("hotel");
        if (chambresContainer != null) {
            chambresContainer.setVisible(isHotel);
            chambresContainer.setManaged(isHotel);
        }
    }

    private void configurerSpinners() {
        if (selectedLogement == null) return;

        int capaciteMax = selectedLogement.getCapacite();
        if (adultesSpinner != null) {
            adultesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, capaciteMax, 1));
        }
        if (enfantsSpinner != null) {
            enfantsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, capaciteMax - 1, 0));
        }
        if (chambresSpinner != null) {
            chambresSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        }
    }

    private void configurerListeners() {
        if (dateArriveePicker != null) {
            dateArriveePicker.valueProperty().addListener((obs, old, val) -> {
                calculerDuree();
                clearDateErrors();
            });
        }
        if (dateDepartPicker != null) {
            dateDepartPicker.valueProperty().addListener((obs, old, val) -> {
                calculerDuree();
                clearDateErrors();
            });
        }
        if (adultesSpinner != null) {
            adultesSpinner.valueProperty().addListener((obs, old, val) -> {
                verifierCapacite();
                calculerTotal();
            });
        }
        if (enfantsSpinner != null) {
            enfantsSpinner.valueProperty().addListener((obs, old, val) -> {
                verifierCapacite();
                calculerTotal();
            });
        }

        if (reservationToEdit != null) preRemplirChamps();

        if (confirmerBtn != null) confirmerBtn.setOnAction(e -> confirmerReservation());
        if (annulerBtn != null) {
            annulerBtn.setOnAction(e -> {
                SessionManager.clearEditingReservation();
                NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
            });
        }
        if (accueilBtn != null) accueilBtn.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml", "Accueil"));
    }

    private void preRemplirChamps() {
        if (reservationToEdit == null) return;

        LocalDate arrivee = reservationToEdit.getDate_debut().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate depart = reservationToEdit.getDate_fin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (dateArriveePicker != null) dateArriveePicker.setValue(arrivee);
        if (dateDepartPicker != null) dateDepartPicker.setValue(depart);

        if (adultesSpinner != null && reservationToEdit.getAdultes() > 0) {
            adultesSpinner.getValueFactory().setValue(reservationToEdit.getAdultes());
        }
        if (enfantsSpinner != null && reservationToEdit.getEnfants() > 0) {
            enfantsSpinner.getValueFactory().setValue(reservationToEdit.getEnfants());
        }

        String modalite = reservationToEdit.getModalite();
        if ("En ligne".equals(modalite) && enLigneRadio != null) {
            enLigneRadio.setSelected(true);
        } else if (surPlaceRadio != null) {
            surPlaceRadio.setSelected(true);
        }

        String modeReservation = reservationToEdit.getModeReservation();
        if (modeReservation != null) {
            switch (modeReservation) {
                case "demi_pension":
                    if (demiPensionRadio != null) demiPensionRadio.setSelected(true);
                    break;
                case "all_inclusive":
                    if (allInclusiveRadio != null) allInclusiveRadio.setSelected(true);
                    break;
                case "all_inclusive_soft":
                    if (allInclusiveSoftRadio != null) allInclusiveSoftRadio.setSelected(true);
                    break;
                default:
                    if (petitDejeunerRadio != null) petitDejeunerRadio.setSelected(true);
            }
        }

        if (chambresSpinner != null && reservationToEdit.getNombreChambres() > 0) {
            chambresSpinner.getValueFactory().setValue(reservationToEdit.getNombreChambres());
        }
        if (repartitionChambres != null && reservationToEdit.getRepartitionChambres() != null) {
            repartitionChambres.setText(reservationToEdit.getRepartitionChambres());
        }

        calculerDuree();
    }

    private void verifierCapacite() {
        if (selectedLogement == null) return;

        int adultes = (adultesSpinner != null && adultesSpinner.getValue() != null) ? adultesSpinner.getValue() : 1;
        int enfants = (enfantsSpinner != null && enfantsSpinner.getValue() != null) ? enfantsSpinner.getValue() : 0;
        int totalPersonnes = adultes + enfants;

        if (totalPersonnes > selectedLogement.getCapacite()) {
            int maxEnfants = selectedLogement.getCapacite() - adultes;
            if (maxEnfants < 0) {
                if (adultesSpinner != null) adultesSpinner.getValueFactory().setValue(selectedLogement.getCapacite());
                if (enfantsSpinner != null) enfantsSpinner.getValueFactory().setValue(0);
            } else {
                if (enfantsSpinner != null) enfantsSpinner.getValueFactory().setValue(maxEnfants);
            }
            if (capaciteErrorLabel != null) {
                capaciteErrorLabel.setText("Capacité maximale dépassée ! Max: " + selectedLogement.getCapacite());
                capaciteErrorLabel.setVisible(true);
            }
        } else {
            if (capaciteErrorLabel != null) {
                capaciteErrorLabel.setVisible(false);
            }
        }
        updateVoyageursLabel();
    }

    private void updateVoyageursLabel() {
        int adultes = (adultesSpinner != null && adultesSpinner.getValue() != null) ? adultesSpinner.getValue() : 1;
        int enfants = (enfantsSpinner != null && enfantsSpinner.getValue() != null) ? enfantsSpinner.getValue() : 0;
        int total = adultes + enfants;
        if (voyageursLabel != null) {
            voyageursLabel.setText(total + " personne" + (total > 1 ? "s" : ""));
        }
    }

    private void calculerDuree() {
        LocalDate arrivee = (dateArriveePicker != null) ? dateArriveePicker.getValue() : null;
        LocalDate depart = (dateDepartPicker != null) ? dateDepartPicker.getValue() : null;

        if (arrivee != null && depart != null && depart.isAfter(arrivee)) {
            currentNuits = (int) ChronoUnit.DAYS.between(arrivee, depart);
        } else {
            currentNuits = 1;
        }

        if (nuitsLabel != null) nuitsLabel.setText(String.valueOf(currentNuits));
        calculerTotal();
    }

    private void calculerTotal() {
        if (selectedLogement == null) return;

        int adultes = (adultesSpinner != null && adultesSpinner.getValue() != null) ? adultesSpinner.getValue() : 1;
        int enfants = (enfantsSpinner != null && enfantsSpinner.getValue() != null) ? enfantsSpinner.getValue() : 0;
        int totalPersonnes = adultes + enfants;

        determinerFormule();

        double prixBaseParPersonne = selectedLogement.getTarif_nuit();
        double prixAvecPension = prixBaseParPersonne * coefficientPension;
        double total = currentNuits * totalPersonnes * prixAvecPension;

        if (prixParPersonneLabel != null) prixParPersonneLabel.setText(String.format("%.0f DT", prixAvecPension));
        if (totalLabel != null) totalLabel.setText(String.format("%.0f DT", total));
        if (formuleLabel != null) formuleLabel.setText(nomsFormules.getOrDefault(formuleSelectionnee, "Petit déjeuner"));
    }

    private void determinerFormule() {
        if (petitDejeunerRadio != null && petitDejeunerRadio.isSelected()) {
            formuleSelectionnee = "logement_petit_dejeuner";
            coefficientPension = 1.0;
        } else if (demiPensionRadio != null && demiPensionRadio.isSelected()) {
            formuleSelectionnee = "demi_pension";
            coefficientPension = 1.20;
        } else if (allInclusiveRadio != null && allInclusiveRadio.isSelected()) {
            formuleSelectionnee = "all_inclusive";
            coefficientPension = 1.45;
        } else if (allInclusiveSoftRadio != null && allInclusiveSoftRadio.isSelected()) {
            formuleSelectionnee = "all_inclusive_soft";
            coefficientPension = 1.37;
        }
    }

    private String validerSaisie() {
        resetErrorLabels();

        if (selectedLogement == null) {
            return "Aucun logement sélectionné.";
        }

        if (!selectedLogement.isDisponibilite()) {
            return "Le logement n'est pas disponible.";
        }

        LocalDate arrivee = (dateArriveePicker != null) ? dateArriveePicker.getValue() : null;
        LocalDate depart = (dateDepartPicker != null) ? dateDepartPicker.getValue() : null;
        LocalDate today = LocalDate.now();

        if (arrivee == null) {
            if (arriveeErrorLabel != null) {
                arriveeErrorLabel.setText("Veuillez sélectionner une date d'arrivée");
                arriveeErrorLabel.setVisible(true);
            }
            return "Date d'arrivée requise";
        } else if (arrivee.isBefore(today)) {
            if (arriveeErrorLabel != null) {
                arriveeErrorLabel.setText("La date d'arrivée ne peut pas être dans le passé");
                arriveeErrorLabel.setVisible(true);
            }
            return "Date d'arrivée invalide";
        }

        if (depart == null) {
            if (departErrorLabel != null) {
                departErrorLabel.setText("Veuillez sélectionner une date de départ");
                departErrorLabel.setVisible(true);
            }
            return "Date de départ requise";
        } else if (arrivee != null && !depart.isAfter(arrivee)) {
            if (departErrorLabel != null) {
                departErrorLabel.setText("La date de départ doit être après la date d'arrivée");
                departErrorLabel.setVisible(true);
            }
            return "Date de départ invalide";
        }

        boolean formuleSelected = (petitDejeunerRadio != null && petitDejeunerRadio.isSelected()) ||
                (demiPensionRadio != null && demiPensionRadio.isSelected()) ||
                (allInclusiveRadio != null && allInclusiveRadio.isSelected()) ||
                (allInclusiveSoftRadio != null && allInclusiveSoftRadio.isSelected());

        if (!formuleSelected) {
            if (pensionErrorLabel != null) {
                pensionErrorLabel.setText("Veuillez choisir une formule de pension");
                pensionErrorLabel.setVisible(true);
            }
            return "Formule de pension non sélectionnée";
        }

        boolean modaliteSelected = (enLigneRadio != null && enLigneRadio.isSelected()) ||
                (surPlaceRadio != null && surPlaceRadio.isSelected());

        if (!modaliteSelected) {
            if (modaliteErrorLabel != null) {
                modaliteErrorLabel.setText("Veuillez choisir une modalité de paiement");
                modaliteErrorLabel.setVisible(true);
            }
            return "Modalité de paiement non sélectionnée";
        }

        int adultes = (adultesSpinner != null && adultesSpinner.getValue() != null) ? adultesSpinner.getValue() : 1;
        int enfants = (enfantsSpinner != null && enfantsSpinner.getValue() != null) ? enfantsSpinner.getValue() : 0;

        if (adultes + enfants > selectedLogement.getCapacite()) {
            if (capaciteErrorLabel != null) {
                capaciteErrorLabel.setText("Le nombre total de personnes dépasse la capacité maximale (" + selectedLogement.getCapacite() + ")");
                capaciteErrorLabel.setVisible(true);
            }
            return "Capacité maximale dépassée";
        }

        if (currentNuits <= 0) {
            if (dureeErrorLabel != null) {
                dureeErrorLabel.setText("La durée du séjour doit être d'au moins 1 nuit");
                dureeErrorLabel.setVisible(true);
            }
            return "Durée invalide";
        }

        return null;
    }

    private void confirmerReservation() {
        System.out.println("=== Confirmation de réservation ===");

        String erreur = validerSaisie();
        if (erreur != null) {
            showSweetAlert("Erreur de validation", erreur, Alert.AlertType.ERROR);
            return;
        }

        String modalite = (enLigneRadio != null && enLigneRadio.isSelected()) ? "En ligne" : "Sur place";
        boolean paiementImmediat = (paiementImmediatCheckbox != null && paiementImmediatCheckbox.isSelected());

        String typeLogement = selectedLogement.getType().toLowerCase();
        boolean isHotel = typeLogement.equals("hôtel") || typeLogement.equals("hotel");
        int nombreChambres = (isHotel && chambresSpinner != null && chambresSpinner.getValue() != null) ? chambresSpinner.getValue() : 1;
        String repartition = (repartitionChambres != null) ? repartitionChambres.getText() : "";

        if (reservationToEdit != null) {
            modifierReservation(modalite, nombreChambres, repartition);
            return;
        }

        // Gestion des statuts selon les règles métier
        if ("Sur place".equals(modalite)) {
            // Paiement sur place -> statut "en_attente"
            enregistrerReservation(Status.en_attente, modalite, nombreChambres, repartition, null, false);
        } else {
            // Paiement en ligne
            if (paiementImmediat) {
                // Paiement immédiat -> créer réservation en_attente + lancer Stripe
                int reservationId = enregistrerReservation(Status.en_attente, modalite, nombreChambres, repartition, null, true);
                if (reservationId > 0) {
                    lancerPaiementStripe(reservationId, modalite);
                }
            } else {
                // Paiement différé -> statut "en_attente" avec date limite 24h
                Date dateLimite = Date.from(LocalDateTime.now().plusHours(24).atZone(ZoneId.systemDefault()).toInstant());
                enregistrerReservation(Status.en_attente, modalite, nombreChambres, repartition, dateLimite, false);
                showSweetAlert("Succès",
                        "⏳ Réservation en attente de paiement.\n\n" +
                                "Vous avez 24h pour finaliser votre paiement.\n" +
                                "Au-delà, votre réservation sera automatiquement annulée.\n\n" +
                                "Un email avec lien de paiement vous a été envoyé.",
                        Alert.AlertType.INFORMATION);
                NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
            }
        }
    }

    private int enregistrerReservation(Status statut, String modalite, int nombreChambres, String repartition, Date dateLimite, boolean returnId) {
        try {
            LocalDate arrivee = (dateArriveePicker != null) ? dateArriveePicker.getValue() : LocalDate.now();
            LocalDate depart = (dateDepartPicker != null) ? dateDepartPicker.getValue() : LocalDate.now().plusDays(1);
            int adultes = (adultesSpinner != null && adultesSpinner.getValue() != null) ? adultesSpinner.getValue() : 1;
            int enfants = (enfantsSpinner != null && enfantsSpinner.getValue() != null) ? enfantsSpinner.getValue() : 0;
            int totalPersonnes = adultes + enfants;
            double total = currentNuits * totalPersonnes * selectedLogement.getTarif_nuit() * coefficientPension;

            System.out.println("Enregistrement réservation:");
            System.out.println("- Statut: " + statut);
            System.out.println("- Modalité: " + modalite);
            System.out.println("- Date limite paiement: " + dateLimite);

            reservationlog reservation = new reservationlog();
            reservation.setId_l(selectedLogement.getId());
            reservation.setIdc(currentUser.getId());
            reservation.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservation.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservation.setMontant((float) total);
            reservation.setStatus(statut);
            reservation.setModalite(modalite);
            reservation.setAdultes(adultes);
            reservation.setEnfants(enfants);
            reservation.setNombreChambres(nombreChambres);
            reservation.setModeReservation(formuleSelectionnee);
            reservation.setRepartitionChambres(repartition);
            reservation.setDateLimitePaiement(dateLimite);
            reservation.setCreatedAt(new Date());

            serviceReservation.ajouter(reservation);
            System.out.println("✅ Réservation enregistrée avec succès ! ID: " + reservation.getId());

            // Envoyer email de confirmation
            envoyerEmailConfirmation(reservation, modalite, statut, dateLimite);

            if (returnId) {
                return reservation.getId();
            } else {
                showSweetAlert("Succès", "Réservation enregistrée avec succès !", Alert.AlertType.INFORMATION);
                NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
                return -1;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
            showSweetAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
            return -1;
        }
    }

    private void lancerPaiementStripe(int reservationId, String modalite) {
        try {
            double montant = Double.parseDouble(totalLabel.getText().replace(" DT", ""));
            long montantCentimes = Math.round(montant * 100);
            String currency = "eur";

            // URLs de retour
            String successUrl = "http://localhost:8080/api/payment/success?reservationId=" + reservationId;
            String cancelUrl = "http://localhost:8080/api/payment/cancel?reservationId=" + reservationId;

            // Créer la session Stripe
            String checkoutUrl = StripeService.createCheckoutSession(montantCentimes, currency, successUrl, cancelUrl);

            // Ouvrir la page de paiement
            ouvrirPagePaiement(checkoutUrl, reservationId, modalite);

        } catch (Exception e) {
            e.printStackTrace();
            showSweetAlert("Erreur", "Impossible de lancer le paiement : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ouvrirPagePaiement(String checkoutUrl, int reservationId, String modalite) {
        Stage stage = new Stage();
        stage.setTitle("Paiement sécurisé - Stripe");

        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #1A3C5A; -fx-padding: 10 15;");

        Label titleLabel = new Label("💳 Paiement par carte bancaire");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        titleBar.getChildren().addAll(titleLabel, spacer, closeButton);

        Region greenLine = new Region();
        greenLine.setPrefHeight(4);
        greenLine.setStyle("-fx-background-color: #2ECC71;");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Ajouter un listener pour la fermeture de la page
        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null && newUrl.contains("payment/success")) {
                // Paiement réussi => mettre à jour le statut de la réservation
                stage.close();
                mettreAJourStatutReservation(reservationId, Status.confirmée);
                showSweetAlert("Succès", "✅ Paiement accepté ! Votre réservation est maintenant confirmée.", Alert.AlertType.INFORMATION);
                NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
            } else if (newUrl != null && newUrl.contains("payment/cancel")) {
                // Paiement annulé => annuler la réservation
                stage.close();
                mettreAJourStatutReservation(reservationId, Status.annulée);
                showSweetAlert("Paiement annulé", "❌ Vous avez annulé le paiement. Votre réservation a été annulée.", Alert.AlertType.WARNING);
                NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
            }
        });

        engine.load(checkoutUrl);

        VBox root = new VBox(titleBar, greenLine, webView);
        VBox.setVgrow(webView, Priority.ALWAYS);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void mettreAJourStatutReservation(int reservationId, Status nouveauStatut) {
        try {
            reservationlog reservation = serviceReservation.rechercherParId(reservationId);
            if (reservation != null) {
                reservation.setStatus(nouveauStatut);
                serviceReservation.modifier(reservation);
                System.out.println("✅ Statut de la réservation " + reservationId + " mis à jour: " + nouveauStatut);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void modifierReservation(String modalite, int nombreChambres, String repartition) {
        try {
            LocalDate arrivee = (dateArriveePicker != null) ? dateArriveePicker.getValue() : LocalDate.now();
            LocalDate depart = (dateDepartPicker != null) ? dateDepartPicker.getValue() : LocalDate.now().plusDays(1);
            int adultes = (adultesSpinner != null && adultesSpinner.getValue() != null) ? adultesSpinner.getValue() : 1;
            int enfants = (enfantsSpinner != null && enfantsSpinner.getValue() != null) ? enfantsSpinner.getValue() : 0;
            int totalPersonnes = adultes + enfants;
            double total = currentNuits * totalPersonnes * selectedLogement.getTarif_nuit() * coefficientPension;

            reservationToEdit.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservationToEdit.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservationToEdit.setMontant((float) total);
            reservationToEdit.setModalite(modalite);
            reservationToEdit.setAdultes(adultes);
            reservationToEdit.setEnfants(enfants);
            reservationToEdit.setNombreChambres(nombreChambres);
            reservationToEdit.setModeReservation(formuleSelectionnee);
            reservationToEdit.setRepartitionChambres(repartition);

            serviceReservation.modifier(reservationToEdit);
            showSweetAlert("Succès", "Réservation modifiée avec succès !", Alert.AlertType.INFORMATION);
            NavigationManager.loadView("/fxml/mesreservations.fxml", "Mes Réservations");
        } catch (SQLException e) {
            showSweetAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void envoyerEmailConfirmation(reservationlog reservation, String modalite, Status statut, Date dateLimite) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String startDate = dateArriveePicker.getValue().format(formatter);
            String endDate = dateDepartPicker.getValue().format(formatter);

            String message = "Bonjour " + currentUser.getPrenom() + ",\n\n";
            message += "Votre réservation a été enregistrée avec succès.\n\n";
            message += "📅 Dates: " + startDate + " au " + endDate + "\n";
            message += "🏠 Logement: " + selectedLogement.getNom() + "\n";
            message += "📍 Adresse: " + selectedLogement.getAdresse() + "\n";
            message += "👥 Personnes: " + reservation.getAdultes() + " adultes, " + reservation.getEnfants() + " enfants\n";
            message += "🍽️ Formule: " + nomsFormules.get(formuleSelectionnee) + "\n";
            message += "💰 Montant total: " + String.format("%.0f", reservation.getMontant()) + " DT\n";
            message += "💳 Modalité de paiement: " + modalite + "\n";
            message += "📊 Statut: " + statut + "\n";

            if ("En ligne".equals(modalite) && statut == Status.en_attente && dateLimite != null) {
                message += "\n⏳ Vous avez jusqu'au " + dateLimite + " pour finaliser votre paiement.\n";
                message += "Passé ce délai, votre réservation sera automatiquement annulée.\n";
            }

            message += "\nMerci de votre confiance !\n";
            message += "L'équipe de réservation";

            EmailService.sendSimpleEmail(currentUser.getEmail(), "Confirmation de réservation - ID: " + reservation.getId(), message);
        } catch (Exception e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
        }
    }

    private void resetErrorLabels() {
        if (arriveeErrorLabel != null) {
            arriveeErrorLabel.setVisible(false);
            arriveeErrorLabel.setText("");
        }
        if (departErrorLabel != null) {
            departErrorLabel.setVisible(false);
            departErrorLabel.setText("");
        }
        if (dureeErrorLabel != null) {
            dureeErrorLabel.setVisible(false);
            dureeErrorLabel.setText("");
        }
        if (modaliteErrorLabel != null) {
            modaliteErrorLabel.setVisible(false);
            modaliteErrorLabel.setText("");
        }
        if (capaciteErrorLabel != null) {
            capaciteErrorLabel.setVisible(false);
            capaciteErrorLabel.setText("");
        }
        if (pensionErrorLabel != null) {
            pensionErrorLabel.setVisible(false);
            pensionErrorLabel.setText("");
        }
    }

    private void clearDateErrors() {
        if (arriveeErrorLabel != null) arriveeErrorLabel.setVisible(false);
        if (departErrorLabel != null) departErrorLabel.setVisible(false);
        if (dureeErrorLabel != null) dureeErrorLabel.setVisible(false);
    }

    private void showSweetAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        if (type == Alert.AlertType.ERROR) {
            dialogPane.setStyle("-fx-background-color: #FEE; -fx-font-family: 'Segoe UI';");
        } else if (type == Alert.AlertType.INFORMATION) {
            dialogPane.setStyle("-fx-background-color: #EFE; -fx-font-family: 'Segoe UI';");
        } else if (type == Alert.AlertType.WARNING) {
            dialogPane.setStyle("-fx-background-color: #FED; -fx-font-family: 'Segoe UI';");
        }

        alert.showAndWait();
    }
}