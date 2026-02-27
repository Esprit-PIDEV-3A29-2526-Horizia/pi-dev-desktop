package controllers.client;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.services.Dashboardservice;
import org.example.services.VehiculeService;

import java.io.IOException;
import java.time.LocalDate;

public class AccueilClientController {

    // ─── FXML fields ─────────────────────────────────────────────
    @FXML private DatePicker       dpDateDebut;
    @FXML private DatePicker       dpDateFin;
    @FXML private ComboBox<String> cbTypeVehicule;
    @FXML private Button           btnRechercher;
    @FXML private VBox             heroSection;
    @FXML private VBox             featuresSection;
    @FXML private Label            lblStatVehicules;
    @FXML private Label            lblStatLocations;

    // ─── Référence racine pour getStage() ────────────────────────
    // La navbar est toujours le premier enfant du VBox racine.
    // On garde une référence sur un élément garanti d'être dans le FXML.
    @FXML private HBox navbar;  // fx:id="navbar" doit être ajouté dans le FXML si absent
    // Sinon on utilise heroSection qui est aussi garanti présent.

    // ─── Services ────────────────────────────────────────────────
    private final VehiculeService  vehiculeService  = new VehiculeService();
    private final Dashboardservice dashboardservice = new Dashboardservice();

    // ─── Stage stocké à l'initialize() ──────────────────────────
    // C'est la solution la plus robuste : on capture le Stage dès initialize()
    // car à ce moment les composants sont déjà dans la scène.
    private Stage cachedStage = null;

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  HORIZIA - Interface Client Chargée");
        System.out.println("═══════════════════════════════════════════════");

        // Capturer le Stage dès que les composants sont disponibles
        // On utilise un listener sur la scene property du heroSection
        // car au moment d'initialize(), la scene peut ne pas encore être attachée.
        if (heroSection != null) {
            heroSection.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && cachedStage == null) {
                    cachedStage = (Stage) newScene.getWindow();
                }
            });
        }

        configurerRecherche();
        chargerStats();
        animerEntree();
    }

    private void configurerRecherche() {
        if (cbTypeVehicule != null) {
            cbTypeVehicule.getItems().addAll(
                    "Tous les types", "Essence", "Diesel", "Hybride", "Électrique"
            );
            cbTypeVehicule.setValue("Tous les types");
        }
        if (dpDateDebut != null) dpDateDebut.setValue(LocalDate.now().plusDays(1));
        if (dpDateFin   != null) dpDateFin.setValue(LocalDate.now().plusDays(3));
        if (dpDateDebut != null && dpDateFin != null) {
            dpDateDebut.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null && dpDateFin.getValue() != null
                        && newVal.isAfter(dpDateFin.getValue())) {
                    dpDateFin.setValue(newVal.plusDays(1));
                }
            });
        }
    }

    private void chargerStats() {
        try {
            int nbDispo   = dashboardservice.getNombreVehiculesDisponibles();
            int nbActives = dashboardservice.getNombreLocationsActives();
            if (lblStatVehicules != null) lblStatVehicules.setText(String.valueOf(nbDispo));
            if (lblStatLocations != null) lblStatLocations.setText(String.valueOf(nbActives));
        } catch (Exception e) {
            System.err.println("[AccueilClient] Erreur stats: " + e.getMessage());
            if (lblStatVehicules != null) lblStatVehicules.setText("—");
            if (lblStatLocations != null) lblStatLocations.setText("—");
        }
    }

    private void animerEntree() {
        if (heroSection != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(900), heroSection);
            fade.setFromValue(0); fade.setToValue(1); fade.play();
        }
        if (featuresSection != null) {
            TranslateTransition slide = new TranslateTransition(Duration.millis(800), featuresSection);
            slide.setFromY(40); slide.setToY(0);
            FadeTransition fade2 = new FadeTransition(Duration.millis(800), featuresSection);
            fade2.setFromValue(0); fade2.setToValue(1);
            slide.play(); fade2.play();
        }
    }

    // ─── ACTIONS FXML ────────────────────────────────────────────

    @FXML
    private void rechercherVoitures() {
        LocalDate debut = dpDateDebut != null ? dpDateDebut.getValue() : null;
        LocalDate fin   = dpDateFin   != null ? dpDateFin.getValue()   : null;
        String    type  = cbTypeVehicule != null ? cbTypeVehicule.getValue() : "Tous les types";

        if (debut == null || fin == null) {
            afficherAlerte("Dates requises", "Veuillez sélectionner les dates de location.", Alert.AlertType.WARNING);
            return;
        }
        if (debut.isBefore(LocalDate.now())) {
            afficherAlerte("Date invalide", "La date de début ne peut pas être dans le passé.", Alert.AlertType.WARNING);
            return;
        }
        if (fin.isBefore(debut)) {
            afficherAlerte("Dates invalides", "La date de fin doit être après la date de début.", Alert.AlertType.WARNING);
            return;
        }
        ouvrirCatalogue(debut, fin, type);
    }

    @FXML private void voirToutesLesVoitures()  { ouvrirCatalogue(null, null, "Tous les types"); }
    @FXML private void scrollToTop()             { /* déjà sur la page d'accueil */ }
    @FXML private void allerCatalogue()          { ouvrirCatalogue(null, null, "Tous les types"); }
    @FXML private void allerMesReservations()    { ouvrirMesReservations(); }

    @FXML
    private void ouvrirMesReservations() {
        navigerVers("/views/client/MesReservations.fxml", "Horizia - Mes Réservations", 1400, 800);
    }

    // ─── NAVIGATION ──────────────────────────────────────────────

    private void ouvrirCatalogue(LocalDate debut, LocalDate fin, String typeVehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/CatalogueVoitures.fxml"));
            Parent root = loader.load();
            CatalogueVoituresController controller = loader.getController();
            controller.initialiserRecherche(debut, fin, typeVehicule);

            Stage stage = getStage();
            if (stage == null) {
                System.err.println("[AccueilClient] Stage null — impossible d'ouvrir le catalogue");
                return;
            }
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Horizia - Catalogue de Voitures");

        } catch (IOException e) {
            System.err.println("[AccueilClient] Erreur catalogue: " + e.getMessage());
            afficherAlerte("Erreur", "Impossible de charger le catalogue.", Alert.AlertType.ERROR);
        }
    }

    private void navigerVers(String fxmlPath, String titre, double w, double h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = getStage();
            if (stage == null) {
                System.err.println("[AccueilClient] Stage null — impossible de naviguer vers " + fxmlPath);
                return;
            }
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(titre);
        } catch (IOException e) {
            System.err.println("[AccueilClient] Erreur navigation: " + e.getMessage());
            afficherAlerte("Erreur", "Impossible de charger la page.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Récupère le Stage.
     * Stratégie 1 : Stage mis en cache dès la première apparition dans la scène.
     * Stratégie 2 : Cherche parmi les composants FXML.
     */
    private Stage getStage() {
        // Stratégie 1 — Stage en cache (le plus fiable)
        if (cachedStage != null) return cachedStage;

        // Stratégie 2 — Chercher dans les composants disponibles
        if (heroSection != null && heroSection.getScene() != null)
            return cachedStage = (Stage) heroSection.getScene().getWindow();
        if (btnRechercher != null && btnRechercher.getScene() != null)
            return cachedStage = (Stage) btnRechercher.getScene().getWindow();
        if (featuresSection != null && featuresSection.getScene() != null)
            return cachedStage = (Stage) featuresSection.getScene().getWindow();
        if (lblStatVehicules != null && lblStatVehicules.getScene() != null)
            return cachedStage = (Stage) lblStatVehicules.getScene().getWindow();

        System.err.println("[AccueilClient] Impossible de récupérer le Stage — aucun composant dans la scène.");
        return null;
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}