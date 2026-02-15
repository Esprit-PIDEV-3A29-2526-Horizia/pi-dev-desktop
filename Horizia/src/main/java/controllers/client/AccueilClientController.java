package controllers.client;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.services.VehiculeService;

import java.io.IOException;
import java.time.LocalDate;

public class AccueilClientController {

    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private ComboBox<String> cbTypeVehicule;
    @FXML private Button btnRechercher;
    @FXML private VBox heroSection;
    @FXML private VBox featuresSection;

    private VehiculeService vehiculeService;

    public AccueilClientController() {
        this.vehiculeService = new VehiculeService();
    }

    @FXML
    public void initialize() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  HORIZIA - Interface Client Chargée");
        System.out.println("═══════════════════════════════════════════════");

        configurerRecherche();
        animerEntree();
    }

    /**
     * Configure les champs de recherche rapide
     */
    private void configurerRecherche() {
        // Types de véhicules
        cbTypeVehicule.getItems().addAll(
                "Tous les types",
                "Essence",
                "Diesel",
                "Hybride",
                "Électrique"
        );
        cbTypeVehicule.setValue("Tous les types");

        // Dates par défaut
        dpDateDebut.setValue(LocalDate.now().plusDays(1));
        dpDateFin.setValue(LocalDate.now().plusDays(3));

        // Validation dates
        dpDateDebut.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && dpDateFin.getValue() != null) {
                if (newVal.isAfter(dpDateFin.getValue())) {
                    dpDateFin.setValue(newVal.plusDays(1));
                }
            }
        });
    }

    /**
     * Animations d'entrée de la page
     */
    private void animerEntree() {
        // Animation fade-in du hero
        if (heroSection != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(1000), heroSection);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }

        // Animation slide-up des features
        if (featuresSection != null) {
            TranslateTransition slide = new TranslateTransition(Duration.millis(800), featuresSection);
            slide.setFromY(50);
            slide.setToY(0);

            FadeTransition fade = new FadeTransition(Duration.millis(800), featuresSection);
            fade.setFromValue(0);
            fade.setToValue(1);

            slide.play();
            fade.play();
        }
    }

    /**
     * Recherche rapide de voitures
     */
    @FXML
    private void rechercherVoitures() {
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin = dpDateFin.getValue();
        String typeVehicule = cbTypeVehicule.getValue();

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

        // Redirection vers le catalogue avec filtres
        ouvrirCatalogue(debut, fin, typeVehicule);
    }

    /**
     * Ouvre le catalogue complet sans filtres
     */
    @FXML
    private void voirToutesLesVoitures() {
        ouvrirCatalogue(null, null, "Tous les types");
    }

    /**
     * Ouvre l'interface du catalogue
     */
    private void ouvrirCatalogue(LocalDate debut, LocalDate fin, String typeVehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/CatalogueVoitures.fxml"));
            Parent root = loader.load();

            // Passer les paramètres de recherche au catalogue
            CatalogueVoituresController controller = loader.getController();
            controller.initialiserRecherche(debut, fin, typeVehicule);

            // Remplacer la scène actuelle
            Stage stage = (Stage) btnRechercher.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.setTitle("Horizia - Catalogue de Voitures");

        } catch (IOException e) {
            System.err.println("Erreur chargement catalogue : " + e.getMessage());
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger le catalogue.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Affiche une alerte
     */
    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}