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
        String sql = "INSERT INTO `participation`(`id_event`, `user_id`, `nombre_places`, `montant_total`, `statut`, `date_participation`, `email_snapshot`, `nom_snapshot`, `prenom_snapshot`, `telephone_snapshot`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, p.getId_event());
        ps.setInt(2, p.getUser_id());
        ps.setInt(3, p.getNombre_places());
        ps.setDouble(4, p.getMontant_total());
        ps.setString(5, p.getStatut());
        ps.setTimestamp(6, p.getDate_participation());
        ps.setString(7, p.getEmail_snapshot());
        ps.setString(8, p.getNom_snapshot());
        ps.setString(9, p.getPrenom_snapshot());
        ps.setString(10, p.getTelephone_snapshot());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            p.setId_participation(rs.getInt(1));
        }

        System.out.println("✅ Participation ajoutée avec succès!");
        RefreshService.refreshAll();

        NotificationService.getInstance().addNotification(
                "Nouvelle réservation",
                p.getNombre_places() + " place(s) réservée(s) pour l'événement #" + p.getId_event(),
                NotificationService.NotificationType.SUCCESS,
                p.getId_event(),
                "event"
        );
    }

    @Override
    public void modifier(Participation p) throws SQLException {
        String sql = "UPDATE `participation` SET `nombre_places`=?, `montant_total`=?, `statut`=?, `email_snapshot`=?, `nom_snapshot`=?, `prenom_snapshot`=?, `telephone_snapshot`=? WHERE id_participation=?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getNombre_places());
        ps.setDouble(2, p.getMontant_total());
        ps.setString(3, p.getStatut());
        ps.setString(4, p.getEmail_snapshot());
        ps.setString(5, p.getNom_snapshot());
        ps.setString(6, p.getPrenom_snapshot());
        ps.setString(7, p.getTelephone_snapshot());
        ps.setInt(8, p.getId_participation());

        ps.executeUpdate();

        RefreshService.refreshAll();

        NotificationService.getInstance().addNotification(
                "Réservation modifiée",
                "La réservation #" + p.getId_participation() + " a été modifiée",
                NotificationService.NotificationType.INFO,
                p.getId_event(),
                "event"
        );
    }

    @Override
    public void supprimer(int id) throws SQLException {
        int eventId = -1;
        String selectSql = "SELECT id_event FROM participation WHERE id_participation = ?";
        PreparedStatement selectPs = connection.prepareStatement(selectSql);
        selectPs.setInt(1, id);
        ResultSet rs = selectPs.executeQuery();
        if (rs.next()) {
            eventId = rs.getInt("id_event");
        }

        String sql = "DELETE FROM participation WHERE id_participation = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        RefreshService.refreshAll();

        NotificationService.getInstance().addNotification(
                "Réservation annulée",
                "Une réservation a été annulée",
                NotificationService.NotificationType.WARNING,
                eventId,
                "event"
        );
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
            p.setUser_id(rs.getInt("user_id"));
            p.setNombre_places(rs.getInt("nombre_places"));
            p.setMontant_total(rs.getDouble("montant_total"));
            p.setStatut(rs.getString("statut"));
            p.setDate_participation(rs.getTimestamp("date_participation"));
            p.setEmail_snapshot(rs.getString("email_snapshot"));
            p.setNom_snapshot(rs.getString("nom_snapshot"));
            p.setPrenom_snapshot(rs.getString("prenom_snapshot"));
            p.setTelephone_snapshot(rs.getString("telephone_snapshot"));
            participations.add(p);
        }
        return participations;
    }

    public List<Participation> getParticipationsByUserId(int userId) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation WHERE user_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId_event(rs.getInt("id_event"));
            p.setUser_id(rs.getInt("user_id"));
            p.setNombre_places(rs.getInt("nombre_places"));
            p.setMontant_total(rs.getDouble("montant_total"));
            p.setStatut(rs.getString("statut"));
            p.setDate_participation(rs.getTimestamp("date_participation"));
            p.setEmail_snapshot(rs.getString("email_snapshot"));
            p.setNom_snapshot(rs.getString("nom_snapshot"));
            p.setPrenom_snapshot(rs.getString("prenom_snapshot"));
            p.setTelephone_snapshot(rs.getString("telephone_snapshot"));
            participations.add(p);
        }
        return participations;
    }

    public List<Participation> rechercher(String keyword) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation WHERE statut LIKE ? OR id_event LIKE ? OR email_snapshot LIKE ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");
        ps.setString(3, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId_event(rs.getInt("id_event"));
            p.setUser_id(rs.getInt("user_id"));
            p.setNombre_places(rs.getInt("nombre_places"));
            p.setMontant_total(rs.getDouble("montant_total"));
            p.setStatut(rs.getString("statut"));
            p.setDate_participation(rs.getTimestamp("date_participation"));
            p.setEmail_snapshot(rs.getString("email_snapshot"));
            p.setNom_snapshot(rs.getString("nom_snapshot"));
            p.setPrenom_snapshot(rs.getString("prenom_snapshot"));
            p.setTelephone_snapshot(rs.getString("telephone_snapshot"));
            participations.add(p);
        }
        return participations;
    }

    public List<Participation> trier(String column, String order) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        List<String> allowedColumns = List.of("id_participation", "id_event", "user_id", "nombre_places", "montant_total", "statut", "date_participation");
        if (!allowedColumns.contains(column)) column = "id_participation";
        if (!order.equalsIgnoreCase("ASC") && !order.equalsIgnoreCase("DESC")) order = "ASC";

        String sql = "SELECT * FROM participation ORDER BY " + column + " " + order;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId_event(rs.getInt("id_event"));
            p.setUser_id(rs.getInt("user_id"));
            p.setNombre_places(rs.getInt("nombre_places"));
            p.setMontant_total(rs.getDouble("montant_total"));
            p.setStatut(rs.getString("statut"));
            p.setDate_participation(rs.getTimestamp("date_participation"));
            p.setEmail_snapshot(rs.getString("email_snapshot"));
            p.setNom_snapshot(rs.getString("nom_snapshot"));
            p.setPrenom_snapshot(rs.getString("prenom_snapshot"));
            p.setTelephone_snapshot(rs.getString("telephone_snapshot"));
            participations.add(p);
        }
        return participations;
    }
}