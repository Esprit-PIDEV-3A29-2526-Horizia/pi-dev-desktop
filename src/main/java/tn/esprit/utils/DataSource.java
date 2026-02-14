package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private Connection cnx;
    private static DataSource instance;

    // Paramètres de connexion
    private final String URL = "jdbc:mysql://localhost:3306/votre_nom_bdd"; // Remplace par ton nom de BDD
    private final String USER = "root";
    private final String PASSWORD = "";

    // Le constructeur doit être privé
    private DataSource() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    // C'EST CETTE MÉTHODE QUI MANQUE PROBABLEMENT
    public Connection getCnx() {
        return cnx;
    }
}