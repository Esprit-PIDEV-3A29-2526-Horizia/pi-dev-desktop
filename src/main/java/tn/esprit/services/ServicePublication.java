package tn.esprit.services;

import tn.esprit.entities.Publication;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePublication {

    private Connection cnx;

    public ServicePublication() {
        try {
            cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/voyage", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ajouter une publication
    public void ajouter(Publication p) throws SQLException {
        String req = "INSERT INTO publication(titre, contenu, image, lieu, tarif, actif, type) VALUES(?,?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getTitre());
        ps.setString(2, p.getContenu());  // contenu au lieu de description
        ps.setString(3, p.getImage());
        ps.setString(4, p.getLieu());
        ps.setFloat(5, p.getTarif());
        ps.setBoolean(6, p.isActif());
        ps.setString(7, p.getType());
        ps.executeUpdate();
    }

    // Afficher toutes les publications
    public List<Publication> afficher() throws SQLException {
        List<Publication> publications = new ArrayList<>();
        String req = "SELECT * FROM publication";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Publication p = new Publication();
            p.setId(rs.getInt("id"));
            p.setTitre(rs.getString("titre"));
            p.setContenu(rs.getString("contenu")); // contenu au lieu de description
            p.setImage(rs.getString("image"));
            p.setLieu(rs.getString("lieu"));
            p.setTarif(rs.getFloat("tarif"));
            p.setActif(rs.getBoolean("actif"));
            p.setType(rs.getString("type"));
            // Tu peux ajouter la date si tu as un champ datePublication dans la table
            // p.setDatePublication(rs.getDate("datePublication").toLocalDate());
            publications.add(p);
        }
        return publications;
    }
}
