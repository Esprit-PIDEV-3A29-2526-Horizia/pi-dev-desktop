package tn.esprit.controllers;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import tn.esprit.entities.Marque;
import tn.esprit.entities.Modele;
import tn.esprit.entities.Vehicule;
import tn.esprit.services.MarqueService;
import tn.esprit.services.ModeleService;
import tn.esprit.services.VehiculeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AfficherVehiculesController {

    @FXML private TextField txtRechercheImmat;
    @FXML private ComboBox<String> comboFiltreEtat;
    @FXML private ComboBox<String> comboFiltreCarburant;
    @FXML private ComboBox<Marque> comboFiltreMarque;
    @FXML private ComboBox<String> comboTri;
    @FXML private FlowPane flowPaneVehicules;
    @FXML private Label lblNombreVehicules;
    @FXML private Label lblNombreResultats;
    @FXML private Label lblResultats;
    @FXML private Label lblFiltresActifs;
    @FXML private VBox vboxAucunVehicule;

    private VehiculeService vehiculeService;
    private MarqueService marqueService;
    private ModeleService modeleService;
    private List<Vehicule> tousLesVehicules;
    private List<Vehicule> vehiculesFiltres;
    private Map<Integer, String> mapModeles; // idModele -> "Marque Modele"

    @FXML
    public void initialize() {
        vehiculeService = new VehiculeService();
        marqueService = new MarqueService();
        modeleService = new ModeleService();
        mapModeles = new HashMap<>();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Afficher Véhicules - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        // Construire la map des modèles
        construireMapModeles();

        // Initialiser les ComboBox
        initialiserComboBoxes();

        // Listeners pour filtrage en temps réel
        txtRechercheImmat.textProperty().addListener((obs, old, nouv) -> filtrerVehicules());
        comboFiltreEtat.valueProperty().addListener((obs, old, nouv) -> filtrerVehicules());
        comboFiltreCarburant.valueProperty().addListener((obs, old, nouv) -> filtrerVehicules());
        comboFiltreMarque.valueProperty().addListener((obs, old, nouv) -> filtrerVehicules());
        comboTri.valueProperty().addListener((obs, old, nouv) -> filtrerVehicules());

        // Charger les véhicules
        chargerVehicules();
    }

    private void construireMapModeles() {
        List<Modele> modeles = modeleService.getAllModeles();
        for (Modele modele : modeles) {
            Marque marque = marqueService.getMarqueById(modele.getIdMarque());
            if (marque != null) {
                mapModeles.put(modele.getIdModele(), marque.getNomMarque() + " " + modele.getNomModele());
            }
        }
        System.out.println("✓ Map des modèles construite : " + mapModeles.size() + " modèles");
    }

    private void initialiserComboBoxes() {
        // États
        comboFiltreEtat.getItems().addAll("Tous", "disponible", "louee", "en_maintenance", "indisponible");
        comboFiltreEtat.setValue("Tous");

        // Carburants
        comboFiltreCarburant.getItems().addAll("Tous", "Essence", "Diesel", "Hybride", "Electrique");
        comboFiltreCarburant.setValue("Tous");

        // Marques
        List<Marque> marques = marqueService.getAllMarquesAlphabetique();
        comboFiltreMarque.getItems().add(null);
        comboFiltreMarque.setPromptText("Toutes");
        comboFiltreMarque.getItems().addAll(marques);

        // Tri
        comboTri.getItems().addAll(
                "Immatriculation (A→Z)",
                "Prix croissant",
                "Prix décroissant",
                "Kilométrage croissant",
                "Année (récente→ancienne)"
        );
        comboTri.setValue("Immatriculation (A→Z)");
    }

    private void chargerVehicules() {
        System.out.println("→ Chargement des véhicules...");

        tousLesVehicules = vehiculeService.getAllVehicules();

        System.out.println("✓ " + tousLesVehicules.size() + " véhicule(s) chargé(s)");
        lblNombreVehicules.setText(String.valueOf(tousLesVehicules.size()));

        filtrerVehicules();
    }

    private void filtrerVehicules() {
        String rechercheImmat = txtRechercheImmat.getText().toLowerCase().trim();
        String etatFiltre = comboFiltreEtat.getValue();
        String carburantFiltre = comboFiltreCarburant.getValue();
        Marque marqueFiltre = comboFiltreMarque.getValue();
        String tri = comboTri.getValue();

        vehiculesFiltres = tousLesVehicules;

        // Filtre immatriculation
        if (!rechercheImmat.isEmpty()) {
            vehiculesFiltres = vehiculesFiltres.stream()
                    .filter(v -> v.getImmatriculation().toLowerCase().contains(rechercheImmat))
                    .collect(Collectors.toList());
        }

        // Filtre état
        if (!"Tous".equals(etatFiltre)) {
            vehiculesFiltres = vehiculesFiltres.stream()
                    .filter(v -> v.getEtat().equals(etatFiltre))
                    .collect(Collectors.toList());
        }

        // Filtre carburant
        if (!"Tous".equals(carburantFiltre)) {
            vehiculesFiltres = vehiculesFiltres.stream()
                    .filter(v -> v.getCarburant().equals(carburantFiltre))
                    .collect(Collectors.toList());
        }

        // Filtre marque
        if (marqueFiltre != null) {
            vehiculesFiltres = vehiculesFiltres.stream()
                    .filter(v -> {
                        Modele modele = modeleService.getModeleById(v.getIdModele());
                        return modele != null && modele.getIdMarque() == marqueFiltre.getIdMarque();
                    })
                    .collect(Collectors.toList());
        }

        // Tri
        if (tri != null) {
            switch (tri) {
                case "Immatriculation (A→Z)":
                    vehiculesFiltres.sort((v1, v2) -> v1.getImmatriculation().compareToIgnoreCase(v2.getImmatriculation()));
                    break;
                case "Prix croissant":
                    vehiculesFiltres.sort((v1, v2) -> Double.compare(v1.getPrixParJour(), v2.getPrixParJour()));
                    break;
                case "Prix décroissant":
                    vehiculesFiltres.sort((v1, v2) -> Double.compare(v2.getPrixParJour(), v1.getPrixParJour()));
                    break;
                case "Kilométrage croissant":
                    vehiculesFiltres.sort((v1, v2) -> Integer.compare(v1.getKilometrage(), v2.getKilometrage()));
                    break;
                case "Année (récente→ancienne)":
                    vehiculesFiltres.sort((v1, v2) -> Integer.compare(v2.getAnnee(), v1.getAnnee()));
                    break;
            }
        }

        afficherVehicules();

        String texteResultat = vehiculesFiltres.size() + " résultat" + (vehiculesFiltres.size() > 1 ? "s" : "");
        lblNombreResultats.setText(texteResultat);

        System.out.println("🔍 Filtrage : " + vehiculesFiltres.size() + " véhicule(s) affiché(s)");
    }

    private void afficherVehicules() {
        flowPaneVehicules.getChildren().clear();

        if (vehiculesFiltres.isEmpty()) {
            vboxAucunVehicule.setVisible(true);
            vboxAucunVehicule.setManaged(true);
            flowPaneVehicules.setVisible(false);
            flowPaneVehicules.setManaged(false);
            return;
        } else {
            vboxAucunVehicule.setVisible(false);
            vboxAucunVehicule.setManaged(false);
            flowPaneVehicules.setVisible(true);
            flowPaneVehicules.setManaged(true);
        }

        for (Vehicule vehicule : vehiculesFiltres) {
            VBox card = creerCardVehicule(vehicule);
            flowPaneVehicules.getChildren().add(card);
        }
    }

    private VBox creerCardVehicule(Vehicule vehicule) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(260);
        card.setMinHeight(380);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );

        // ══════════════════════════════════════════════════════
        // IMAGE DU VÉHICULE
        // ══════════════════════════════════════════════════════
        ImageView imageView = creerImageVehicule(vehicule.getPhoto());

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 8;"
        );
        imageContainer.setPrefHeight(170);
        imageContainer.setMaxHeight(170);

        card.getChildren().add(imageContainer);

        // ══════════════════════════════════════════════════════
        // INFORMATIONS DU VÉHICULE
        // ══════════════════════════════════════════════════════
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(5, 0, 0, 0));

        // Badge État
        String couleurEtat = getCouleurEtat(vehicule.getEtat());
        Label badgeEtat = new Label(getLibelleEtat(vehicule.getEtat()));
        badgeEtat.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: " + couleurEtat + ";" +
                        "-fx-padding: 4 10;" +
                        "-fx-background-radius: 10;"
        );

        // Immatriculation
        Label immat = new Label(vehicule.getImmatriculation());
        immat.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Modèle
        String nomModele = mapModeles.get(vehicule.getIdModele());
        Label modele = new Label(nomModele != null ? nomModele : "Modèle inconnu");
        modele.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        modele.setWrapText(true);
        modele.setMaxWidth(240);

        // Séparateur
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setStyle("-fx-background-color: #ecf0f1;");

        // Infos techniques
        VBox infos = new VBox(4);
        infos.getChildren().addAll(
                creerLigneInfo("📅", "Année : " + vehicule.getAnnee()),
                creerLigneInfo("⛽", vehicule.getCarburant()),
                creerLigneInfo("📏", String.format("%,d km", vehicule.getKilometrage()))
        );

        // Prix
        Label prix = new Label(String.format("%.3f TND/jour", vehicule.getPrixParJour()));
        prix.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        infoBox.getChildren().addAll(badgeEtat, immat, modele, sep, infos, prix);
        card.getChildren().add(infoBox);

        // Animations
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f8f9fa;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #3498db;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 15, 0, 0, 6);" +
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
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
                            "-fx-cursor: hand;"
            +
            "-fx-padding: 18;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
                    "-fx-cursor: hand;"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        card.setOnMouseClicked(e -> {
            System.out.println("📌 Véhicule sélectionné : " + vehicule.getImmatriculation());
        });

        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        return card;
    }

    private HBox creerLigneInfo(String icone, String texte) {
        HBox ligne = new HBox(6);
        ligne.setAlignment(Pos.CENTER_LEFT);
        Label lblIcone = new Label(icone);
        lblIcone.setStyle("-fx-font-size: 12px;");
        Label lblTexte = new Label(texte);
        lblTexte.setStyle("-fx-font-size: 11px; -fx-text-fill: #34495e;");
        ligne.getChildren().addAll(lblIcone, lblTexte);
        return ligne;
    }

    private String getCouleurEtat(String etat) {
        switch (etat) {
            case "disponible": return "#27ae60";
            case "louee": return "#e74c3c";
            case "en_maintenance": return "#f39c12";
            case "indisponible": return "#95a5a6";
            default: return "#95a5a6";
        }
    }

    private String getLibelleEtat(String etat) {
        switch (etat) {
            case "disponible": return "✓ DISPONIBLE";
            case "louee": return "🔒 LOUÉE";
            case "en_maintenance": return "🔧 MAINTENANCE";
            case "indisponible": return "✖ INDISPONIBLE";
            default: return etat.toUpperCase();
        }
    }

    /**
     * Crée l'ImageView pour afficher la photo du véhicule
     */
    private ImageView creerImageVehicule(String photoUrl) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(230);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Image par défaut si URL vide
        String imageUrlToUse = (photoUrl == null || photoUrl.trim().isEmpty())
                ? "https://via.placeholder.com/300x200/3498db/ffffff?text=Pas+d%27image"
                : photoUrl.trim();

        try {
            Image image = new Image(imageUrlToUse, true); // true = chargement async

            // Gestion d'erreur si l'image ne charge pas
            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    try {
                        Image defaultImg = new Image(
                                "https://via.placeholder.com/300x200/3498db/ffffff?text=Pas+d%27image",
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
                        "https://via.placeholder.com/300x200/3498db/ffffff?text=Pas+d%27image",
                        true
                );
                imageView.setImage(defaultImg);
            } catch (Exception ex) {
                // Silence
            }
        }

        return imageView;
    }

    @FXML
    private void rafraichir() {
        System.out.println("🔄 Rafraîchissement des véhicules...");
        chargerVehicules();
    }

    @FXML
    private void reinitialiserFiltres() {
        System.out.println("↺ Réinitialisation des filtres");
        txtRechercheImmat.clear();
        comboFiltreEtat.setValue("Tous");
        comboFiltreCarburant.setValue("Tous");
        comboFiltreMarque.setValue(null);
        comboTri.setValue("Immatriculation (A→Z)");
        filtrerVehicules();
    }
}