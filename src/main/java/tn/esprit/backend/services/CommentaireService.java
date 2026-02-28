package tn.esprit.backend.services;

import tn.esprit.backend.utils.Database;  // ✅ Fixed import
import tn.esprit.backend.entities.Commentaire;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommentaireService {

    private static final Logger LOGGER = Logger.getLogger(CommentaireService.class.getName());
    private final Connection conn;

    public CommentaireService() {
        conn = Database.getInstance().getCnx();
    }

    /**
     * ðŸ”¥ AJOUTER UN COMMENTAIRE - MÃ©thode principale
     */
    public void ajouter(Commentaire c) {
        String sql = "INSERT INTO commentaire (publication_id, utilisateur_id, auteur, contenu, date_creation, modifie) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Remplir les paramÃ¨tres
            pst.setInt(1, c.getPublicationId());
            pst.setInt(2, c.getUtilisateurId());
            pst.setString(3, c.getAuteur());
            pst.setString(4, c.getContenu());
            pst.setTimestamp(5, Timestamp.valueOf(c.getDateCreation()));
            pst.setBoolean(6, c.isModifie());

            // ExÃ©cuter l'insertion
            int affectedRows = pst.executeUpdate();

            // RÃ©cupÃ©rer l'ID gÃ©nÃ©rÃ©
            if (affectedRows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                    LOGGER.info("âœ… Commentaire ajoutÃ© avec ID: " + c.getId());
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "âŒ Erreur ajout commentaire", e);
            throw new RuntimeException("Erreur lors de l'ajout du commentaire: " + e.getMessage());
        }
    }

    /**
     * RÃ©cupÃ©rer les commentaires d'une publication
     */
    public List<Commentaire> getByPublication(int publicationId) {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE publication_id = ? ORDER BY date_creation DESC";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, publicationId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            LOGGER.info("ðŸ“‹ " + list.size() + " commentaires chargÃ©s pour publication " + publicationId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "âŒ Erreur chargement commentaires", e);
        }
        return list;
    }

    /**
     * Modifier un commentaire
     */
    public void modifier(Commentaire c) {
        String sql = "UPDATE commentaire SET contenu = ?, modifie = true WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, c.getContenu());
            pst.setInt(2, c.getId());

            pst.executeUpdate();
            LOGGER.info("âœï¸ Commentaire " + c.getId() + " modifiÃ©");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "âŒ Erreur modification commentaire", e);
        }
    }

    /**
     * Supprimer un commentaire
     */
    public void supprimer(int id) {
        String sql = "DELETE FROM commentaire WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            int affected = pst.executeUpdate();

            if (affected > 0) {
                LOGGER.info("ðŸ—‘ï¸ Commentaire " + id + " supprimÃ©");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "âŒ Erreur suppression commentaire", e);
        }
    }

    /**
     * Mapper ResultSet vers EntitÃ©
     */
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

