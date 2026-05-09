package tn.esprit.backend.services;

import tn.esprit.backend.entities.Commentaire;
import tn.esprit.backend.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommentaireService {

    private static final Logger LOGGER = Logger.getLogger(CommentaireService.class.getName());
    private final Connection conn;

    public CommentaireService() {
        this.conn = Database.getInstance().getCnx();
    }

    public void ajouter(Commentaire c) {
        String sql = "INSERT INTO commentaire (publication_id, utilisateur_id, auteur, contenu, date_creation, modifie) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, c.getPublicationId());
            pst.setInt(2, c.getUtilisateurId());
            pst.setString(3, c.getAuteur());
            pst.setString(4, c.getContenu());
            pst.setTimestamp(5, Timestamp.valueOf(c.getDateCreation()));
            pst.setBoolean(6, c.isModifie());
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
            }
            LOGGER.info("✅ Commentaire ajouté");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur ajout commentaire", e);
        }
    }

    public List<Commentaire> getByPublication(int publicationId) {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE publication_id = ? ORDER BY date_creation DESC";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, publicationId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur getByPublication", e);
        }
        return list;
    }

    // ✅ AJOUTÉ : Récupérer TOUS les commentaires (pour admin)
    public List<Commentaire> getAll() {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire ORDER BY date_creation DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            LOGGER.info("📋 " + list.size() + " commentaires chargés pour l'admin");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur getAll commentaires", e);
        }
        return list;
    }

    // ✅ AJOUTÉ : Rechercher des commentaires par mot-clé
    public List<Commentaire> rechercher(String keyword) {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE contenu LIKE ? OR auteur LIKE ? ORDER BY date_creation DESC";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            String search = "%" + keyword + "%";
            pst.setString(1, search);
            pst.setString(2, search);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur recherche commentaires", e);
        }
        return list;
    }

    public void modifier(Commentaire c) {
        String sql = "UPDATE commentaire SET contenu = ?, modifie = true WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, c.getContenu());
            pst.setInt(2, c.getId());
            pst.executeUpdate();
            LOGGER.info("✏️ Commentaire modifié");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur modification commentaire", e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            LOGGER.info("🗑️ Commentaire supprimé: " + id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur suppression commentaire", e);
        }
    }

    private Commentaire mapResultSet(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getInt("id"));
        c.setPublicationId(rs.getInt("publication_id"));
        c.setUtilisateurId(rs.getInt("utilisateur_id"));
        c.setAuteur(rs.getString("auteur"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        c.setModifie(rs.getBoolean("modifie"));
        return c;
    }
}