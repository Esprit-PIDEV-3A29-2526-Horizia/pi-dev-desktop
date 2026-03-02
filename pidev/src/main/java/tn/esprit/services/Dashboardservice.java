package tn.esprit.services;

import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Dashboardservice {

    private Connection getConn() throws SQLException {
        return MyDataBase.getInstance().getMyConnection();
    }

    /**
     * Récupère le modèle de voiture le plus loué
     * @return Map avec "nomMarque", "nomModele", "nombreLocations"
     */
    public Map<String, Object> getModeleLesPlusLoue() {
        Map<String, Object> resultat = new HashMap<>();

        String sql = "SELECT ma.nom_marque, mo.nom_modele, COUNT(l.id_location) as nb_locations " +
                "FROM location l " +
                "INNER JOIN vehicule v ON l.id_vehicule = v.id_vehicule " +
                "INNER JOIN modele mo ON v.id_modele = mo.id_modele " +
                "INNER JOIN marque ma ON mo.id_marque = ma.id_marque " +
                "GROUP BY ma.nom_marque, mo.nom_modele " +
                "ORDER BY nb_locations DESC " +
                "LIMIT 1";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                resultat.put("nomMarque", rs.getString("nom_marque"));
                resultat.put("nomModele", rs.getString("nom_modele"));
                resultat.put("nombreLocations", rs.getInt("nb_locations"));
            } else {
                resultat.put("nomMarque", "Aucun");
                resultat.put("nomModele", "");
                resultat.put("nombreLocations", 0);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération modèle le plus loué : " + e.getMessage());
            resultat.put("nomMarque", "Erreur");
            resultat.put("nomModele", "");
            resultat.put("nombreLocations", 0);
        }

        return resultat;
    }

    /**
     * Récupère le nombre total de locations actives (réservée + en_cours)
     */
    public int getNombreLocationsActives() {
        String sql = "SELECT COUNT(*) as total FROM location WHERE statut IN ('réservée', 'en_cours')";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération locations actives : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Récupère le nombre de véhicules disponibles
     */
    public int getNombreVehiculesDisponibles() {
        String sql = "SELECT COUNT(*) as total FROM vehicule WHERE etat = 'disponible'";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicules disponibles : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Récupère le nombre de véhicules loués
     */
    public int getNombreVehiculesLoues() {
        String sql = "SELECT COUNT(*) as total FROM vehicule WHERE etat = 'louee'";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicules loués : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Récupère les données pour le graphique : nombre de locations par modèle (top 5)
     * @return Map<String, Integer> avec clé = "Marque Modèle" et valeur = nombre de locations
     */
    public Map<String, Integer> getTop5ModelesLoues() {
        Map<String, Integer> resultat = new LinkedHashMap<>();

        String sql = "SELECT ma.nom_marque, mo.nom_modele, COUNT(l.id_location) as nb_locations " +
                "FROM location l " +
                "INNER JOIN vehicule v ON l.id_vehicule = v.id_vehicule " +
                "INNER JOIN modele mo ON v.id_modele = mo.id_modele " +
                "INNER JOIN marque ma ON mo.id_marque = ma.id_marque " +
                "GROUP BY ma.nom_marque, mo.nom_modele " +
                "ORDER BY nb_locations DESC " +
                "LIMIT 5";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String label = rs.getString("nom_marque") + " " + rs.getString("nom_modele");
                int count = rs.getInt("nb_locations");
                resultat.put(label, count);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération top 5 modèles : " + e.getMessage());
        }

        return resultat;
    }

    /**
     * Récupère la répartition des locations par statut
     * @return Map<String, Integer> avec clé = statut et valeur = nombre
     */
    public Map<String, Integer> getLocationsParStatut() {
        Map<String, Integer> resultat = new LinkedHashMap<>();

        String sql = "SELECT statut, COUNT(*) as nb FROM location GROUP BY statut ORDER BY nb DESC";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String statut = rs.getString("statut");
                int count = rs.getInt("nb");
                resultat.put(statut, count);
            }

            // Debug: Afficher les statuts récupérés
            System.out.println("=== STATUTS RÉCUPÉRÉS ===");
            for (Map.Entry<String, Integer> entry : resultat.entrySet()) {
                System.out.println("  " + entry.getKey() + " : " + entry.getValue());
            }
            System.out.println("==========================");

        } catch (SQLException e) {
            System.err.println("Erreur récupération locations par statut : " + e.getMessage());
        }

        return resultat;
    }
}