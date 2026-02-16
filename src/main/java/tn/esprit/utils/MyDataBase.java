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

    //Le constructeur privé
    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    //Méthode pour récupérer l'unique instance de la classe
    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    //Méthode pour récupérer l'objet Connection
    public Connection getCnx() {
        return cnx;
    }
}