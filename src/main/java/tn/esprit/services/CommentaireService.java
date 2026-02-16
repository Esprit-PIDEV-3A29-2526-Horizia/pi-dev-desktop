package tn.esprit.services;

import tn.esprit.entities.Commentaire;
import tn.esprit.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private Connection conn;

    public CommentaireService() {
        conn = Database.getInstance().getCnx();
    }

    public void ajouter(Commentaire c) {
        String sql = "INSERT INTO commentaire (publication_id, auteur, contenu, date_creation) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, c.getPublicationId());
            pst.setString(2, c.getAuteur());
            pst.setString(3, c.getContenu());
            pst.setTimestamp(4, Timestamp.valueOf(c.getDateCreation()));

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
            }
            System.out.println("✅ Commentaire ajouté");

        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout commentaire: " + e.getMessage());
        }
    }

    public void modifier(Commentaire c) {
        String sql = "UPDATE commentaire SET auteur=?, contenu=? WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, c.getAuteur());
            pst.setString(2, c.getContenu());
            pst.setInt(3, c.getId());

            pst.executeUpdate();
            System.out.println("✅ Commentaire modifié");

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM commentaire WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("✅ Commentaire supprimé");

        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression: " + e.getMessage());
        }
    }

    // Récupérer les commentaires d'une publication
    public List<Commentaire> getByPublication(int publicationId) {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE publication_id = ? ORDER BY date_creation DESC";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, publicationId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(mapCommentaire(rs));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
        return list;
    }

    public Commentaire getById(int id) {
        String sql = "SELECT * FROM commentaire WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapCommentaire(rs);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
        return null;
    }

    private Commentaire mapCommentaire(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getInt("id"));
        c.setPublicationId(rs.getInt("publication_id"));
        c.setAuteur(rs.getString("auteur"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return c;
    }
}