package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Scene;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.entities.User;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.EmailService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.QRCodeGenerator;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
    @FXML
    private HBox userBox;
    @FXML
    private Label userNameLabel;
    @FXML
    private Button btnAccueil;
    @FXML
    private Button btnNosLogements;
    @FXML
    private Button btnMesReservationsNav;

    private Servicereservationlog serviceReservation = new Servicereservationlog();
    private Servicelogement serviceLogement = new Servicelogement();
    private ObservableList<reservationlog> reservationsList = FXCollections.observableArrayList();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        setupNavigationAndUser();
        filterStatusCombo.getItems().addAll(Status.values());
        filterStatusCombo.setPromptText("Tous les statuts");
        chargerReservations();
        btnRechercher.setOnAction(e -> filtrerReservations());
        btnReset.setOnAction(e -> resetFiltres());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerReservations());
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrerReservations());
        System.out.println("MesReservationsController initialisé pour l'utilisateur: " +
                (currentUser != null ? currentUser.getEmail() : "non connecté"));
    }

    private void setupNavigationAndUser() {
        if (btnMesReservationsNav != null) {
            btnMesReservationsNav.getStyleClass().add("nav-button-active");
        }
        if (btnNosLogements != null) {
            btnNosLogements.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml", "Catalogue"));
        }
        if (btnAccueil != null) {
            btnAccueil.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml", "Catalogue"));
        }
        if (SessionManager.isLoggedIn() && currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }
            if (userBox != null) {
                userBox.setCursor(javafx.scene.Cursor.HAND);
                userBox.setOnMouseClicked(e -> showUserProfile());
                userBox.setOnMouseEntered(e -> onUserBoxHover());
                userBox.setOnMouseExited(e -> onUserBoxExit());
            }
        } else {
            if (userNameLabel != null) {
                userNameLabel.setText("Connexion");
            }
            if (userBox != null) {
                userBox.setCursor(javafx.scene.Cursor.HAND);
                userBox.setOnMouseClicked(e -> NavigationManager.loadView("/fxml/Login.fxml", "Catalogue"));
            }
        }
    }

    private void chargerReservations() {
        try {
            List<reservationlog> toutes = serviceReservation.afficher();
            int clientId = (SessionManager.isLoggedIn() && currentUser != null) ? currentUser.getId() : 14;
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
        logement log = null;
        try {
            log = serviceLogement.rechercherParId(r.getId_l());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String nomLogement = (log != null) ? log.getNom() : "Logement inconnu";
        String adresseLogement = (log != null) ? log.getAdresse() : "";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateArrivee = sdf.format(r.getDate_debut());
        String dateDepart = sdf.format(r.getDate_fin());

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

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

        Label lblStatut = new Label(r.getStatus().toString());
        String statusColor;
        switch (r.getStatus()) {
            case confirmée: statusColor = "#81AE8D"; break;
            case en_attente: statusColor = "#E8B156"; break;
            case terminée: statusColor = "#e74c3c"; break;
            default: statusColor = "#7f8c8d";
        }
        lblStatut.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Boutons
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #3D94CA; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        btnModifier.setOnAction(e -> modifierReservation(r));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        btnSupprimer.setOnAction(e -> supprimerReservation(r));

        // Bouton QR conditionnel
        Button btnQR = null;
        if ("Sur place".equals(r.getModalite()) ||
                ("En ligne".equals(r.getModalite()) && r.getStatus() == Status.confirmée)) {
            btnQR = new Button("QR Code");
            btnQR.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
            btnQR.setOnAction(e -> afficherQRCode(r));
        }

        if (r.getStatus() == Status.terminée) {
            btnModifier.setVisible(false);
            btnSupprimer.setVisible(false);
        }

        if (btnQR != null) {
            actions.getChildren().add(btnQR);
        }
        actions.getChildren().addAll(btnModifier, btnSupprimer);

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(lblNom, lblAdresse, lblDates, lblMontant, lblStatut);
        card.getChildren().addAll(infoBox, actions);
        return card;
    }

    private void afficherQRCode(reservationlog r) {
        try {
            String nomLogement = getNomLogement(r.getId_l());
            LocalDate dateArrivee = r.getDate_debut().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate dateDepart = r.getDate_fin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            StringBuilder qrContent = new StringBuilder();
            qrContent.append("🏠 Logement: ").append(nomLogement).append("\n");
            qrContent.append("📅 Arrivée: ").append(new SimpleDateFormat("dd/MM/yyyy").format(r.getDate_debut())).append("\n");
            qrContent.append("📅 Départ: ").append(new SimpleDateFormat("dd/MM/yyyy").format(r.getDate_fin())).append("\n");
            qrContent.append("💰 Montant: ").append(r.getMontant()).append(" DT\n");
            qrContent.append("💳 Modalité: ").append(r.getModalite()).append("\n");
            if ("En ligne".equals(r.getModalite())) {
                qrContent.append(r.getStatus() == Status.confirmée ? "✅ Paiement: Effectué" : "⏳ Paiement: " + r.getStatus());
            } else {
                qrContent.append("🏧 Paiement: À régler sur place");
            }

            // Générer l'image QR pour affichage (utiliser QRCodeGenerator)
            Image qrImage = QRCodeGenerator.generateQRCode(qrContent.toString(), 300, 300);

            Stage stage = new Stage();
            stage.setTitle("QR Code - " + nomLogement);
            stage.setResizable(false);

            ImageView imageView = new ImageView(qrImage);
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5); -fx-background-radius: 15;");

            Label lblInfo = new Label("Scannez ce code pour voir les détails");
            lblInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #1A3C5A; -fx-font-weight: bold;");

            Button btnEmailPDF = new Button("📧 Envoyer par email (PDF)");
            btnEmailPDF.setStyle(
                    "-fx-background-color: #1A3C5A; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 25; " +
                            "-fx-padding: 12 25; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );
            btnEmailPDF.setOnMouseEntered(e ->
                    btnEmailPDF.setStyle(btnEmailPDF.getStyle() + "-fx-background-color: #2C7AA0;")
            );
            btnEmailPDF.setOnMouseExited(e ->
                    btnEmailPDF.setStyle(btnEmailPDF.getStyle().replace("-fx-background-color: #2C7AA0;", "-fx-background-color: #1A3C5A;"))
            );            btnEmailPDF.setOnAction(e -> {
                try {
                    String adresse = getAdresseLogement(r.getId_l());
                    EmailService.sendReservationEmailWithPDF(
                            currentUser.getEmail(),
                            "Votre QR Code de réservation",
                            currentUser.getNom(),
                            currentUser.getPrenom(),
                            nomLogement,
                            adresse,
                            dateArrivee,
                            dateDepart,
                            r.getMontant(),
                            r.getModalite(),
                            r.getStatus().toString(),
                            qrContent.toString(),
                            "reservation_logement.pdf"
                    );
                    showAlert("Succès", "Email envoyé à " + currentUser.getEmail());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Erreur", "Impossible d'envoyer l'email : " + ex.getMessage());
                }
            });
            VBox vbox = new VBox(15, imageView, lblInfo, btnEmailPDF);
            vbox.setAlignment(Pos.CENTER);
            vbox.setStyle("-fx-padding: 25; -fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

            Scene scene = new Scene(vbox);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le QR code.");
        }
    }

    private String getNomLogement(int idLogement) {
        try {
            logement log = serviceLogement.rechercherParId(idLogement);
            return log != null ? log.getNom() : "Inconnu";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur";
        }
    }

    private void filtrerReservations() {
        String searchText = searchField.getText().toLowerCase().trim();
        Status selectedStatus = filterStatusCombo.getValue();
        List<reservationlog> filtered = reservationsList.stream()
                .filter(r -> {
                    if (selectedStatus != null && r.getStatus() != selectedStatus) return false;
                    if (!searchText.isEmpty()) {
                        try {
                            logement log = serviceLogement.rechercherParId(r.getId_l());
                            if (log == null || !log.getNom().toLowerCase().contains(searchText)) return false;
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
        NavigationManager.loadView("/fxml/ReservationForm.fxml", "Catalogue");
    }

    private void supprimerReservation(reservationlog r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réservation");
        confirm.setContentText("Voulez-vous vraiment supprimer cette réservation ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String nomLogement = getNomLogement(r.getId_l());
                double montant = r.getMontant();
                String dates = new SimpleDateFormat("dd/MM/yyyy").format(r.getDate_debut()) + " au " +
                        new SimpleDateFormat("dd/MM/yyyy").format(r.getDate_fin());

                serviceReservation.supprimer(r.getId());

                // Envoyer email d'annulation stylisé
                try {
                    EmailService.sendCancellationEmail(
                            currentUser.getEmail(),
                            currentUser.getNom(),
                            currentUser.getPrenom(),
                            nomLogement,
                            dates,
                            montant
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                chargerReservations();
                showAlert("Succès", "Réservation supprimée.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }
    private String getAdresseLogement(int idLogement) {
        try {
            logement log = serviceLogement.rechercherParId(idLogement);
            return log != null ? log.getAdresse() : "";
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }
    @FXML
    private void retourAccueil() {
        NavigationManager.loadView("/fxml/accueil.fxml", "Catalogue");
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
            NavigationManager.loadView("/fxml/UserProfil.fxml", "Catalogue");
        } else {
            System.out.println("vous n'etes pas connécter ! ");
            NavigationManager.loadView("/fxml/Login.fxml", "Catalogue");
        }
    }
}