package tn.esprit.tests;

import tn.esprit.utils.MyDataBase;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Test de la connexion
        Connection conn = MyDataBase.getInstance().getMyConnection();

        if (conn != null) {
            System.out.println("✅ Connexion réussie à la base Horiza");
        } else {
            System.out.println("❌ Échec de connexion");
        }
    }
}