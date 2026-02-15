package tn.esprit.services;

import tn.esprit.entities.Publication;
import tn.esprit.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationService {

    private Connection conn;

    public PublicationService() {
        conn = Database.getInstance().getCnx();
    }

    public void ajouter(Publication p) {
        String sql = "INSERT INTO publication (titre, description, image, date_publication) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getTitre());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setTimestamp(4, Timestamp.valueOf(p.getDatePublication()));

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                p.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            System.out.println("Erreur ajout: " + e.getMessage());
        }
    }

    public void modifier(Publication p) {
        String sql = "UPDATE publication SET titre=?, description=?, image=? WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, p.getTitre());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setInt(4, p.getId());

            pst.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur modification: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM publication WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur suppression: " + e.getMessage());
        }
    }

    public Publication getById(int id) {
        String sql = "SELECT * FROM publication WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapPublication(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur recherche: " + e.getMessage());
        }
        return null;
    }

    public List<Publication> getAll() {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication ORDER BY date_publication DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapPublication(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur liste: " + e.getMessage());
        }
        return list;
    }

    public List<Publication> rechercherParTitre(String titre) {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication WHERE titre LIKE ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, "%" + titre + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(mapPublication(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur recherche: " + e.getMessage());
        }
        return list;
    }

    private Publication mapPublication(ResultSet rs) throws SQLException {
        Publication p = new Publication();
        p.setId(rs.getInt("id"));
        p.setTitre(rs.getString("titre"));
        p.setDescription(rs.getString("description"));
        p.setImage(rs.getString("image"));
        p.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());
        return p;
    }
}