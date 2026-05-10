package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tn.esprit.entities.logement;
import tn.esprit.entities.User;
import tn.esprit.services.GeminiService;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AccueilController {

    // Éléments de la navbar (inclus via fx:include)
    @FXML private NavbarController navbarController;

    // Éléments du contenu principal
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button allFilterBtn;
    @FXML private Button villaFilterBtn;
    @FXML private Button hotelFilterBtn;
    @FXML private Button appartFilterBtn;
    @FXML private FlowPane flowLogements;
    @FXML private Button btnRecommendations;

    private Servicelogement serviceLogement;
    private List<logement> tousLesLogements;
    private List<logement> logementsFiltres;
    private Button activeFilterBtn;
    private User currentUser;

    @FXML
    public void initialize() {
        System.out.println("Initialisation de AccueilController...");

        serviceLogement = new Servicelogement();
        currentUser = SessionManager.getCurrentUser();

        // Afficher les informations de l'utilisateur connecté
        if (currentUser != null) {
            System.out.println("Utilisateur connecté: " + currentUser.getEmail());
        }

        // Configuration des filtres
        setupFilters();

        // Configuration de la recherche et du tri
        setupSearchAndSort();

        // Chargement des logements
        chargerLogements();

        // Configuration des recommandations
        if (btnRecommendations != null) {
            btnRecommendations.setOnAction(e -> chargerRecommandations());
        }

        if (navbarController != null) {
            navbarController.setActiveNosLogements();
        }
    }

    private void setupFilters() {
        activeFilterBtn = allFilterBtn;

        if (allFilterBtn != null) {
            allFilterBtn.setOnAction(e -> {
                setActiveFilter(allFilterBtn);
                if (tousLesLogements != null) {
                    logementsFiltres = new ArrayList<>(tousLesLogements);
                    appliquerRechercheEtTri();
                }
            });
        }

        if (villaFilterBtn != null) {
            villaFilterBtn.setOnAction(e -> {
                setActiveFilter(villaFilterBtn);
                filtrerParType("Villa");
            });
        }

        if (hotelFilterBtn != null) {
            hotelFilterBtn.setOnAction(e -> {
                setActiveFilter(hotelFilterBtn);
                filtrerParType("Hôtel");
            });
        }

        if (appartFilterBtn != null) {
            appartFilterBtn.setOnAction(e -> {
                setActiveFilter(appartFilterBtn);
                filtrerParType("Appartement");
            });
        }
    }

    private void setupSearchAndSort() {
        if (sortCombo != null) {
            sortCombo.getItems().addAll("Prix croissant", "Prix décroissant");
            sortCombo.setOnAction(e -> trier());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> rechercher(newVal));
        }
    }

    private void chargerLogements() {
        try {
            tousLesLogements = serviceLogement.afficher();
            if (tousLesLogements != null && !tousLesLogements.isEmpty()) {
                logementsFiltres = new ArrayList<>(tousLesLogements);
                afficherLogements(logementsFiltres);
                System.out.println("Logements chargés: " + tousLesLogements.size());
            } else {
                System.out.println("Aucun logement trouvé");
                if (flowLogements != null) {
                    flowLogements.getChildren().clear();
                    Label noDataLabel = new Label("Aucun logement disponible");
                    noDataLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
                    flowLogements.getChildren().add(noDataLabel);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des logements: " + e.getMessage());
            showAlert("Erreur de chargement", "Impossible de charger les logements : " + e.getMessage());
        }
    }

    private void setActiveFilter(Button newActiveBtn) {
        if (activeFilterBtn != null) {
            activeFilterBtn.getStyleClass().remove("filter-button-active");
            activeFilterBtn.getStyleClass().add("filter-button");
        }
        activeFilterBtn = newActiveBtn;
        if (activeFilterBtn != null) {
            activeFilterBtn.getStyleClass().remove("filter-button");
            activeFilterBtn.getStyleClass().add("filter-button-active");
        }
    }

    private void filtrerParType(String type) {
        if (tousLesLogements == null) return;
        logementsFiltres = tousLesLogements.stream()
                .filter(l -> l.getType() != null && l.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        appliquerRechercheEtTri();
    }

    private void rechercher(String texte) {
        if (logementsFiltres == null) return;

        if (texte == null || texte.trim().isEmpty()) {
            afficherLogements(logementsFiltres);
            return;
        }

        String recherche = texte.toLowerCase();
        List<logement> resultat = logementsFiltres.stream()
                .filter(l -> (l.getNom() != null && l.getNom().toLowerCase().contains(recherche)) ||
                        (l.getAdresse() != null && l.getAdresse().toLowerCase().contains(recherche)))
                .collect(Collectors.toList());
        afficherLogements(resultat);
    }

    private void trier() {
        if (logementsFiltres == null || logementsFiltres.isEmpty()) return;

        String choix = sortCombo.getValue();
        if (choix == null) return;

        Comparator<logement> comparator = null;
        switch (choix) {
            case "Prix croissant":
                comparator = Comparator.comparingDouble(logement::getTarif_nuit);
                break;
            case "Prix décroissant":
                comparator = Comparator.comparingDouble(logement::getTarif_nuit).reversed();
                break;
            default:
                return;
        }

        List<logement> triee = logementsFiltres.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        logementsFiltres = triee;
        afficherLogements(triee);
    }

    private void appliquerRechercheEtTri() {
        if (searchField != null) {
            String texte = searchField.getText();
            if (texte != null && !texte.trim().isEmpty()) {
                rechercher(texte);
            } else {
                afficherLogements(logementsFiltres);
            }
        } else {
            afficherLogements(logementsFiltres);
        }
        trier();
    }

    private void afficherLogements(List<logement> liste) {
        if (flowLogements == null) return;

        Platform.runLater(() -> {
            flowLogements.getChildren().clear();

            if (liste == null || liste.isEmpty()) {
                Label noDataLabel = new Label("Aucun logement trouvé");
                noDataLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
                flowLogements.getChildren().add(noDataLabel);
                return;
            }

            for (logement l : liste) {
                flowLogements.getChildren().add(creerCarteLogement(l));
            }
        });
    }

    private VBox creerCarteLogement(logement l) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setMinWidth(280);

        // Conteneur pour l'image avec taille fixe
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(240);
        imageContainer.setPrefHeight(160);
        imageContainer.setMinWidth(240);
        imageContainer.setMinHeight(160);
        imageContainer.setMaxWidth(240);
        imageContainer.setMaxHeight(160);
        imageContainer.setStyle("-fx-background-radius: 10; -fx-background-color: #f0f0f0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false); // Important: false pour forcer la taille exacte
        imageView.setStyle("-fx-background-radius: 10;");

        // Centrer l'image dans le conteneur
        imageContainer.getChildren().add(imageView);

        // Chargement de l'image
        try {
            String imagePath = l.getImage();
            Image img = null;

            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http")) {
                    img = new Image(imagePath, 240, 160, false, true);
                } else {
                    try {
                        img = new Image(getClass().getResourceAsStream(imagePath), 240, 160, false, true);
                    } catch (Exception e) {
                        System.err.println("Image non trouvée: " + imagePath);
                    }
                }
            }

            if (img == null) {
                try {
                    img = new Image(getClass().getResourceAsStream("/images/default.jpg"), 240, 160, false, true);
                } catch (Exception e) {
                    // Image par défaut simple
                    img = new Image("https://via.placeholder.com/240x160?text=No+Image", 240, 160, false, true);
                }
            }
            imageView.setImage(img);
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
            // Image par défaut en cas d'erreur
            imageView.setImage(new Image("https://via.placeholder.com/240x160?text=No+Image", 240, 160, false, true));
        }

        Label title = new Label(l.getNom());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);
        title.setMaxWidth(240);

        Label location = new Label("📍 " + l.getAdresse());
        location.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        location.setWrapText(true);
        location.setMaxWidth(240);

        HBox badgeBox = new HBox(10);
        badgeBox.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(l.getType());
        typeBadge.setStyle("-fx-background-color: #E8B156; -fx-text-fill: white; -fx-background-radius: 12; " +
                "-fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
        badgeBox.getChildren().add(typeBadge);

        if (l.isDisponibilite()) {
            Label dispo = new Label("Disponible");
            dispo.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; -fx-background-radius: 12; " +
                    "-fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
            badgeBox.getChildren().add(dispo);
        }

        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(String.format("%.2f", l.getTarif_nuit()) + " DT");
        price.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #23779C;");

        Label nuitLabel = new Label("/nuit");
        nuitLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        priceBox.getChildren().addAll(price, nuitLabel);

        Label equipementLabel = new Label("⚙️ " + (l.getEquipement() != null && !l.getEquipement().isEmpty() ? l.getEquipement() : "Équipements standard"));
        equipementLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        equipementLabel.setWrapText(true);
        equipementLabel.setMaxWidth(240);

        Button btn = new Button("Réserver");
        btn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: white; -fx-background-radius: 25; " +
                "-fx-padding: 10 0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btn.setMaxWidth(Double.MAX_VALUE);

        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #D49B3D; -fx-text-fill: white; -fx-background-radius: 25; " +
                        "-fx-padding: 10 0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;")
        );

        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: white; -fx-background-radius: 25; " +
                        "-fx-padding: 10 0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;")
        );

        btn.setOnAction(e -> {
            if (SessionManager.isLoggedIn()) {
                if (l.isDisponibilite()) {
                    SessionManager.setSelectedLogement(l);
                    NavigationManager.loadView("/fxml/ReservationForm.fxml", "Réservation");
                } else {
                    showAlert("Indisponible", "Ce logement n'est pas disponible pour le moment.");
                }
            } else {
                NavigationManager.showLogin();
            }
        });

        card.getChildren().addAll(imageContainer, title, location, badgeBox, priceBox, equipementLabel, btn);
        return card;
    }

    private void chargerRecommandations() {
        if (btnRecommendations == null) return;

        if (!SessionManager.isLoggedIn()) {
            showAlert("Connexion requise", "Veuillez vous connecter pour obtenir des recommandations personnalisées.");
            return;
        }

        if (tousLesLogements == null || tousLesLogements.isEmpty()) {
            showAlert("Information", "Aucun logement disponible pour les recommandations.");
            return;
        }

        btnRecommendations.setDisable(true);
        btnRecommendations.setText("Chargement...");

        new Thread(() -> {
            try {
                GeminiService gemini = new GeminiService();
                List<Map<String, Object>> recos = gemini.getRecommendations(currentUser, tousLesLogements);

                List<logement> logementsRecommandes = new ArrayList<>();
                for (Map<String, Object> reco : recos) {
                    Number idNumber = (Number) reco.get("id_logement");
                    int id = idNumber.intValue();

                    tousLesLogements.stream()
                            .filter(l -> l.getId() == id)
                            .findFirst()
                            .ifPresent(logementsRecommandes::add);
                }

                Platform.runLater(() -> {
                    if (!logementsRecommandes.isEmpty()) {
                        afficherLogements(logementsRecommandes);
                    } else {
                        showAlert("Recommandations", "Aucune recommandation disponible pour le moment.");
                    }
                    btnRecommendations.setDisable(false);
                    btnRecommendations.setText("Recommandations pour vous");
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Erreur", "Impossible d'obtenir des recommandations pour le moment.");
                    btnRecommendations.setDisable(false);
                    btnRecommendations.setText("Recommandations pour vous");
                });
            }
        }).start();
    }

    private void voirToutesLesOffres() {
        if (tousLesLogements != null) {
            afficherLogements(tousLesLogements);
            if (allFilterBtn != null) {
                setActiveFilter(allFilterBtn);
            }
            if (searchField != null) {
                searchField.clear();
            }
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        System.out.println("Utilisateur mis à jour: " + (user != null ? user.getEmail() : "null"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}