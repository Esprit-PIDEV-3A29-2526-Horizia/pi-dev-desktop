package tn.esprit.services;

import tn.esprit.entities.Participation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceParticipation implements IService<Participation> {

    private Connection connection;

    public ServiceParticipation() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Participation p) throws SQLException {
        String sql = "INSERT INTO `participation`(`id_event`, `nombre_places`, `montant_total`, `statut`, `date_participation`) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getId_event());
        ps.setInt(2, p.getNombrePlaces());
        ps.setFloat(3, p.getMontantTotal());
        ps.setString(4, p.getStatut());
        ps.setTimestamp(5, p.getDateParticipation());

        ps.executeUpdate();
        System.out.println("✅ Participation ajoutée avec succès!");
    }

    @Override
    public void modifier(Participation p) throws SQLException {
        String sql = "UPDATE `participation` SET `nombre_places`=?, `montant_total`=?, `statut`=? WHERE id_participation=?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getNombrePlaces());
        ps.setDouble(2, p.getMontantTotal());
        ps.setString(3, p.getStatut());
        ps.setInt(4, p.getId_participation());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM participation WHERE id_participation = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public void updatePlaces(int id_event, int Places_Restantes) throws SQLException {
        // This method is for events, not participations
    }

    @Override
    public List<Participation> afficher() throws SQLException {
        List<Participation> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId_event(rs.getInt("id_event"));
            p.setNombrePlaces(rs.getInt("nombre_places"));
            p.setMontantTotal(rs.getFloat("montant_total"));
            p.setStatut(rs.getString("statut"));
            p.setDateParticipation(rs.getTimestamp("date_participation"));
            participations.add(p);
        }
        return participations;
    }

    @Override
    public List<Participation> rechercher(String keyword) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation WHERE statut LIKE ? OR id_event LIKE ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId_event(rs.getInt("id_event"));
            p.setNombrePlaces(rs.getInt("nombre_places"));
            p.setMontantTotal(rs.getFloat("montant_total"));
            p.setStatut(rs.getString("statut"));
            p.setDateParticipation(rs.getTimestamp("date_participation"));
            participations.add(p);
        }
        return participations;
    }

    @Override
    public List<Participation> trier(String column, String order) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        List<String> allowedColumns = List.of("id_participation","id_event","nombre_places","montant_total","statut","date_participation");
        if (!allowedColumns.contains(column)) column = "id_participation";
        if (!order.equalsIgnoreCase("ASC") && !order.equalsIgnoreCase("DESC")) order = "ASC";

        String sql = "SELECT * FROM participation ORDER BY " + column + " " + order;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId_event(rs.getInt("id_event"));
            p.setNombrePlaces(rs.getInt("nombre_places"));
            p.setMontantTotal(rs.getFloat("montant_total"));
            p.setStatut(rs.getString("statut"));
            p.setDateParticipation(rs.getTimestamp("date_participation"));
            participations.add(p);
        }
        return participations;
    }
}