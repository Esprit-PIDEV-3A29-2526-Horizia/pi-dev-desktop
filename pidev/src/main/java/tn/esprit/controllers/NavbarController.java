package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import tn.esprit.entities.User;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

public class NavbarController {

    @FXML private Button    btnAccueil;
    @FXML private Button    btnNosLogements;
    @FXML private MenuButton menuReservations;
    @FXML private MenuItem  menuVoirReservations;
    @FXML private MenuItem  menuHistorique;
    @FXML private MenuItem  menuEvenementReservations;
    @FXML private Button    btnEvenements;
    @FXML private Button    btnLocation;
    @FXML private MenuButton menuPublications;
    @FXML private MenuItem  menuExplorerPublications;
    @FXML private MenuItem  menuMesPublications;
    @FXML private MenuItem  menuMesFavoris;
    @FXML private MenuItem  menuAjouterPublication;
    @FXML private Button    btnContact;
    @FXML private HBox      userBox;
    @FXML private Label     userNameLabel;
    @FXML private Label     userIconLabel;
    @FXML private Button    btnLogout;

    private static NavbarController instance;

    private static final String S_NORMAL =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: #D1D5DB;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 17;" +
                    "-fx-border-color: transparent;" +
                    "-fx-cursor: hand;";

    private static final String S_HOVER =
            "-fx-background-color: rgba(255,255,255,0.10);" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 17;" +
                    "-fx-border-color: transparent;" +
                    "-fx-cursor: hand;";

    private static final String S_ACTIVE =
            "-fx-background-color: #E8B156;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 17;" +
                    "-fx-border-color: transparent;" +
                    "-fx-cursor: hand;";

    private Button[] navButtons;

    @FXML
    public void initialize() {
        instance = this;
        System.out.println("✅ NavbarController initialisé");

        navButtons = new Button[]{
                btnAccueil, btnNosLogements,
                btnEvenements, btnLocation, btnContact
        };

        for (Button b : navButtons) {
            if (b == null) continue;
            b.setStyle(S_NORMAL);
            b.setOnMouseEntered(e -> { if (!isActive(b)) b.setStyle(S_HOVER);  });
            b.setOnMouseExited( e -> { if (!isActive(b)) b.setStyle(S_NORMAL); });
        }

        if (menuReservations != null) {
            menuReservations.setStyle(S_NORMAL);
            menuReservations.setOnMouseEntered(e -> menuReservations.setStyle(S_HOVER));
            menuReservations.setOnMouseExited( e -> menuReservations.setStyle(S_NORMAL));
        }

        if (menuPublications != null) {
            menuPublications.setStyle(S_NORMAL);
            menuPublications.setOnMouseEntered(e -> menuPublications.setStyle(S_HOVER));
            menuPublications.setOnMouseExited( e -> menuPublications.setStyle(S_NORMAL));
        }

        setupNavButtons();
        refreshUserDisplay();
        setActive(btnAccueil);
    }

    public static void refreshNavbar() {
        if (instance != null) {
            Platform.runLater(() -> {
                System.out.println("🔄 Rafraîchissement navbar");
                instance.refreshUserDisplay();
            });
        } else {
            System.err.println("❌ Instance NavbarController null");
        }
    }

    private boolean isActive(Button b) {
        String s = b.getStyle();
        return s != null && s.contains("#E8B156");
    }

    private void setActive(Button active) {
        for (Button b : navButtons) {
            if (b == null) continue;
            b.setStyle(b == active ? S_ACTIVE : S_NORMAL);
        }
        if (menuReservations != null) {
            menuReservations.setStyle(S_NORMAL);
        }
        if (menuPublications != null) {
            menuPublications.setStyle(S_NORMAL);
        }
    }

    public void setActiveAccueil() {
        setActive(btnAccueil);
    }

    public void setActiveNosLogements() {
        setActive(btnNosLogements);
    }

    public void setActiveEvenements() {
        setActive(btnEvenements);
    }

    public void setActivePublications() {
        if (menuPublications != null) {
            menuPublications.setStyle(S_ACTIVE);
        }
        for (Button b : navButtons) {
            if (b == null) continue;
            b.setStyle(S_NORMAL);
        }
    }

    public void setActiveReservations() {
        for (Button b : navButtons) {
            if (b == null) continue;
            b.setStyle(S_NORMAL);
        }
        if (menuReservations != null) {
            menuReservations.setStyle(S_ACTIVE);
        }
        if (menuPublications != null) {
            menuPublications.setStyle(S_NORMAL);
        }
    }

    private void setupNavButtons() {
        if (btnAccueil != null)
            btnAccueil.setOnAction(e -> {
                setActiveAccueil();
                NavigationManager.loadView("/fxml/CatalogueUser.fxml", "Accueil");
            });

        if (btnNosLogements != null)
            btnNosLogements.setOnAction(e -> {
                setActiveNosLogements();
                NavigationManager.loadView("/fxml/Accueil.fxml", "Nos Logements");
            });

        if (menuVoirReservations != null)
            menuVoirReservations.setOnAction(e -> {
                setActiveReservations();
                NavigationManager.loadView("/fxml/MesReservations.fxml", "Mes Réservations de logement");
            });

        if (menuHistorique != null)
            menuHistorique.setOnAction(e -> {
                setActiveReservations();
                NavigationManager.loadView("/fxml/MesReservationsvoy.fxml", "Mes réservations de voyage");
            });

        if (menuEvenementReservations != null)
            menuEvenementReservations.setOnAction(e -> {
                setActiveReservations();
                NavigationManager.loadView("/MyEvents.fxml", "Mes réservations d'événements");
            });

        if (btnEvenements != null)
            btnEvenements.setOnAction(e -> {
                setActiveEvenements();
                NavigationManager.loadView("/UserHome.fxml", "Événements");
            });

        if (btnLocation != null)
            btnLocation.setOnAction(e -> {
                setActive(btnLocation);
                NavigationManager.loadView("/fxml/Location.fxml", "Location");
            });

        // Nouveaux menus Publications
        if (menuExplorerPublications != null)
            menuExplorerPublications.setOnAction(e -> {
                setActivePublications();
                NavigationManager.loadView("/fxml/UserExplorer.fxml", "Explorer les publications");
            });

        if (menuMesPublications != null)
            menuMesPublications.setOnAction(e -> {
                setActivePublications();
                NavigationManager.loadView("/views/user/MesPublications.fxml", "Mes publications");
            });

        if (menuMesFavoris != null)
            menuMesFavoris.setOnAction(e -> {
                setActivePublications();
                NavigationManager.loadView("/views/user/MesFavoris.fxml", "Mes favoris");
            });

        if (menuAjouterPublication != null)
            menuAjouterPublication.setOnAction(e -> {
                setActivePublications();
                NavigationManager.loadView("/views/common/AjouterPublication.fxml", "Ajouter une publication");
            });

        if (btnContact != null)
            btnContact.setOnAction(e -> {
                setActive(btnContact);
                NavigationManager.loadView("/fxml/Contact.fxml", "Contact");
            });

        if (btnLogout != null)
            btnLogout.setOnAction(e -> handleLogout());
    }

    public void refreshUserDisplay() {
        boolean isLoggedIn = SessionManager.isLoggedIn();
        User currentUser = SessionManager.getCurrentUser();

        if (isLoggedIn && currentUser != null) {
            String nom;
            if (currentUser.getPrenom() != null && !currentUser.getPrenom().isBlank()
                    && currentUser.getNom() != null && !currentUser.getNom().isBlank()) {
                nom = currentUser.getPrenom() + " " + currentUser.getNom();
            } else if (currentUser.getEmail() != null) {
                nom = currentUser.getEmail().split("@")[0];
            } else {
                nom = "Utilisateur";
            }

            if (userNameLabel != null) {
                userNameLabel.setText(nom);
            }
            if (userIconLabel != null) userIconLabel.setText("👤");
            if (btnLogout != null) {
                btnLogout.setVisible(true);
                btnLogout.setManaged(true);
            }
            if (userBox != null) {
                userBox.setOnMouseClicked(e -> showUserProfile());
                userBox.setOnMouseEntered(e -> userBox.setStyle(
                        "-fx-background-color: #2C7AA0; -fx-background-radius: 20;" +
                                "-fx-padding: 0 14; -fx-cursor: hand;"));
                userBox.setOnMouseExited(e -> userBox.setStyle(
                        "-fx-background-color: #3D94CA; -fx-background-radius: 20;" +
                                "-fx-padding: 0 14; -fx-cursor: hand;"));
            }
        } else {
            if (userNameLabel != null) userNameLabel.setText("Connexion");
            if (userIconLabel != null) userIconLabel.setText("🔑");
            if (btnLogout != null) {
                btnLogout.setVisible(false);
                btnLogout.setManaged(false);
            }
            if (userBox != null) {
                userBox.setOnMouseClicked(e -> NavigationManager.showLogin());
                userBox.setOnMouseEntered(e -> userBox.setStyle(
                        "-fx-background-color: #2C7AA0; -fx-background-radius: 20;" +
                                "-fx-padding: 0 14; -fx-cursor: hand;"));
                userBox.setOnMouseExited(e -> userBox.setStyle(
                        "-fx-background-color: #3D94CA; -fx-background-radius: 20;" +
                                "-fx-padding: 0 14; -fx-cursor: hand;"));
            }
        }
    }

    public void updateUserInfo() {
        Platform.runLater(() -> {
            System.out.println("🔄 Mise à jour info utilisateur");
            refreshUserDisplay();
        });
    }

    private void showUserProfile() {
        if (SessionManager.isLoggedIn())
            NavigationManager.loadView("/fxml/UserProfil.fxml", "Mon Profil");
        else
            NavigationManager.showLogin();
    }

    private void handleLogout() {
        SessionManager.logout();
        refreshUserDisplay();
        setActiveAccueil();
        NavigationManager.showLogin();
    }
}