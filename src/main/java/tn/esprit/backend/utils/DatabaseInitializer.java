package tn.esprit.backend.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        Connection conn = Database.getInstance().getCnx();

        String[] sql = {
                "CREATE DATABASE IF NOT EXISTS horozia",  // ← CHANGÉ (horozia au lieu de horizia)
                "USE horozia",                            // ← CHANGÉ (horozia)

                "CREATE TABLE IF NOT EXISTS publication (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "titre VARCHAR(255) NOT NULL," +
                        "description TEXT," +
                        "image VARCHAR(500)," +
                        "categorie VARCHAR(50)," +
                        "utilisateur_id INT DEFAULT 0," +
                        "auteur VARCHAR(100)," +
                        "likes INT DEFAULT 0," +
                        "commentaires INT DEFAULT 0," +
                        "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                "CREATE TABLE IF NOT EXISTS commentaire (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "publication_id INT NOT NULL," +
                        "utilisateur_id INT DEFAULT 0," +
                        "auteur VARCHAR(100)," +
                        "contenu TEXT NOT NULL," +
                        "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "modifie BOOLEAN DEFAULT FALSE," +
                        "FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE)",

                "CREATE TABLE IF NOT EXISTS utilisateur (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "nom VARCHAR(100)," +
                        "prenom VARCHAR(100)," +
                        "email VARCHAR(255) UNIQUE NOT NULL," +
                        "mot_de_passe VARCHAR(255)," +
                        "telephone VARCHAR(20)," +
                        "adresse VARCHAR(255)," +
                        "role VARCHAR(20) DEFAULT 'USER'," +
                        "image_profil VARCHAR(500)," +
                        "date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "actif BOOLEAN DEFAULT TRUE)",

                "CREATE TABLE IF NOT EXISTS favoris (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id INT NOT NULL," +
                        "publication_id INT NOT NULL," +
                        "date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE," +
                        "UNIQUE KEY unique_favori (user_id, publication_id))",

                // Vérifier et ajouter la colonne ville si elle n'existe pas
                "ALTER TABLE publication ADD COLUMN IF NOT EXISTS ville VARCHAR(100)",

                // Vérifier et ajouter la colonne pays si elle n'existe pas
                "ALTER TABLE publication ADD COLUMN IF NOT EXISTS pays VARCHAR(100)"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String s : sql) {
                try {
                    stmt.execute(s);
                } catch (SQLException e) {
                    // Ignorer les erreurs "Duplicate column" etc.
                    if (!e.getMessage().contains("Duplicate column")) {
                        System.err.println("⚠️ Erreur: " + e.getMessage());
                    }
                }
            }
            System.out.println("✅ Base de données initialisée!");
        } catch (SQLException e) {
            System.err.println("❌ Erreur initialisation: " + e.getMessage());
        }
    }
}