package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String URL = "jdbc:mysql://localhost:3306/horizia?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PSW = "";

    private Connection myConnection;
    private static MyDataBase instance;

    private MyDataBase() {
        try {
            // Charger explicitement le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connexion à la base
            myConnection = DriverManager.getConnection(URL, USER, PSW);
            System.out.println(" Connexion à la base Horizia établie !");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(" Erreur connexion DB : " + e.getMessage());
        }
    }

    public Connection getMyConnection() {
        return myConnection;
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
        return instance;
    }
}
