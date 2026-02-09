package org.example.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseConnection {
    private final String URL = "jdbc:mysql://localhost:3306/horizia?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private final String USER = "root";
    private final String PSW = "";
    private Connection myConnection;

    private static DatabaseConnection instance ;
    private DatabaseConnection() {
        try {
            myConnection = DriverManager.getConnection(URL, USER, PSW);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("ÉCHEC CONNEXION DATABASE !");
            System.err.println("Message : " + e.getMessage());
            System.err.println("SQL State : " + e.getSQLState());
            System.err.println("Error Code : " + e.getErrorCode());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (myConnection == null || myConnection.isClosed() || !myConnection.isValid(2)) {
                System.out.println("Reconnexion automatique...");
                myConnection = DriverManager.getConnection(URL, USER, PSW);
            }
        } catch (SQLException e) {
            System.err.println("Reconnexion échouée : " + e.getMessage());
        }
        return myConnection;
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();

        } return instance;
    }



}
