package tn.esprit.services;

import org.hibernate.Session;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import tn.esprit.entities.Events;
import tn.esprit.utils.HibernateUtil;

import java.util.List;

public class SearchService {

    // Recherche avec Hibernate Search
    public List<Events> search(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            SearchSession searchSession = Search.session(session);

            return searchSession.search(Events.class)
                    .where(f -> f.match()
                            .fields("titre", "description", "categorie", "location")
                            .matching(keyword)
                            .fuzzy(2)
                    )
                    .fetchHits(20);
        } catch (Exception e) {
            System.err.println("Erreur de recherche: " + e.getMessage());
            return List.of();
        }
    }

    // Recherche avec filtre par catégorie
    public List<Events> searchByCategory(String keyword, String categorie) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            SearchSession searchSession = Search.session(session);

            return searchSession.search(Events.class)
                    .where(f -> f.bool()
                            .must(f.match()
                                    .fields("titre", "description")
                                    .matching(keyword)
                                    .fuzzy(2)
                            )
                            .must(f.match()
                                    .field("categorie")
                                    .matching(categorie)
                            )
                    )
                    .fetchHits(20);
        }
    }

    // Pour réindexer toutes les données
    public void reindexAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            SearchSession searchSession = Search.session(session);
            searchSession.massIndexer().startAndWait();
            System.out.println("✅ Indexation terminée");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}