package tn.esprit.services;

import tn.esprit.entities.Publication;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavorisService {

    private Connection connection;

    public FavorisService() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    // Ajouter un favori - Sans date_ajout
    public boolean ajouterFavori(int utilisateurId, int publicationId) throws SQLException {
        if (estFavori(utilisateurId, publicationId)) {
            return false;
        }

        // Supprimer la colonne date_ajout si elle n'existe pas
        String sql = "INSERT INTO favoris (utilisateur_id, publication_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ps.setInt(2, publicationId);
            return ps.executeUpdate() > 0;
        }
    }

    // Supprimer un favori
    public boolean supprimerFavori(int utilisateurId, int publicationId) throws SQLException {
        String sql = "DELETE FROM favoris WHERE utilisateur_id = ? AND publication_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ps.setInt(2, publicationId);
            return ps.executeUpdate() > 0;
        }
    }

    // Vérifier si une publication est en favori
    public boolean estFavori(int utilisateurId, int publicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM favoris WHERE utilisateur_id = ? AND publication_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ps.setInt(2, publicationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    // Récupérer tous les favoris d'un utilisateur - Sans ORDER BY date_ajout
    public List<Publication> getFavorisByUtilisateur(int utilisateurId) throws SQLException {
        List<Publication> favoris = new ArrayList<>();
        // Supprimer ORDER BY date_ajout si la colonne n'existe pas
        String sql = "SELECT p.* FROM publication p " +
                "INNER JOIN favoris f ON p.id = f.publication_id " +
                "WHERE f.utilisateur_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Publication p = new Publication();
                p.setId(rs.getInt("id"));
                p.setTitre(rs.getString("titre"));
                p.setDescription(rs.getString("description"));
                p.setImage(rs.getString("image"));
                p.setLikes(rs.getInt("likes"));
                p.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                p.setAuteur(rs.getString("auteur"));
                favoris.add(p);
            }
        }
        return favoris;
    }

    // Récupérer les IDs des publications favoris d'un utilisateur
    public List<Integer> getFavorisIdsByUtilisateur(int utilisateurId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT publication_id FROM favoris WHERE utilisateur_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("publication_id"));
            }
        }
        return ids;
    }
}