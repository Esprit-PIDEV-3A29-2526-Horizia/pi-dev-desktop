package org.example.services;

import org.example.entities.Modele;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModeleService {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ───────────────────────────────────────────────
    // Vérifier si un modèle existe déjà pour une marque donnée
    // ───────────────────────────────────────────────
    private boolean existeDeja(int idMarque, String nomModele) {
        if (nomModele == null || nomModele.trim().isEmpty()) {
            System.out.println("Erreur : Le nom du modèle ne peut pas être vide.");
            return true;
        }

        String sql = "SELECT COUNT(*) FROM modele WHERE id_marque = ? AND LOWER(TRIM(nom_modele)) = LOWER(?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMarque);
            ps.setString(2, nomModele.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification unicité modèle : " + e.getMessage());
        }
        return false;
    }

    // ───────────────────────────────────────────────
    // Vérifier si la marque existe (avant d'ajouter un modèle)
    // ───────────────────────────────────────────────
    private boolean marqueExiste(int idMarque) {
        String sql = "SELECT COUNT(*) FROM marque WHERE id_marque = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMarque);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification existence marque : " + e.getMessage());
        }
        return false;
    }

    // ───────────────────────────────────────────────
    // Ajouter un nouveau modèle
    // ───────────────────────────────────────────────
    public boolean ajouterModele(Modele modele) {
        if (modele == null || modele.getNomModele() == null || modele.getNomModele().trim().isEmpty()) {
            System.out.println("Erreur : Nom du modèle invalide ou vide.");
            return false;
        }

        if (modele.getIdMarque() <= 0) {
            System.out.println("Erreur : ID de marque invalide (doit être > 0).");
            return false;
        }

        String nom = modele.getNomModele().trim();

        // 1. Vérifier que la marque existe
        if (!marqueExiste(modele.getIdMarque())) {
            System.out.println("Erreur : La marque avec ID " + modele.getIdMarque() + " n'existe pas.");
            return false;
        }

        // 2. Vérifier unicité du nom pour cette marque
        if (existeDeja(modele.getIdMarque(), nom)) {
            System.out.println("Erreur : Le modèle '" + nom + "' existe déjà pour cette marque.");
            return false;
        }

        String sql = "INSERT INTO modele (id_marque, nom_modele) VALUES (?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, modele.getIdMarque());
            ps.setString(2, nom);

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    modele.setIdModele(rs.getInt(1));
                }
                System.out.println("Succès : Modèle '" + nom + "' ajouté pour la marque ID " + modele.getIdMarque() +
                        " (ID modèle : " + modele.getIdModele() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout modèle : " + e.getMessage());
        }
        return false;
    }

    // ───────────────────────────────────────────────
    // Lister tous les modèles (tri par nom)
    // ───────────────────────────────────────────────
    public List<Modele> getAllModeles() {
        List<Modele> modeles = new ArrayList<>();
        String sql = "SELECT m.*, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "ORDER BY ma.nom_marque ASC, m.nom_modele ASC";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                modeles.add(m);

                // Affichage enrichi avec nom marque
                System.out.println("Modèle : " + m.getNomModele() + " | Marque : " + rs.getString("nom_marque"));
            }

            if (modeles.isEmpty()) {
                System.out.println("Aucun modèle présent dans la base.");
            } else {
                System.out.println("Nombre de modèles trouvés : " + modeles.size());
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération modèles : " + e.getMessage());
        }

        return modeles;
    }

    // ───────────────────────────────────────────────
    // Rechercher un modèle par nom (partiel)
    // ───────────────────────────────────────────────
    public List<Modele> rechercherModeleParNom(String recherche) {
        List<Modele> resultat = new ArrayList<>();

        if (recherche == null || recherche.trim().isEmpty()) {
            System.out.println("Recherche vide → affichage de tous les modèles.");
            return getAllModeles();
        }

        String sql = "SELECT m.*, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "WHERE LOWER(m.nom_modele) LIKE LOWER(?) " +
                "ORDER BY ma.nom_marque ASC, m.nom_modele ASC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + recherche.trim() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                resultat.add(m);

                System.out.println("Modèle : " + m.getNomModele() + " | Marque : " + rs.getString("nom_marque"));
            }

            if (resultat.isEmpty()) {
                System.out.println("Aucun modèle trouvé contenant '" + recherche + "'.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche modèle : " + e.getMessage());
        }

        return resultat;
    }

    // ───────────────────────────────────────────────
    // Récupérer un modèle par ID
    // ───────────────────────────────────────────────
    public Modele getModeleById(int id) {
        if (id <= 0) {
            System.out.println("ID invalide (doit être > 0).");
            return null;
        }

        String sql = "SELECT m.*, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "WHERE m.id_modele = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                System.out.println("Modèle trouvé : " + m.getNomModele() + " (Marque ID: " + m.getIdMarque() + ")");
                return m;
            } else {
                System.out.println("Aucun modèle trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getModeleById : " + e.getMessage());
        }
        return null;
    }

    // Recherche des modèles par nom de marque (partielle)
    public List<Modele> rechercherModelesParNomMarque(String nomMarqueRecherche) {
        List<Modele> resultat = new ArrayList<>();

        if (nomMarqueRecherche == null || nomMarqueRecherche.trim().isEmpty()) {
            System.out.println("Recherche vide → retour de tous les modèles.");
            return getAllModeles();
        }

        String sql = "SELECT m.*, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "WHERE LOWER(ma.nom_marque) LIKE LOWER(?) " +
                "ORDER BY ma.nom_marque ASC, m.nom_modele ASC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nomMarqueRecherche.trim() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                resultat.add(m);
            }

            if (resultat.isEmpty()) {
                System.out.println("Aucun modèle trouvé pour une marque contenant '" + nomMarqueRecherche + "'.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche modèles par marque : " + e.getMessage());
        }

        return resultat;
    }
}