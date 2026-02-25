package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.entities.User;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class MesReservationsController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Status> filterStatusCombo;
    @FXML
    private Button btnRechercher;
    @FXML
    private Button btnReset;
    @FXML
    private FlowPane flowReservations;
    @FXML
    private Label emptyMessage;

    // Éléments pour l'affichage du nom utilisateur (ajoutés pour correspondre à l'accueil)
    @FXML
    private HBox userBox;
    @FXML
    private Label userNameLabel;
    @FXML
    private Button btnAccueil;
    @FXML
    private Button btnNosLogements;
    @FXML
    private Button btnMesReservationsNav; // Bouton de navigation "Mes Réservations" dans la barre

    private Servicereservationlog serviceReservation = new Servicereservationlog();
    private Servicelogement serviceLogement = new Servicelogement();

    private ObservableList<reservationlog> reservationsList = FXCollections.observableArrayList();
    private User currentUser;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getCurrentUser();

        // Configuration de la navigation et de l'affichage utilisateur (comme dans l'accueil)
        setupNavigationAndUser();

        // Configuration du combo de statuts
        filterStatusCombo.getItems().addAll(Status.values());
        filterStatusCombo.setPromptText("Tous les statuts");

        // Charger les réservations
        chargerReservations();

        // Configuration des boutons de filtre
        btnRechercher.setOnAction(e -> filtrerReservations());
        btnReset.setOnAction(e -> resetFiltres());

        // Recherche et filtres en temps réel
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerReservations());
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrerReservations());

        System.out.println("MesReservationsController initialisé pour l'utilisateur: " +
                (currentUser != null ? currentUser.getEmail() : "non connecté"));
    }

    private void setupNavigationAndUser() {
        // Style du bouton actif (Mes Réservations est actif sur cette page)
        if (btnMesReservationsNav != null) {
            btnMesReservationsNav.getStyleClass().add("nav-button-active");
        }

        // Configuration des boutons de navigation
        if (btnNosLogements != null) {
            btnNosLogements.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml"));
        }

        if (btnAccueil != null) {
            btnAccueil.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml"));
        }

        // Gestion de l'affichage utilisateur
        if (SessionManager.isLoggedIn() && currentUser != null) {
            // Afficher le nom de l'utilisateur
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }

            // Rendre userBox cliquable pour accéder au profil (pas déconnexion)
            if (userBox != null) {
                userBox.setCursor(javafx.scene.Cursor.HAND);
                // SUPPRIMER showLogoutConfirmation() et METTRE showUserProfile()
                userBox.setOnMouseClicked(e -> showUserProfile());
            }

        } else {
            // Utilisateur non connecté - rediriger vers login
            if (userNameLabel != null) {
                userNameLabel.setText("Connexion");
            }
            if (userBox != null) {
                userBox.setOnMouseClicked(e -> NavigationManager.loadView("/fxml/Login.fxml"));
            }
        }
    }

    /**
     * Affiche une confirmation de déconnexion
     */
    private void showLogoutConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.logout();
                NavigationManager.loadView("/fxml/Login.fxml");
            }
        });
    }

    private void chargerReservations() {
        try {
            List<reservationlog> toutes = serviceReservation.afficher();

            // Récupérer l'ID du client connecté depuis SessionManager
            int clientId;
            if (SessionManager.isLoggedIn() && currentUser != null) {
                clientId = currentUser.getId(); // Adaptez selon votre méthode
            } else {
                // Fallback pour le développement
                clientId = 14;
                System.out.println("Aucun utilisateur connecté, utilisation de l'ID par défaut: 14");
            }

            int finalClientId = clientId;
            List<reservationlog> duClient = toutes.stream()
                    .filter(r -> r.getIdc() == finalClientId)
                    .collect(Collectors.toList());

            reservationsList.setAll(duClient);
            afficherReservations(reservationsList);

            System.out.println("Réservations chargées: " + duClient.size() + " pour le client ID " + clientId);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les réservations : " + e.getMessage());
        }
    }

    private void afficherReservations(List<reservationlog> reservations) {
        flowReservations.getChildren().clear();
        if (reservations.isEmpty()) {
            emptyMessage.setVisible(true);
            emptyMessage.setManaged(true);
        } else {
            emptyMessage.setVisible(false);
            emptyMessage.setManaged(false);
            for (reservationlog r : reservations) {
                VBox card = createReservationCard(r);
                flowReservations.getChildren().add(card);
            }
        }
    }

    private VBox createReservationCard(reservationlog r) {
        // Récupérer le logement associé
        logement log = null;
        try {
            log = serviceLogement.rechercherParId(r.getId_l());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String nomLogement = (log != null) ? log.getNom() : "Logement inconnu";
        String adresseLogement = (log != null) ? log.getAdresse() : "";

        // Formatage des dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateArrivee = sdf.format(r.getDate_debut());
        String dateDepart = sdf.format(r.getDate_fin());

        // Création de la carte
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        // Effet au survol
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);")
        );

        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);")
        );

        Label lblNom = new Label(nomLogement);
        lblNom.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblNom.setStyle("-fx-text-fill: #23779C;");

        Label lblAdresse = new Label("📍 " + adresseLogement);
        lblAdresse.setStyle("-fx-text-fill: #81AE8D; -fx-font-size: 14;");

        Label lblDates = new Label("📅 " + dateArrivee + " → " + dateDepart);
        lblDates.setStyle("-fx-text-fill: #3D94CA; -fx-font-size: 14;");

        Label lblMontant = new Label("💰 " + r.getMontant() + " DT");
        lblMontant.setStyle("-fx-text-fill: #E8B156; -fx-font-size: 16; -fx-font-weight: bold;");

        // Badge de statut
        Label lblStatut = new Label(r.getStatus().toString());
        String statusColor;
        switch (r.getStatus()) {
            case confirmée:
                statusColor = "#81AE8D";
                break;
            case en_attente:
                statusColor = "#E8B156";
                break;
            case terminée:
                statusColor = "#e74c3c";
                break;
            default:
                statusColor = "#7f8c8d";
        }
        lblStatut.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Boutons d'action
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #3D94CA; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        btnModifier.setOnAction(e -> modifierReservation(r));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        btnSupprimer.setOnAction(e -> supprimerReservation(r));

        // Condition : cacher les boutons si le statut est "terminée"
        if (r.getStatus() == Status.terminée) {
            btnModifier.setVisible(false);
            btnSupprimer.setVisible(false);
        }

        actions.getChildren().addAll(btnModifier, btnSupprimer);

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(lblNom, lblAdresse, lblDates, lblMontant, lblStatut);

        card.getChildren().addAll(infoBox, actions);

        return card;
    }

    private void filtrerReservations() {
        String searchText = searchField.getText().toLowerCase().trim();
        Status selectedStatus = filterStatusCombo.getValue();

        List<reservationlog> filtered = reservationsList.stream()
                .filter(r -> {
                    // Filtre par statut
                    if (selectedStatus != null && r.getStatus() != selectedStatus) {
                        return false;
                    }

                    // Filtre par recherche textuelle
                    if (!searchText.isEmpty()) {
                        try {
                            logement log = serviceLogement.rechercherParId(r.getId_l());
                            if (log == null || !log.getNom().toLowerCase().contains(searchText)) {
                                return false;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        afficherReservations(filtered);
    }

    private void resetFiltres() {
        searchField.clear();
        filterStatusCombo.setValue(null);
        afficherReservations(reservationsList);
    }

    private void modifierReservation(reservationlog r) {
        SessionManager.setEditingReservation(r);
        NavigationManager.loadView("/fxml/ReservationForm.fxml");
    }

    private void supprimerReservation(reservationlog r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réservation");
        confirm.setContentText("Voulez-vous vraiment supprimer cette réservation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                serviceReservation.supprimer(r.getId());
                chargerReservations(); // Recharger la liste après suppression
                showAlert("Succès", "Réservation supprimée.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    @FXML
    private void retourAccueil() {
        NavigationManager.loadView("/fxml/accueil.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void onUserBoxHover() {
        if (userBox != null) {
            // Animation de transition
            userBox.setStyle("-fx-background-color: #2C7AA0; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; " +
                    "-fx-scale-x: 1.05; -fx-scale-y: 1.05; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        }
    }

    @FXML
    private void onUserBoxExit() {
        if (userBox != null) {
            userBox.setStyle("-fx-background-color: #3D94CA; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; " +
                    "-fx-scale-x: 1.0; -fx-scale-y: 1.0; -fx-effect: null;");
        }
    }
    @FXML
    private void showUserProfile() {
        if (SessionManager.isLoggedIn() && currentUser != null) {
            System.out.println("Ouverture du profil pour: " + currentUser.getEmail());
            NavigationManager.loadView("/fxml/UserProfil.fxml");
        } else {
            System.out.println("vous n'etes pas connécter ! ");
            NavigationManager.loadView("/fxml/Login.fxml");
        }
    }
}