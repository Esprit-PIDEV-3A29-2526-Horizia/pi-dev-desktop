package tn.esprit.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            // Configuration de la base de données
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/gestionevents");
            configuration.setProperty("hibernate.connection.username", "root");
            configuration.setProperty("hibernate.connection.password", "");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            configuration.setProperty("hibernate.show_sql", "false");

            // Configuration Hibernate Search
            configuration.setProperty("hibernate.search.backend.type", "lucene");
            configuration.setProperty("hibernate.search.backend.directory.root", "./lucene-indexes");
            configuration.setProperty("hibernate.search.backend.analysis.configurer", "tn.esprit.utils.FrenchAnalysisConfigurer");

            // Ajouter les classes annotées
            configuration.addAnnotatedClass(Events.class);
            configuration.addAnnotatedClass(Participation.class);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Erreur Hibernate: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}