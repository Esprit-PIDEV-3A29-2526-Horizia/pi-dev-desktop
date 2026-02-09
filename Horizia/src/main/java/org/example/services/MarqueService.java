package org.example.services;

import org.example.entities.Marque;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarqueService {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ───────────────────────────────────────────────
    // Vérifier si une marque existe déjà par nom
    // ───────────────────────────────────────────────
    private boolean existeDeja(String nomMarque) {
        if (nomMarque == null || nomMarque.trim().isEmpty()) {
            System.out.println("Erreur : Le nom de la marque ne peut pas être vide.");
            return true; // bloque l'ajout
        }

        String sql = "SELECT COUNT(*) FROM marque WHERE LOWER(TRIM(nom_marque)) = LOWER(?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomMarque.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification unicité marque : " + e.getMessage());
        }
        return false;
    }

    // ───────────────────────────────────────────────
    // Ajouter une nouvelle marque
    // ───────────────────────────────────────────────
    public boolean ajouterMarque(Marque marque) {
        if (marque == null || marque.getNomMarque() == null || marque.getNomMarque().trim().isEmpty()) {
            System.out.println("Erreur : Nom de marque invalide ou vide.");
            return false;
        }

        String nom = marque.getNomMarque().trim();

        if (existeDeja(nom)) {
            System.out.println("Erreur : La marque '" + nom + "' existe déjà dans la base.");
            return false;
        }

        String sql = "INSERT INTO marque (nom_marque) VALUES (?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nom);

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    marque.setIdMarque(rs.getInt(1));
                }
                System.out.println("Succès : Marque '" + nom + "' ajoutée (ID: " + marque.getIdMarque() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout marque : " + e.getMessage());
        }
        return false;
    }

    // ───────────────────────────────────────────────
    // Lister toutes les marques (avec tri alphabétique)
    // ───────────────────────────────────────────────
    public List<Marque> getAllMarques(String tri) {
        List<Marque> marques = new ArrayList<>();

        String orderBy = "ASC";
        if ("DESC".equalsIgnoreCase(tri)) {
            orderBy = "DESC";
        }

        String sql = "SELECT * FROM marque ORDER BY nom_marque " + orderBy;

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Marque m = new Marque();
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomMarque(rs.getString("nom_marque"));
                marques.add(m);
            }

            if (marques.isEmpty()) {
                System.out.println("Aucune marque présente dans la base pour le moment.");
            } else {
                System.out.println("Marques trouvées (" + marques.size() + ") - triées " + orderBy + " :");
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération marques : " + e.getMessage());
        }

        return marques;
    }

    // Méthodes de commodité pour tri
    public List<Marque> getAllMarquesAlphabetique() {
        return getAllMarques("ASC");
    }

    public List<Marque> getAllMarquesInverseAlphabetique() {
        return getAllMarques("DESC");
    }

    // ───────────────────────────────────────────────
    // Rechercher une marque par nom (partiel)
    // ───────────────────────────────────────────────
    public List<Marque> rechercherMarqueParNom(String recherche) {
        List<Marque> resultat = new ArrayList<>();

        if (recherche == null || recherche.trim().isEmpty()) {
            System.out.println("Recherche vide → affichage de toutes les marques.");
            return getAllMarques("ASC");
        }

        String sql = "SELECT * FROM marque WHERE LOWER(nom_marque) LIKE LOWER(?) ORDER BY nom_marque ASC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + recherche.trim() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Marque m = new Marque();
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomMarque(rs.getString("nom_marque"));
                resultat.add(m);
            }

            if (resultat.isEmpty()) {
                System.out.println("Aucune marque trouvée contenant '" + recherche + "'.");
            } else {
                System.out.println(resultat.size() + " marque(s) trouvée(s) pour '" + recherche + "'");
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche marque : " + e.getMessage());
        }

        return resultat;
    }

    // ───────────────────────────────────────────────
    // Récupérer une marque par ID
    // ───────────────────────────────────────────────
    public Marque getMarqueById(int id) {
        if (id <= 0) {
            System.out.println("ID invalide (doit être > 0).");
            return null;
        }

        String sql = "SELECT * FROM marque WHERE id_marque = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Marque m = new Marque();
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomMarque(rs.getString("nom_marque"));
                return m;
            } else {
                System.out.println("Aucune marque trouvée avec l'ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getMarqueById : " + e.getMessage());
        }
        return null;
    }
}