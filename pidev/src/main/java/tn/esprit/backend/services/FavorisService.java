package tn.esprit.backend.services;

import tn.esprit.backend.entities.Publication;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class FavorisService {

    private final Connection conn;

    public FavorisService() {
        this.conn = MyDataBase.getInstance().getMyConnection();
    }

    // Ajouter un favori
    public void ajouterFavori(int utilisateurId, int publicationId) throws SQLException {
        String sql = "INSERT INTO favoris (utilisateur_id, publication_id) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, utilisateurId);
            pst.setInt(2, publicationId);
            pst.executeUpdate();
        }
    }

    // Supprimer un favori
    public void supprimerFavori(int utilisateurId, int publicationId) throws SQLException {
        String sql = "DELETE FROM favoris WHERE utilisateur_id = ? AND publication_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, utilisateurId);
            pst.setInt(2, publicationId);
            pst.executeUpdate();
        }
    }

    // Vérifier si une publication est en favori
    public boolean estFavori(int utilisateurId, int publicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM favoris WHERE utilisateur_id = ? AND publication_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, utilisateurId);
            pst.setInt(2, publicationId);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Récupérer les publications favorites d'un utilisateur
    public List<Publication> getFavorisByUtilisateur(int utilisateurId) throws SQLException {
        List<Publication> favoris = new ArrayList<>();
        String sql = "SELECT p.* FROM publications p JOIN favoris f ON p.id = f.publication_id WHERE f.utilisateur_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, utilisateurId);
            ResultSet rs = pst.executeQuery();
            PublicationService pubService = new PublicationService();
            while (rs.next()) {
                favoris.add(pubService.mapResultSet(rs));
            }
        }
        return favoris;
    }
}