package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private final String URL = "jdbc:mysql://localhost:3306/horizia";
    private final String USER = "root";
    private final String PASSWORD = "";
    //L'instance statique pour le Singleton
    private static MyDataBase instance;
    private Connection cnx;

    private MyDataBase() {
        ouvrirConnexion();
    }
    private void ouvrirConnexion() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static synchronized  MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {

        try {
            if (cnx == null || cnx.isClosed()) {
                ouvrirConnexion(); // 🔥 réouvre si fermée
            }
        } catch (SQLException e) {
            System.err.println("Erreur check cnx : " + e.getMessage());
            ouvrirConnexion();
        }return cnx;
    }
}