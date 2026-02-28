package tn.esprit.frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.services.UtilisateurService;
import tn.esprit.backend.utils.Session;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML private BarChart<String, Number> reservationsChart;
    @FXML private ProgressBar satisfactionProgress;

    private PublicationService publicationService = new PublicationService();
    private UtilisateurService utilisateurService = new UtilisateurService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.estAdmin()) return;

        chargerStatistiques();
        chargerGraphique();
    }

    private void chargerStatistiques() {
        // Vous pouvez mettre à jour les labels avec des données réelles
        // Exemple:
        // totalReservationsLabel.setText(String.valueOf(....));
    }

    private void chargerGraphique() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations 2026");

        // Données d'exemple - à remplacer par vos vraies données
        series.getData().add(new XYChart.Data<>("Jan", 120));
        series.getData().add(new XYChart.Data<>("Fév", 150));
        series.getData().add(new XYChart.Data<>("Mar", 180));
        series.getData().add(new XYChart.Data<>("Avr", 210));
        series.getData().add(new XYChart.Data<>("Mai", 250));
        series.getData().add(new XYChart.Data<>("Juin", 300));

        reservationsChart.getData().clear();
        reservationsChart.getData().add(series);
    }
}