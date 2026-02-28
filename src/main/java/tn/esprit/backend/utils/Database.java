package tn.esprit.backend.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Database instance;
    private Connection cnx;

    // ParamÃ¨tres de connexion - MODIFIEZ SELON VOTRE CONFIG
    private final String URL = "jdbc:mysql://localhost:3306/horizia";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Database() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion Ã©tablie avec succÃ¨s !");
        } catch (SQLException ex) {
            System.err.println("Erreur de connexion: " + ex.getMessage());
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}

