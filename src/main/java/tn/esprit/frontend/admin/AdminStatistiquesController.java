package tn.esprit.frontend.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Categorie;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.services.UtilisateurService;
import tn.esprit.backend.utils.Session;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminStatistiquesController implements Initializable {

    @FXML private Label totalPublicationsLabel;      // ✅ CORRIGÉ (avec s)
    @FXML private Label totalCommentairesLabel;
    @FXML private Label totalLikesLabel;
    @FXML private Label totalUtilisateursLabel;
    @FXML private PieChart categoriePieChart;
    @FXML private BarChart<String, Number> moisBarChart;
    @FXML private VBox topPublicationsList;          // ✅ CORRIGÉ (avec s)
    @FXML private VBox activitesList;

    private PublicationService publicationService = new PublicationService();  // ✅ CORRIGÉ (minuscule)
    private CommentaireService commentaireService = new CommentaireService();
    private UtilisateurService utilisateurService = new UtilisateurService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.estAdmin()) {
            return;
        }

        chargerStatistiques();
    }

    private void chargerStatistiques() {
        List<Publication> publications = publicationService.getAll();  // ✅ CORRIGÉ

        // Statistiques globales
        totalPublicationsLabel.setText(String.valueOf(publications.size()));  // ✅ CORRIGÉ

        int totalComments = publications.stream()
                .mapToInt(p -> commentaireService.getByPublication(p.getId()).size())
                .sum();
        totalCommentairesLabel.setText(String.valueOf(totalComments));

        int totalLikes = publications.stream().mapToInt(Publication::getLikes).sum();
        totalLikesLabel.setText(String.valueOf(totalLikes));

        totalUtilisateursLabel.setText(String.valueOf(utilisateurService.getAll().size()));

        // Graphique par catégorie
        chargerGraphiqueCategorie(publications);

        // Graphique par mois
        chargerGraphiqueMois(publications);

        // Top publications
        chargerTopPublications(publications);

        // Dernières activités
        chargerDernieresActivites(publications);
    }

    private void chargerGraphiqueCategorie(List<Publication> publications) {  // ✅ CORRIGÉ
        Map<Categorie, Long> countByCategorie = publications.stream()
                .collect(Collectors.groupingBy(Publication::getCategorie, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<Categorie, Long> entry : countByCategorie.entrySet()) {
            pieChartData.add(new PieChart.Data(
                    entry.getKey().getLabel() + " (" + entry.getValue() + ")",
                    entry.getValue()
            ));
        }

        categoriePieChart.setData(pieChartData);
        categoriePieChart.setTitle("Publications par catégorie");
    }

    private void chargerGraphiqueMois(List<Publication> publications) {  // ✅ CORRIGÉ
        Map<String, Long> countByMois = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Publication p : publications) {
            String moisAnnee = p.getDateCreation().format(formatter);
            countByMois.put(moisAnnee, countByMois.getOrDefault(moisAnnee, 0L) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Publications");

        countByMois.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach(entry -> series.getData().add(
                        new XYChart.Data<>(entry.getKey(), entry.getValue())
                ));

        moisBarChart.getData().clear();
        moisBarChart.getData().add(series);
    }

    private void chargerTopPublications(List<Publication> publications) {  // ✅ CORRIGÉ
        List<Publication> top5 = publications.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()))
                .limit(5)
                .collect(Collectors.toList());

        topPublicationsList.getChildren().clear();  // ✅ CORRIGÉ

        for (int i = 0; i < top5.size(); i++) {
            Publication p = top5.get(i);
            Label item = new Label((i + 1) + ". " + p.getTitre() + " - ❤️ " + p.getLikes() + " likes");
            item.setStyle("-fx-padding: 5; -fx-background-color: #f1f5f9; -fx-background-radius: 5;");
            topPublicationsList.getChildren().add(item);
        }
    }

    private void chargerDernieresActivites(List<Publication> publications) {  // ✅ CORRIGÉ
        List<Publication> recentes = publications.stream()
                .sorted((p1, p2) -> p2.getDateCreation().compareTo(p1.getDateCreation()))
                .limit(5)
                .collect(Collectors.toList());

        activitesList.getChildren().clear();

        for (Publication p : recentes) {
            Label item = new Label("📰 " + p.getTitre() + " - " +
                    p.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            item.setStyle("-fx-padding: 5; -fx-background-color: #f1f5f9; -fx-background-radius: 5;");
            activitesList.getChildren().add(item);
        }
    }
}