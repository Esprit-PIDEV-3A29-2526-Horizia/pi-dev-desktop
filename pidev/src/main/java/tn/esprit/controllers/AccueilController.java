package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AccueilController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private Button allFilterBtn;
    @FXML
    private Button villaFilterBtn;
    @FXML
    private Button hotelFilterBtn;
    @FXML
    private Button appartFilterBtn;
    @FXML
    private FlowPane flowLogements;
    @FXML
    private Button btnNosLogements;
    @FXML
    private Button btnMesReservations; // Nouveau

    // Optionnel : pour personnaliser le bloc utilisateur
    @FXML
    private HBox userBox;
    @FXML
    private Label userNameLabel;

    private Servicelogement serviceLogement;
    private List<logement> tousLesLogements;
    private List<logement> logementsFiltres;
    private Button activeFilterBtn;

    @FXML
    public void initialize() {
        serviceLogement = new Servicelogement();

        btnNosLogements.getStyleClass().add("nav-button-active");

        // Gestion du bouton Mes Réservations
        if (SessionManager.isLoggedIn()) {
            btnMesReservations.setVisible(true);
            btnMesReservations.setOnAction(e -> NavigationManager.loadView("/mesreservations.fxml"));
            // Optionnel : afficher le nom de l'utilisateur
            // userNameLabel.setText(SessionManager.getCurrentUser().getNom());
        } else {
            btnMesReservations.setVisible(false);
            // Optionnel : transformer le bloc utilisateur en bouton de connexion
            // userNameLabel.setText("Connexion");
            // userBox.setOnMouseClicked(e -> NavigationManager.loadView("/login.fxml"));
        }

        sortCombo.getItems().addAll("Prix croissant", "Prix décroissant");

        try {
            tousLesLogements = serviceLogement.afficher();
            logementsFiltres = new ArrayList<>(tousLesLogements);
            loadLogements();
        } catch (SQLException e) {
            showAlert("Erreur de chargement", "Impossible de charger les logements : " + e.getMessage());
            e.printStackTrace();
        }

        activeFilterBtn = allFilterBtn;

        allFilterBtn.setOnAction(e -> {
            setActiveFilter(allFilterBtn);
            logementsFiltres = new ArrayList<>(tousLesLogements);
            appliquerRechercheEtTri();
        });
        villaFilterBtn.setOnAction(e -> {
            setActiveFilter(villaFilterBtn);
            filtrerParType("Villa");
        });
        hotelFilterBtn.setOnAction(e -> {
            setActiveFilter(hotelFilterBtn);
            filtrerParType("Hôtel");
        });
        appartFilterBtn.setOnAction(e -> {
            setActiveFilter(appartFilterBtn);
            filtrerParType("Appartement");
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> rechercher(newVal));
        sortCombo.setOnAction(e -> trier());
    }

    // ... le reste des méthodes inchangées ...


    private void setActiveFilter(Button newActiveBtn) {
        if (activeFilterBtn != null) {
            activeFilterBtn.getStyleClass().remove("filter-button-active");
            activeFilterBtn.getStyleClass().add("filter-button");
        }
        activeFilterBtn = newActiveBtn;
        activeFilterBtn.getStyleClass().remove("filter-button");
        activeFilterBtn.getStyleClass().add("filter-button-active");
    }

    private void loadLogements() {
        afficherLogements(tousLesLogements);
    }

    private void filtrerParType(String type) {
        if (tousLesLogements == null) return;
        logementsFiltres = tousLesLogements.stream()
                .filter(l -> l.getType().equalsIgnoreCase(type))
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
                .filter(l -> l.getNom().toLowerCase().contains(recherche) ||
                        l.getAdresse().toLowerCase().contains(recherche))
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
        String texte = searchField.getText();
        if (texte != null && !texte.trim().isEmpty()) {
            rechercher(texte);
        } else {
            afficherLogements(logementsFiltres);
        }
        trier();
    }

    private void afficherLogements(List<logement> liste) {
        flowLogements.getChildren().clear();
        for (logement l : liste) {
            flowLogements.getChildren().add(creerCarteLogement(l));
        }
    }

    private VBox creerCarteLogement(logement l) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(250);

        // Image du logement (pleine largeur disponible, en tenant compte du padding)
        ImageView imageView = new ImageView();
        try {
            String imagePath = l.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http")) {
                    imageView.setImage(new Image(imagePath));
                } else {
                    imageView.setImage(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                }
            } else {
                Region placeholder = new Region();
                placeholder.setStyle("-fx-background-color: #e0e0e0;");
                placeholder.setPrefSize(210, 150);
                imageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image pour " + l.getNom() + " : " + e.getMessage());
            Region placeholder = new Region();
            placeholder.setStyle("-fx-background-color: #e0e0e0;");
            placeholder.setPrefSize(210, 150);
        }
        imageView.setFitWidth(210);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Label title = new Label(l.getNom());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #23779C;");

        Label location = new Label(l.getAdresse());
        location.setStyle("-fx-font-size: 14px; -fx-text-fill: #81AE8D;");

        // Badge Disponible
        HBox infoBox = new HBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        if (l.isDisponibilite()) {
            Label dispo = new Label("Disponible");
            dispo.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 12px; -fx-font-weight: bold;");
            infoBox.getChildren().add(dispo);
        }
        infoBox.getChildren().add(location);

        Label price = new Label(l.getTarif_nuit() + " DT / nuit");
        price.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #23779C;");

        // Équipement sous le tarif
        Label equipementLabel = new Label("Équipement: " + l.getEquipement());
        equipementLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        Button btn = new Button("Réserver");
        btn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; -fx-background-radius: 20; -fx-padding: 10 0; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            // Vérifier si l'utilisateur est connecté
            if (SessionManager.isLoggedIn()) {
                // Utilisateur connecté : aller au formulaire de réservation
                SessionManager.setSelectedLogement(l);  // Passer le logement sélectionné
                NavigationManager.loadView("/ReservationForm.fxml");  // Utilise NavigationManager
            } else {
                // Utilisateur non connecté : aller à la page de login
                NavigationManager.loadView("/Login.fxml");  // Utilise NavigationManager
            }
        });

        card.getChildren().addAll(imageView, title, infoBox, price, equipementLabel, btn);
        return card;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}