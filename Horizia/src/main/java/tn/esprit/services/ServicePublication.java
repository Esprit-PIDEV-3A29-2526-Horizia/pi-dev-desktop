package tn.esprit.services;

import tn.esprit.entities.Publication;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ServicePublication implements IService<Publication> {

    private Connection cnx;

    public ServicePublication() {
        cnx = MyDataBase.getInstance().getMyConnection();
    }

    // ======================== AJOUTER ========================
    @Override
    public void ajouter(Publication p) throws SQLException {
        String sql = "INSERT INTO publication(titre, description, destination, date_publication) VALUES (?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, p.getTitre());
        ps.setString(2, p.getDescription());
        ps.setString(3, p.getDestination());
        ps.setDate(4, java.sql.Date.valueOf(p.getDatePublication()));
        ps.executeUpdate();
    }

    // ======================== MODIFIER ========================
    @Override
    public void modifier(Publication p) throws SQLException {
        String sql = "UPDATE publication SET titre=?, description=?, destination=?, date_publication=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, p.getTitre());
        ps.setString(2, p.getDescription());
        ps.setString(3, p.getDestination());
        ps.setDate(4, java.sql.Date.valueOf(p.getDatePublication()));
        ps.setInt(5, p.getId());
        ps.executeUpdate();
    }

    // ======================== SUPPRIMER ========================
    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM publication WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ======================== AFFICHER ========================
    @Override
    public List<Publication> afficher() throws SQLException {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(new Publication(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getString("destination"),
                    rs.getDate("date_publication").toLocalDate()
            ));
        }
        return list;
    }

    // ======================== RECHERCHE / FILTRE ========================
    public List<Publication> rechercherParDestination(String dest) throws SQLException {
        return afficher().stream()
                .filter(p -> p.getDestination().equalsIgnoreCase(dest))
                .collect(Collectors.toList());
    }

    // ======================== TRI ========================
    public List<Publication> trierParDate() throws SQLException {
        return afficher().stream()
                .sorted(Comparator.comparing(Publication::getDatePublication))
                .collect(Collectors.toList());
    }

}
