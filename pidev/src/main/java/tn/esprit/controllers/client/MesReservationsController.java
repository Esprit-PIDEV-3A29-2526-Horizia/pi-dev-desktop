package tn.esprit.controllers.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Location;
import tn.esprit.entities.Vehicule;
import tn.esprit.entities.Modele;
import tn.esprit.services.LocationService;
import tn.esprit.services.QRCodeService;
import tn.esprit.services.VehiculeService;
import tn.esprit.services.ModeleService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ✅ Contrôleur complet de la page "Mes Réservations"
 * Avec filtres avancés et affichage amélioré
 * CORRECTION: Gestion des composants FXML optionnels
 */
public class MesReservationsController implements Initializable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_COMPLET = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH);

    // ─── FXML ────────────────────────────────────────────────────
    @FXML private TextField txtCinRecherche;
    @FXML private Label lblErreur;
    @FXML private VBox vboxResultats;
    @FXML private Label lblNbResultats;
    @FXML private VBox vboxListeReservations;
    @FXML private VBox vboxVide;

    // ✅ FILTRES (optionnels - peuvent être null si non présents dans le FXML)
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private ComboBox<String> comboTri;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private Button btnAppliquerFiltres;
    @FXML private Button btnReinitialiserFiltres;

    private LocationService locationService = new LocationService();
    private VehiculeService vehiculeService = new VehiculeService();
    private ModeleService modeleService = new ModeleService();

    private List<Location> toutesLesReservations = new java.util.ArrayList<>();
    private String cinRecherche = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Permettre la recherche avec ENTER
        if (txtCinRecherche != null) {
            txtCinRecherche.setOnAction(e -> rechercherReservations());
        }

        configurerFiltres();
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIGURATION DES FILTRES (avec vérification null)
    // ─────────────────────────────────────────────────────────────

    private void configurerFiltres() {
        if (comboFiltreStatut != null) {
            comboFiltreStatut.getItems().addAll(
                    "Tous les statuts",
                    "📅 À venir",
                    "🚗 En cours",
                    "✅ Terminées",
                    "❌ Annulées"
            );
            comboFiltreStatut.setValue("Tous les statuts");

            // Ajouter listener seulement si le composant existe
            comboFiltreStatut.valueProperty().addListener((obs, old, newVal) -> {
                if (!toutesLesReservations.isEmpty()) appliquerFiltres();
            });
        }

        if (comboTri != null) {
            comboTri.getItems().addAll(
                    "Plus récentes",
                    "Plus anciennes",
                    "Montant croissant",
                    "Montant décroissant",
                    "Durée croissante",
                    "Durée décroissante"
            );
            comboTri.setValue("Plus récentes");

            comboTri.valueProperty().addListener((obs, old, newVal) -> {
                if (!toutesLesReservations.isEmpty()) appliquerFiltres();
            });
        }

        if (dpDateDebut != null) {
            dpDateDebut.setValue(null);
        }
        if (dpDateFin != null) {
            dpDateFin.setValue(null);
        }

        if (btnAppliquerFiltres != null) {
            btnAppliquerFiltres.setOnAction(e -> appliquerFiltres());
        }

        if (btnReinitialiserFiltres != null) {
            btnReinitialiserFiltres.setOnAction(e -> reinitialiserFiltres());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RECHERCHE PRINCIPALE
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void rechercherReservations() {
        String cin = txtCinRecherche.getText().trim().toUpperCase().replaceAll("[\\s-]", "");

        if (cin.isEmpty()) {
            lblErreur.setText("⚠ Veuillez saisir votre CIN ou numéro de Passport");
            masquerResultats();
            return;
        }

        lblErreur.setText("");
        this.cinRecherche = cin;

        // Rechercher dans toutes les locations
        List<Location> toutes = locationService.getAllLocations();
        toutesLesReservations = toutes.stream()
                .filter(l -> l.getClientCin() != null &&
                        l.getClientCin().trim().toUpperCase().replaceAll("[\\s-]", "").equals(cin))
                .collect(Collectors.toList());

        if (toutesLesReservations.isEmpty()) {
            masquerResultats();
            vboxVide.setVisible(true);
            vboxVide.setManaged(true);
        } else {
            vboxVide.setVisible(false);
            vboxVide.setManaged(false);
            appliquerFiltres();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // APPLICATION DES FILTRES (avec vérification null)
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void appliquerFiltres() {
        if (toutesLesReservations.isEmpty()) return;

        List<Location> filtrees = new java.util.ArrayList<>(toutesLesReservations);

        // ✅ FILTRE PAR STATUT - seulement si le composant existe
        if (comboFiltreStatut != null) {
            String filtreStatut = comboFiltreStatut.getValue();
            if (filtreStatut != null && !"Tous les statuts".equals(filtreStatut)) {
                LocalDate now = LocalDate.now();

                filtrees = filtrees.stream()
                        .filter(l -> {
                            LocalDate debut = l.getDateDebut() != null ?
                                    l.getDateDebut().toLocalDateTime().toLocalDate() : null;
                            LocalDate fin = l.getDateFinPrev() != null ?
                                    l.getDateFinPrev().toLocalDateTime().toLocalDate() : null;

                            switch (filtreStatut) {
                                case "📅 À venir":
                                    return debut != null && debut.isAfter(now);
                                case "🚗 En cours":
                                    return debut != null && fin != null &&
                                            !debut.isAfter(now) && !fin.isBefore(now) &&
                                            !"terminée".equals(l.getStatut()) &&
                                            !"annulée".equals(l.getStatut()) &&
                                            !"no_show".equals(l.getStatut());
                                case "✅ Terminées":
                                    return fin != null && fin.isBefore(now) &&
                                            "terminée".equals(l.getStatut());
                                case "❌ Annulées":
                                    return "annulée".equals(l.getStatut()) ||
                                            "no_show".equals(l.getStatut());
                                default:
                                    return true;
                            }
                        })
                        .collect(Collectors.toList());
            }
        }

        // ✅ FILTRE PAR PLAGE DE DATES - seulement si les composants existent
        if (dpDateDebut != null || dpDateFin != null) {
            LocalDate debutFiltre = (dpDateDebut != null && dpDateDebut.getValue() != null)
                    ? dpDateDebut.getValue() : LocalDate.MIN;
            LocalDate finFiltre = (dpDateFin != null && dpDateFin.getValue() != null)
                    ? dpDateFin.getValue() : LocalDate.MAX;

            if (debutFiltre != LocalDate.MIN || finFiltre != LocalDate.MAX) {
                filtrees = filtrees.stream()
                        .filter(l -> {
                            LocalDate debutLoc = l.getDateDebut() != null ?
                                    l.getDateDebut().toLocalDateTime().toLocalDate() : null;
                            return debutLoc != null &&
                                    (debutLoc.isEqual(debutFiltre) || debutLoc.isAfter(debutFiltre)) &&
                                    (debutLoc.isEqual(finFiltre) || debutLoc.isBefore(finFiltre));
                        })
                        .collect(Collectors.toList());
            }
        }

        // ✅ TRI - seulement si le composant existe
        if (comboTri != null) {
            String tri = comboTri.getValue();
            if (tri != null) {
                switch (tri) {
                    case "Plus récentes":
                        filtrees.sort((l1, l2) -> {
                            if (l1.getDateDebut() == null) return 1;
                            if (l2.getDateDebut() == null) return -1;
                            return l2.getDateDebut().compareTo(l1.getDateDebut());
                        });
                        break;
                    case "Plus anciennes":
                        filtrees.sort((l1, l2) -> {
                            if (l1.getDateDebut() == null) return 1;
                            if (l2.getDateDebut() == null) return -1;
                            return l1.getDateDebut().compareTo(l2.getDateDebut());
                        });
                        break;
                    case "Montant croissant":
                        filtrees.sort(Comparator.comparingDouble(Location::getMontantTotal));
                        break;
                    case "Montant décroissant":
                        filtrees.sort((l1, l2) -> Double.compare(l2.getMontantTotal(), l1.getMontantTotal()));
                        break;
                    case "Durée croissante":
                        filtrees.sort((l1, l2) -> {
                            long d1 = calculerDuree(l1);
                            long d2 = calculerDuree(l2);
                            return Long.compare(d1, d2);
                        });
                        break;
                    case "Durée décroissante":
                        filtrees.sort((l1, l2) -> {
                            long d1 = calculerDuree(l1);
                            long d2 = calculerDuree(l2);
                            return Long.compare(d2, d1);
                        });
                        break;
                }
            }
        }

        afficherReservations(filtrees);
    }

    private long calculerDuree(Location loc) {
        if (loc.getDateDebut() == null || loc.getDateFinPrev() == null) return 0;
        return java.time.Duration.between(
                loc.getDateDebut().toLocalDateTime(),
                loc.getDateFinPrev().toLocalDateTime()
        ).toDays();
    }

    @FXML
    private void reinitialiserFiltres() {
        if (comboFiltreStatut != null) comboFiltreStatut.setValue("Tous les statuts");
        if (comboTri != null) comboTri.setValue("Plus récentes");
        if (dpDateDebut != null) dpDateDebut.setValue(null);
        if (dpDateFin != null) dpDateFin.setValue(null);

        appliquerFiltres();
    }

    private void masquerResultats() {
        vboxResultats.setVisible(false);
        vboxResultats.setManaged(false);
    }

    // ─────────────────────────────────────────────────────────────
    // AFFICHAGE DES RÉSERVATIONS
    // ─────────────────────────────────────────────────────────────

    private void afficherReservations(List<Location> locations) {
        vboxListeReservations.getChildren().clear();

        lblNbResultats.setText(locations.size() + " réservation(s) trouvée(s)");

        for (Location loc : locations) {
            VBox card = creerCarteReservation(loc);
            vboxListeReservations.getChildren().add(card);
        }

        vboxResultats.setVisible(true);
        vboxResultats.setManaged(true);
    }

    /**
     * Crée une carte visuelle pour chaque réservation
     */
    private VBox creerCarteReservation(Location loc) {
        // Résoudre le nom véhicule
        String nomVehicule = "Véhicule #" + loc.getIdVehicule();
        Vehicule v = vehiculeService.getVehiculeById(loc.getIdVehicule());
        if (v != null) {
            Modele m = modeleService.getModeleById(v.getIdModele());
            nomVehicule = (m != null ? m.getNomModele() : "Véhicule") + " — " + v.getImmatriculation();
        }
        final String nomVehiculeF = nomVehicule;

        // Couleur selon statut
        String couleurStatut = getCouleurStatut(loc.getStatut());
        String emojiStatut = getEmojiStatut(loc.getStatut());

        // Card principale
        HBox card = new HBox(25);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 25;" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;"
        );
        card.setAlignment(Pos.CENTER_LEFT);

        // ─── Partie gauche : infos ───────────────────────────────
        VBox infos = new VBox(12);
        HBox.setHgrow(infos, Priority.ALWAYS);

        // Header : numéro + statut
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblNum = new Label("Réservation  #" + loc.getIdLocation());
        lblNum.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblStatut = new Label(emojiStatut + "  " + loc.getStatut().toUpperCase());
        lblStatut.setStyle(
                "-fx-background-color: " + couleurStatut + "22;" +
                        "-fx-text-fill: " + couleurStatut + ";" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-padding: 5 14; -fx-background-radius: 12;"
        );
        header.getChildren().addAll(lblNum, lblStatut);

        // Détails
        Label lblVehiculeL = new Label("🚗  " + nomVehiculeF);
        lblVehiculeL.setStyle("-fx-font-size: 15px; -fx-text-fill: #bdc3c7;");

        String periode = "📅  ";
        if (loc.getDateDebut() != null && loc.getDateFinPrev() != null) {
            periode += loc.getDateDebut().toLocalDateTime().format(FMT) +
                    "  →  " + loc.getDateFinPrev().toLocalDateTime().format(FMT);
        } else {
            periode += "Dates non disponibles";
        }
        Label lblPer = new Label(periode);
        lblPer.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label lblMontantL = new Label(String.format("💰  %.3f TND  (avance: %.3f TND)",
                loc.getMontantTotal(), loc.getAvance()));
        lblMontantL.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");

        Label lblCin = new Label("🪪  CIN: " + loc.getClientCin() + "  •  " + loc.getClientNomComplet());
        lblCin.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");

        infos.getChildren().addAll(header, lblVehiculeL, lblPer, lblMontantL, lblCin);

        // ─── Partie droite : QR Code miniature ──────────────────
        VBox qrBox = new VBox(10);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setMinWidth(140);

        StackPane qrContainer = new StackPane();
        qrContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 8;");
        qrContainer.setMinWidth(120);
        qrContainer.setMinHeight(120);

        ImageView imgQR = new ImageView();
        imgQR.setFitWidth(110);
        imgQR.setFitHeight(110);
        imgQR.setPreserveRatio(true);

        Label lblGenQR = new Label("⏳");
        lblGenQR.setStyle("-fx-font-size: 24px;");

        qrContainer.getChildren().addAll(lblGenQR, imgQR);

        Button btnQR = new Button("Voir le QR Code");
        btnQR.setStyle(
                "-fx-background-color: rgba(52,152,219,0.15);" +
                        "-fx-text-fill: #3498db; -fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(52,152,219,0.3); -fx-border-width: 1; -fx-border-radius: 8;"
        );

        String dateDebutStr = loc.getDateDebut() != null
                ? loc.getDateDebut().toLocalDateTime().format(FMT) : "?";
        String dateFinStr = loc.getDateFinPrev() != null
                ? loc.getDateFinPrev().toLocalDateTime().format(FMT) : "?";

        btnQR.setOnAction(e -> afficherQRCodePopup(loc, nomVehiculeF, dateDebutStr, dateFinStr));

        qrBox.getChildren().addAll(qrContainer, btnQR);

        // Générer QR miniature en arrière-plan
        new Thread(() -> {
            Image qrImg = QRCodeService.genererQRCodeLocation(
                    loc.getIdLocation(),
                    loc.getClientNomComplet(),
                    nomVehiculeF, dateDebutStr, dateFinStr
            );
            Platform.runLater(() -> {
                if (qrImg != null) {
                    imgQR.setImage(qrImg);
                    lblGenQR.setVisible(false);
                }
            });
        }).start();

        card.getChildren().addAll(infos, qrBox);

        // Wrapper
        VBox wrapper = new VBox(card);
        return wrapper;
    }

    /**
     * Affiche le QR Code en grand dans une popup
     */
    private void afficherQRCodePopup(Location loc, String nomVehicule,
                                     String dateDebut, String dateFin) {
        // Créer un dialog personnalisé
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("QR Code — Réservation #" + loc.getIdLocation());
        dialog.setHeaderText("QR Code de votre réservation");

        // Style du dialog pour correspondre au thème
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0a0f1e;");
        dialogPane.getScene().getWindow().setOnCloseRequest(e -> dialog.close());

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 20;");

        ImageView imgQR = new ImageView();
        imgQR.setFitWidth(300);
        imgQR.setFitHeight(300);
        imgQR.setPreserveRatio(true);

        Label loading = new Label("Génération du QR Code...");
        loading.setStyle("-fx-text-fill: #7f8c8d;");

        Label info = new Label("Présentez ce QR Code lors du retrait du véhicule");
        info.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Générer en arrière-plan
        new Thread(() -> {
            Image qrImg = QRCodeService.genererQRCodeLocation(
                    loc.getIdLocation(), loc.getClientNomComplet(),
                    nomVehicule, dateDebut, dateFin
            );
            Platform.runLater(() -> {
                if (qrImg != null) {
                    imgQR.setImage(qrImg);
                    loading.setVisible(false);
                }
            });
        }).start();

        content.getChildren().addAll(imgQR, loading, info);
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────
    // UTILITAIRES STATUT
    // ─────────────────────────────────────────────────────────────

    private String getCouleurStatut(String statut) {
        if (statut == null) return "#95a5a6";
        return switch (statut.toLowerCase()) {
            case "réservée" -> "#3498db";
            case "en_cours" -> "#27ae60";
            case "terminée" -> "#95a5a6";
            case "annulée"  -> "#e74c3c";
            case "no_show"  -> "#e67e22";
            default         -> "#95a5a6";
        };
    }

    private String getEmojiStatut(String statut) {
        if (statut == null) return "❓";
        return switch (statut.toLowerCase()) {
            case "réservée" -> "📅";
            case "en_cours" -> "🚗";
            case "terminée" -> "✅";
            case "annulée"  -> "❌";
            case "no_show"  -> "⚠️";
            default         -> "❓";
        };
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void retourAccueil() {
        navigerVers("/views/client/AccueilClient.fxml", "Horizia - Location de Voitures", 1400, 800);
    }

    @FXML
    private void retourCatalogue() {
        navigerVers("/views/client/CatalogueVoitures.fxml", "Horizia - Catalogue", 1400, 800);
    }

    @FXML
    private void allerPlanning() {
        navigerVers("/views/client/ClientPlanning.fxml", "Horizia - Planning", 1200, 800);
    }

    private void navigerVers(String fxmlPath, String titre, double w, double h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) txtCinRecherche.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(titre);
        } catch (IOException e) {
            System.err.println("[MesReservations] Erreur navigation: " + e.getMessage());
            afficherAlerte("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}