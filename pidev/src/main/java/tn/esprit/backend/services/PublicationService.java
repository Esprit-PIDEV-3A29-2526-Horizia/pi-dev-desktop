package tn.esprit.backend.services;

import tn.esprit.backend.entities.Categorie;
import tn.esprit.backend.entities.Publication;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicationService {

    private static final Logger LOGGER = Logger.getLogger(PublicationService.class.getName());
    private final Connection conn;

    public PublicationService() {
        this.conn = MyDataBase.getInstance().getMyConnection();
    }

    public void ajouter(Publication p) throws SQLException {
        String sql = "INSERT INTO publications (titre, description, image, categorie, utilisateur_id, auteur, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getTitre());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setString(4, p.getCategorie().name());
            pst.setInt(5, p.getUtilisateurId());
            pst.setString(6, p.getAuteur());
            pst.setTimestamp(7, Timestamp.valueOf(p.getDateCreation()));

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                p.setId(rs.getInt(1));
            }
            LOGGER.info("✅ Publication ajoutée: " + p.getTitre());
        }
    }

    public List<Publication> getAll() {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publications ORDER BY date_creation DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur getAll", e);
        }
        return list;
    }

    public Publication getById(int id) {
        String sql = "SELECT * FROM publications WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur getById", e);
        }
        return null;
    }

    public void modifier(Publication p) throws SQLException {
        String sql = "UPDATE publications SET titre=?, description=?, image=?, categorie=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, p.getTitre());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setString(4, p.getCategorie().name());
            pst.setInt(5, p.getId());
            pst.executeUpdate();
            LOGGER.info("✏️ Publication modifiée: " + p.getId());
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM publications WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            LOGGER.info("🗑️ Publication supprimée: " + id);
        }
    }

    public void incrementerLikes(int id) {
        String sql = "UPDATE publications SET likes = likes + 1 WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur likes", e);
        }
    }

    public boolean titreExiste(String titre) {
        return titreExiste(titre, -1);
    }

    public boolean titreExiste(String titre, int excludeId) {
        String sql = "SELECT COUNT(*) FROM publications WHERE titre = ? AND id != ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, titre);
            pst.setInt(2, excludeId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur titreExiste", e);
        }
        return false;
    }

    // Méthode publique pour mapper un ResultSet vers Publication
    public Publication mapResultSet(ResultSet rs) throws SQLException {
        Publication p = new Publication();
        p.setId(rs.getInt("id"));
        p.setTitre(rs.getString("titre"));
        p.setDescription(rs.getString("description"));
        p.setImage(rs.getString("image"));
        String catStr = rs.getString("categorie");
        p.setCategorie(catStr != null ? Categorie.valueOf(catStr) : Categorie.TOUS);
        p.setUtilisateurId(rs.getInt("utilisateur_id"));
        p.setAuteur(rs.getString("auteur"));
        p.setLikes(rs.getInt("likes"));
        p.setCommentaires(rs.getInt("commentaires"));
        p.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return p;
    }
}