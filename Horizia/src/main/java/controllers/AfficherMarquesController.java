package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.entities.Marque;
import org.example.services.MarqueService;

import java.util.List;
import java.util.stream.Collectors;

public class AfficherMarquesController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;
    @FXML private FlowPane flowPaneMarques;
    @FXML private Label lblNombreMarques;
    @FXML private Label lblNombreResultats;
    @FXML private Label lblResultats;
    @FXML private Label lblFiltreActif;
    @FXML private VBox vboxAucuneMarque;

    private MarqueService marqueService;
    private List<Marque> toutesLesMarques;
    private List<Marque> marquesFiltrees;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        marqueService = new MarqueService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Afficher Marques - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        // Initialiser le ComboBox de tri
        comboTri.getItems().addAll(
                "A → Z (alphabétique)",
                "Z → A (inverse)",
                "Récents d'abord"
        );
        comboTri.setValue("A → Z (alphabétique)");

        // Listeners pour recherche et tri en temps réel
        txtRecherche.textProperty().addListener((obs, old, nouv) -> filtrerMarques());
        comboTri.valueProperty().addListener((obs, old, nouv) -> filtrerMarques());

        // Charger les marques
        chargerMarques();
    }

    /**
     * Charge toutes les marques depuis la base de données
     */
    private void chargerMarques() {
        System.out.println("→ Chargement des marques...");

        toutesLesMarques = marqueService.getAllMarquesAlphabetique();

        System.out.println("✓ " + toutesLesMarques.size() + " marque(s) chargée(s)");

        // Mettre à jour le compteur global
        lblNombreMarques.setText(String.valueOf(toutesLesMarques.size()));

        // Afficher les marques
        filtrerMarques();
    }

    /**
     * Filtre et affiche les marques selon les critères de recherche et tri
     */
    private void filtrerMarques() {
        String recherche = txtRecherche.getText().toLowerCase().trim();
        String triSelectionne = comboTri.getValue();

        // ═══════════════════════════════════════════════════════
        // ÉTAPE 1 : Filtrer par recherche
        // ═══════════════════════════════════════════════════════
        if (recherche.isEmpty()) {
            marquesFiltrees = toutesLesMarques;
            lblFiltreActif.setText("Tous");
            lblResultats.setText("Affichage de toutes les marques");
        } else {
            marquesFiltrees = toutesLesMarques.stream()
                    .filter(m -> m.getNomMarque().toLowerCase().contains(recherche))
                    .collect(Collectors.toList());

            lblFiltreActif.setText("Recherche : " + txtRecherche.getText());
            lblResultats.setText("Résultats pour : \"" + txtRecherche.getText() + "\"");
        }

        // ═══════════════════════════════════════════════════════
        // ÉTAPE 2 : Trier les résultats
        // ═══════════════════════════════════════════════════════
        if (triSelectionne != null) {
            switch (triSelectionne) {
                case "A → Z (alphabétique)":
                    marquesFiltrees.sort((m1, m2) ->
                            m1.getNomMarque().compareToIgnoreCase(m2.getNomMarque()));
                    break;

                case "Z → A (inverse)":
                    marquesFiltrees.sort((m1, m2) ->
                            m2.getNomMarque().compareToIgnoreCase(m1.getNomMarque()));
                    break;

                case "Récents d'abord":
                    marquesFiltrees.sort((m1, m2) ->
                            Integer.compare(m2.getIdMarque(), m1.getIdMarque()));
                    break;
            }
        }

        // ═══════════════════════════════════════════════════════
        // ÉTAPE 3 : Afficher les résultats
        // ═══════════════════════════════════════════════════════
        afficherMarques();

        // Mettre à jour le compteur de résultats
        String texteResultat = marquesFiltrees.size() + " résultat" + (marquesFiltrees.size() > 1 ? "s" : "");
        lblNombreResultats.setText(texteResultat);

        System.out.println("🔍 Filtrage : " + marquesFiltrees.size() + " marque(s) affichée(s)");
    }

    /**
     * Affiche les marques sous forme de cards élégantes
     */
    private void afficherMarques() {
        flowPaneMarques.getChildren().clear();

        if (marquesFiltrees.isEmpty()) {
            // Afficher le message "Aucune marque"
            vboxAucuneMarque.setVisible(true);
            vboxAucuneMarque.setManaged(true);
            flowPaneMarques.setVisible(false);
            flowPaneMarques.setManaged(false);
            return;
        } else {
            vboxAucuneMarque.setVisible(false);
            vboxAucuneMarque.setManaged(false);
            flowPaneMarques.setVisible(true);
            flowPaneMarques.setManaged(true);
        }

        // ═══════════════════════════════════════════════════════
        // Créer une card pour chaque marque
        // ═══════════════════════════════════════════════════════
        for (Marque marque : marquesFiltrees) {
            VBox card = creerCardMarque(marque);
            flowPaneMarques.getChildren().add(card);
        }
    }

    /**
     * Crée une card élégante pour une marque avec son logo
     */
    private VBox creerCardMarque(Marque marque) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setPrefHeight(200);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f9fa);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        // ══════════════════════════════════════════════════════
        // LOGO DE LA MARQUE (CIRCULAIRE)
        // ══════════════════════════════════════════════════════
        ImageView logoView = creerLogoMarque(marque.getLogo());

        StackPane logoContainer = new StackPane(logoView);
        logoContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 50;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 50;" +
                        "-fx-padding: 5;"
        );
        logoContainer.setPrefSize(80, 80);
        logoContainer.setMaxSize(80, 80);

        // Nom de la marque
        Label nom = new Label(marque.getNomMarque());
        nom.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-wrap-text: true;" +
                        "-fx-text-alignment: center;"
        );
        nom.setMaxWidth(160);
        nom.setWrapText(true);
        nom.setAlignment(Pos.CENTER);

        // Badge "Mondiale"
        Label badge = new Label("🌍 Marque Mondiale");
        badge.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-text-fill: #3498db;" +
                        "-fx-background-color: #e8f4f8;" +
                        "-fx-padding: 3 8;" +
                        "-fx-background-radius: 10;"
        );

        card.getChildren().addAll(logoContainer, nom, badge);

        // ═══════════════════════════════════════════════════════
        // ANIMATIONS & INTERACTIONS
        // ═══════════════════════════════════════════════════════

        // Effet hover : agrandissement + ombre
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffffff, #e8f4f8);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #3498db;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 15, 0, 0, 5);" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f9fa);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Clic : Animation de confirmation
        card.setOnMouseClicked(e -> {
            System.out.println("📌 Marque sélectionnée : " + marque.getNomMarque() + " (ID: " + marque.getIdMarque() + ")");

            ScaleTransition click = new ScaleTransition(Duration.millis(100), card);
            click.setToX(0.95);
            click.setToY(0.95);
            click.setCycleCount(2);
            click.setAutoReverse(true);
            click.play();
        });

        // Animation d'apparition
        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        return card;
    }

    /**
     * Crée l'ImageView circulaire pour le logo de la marque
     */
    private ImageView creerLogoMarque(String logoUrl) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(70);
        imageView.setFitHeight(70);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Créer un clip circulaire
        Circle clip = new Circle(35, 35, 35);
        imageView.setClip(clip);

        // Image par défaut si URL vide
        String imageUrlToUse = (logoUrl == null || logoUrl.trim().isEmpty())
                ? "https://via.placeholder.com/150/2ecc71/ffffff?text=Logo"
                : logoUrl.trim();

        try {
            Image image = new Image(imageUrlToUse, true);

            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    try {
                        Image defaultImg = new Image(
                                "https://via.placeholder.com/150/2ecc71/ffffff?text=Logo",
                                true
                        );
                        imageView.setImage(defaultImg);
                    } catch (Exception ex) {
                        // Silence
                    }
                }
            });

            imageView.setImage(image);
        } catch (Exception e) {
            try {
                Image defaultImg = new Image(
                        "https://via.placeholder.com/150/2ecc71/ffffff?text=Logo",
                        true
                );
                imageView.setImage(defaultImg);
            } catch (Exception ex) {
                // Silence
            }
        }

        return imageView;
    }

    /**
     * Rafraîchit l'affichage des marques
     */
    @FXML
    private void rafraichir() {
        System.out.println("🔄 Rafraîchissement des marques...");
        chargerMarques();

        // Animation du bouton rafraîchir
        Label lblRefresh = (Label) flowPaneMarques.getParent().getParent().getParent().lookup(".label");
        if (lblRefresh != null) {
            ScaleTransition st = new ScaleTransition(Duration.millis(300), lblRefresh);
            st.setByX(0.2);
            st.setByY(0.2);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();
        }
    }

    /**
     * Réinitialise les filtres de recherche
     */
    @FXML
    private void reinitialiserFiltres() {
        System.out.println("↺ Réinitialisation des filtres");
        txtRecherche.clear();
        comboTri.setValue("A → Z (alphabétique)");
        filtrerMarques();
    }
}