package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.entities.Marque;
import org.example.entities.Modele;
import org.example.services.MarqueService;
import org.example.services.ModeleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AfficherModelesController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;
    @FXML private ComboBox<Marque> comboFiltreMarque;
    @FXML private FlowPane flowPaneModeles;
    @FXML private Label lblNombreModeles;
    @FXML private Label lblNombreResultats;
    @FXML private Label lblResultats;
    @FXML private Label lblFiltreActif;
    @FXML private VBox vboxAucunModele;

    private ModeleService modeleService;
    private MarqueService marqueService;
    private List<Modele> tousLesModeles;
    private List<Modele> modelesFiltres;
    private Map<Integer, String> mapMarques; // idMarque -> nomMarque

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        modeleService = new ModeleService();
        marqueService = new MarqueService();
        mapMarques = new HashMap<>();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Afficher Modèles - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        // Initialiser les ComboBox
        comboTri.getItems().addAll(
                "A → Z (alphabétique)",
                "Z → A (inverse)",
                "Par marque (A → Z)",
                "Récents d'abord"
        );
        comboTri.setValue("Par marque (A → Z)");

        // Charger les marques pour le filtre
        chargerMarquesPourFiltre();

        // Listeners pour recherche et tri en temps réel
        txtRecherche.textProperty().addListener((obs, old, nouv) -> filtrerModeles());
        comboTri.valueProperty().addListener((obs, old, nouv) -> filtrerModeles());
        comboFiltreMarque.valueProperty().addListener((obs, old, nouv) -> filtrerModeles());

        // Charger les modèles
        chargerModeles();
    }

    /**
     * Charge les marques dans le ComboBox de filtrage
     */
    private void chargerMarquesPourFiltre() {
        List<Marque> marques = marqueService.getAllMarquesAlphabetique();

        // Option "Toutes les marques"
        comboFiltreMarque.getItems().add(null);
        comboFiltreMarque.setPromptText("Toutes les marques");

        // Ajouter toutes les marques
        comboFiltreMarque.getItems().addAll(marques);

        // Remplir la map pour accès rapide
        for (Marque m : marques) {
            mapMarques.put(m.getIdMarque(), m.getNomMarque());
        }

        System.out.println("✓ " + marques.size() + " marque(s) chargée(s) pour le filtre");
    }

    /**
     * Charge tous les modèles depuis la base de données
     */
    private void chargerModeles() {
        System.out.println("→ Chargement des modèles...");

        tousLesModeles = modeleService.getAllModeles();

        System.out.println("✓ " + tousLesModeles.size() + " modèle(s) chargé(s)");

        // Mettre à jour le compteur global
        lblNombreModeles.setText(String.valueOf(tousLesModeles.size()));

        // Afficher les modèles
        filtrerModeles();
    }

    /**
     * Filtre et affiche les modèles selon les critères
     */
    private void filtrerModeles() {
        String recherche = txtRecherche.getText().toLowerCase().trim();
        String triSelectionne = comboTri.getValue();
        Marque marqueFiltre = comboFiltreMarque.getValue();

        // ═══════════════════════════════════════════════════════
        // ÉTAPE 1 : Filtrer par marque
        // ═══════════════════════════════════════════════════════
        if (marqueFiltre != null) {
            modelesFiltres = tousLesModeles.stream()
                    .filter(m -> m.getIdMarque() == marqueFiltre.getIdMarque())
                    .collect(Collectors.toList());

            lblFiltreActif.setText("Marque : " + marqueFiltre.getNomMarque());
            lblFiltreActif.setStyle("-fx-font-size: 12px; " +
                    "-fx-text-fill: #e74c3c; " +
                    "-fx-background-color: #fadbd8; " +
                    "-fx-padding: 5 12; " +
                    "-fx-background-radius: 15; " +
                    "-fx-font-weight: bold;");
        } else {
            modelesFiltres = tousLesModeles;
            lblFiltreActif.setText("Tous les modèles");
            lblFiltreActif.setStyle("-fx-font-size: 12px; " +
                    "-fx-text-fill: #3498db; " +
                    "-fx-background-color: #e8f4f8; " +
                    "-fx-padding: 5 12; " +
                    "-fx-background-radius: 15; " +
                    "-fx-font-weight: bold;");
        }

        // ═══════════════════════════════════════════════════════
        // ÉTAPE 2 : Filtrer par recherche textuelle
        // ═══════════════════════════════════════════════════════
        if (!recherche.isEmpty()) {
            modelesFiltres = modelesFiltres.stream()
                    .filter(m -> m.getNomModele().toLowerCase().contains(recherche) ||
                            (mapMarques.get(m.getIdMarque()) != null &&
                                    mapMarques.get(m.getIdMarque()).toLowerCase().contains(recherche)))
                    .collect(Collectors.toList());

            lblResultats.setText("Résultats pour : \"" + txtRecherche.getText() + "\"");
        } else {
            if (marqueFiltre != null) {
                lblResultats.setText("Modèles de la marque " + marqueFiltre.getNomMarque());
            } else {
                lblResultats.setText("Affichage de tous les modèles");
            }
        }

        // ═══════════════════════════════════════════════════════
        // ÉTAPE 3 : Trier les résultats
        // ═══════════════════════════════════════════════════════
        if (triSelectionne != null) {
            switch (triSelectionne) {
                case "A → Z (alphabétique)":
                    modelesFiltres.sort((m1, m2) ->
                            m1.getNomModele().compareToIgnoreCase(m2.getNomModele()));
                    break;

                case "Z → A (inverse)":
                    modelesFiltres.sort((m1, m2) ->
                            m2.getNomModele().compareToIgnoreCase(m1.getNomModele()));
                    break;

                case "Par marque (A → Z)":
                    modelesFiltres.sort((m1, m2) -> {
                        String marque1 = mapMarques.get(m1.getIdMarque());
                        String marque2 = mapMarques.get(m2.getIdMarque());
                        int cmpMarque = marque1.compareToIgnoreCase(marque2);
                        if (cmpMarque != 0) return cmpMarque;
                        return m1.getNomModele().compareToIgnoreCase(m2.getNomModele());
                    });
                    break;

                case "Récents d'abord":
                    modelesFiltres.sort((m1, m2) ->
                            Integer.compare(m2.getIdModele(), m1.getIdModele()));
                    break;
            }
        }

        // ═══════════════════════════════════════════════════════
        // ÉTAPE 4 : Afficher les résultats
        // ═══════════════════════════════════════════════════════
        afficherModeles();

        // Mettre à jour le compteur de résultats
        String texteResultat = modelesFiltres.size() + " résultat" + (modelesFiltres.size() > 1 ? "s" : "");
        lblNombreResultats.setText(texteResultat);

        System.out.println("🔍 Filtrage : " + modelesFiltres.size() + " modèle(s) affiché(s)");
    }

    /**
     * Affiche les modèles sous forme de cards élégantes
     */
    private void afficherModeles() {
        flowPaneModeles.getChildren().clear();

        if (modelesFiltres.isEmpty()) {
            // Afficher le message "Aucun modèle"
            vboxAucunModele.setVisible(true);
            vboxAucunModele.setManaged(true);
            flowPaneModeles.setVisible(false);
            flowPaneModeles.setManaged(false);
            return;
        } else {
            vboxAucunModele.setVisible(false);
            vboxAucunModele.setManaged(false);
            flowPaneModeles.setVisible(true);
            flowPaneModeles.setManaged(true);
        }

        // ═══════════════════════════════════════════════════════
        // Créer une card pour chaque modèle
        // ═══════════════════════════════════════════════════════
        for (Modele modele : modelesFiltres) {
            VBox card = creerCardModele(modele);
            flowPaneModeles.getChildren().add(card);
        }
    }

    /**
     * Crée une card élégante pour un modèle
     */
    private VBox creerCardModele(Modele modele) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setPrefHeight(160);
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

        // Icône du modèle
        Label icone = new Label("🚙");
        icone.setStyle("-fx-font-size: 40px;");

        // Nom de la marque (en petit)
        String nomMarque = mapMarques.get(modele.getIdMarque());
        Label marque = new Label(nomMarque != null ? nomMarque : "Marque inconnue");
        marque.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #95a5a6;" +
                        "-fx-text-alignment: center;"
        );

        // Nom du modèle (en gros)
        Label nom = new Label(modele.getNomModele());
        nom.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-wrap-text: true;" +
                        "-fx-text-alignment: center;"
        );
        nom.setMaxWidth(200);
        nom.setWrapText(true);
        nom.setAlignment(Pos.CENTER);

        // Badge "Modèle Mondial"
        Label badge = new Label("🌍 Mondial");
        badge.setStyle(
                "-fx-font-size: 9px;" +
                        "-fx-text-fill: #e74c3c;" +
                        "-fx-background-color: #fadbd8;" +
                        "-fx-padding: 3 8;" +
                        "-fx-background-radius: 10;"
        );

        card.getChildren().addAll(icone, marque, nom, badge);

        // ═══════════════════════════════════════════════════════
        // ANIMATIONS & INTERACTIONS
        // ═══════════════════════════════════════════════════════

        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffffff, #fadbd8);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #e74c3c;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 15, 0, 0, 5);" +
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

        // Clic
        card.setOnMouseClicked(e -> {
            System.out.println("📌 Modèle sélectionné : " + nomMarque + " " + modele.getNomModele() +
                    " (ID: " + modele.getIdModele() + ")");

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
     * Rafraîchit l'affichage
     */
    @FXML
    private void rafraichir() {
        System.out.println("🔄 Rafraîchissement des modèles...");
        chargerMarquesPourFiltre();
        chargerModeles();
    }

    /**
     * Réinitialise les filtres
     */
    @FXML
    private void reinitialiserFiltres() {
        System.out.println("↺ Réinitialisation des filtres");
        txtRecherche.clear();
        comboFiltreMarque.setValue(null);
        comboTri.setValue("Par marque (A → Z)");
        filtrerModeles();
    }
}