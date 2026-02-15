package controllers.client;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Modele;
import org.example.entities.Vehicule;
import org.example.services.ModeleService;
import org.example.services.VehiculeService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CatalogueVoituresController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cbCarburant;
    @FXML private ComboBox<String> cbTri;
    @FXML private Slider sliderPrixMax;
    @FXML private Label lblPrixMax;
    @FXML private Label lblNombreResultats;
    @FXML private FlowPane flowPaneVoitures;
    @FXML private VBox vboxAucuneVoiture;
    @FXML private Button btnRetourAccueil;

    private VehiculeService vehiculeService;
    private ModeleService modeleService;
    private List<Vehicule> toutesLesVoitures;
    private List<Vehicule> voituresFiltrees;
    private Map<Integer, String> mapModeles; // idModele -> "Marque Modele"

    private LocalDate dateDebut;
    private LocalDate dateFin;

    public CatalogueVoituresController() {
        this.vehiculeService = new VehiculeService();
        this.modeleService = new ModeleService();
        this.mapModeles = new HashMap<>();
    }

    @FXML
    public void initialize() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Catalogue Client - Chargé");
        System.out.println("═══════════════════════════════════════════════");

        configurerFiltres();
        chargerModeles();
        chargerVoitures();
    }

    /**
     * Initialise la recherche avec des paramètres (appelé depuis AccueilClient)
     */
    public void initialiserRecherche(LocalDate debut, LocalDate fin, String typeVehicule) {
        this.dateDebut = debut;
        this.dateFin = fin;

        if (typeVehicule != null && !typeVehicule.equals("Tous les types")) {
            cbCarburant.setValue(typeVehicule);
        }

        System.out.println("🔍 Recherche initialisée : " + debut + " → " + fin + " | " + typeVehicule);
    }

    /**
     * Configure les filtres
     */
    private void configurerFiltres() {
        // Carburants
        cbCarburant.setItems(FXCollections.observableArrayList(
                "Tous", "Essence", "Diesel", "Hybride", "Électrique"
        ));
        cbCarburant.setValue("Tous");

        // Tri
        cbTri.setItems(FXCollections.observableArrayList(
                "Prix croissant",
                "Prix décroissant",
                "Modèle A-Z",
                "Kilométrage croissant"
        ));
        cbTri.setValue("Prix croissant");

        // Slider prix
        sliderPrixMax.setMin(0);
        sliderPrixMax.setMax(300);
        sliderPrixMax.setValue(300);
        lblPrixMax.setText("300 TND/jour");

        // Listeners
        txtRecherche.textProperty().addListener((obs, old, newVal) -> filtrerVoitures());
        cbCarburant.valueProperty().addListener((obs, old, newVal) -> filtrerVoitures());
        cbTri.valueProperty().addListener((obs, old, newVal) -> filtrerVoitures());
        sliderPrixMax.valueProperty().addListener((obs, old, newVal) -> {
            lblPrixMax.setText(String.format("%.0f TND/jour", newVal));
            filtrerVoitures();
        });
    }

    /**
     * Charge les modèles pour affichage
     */
    private void chargerModeles() {
        List<Modele> modeles = modeleService.getAllModeles();
        // Note: Vous devrez adapter selon votre base (JOIN avec marque)
        for (Modele m : modeles) {
            mapModeles.put(m.getIdModele(), m.getNomModele());
        }
    }

    /**
     * Charge toutes les voitures disponibles
     */
    private void chargerVoitures() {
        toutesLesVoitures = vehiculeService.getAllVehicules().stream()
                .filter(v -> "disponible".equals(v.getEtat()))
                .collect(Collectors.toList());

        System.out.println("✓ " + toutesLesVoitures.size() + " voiture(s) disponible(s) chargée(s)");
        filtrerVoitures();
    }

    /**
     * Filtre et affiche les voitures
     */
    private void filtrerVoitures() {
        String recherche = txtRecherche.getText().toLowerCase().trim();
        String carburant = cbCarburant.getValue();
        double prixMax = sliderPrixMax.getValue();

        // Filtrage
        voituresFiltrees = toutesLesVoitures.stream()
                .filter(v -> {
                    // Recherche textuelle
                    if (!recherche.isEmpty()) {
                        String modele = mapModeles.getOrDefault(v.getIdModele(), "").toLowerCase();
                        String immat = v.getImmatriculation().toLowerCase();
                        if (!modele.contains(recherche) && !immat.contains(recherche)) {
                            return false;
                        }
                    }

                    // Carburant
                    if (!carburant.equals("Tous") && !v.getCarburant().equals(carburant)) {
                        return false;
                    }

                    // Prix
                    if (v.getPrixParJour() > prixMax) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Tri
        String tri = cbTri.getValue();
        switch (tri) {
            case "Prix croissant":
                voituresFiltrees.sort((v1, v2) -> Double.compare(v1.getPrixParJour(), v2.getPrixParJour()));
                break;
            case "Prix décroissant":
                voituresFiltrees.sort((v1, v2) -> Double.compare(v2.getPrixParJour(), v1.getPrixParJour()));
                break;
            case "Modèle A-Z":
                voituresFiltrees.sort((v1, v2) -> {
                    String m1 = mapModeles.getOrDefault(v1.getIdModele(), "");
                    String m2 = mapModeles.getOrDefault(v2.getIdModele(), "");
                    return m1.compareToIgnoreCase(m2);
                });
                break;
            case "Kilométrage croissant":
                voituresFiltrees.sort((v1, v2) -> Integer.compare(v1.getKilometrage(), v2.getKilometrage()));
                break;
        }

        afficherVoitures();
        lblNombreResultats.setText(voituresFiltrees.size() + " voiture(s) disponible(s)");
    }

    /**
     * Affiche les voitures sous forme de cards
     */
    private void afficherVoitures() {
        flowPaneVoitures.getChildren().clear();

        if (voituresFiltrees.isEmpty()) {
            vboxAucuneVoiture.setVisible(true);
            vboxAucuneVoiture.setManaged(true);
            flowPaneVoitures.setVisible(false);
            return;
        }

        vboxAucuneVoiture.setVisible(false);
        vboxAucuneVoiture.setManaged(false);
        flowPaneVoitures.setVisible(true);

        for (Vehicule voiture : voituresFiltrees) {
            VBox card = creerCardVoiture(voiture);
            flowPaneVoitures.getChildren().add(card);
        }
    }

    /**
     * Crée une card attractive pour une voiture
     */
    private VBox creerCardVoiture(Vehicule voiture) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(280);
        card.setPrefHeight(380);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );

        // Image de la voiture
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        String imageUrl = (voiture.getPhoto() == null || voiture.getPhoto().trim().isEmpty())
                ? "https://via.placeholder.com/400x250/3498db/ffffff?text=Voiture"
                : voiture.getPhoto();

        try {
            Image image = new Image(imageUrl, true);
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/400x250/3498db/ffffff?text=Voiture", true));
        }

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setStyle("-fx-background-radius: 16 16 0 0;");
        imageContainer.setMaxHeight(180);

        // Badge carburant
        Label badgeCarburant = new Label(getIconeCarburant(voiture.getCarburant()) + " " + voiture.getCarburant());
        badgeCarburant.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 12;"
        );
        StackPane.setAlignment(badgeCarburant, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeCarburant, new javafx.geometry.Insets(10));
        imageContainer.getChildren().add(badgeCarburant);

        // Infos voiture
        VBox infos = new VBox(8);
        infos.setPadding(new javafx.geometry.Insets(0, 15, 15, 15));

        String nomModele = mapModeles.getOrDefault(voiture.getIdModele(), "Modèle inconnu");
        Label lblModele = new Label(nomModele);
        lblModele.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;"
        );

        Label lblDetails = new Label(voiture.getAnnee() + " • " + voiture.getCouleur());
        lblDetails.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #7f8c8d;"
        );

        HBox caracteristiques = new HBox(15);
        Label lblKm = new Label("📏 " + String.format("%,d", voiture.getKilometrage()) + " km");
        lblKm.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        Label lblImmat = new Label("🔖 " + voiture.getImmatriculation());
        lblImmat.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        caracteristiques.getChildren().addAll(lblKm, lblImmat);

        // Prix et bouton
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        VBox prixBox = new VBox(2);
        Label lblPrix = new Label(String.format("%.2f TND", voiture.getPrixParJour()));
        lblPrix.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #27ae60;"
        );
        Label lblParJour = new Label("par jour");
        lblParJour.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        prixBox.getChildren().addAll(lblPrix, lblParJour);

        Button btnReserver = new Button("Réserver");
        btnReserver.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        btnReserver.setOnAction(e -> ouvrirReservation(voiture));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().addAll(prixBox, spacer, btnReserver);

        infos.getChildren().addAll(lblModele, lblDetails, caracteristiques, footer);
        card.getChildren().addAll(imageContainer, infos);

        // Animations hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-color: #3498db;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 16;" +
                            "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 20, 0, 0, 8);" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 16;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Animation d'apparition
        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        return card;
    }

    /**
     * Icône selon le type de carburant
     */
    private String getIconeCarburant(String carburant) {
        switch (carburant) {
            case "Essence": return "⛽";
            case "Diesel": return "🛢️";
            case "Hybride": return "🔋";
            case "Électrique": return "⚡";
            default: return "🚗";
        }
    }

    /**
     * Ouvre l'interface de réservation pour une voiture
     */
    private void ouvrirReservation(Vehicule voiture) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/ReservationVoiture.fxml"));
            Parent root = loader.load();

            ReservationVoitureController controller = loader.getController();
            controller.initialiserReservation(voiture, dateDebut, dateFin);

            Stage stage = (Stage) flowPaneVoitures.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Horizia - Réservation");

        } catch (IOException e) {
            System.err.println("Erreur chargement réservation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retour à l'accueil
     */
    @FXML
    private void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/AccueilClient.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRetourAccueil.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.setTitle("Horizia - Location de Voitures");

        } catch (IOException e) {
            System.err.println("Erreur retour accueil : " + e.getMessage());
        }
    }

    /**
     * Réinitialise les filtres
     */
    @FXML
    private void reinitialiserFiltres() {
        txtRecherche.clear();
        cbCarburant.setValue("Tous");
        cbTri.setValue("Prix croissant");
        sliderPrixMax.setValue(300);
    }
}