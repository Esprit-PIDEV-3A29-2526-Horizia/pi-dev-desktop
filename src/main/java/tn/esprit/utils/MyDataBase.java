//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private final String URL = "jdbc:mysql://localhost:3306/horizia";
    private final String USER = "root";
    private final String PSW = "";
    private Connection myConnection;
    private static MyDataBase instance;

    private MyDataBase() {
        try {
            this.myConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/horizia", "root", "");
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public Connection getMyConnection() {
        return this.myConnection;
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }

        return instance;
    }
}
