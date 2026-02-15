package tn.esprit.services;

import tn.esprit.utils.Database;
import tn.esprit.entities.Publication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePublication {

    private Connection cnx;

    public ServicePublication() {
        cnx = Database.getInstance().getCnx();
    }

    // ================= AFFICHER =================
    public List<Publication> afficher() throws SQLException {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Publication p = new Publication();
            p.setId(rs.getInt("id"));
            p.setTitre(rs.getString("titre"));
            p.setLieu(rs.getString("lieu"));
            p.setImage(rs.getString("image"));
            // Correction : Utilisation du nom de colonne 'description' au lieu de 'contenu'
            p.setContenu(rs.getString("description"));
            p.setTarif(rs.getFloat("tarif"));
            p.setEstPublie(rs.getBoolean("estPublie"));
            list.add(p);
        }
        return list;
    }

    // ================= RECHERCHER =================
    public List<Publication> rechercher(String keyword) throws SQLException {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication WHERE titre LIKE ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setString(1, "%" + keyword + "%");
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            Publication p = new Publication();
            p.setId(rs.getInt("id"));
            p.setTitre(rs.getString("titre"));
            p.setLieu(rs.getString("lieu"));
            p.setImage(rs.getString("image"));
            // Correction : Utilisation de 'description'
            p.setContenu(rs.getString("description"));
            p.setTarif(rs.getFloat("tarif"));
            p.setEstPublie(rs.getBoolean("estPublie"));
            list.add(p);
        }
        return list;
    }

    // ================= AJOUTER =================
    public void ajouter(Publication p) throws SQLException {
        // Correction : Utilisation de 'description' dans la requête INSERT
        String sql = "INSERT INTO publication (titre, lieu, image, description, tarif, estPublie) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setString(1, p.getTitre());
        pst.setString(2, p.getLieu());
        pst.setString(3, p.getImage());
        pst.setString(4, p.getContenu()); // La valeur de p.getContenu() ira dans la colonne 'description'
        pst.setFloat(5, p.getTarif());
        pst.setBoolean(6, p.isEstPublie());
        pst.executeUpdate();
    }

    // ================= MODIFIER =================
    public void modifier(Publication p) throws SQLException {
        // Correction : UPDATE du champ 'description'
        String sql = "UPDATE publication SET titre=?, lieu=?, image=?, description=?, tarif=?, estPublie=? WHERE id=?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setString(1, p.getTitre());
        pst.setString(2, p.getLieu());
        pst.setString(3, p.getImage());
        pst.setString(4, p.getContenu());
        pst.setFloat(5, p.getTarif());
        pst.setBoolean(6, p.isEstPublie());
        pst.setInt(7, p.getId());
        pst.executeUpdate();
    }

    // ================= SUPPRIMER =================
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM publication WHERE id=?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();
    }
}