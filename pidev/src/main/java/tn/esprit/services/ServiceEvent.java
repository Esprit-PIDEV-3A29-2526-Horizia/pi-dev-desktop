//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.entities.Events;
import tn.esprit.utils.MyDataBase;

public class ServiceEvent implements IService<Events> {
    private Connection connection = MyDataBase.getInstance().getMyConnection();

    public void ajouter(Events events) throws SQLException {
        String sql = "INSERT INTO `events`(`titre`,`description`,`categorie`,`location`,`date_debut`,`date_fin`,`prix`,`capacite_max`,`places_restantes`,`image_url`,`statut`,`id_createur`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, events.getTitre());
        ps.setString(2, events.getDescription());
        ps.setString(3, events.getCategorie());
        ps.setString(4, events.getLocation());
        ps.setTimestamp(5, events.getDateDebut());
        ps.setTimestamp(6, events.getDateFin());
        ps.setFloat(7, events.getPrix());
        ps.setInt(8, events.getCapaciteMax());
        ps.setInt(9, events.getPlacesRestantes());
        ps.setString(10, events.getImage_url());
        ps.setString(11, events.getStatut());
        ps.setInt(12, events.getId_createur());

        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            events.setId_event(rs.getInt(1));
        }

        RefreshService.refreshAll();
        NotificationService.getInstance().addNotification(
                "Événement créé",
                events.getTitre() + " a été ajouté",
                NotificationService.NotificationType.SUCCESS,
                events.getId_event(),
                "event"
        );
    }

    public void modifier(Events events) throws SQLException {
        String sql = "UPDATE `events` SET `titre`=?,`description`=?,`categorie`=?,`location`=?,`date_debut`=?,`date_fin`=?,`prix`=?,`capacite_max`=?,`places_restantes`=?,`image_url`=?,`statut`=? WHERE id_event=?";
        PreparedStatement ps = this.connection.prepareStatement(sql);
        ps.setString(1, events.getTitre());
        ps.setString(2, events.getDescription());
        ps.setString(3, events.getCategorie());
        ps.setString(4, events.getLocation());
        ps.setTimestamp(5, events.getDateDebut());
        ps.setTimestamp(6, events.getDateFin());
        ps.setDouble(7, (double)events.getPrix());
        ps.setInt(8, events.getCapaciteMax());
        ps.setInt(9, events.getPlacesRestantes());
        ps.setString(10, events.getImage_url());
        ps.setString(11, events.getStatut());
        ps.setInt(12, events.getId_event());
        ps.executeUpdate();

        RefreshService.refreshAll();
        NotificationService.getInstance().addNotification(
                "Événement modifié",
                events.getTitre() + " a été mis à jour",
                NotificationService.NotificationType.INFO,
                events.getId_event(),
                "event"
        );
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM events WHERE id_event=?";
        PreparedStatement ps = this.connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        RefreshService.refreshAll();
        NotificationService.getInstance().addNotification(
                "Événement supprimé",
                "Un événement a été supprimé",
                NotificationService.NotificationType.WARNING,
                id,
                "event"
        );
    }

    public List<Events> afficher() throws SQLException {
        List<Events> events = new ArrayList();
        String sql = "SELECT * FROM events";
        Statement statement = this.connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while(rs.next()) {
            Events e = new Events();
            e.setId_event(rs.getInt("id_event"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setCategorie(rs.getString("categorie"));
            e.setLocation(rs.getString("location"));
            e.setDateDebut(rs.getTimestamp("date_debut"));
            e.setDateFin(rs.getTimestamp("date_fin"));
            e.setPrix(rs.getFloat("prix"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesRestantes(rs.getInt("places_restantes"));
            e.setImage_url(rs.getString("image_url"));
            e.setStatut(rs.getString("statut"));
            e.setId_createur(rs.getInt("id_createur"));
            events.add(e);
        }

        return events;
    }


    public List<Events> rechercher(String keyword) throws SQLException {
        List<Events> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE titre LIKE ? OR categorie LIKE ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Events e = new Events();
            e.setId_event(rs.getInt("id_event"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setCategorie(rs.getString("categorie"));
            e.setLocation(rs.getString("location"));
            e.setDateDebut(rs.getTimestamp("date_debut"));
            e.setDateFin(rs.getTimestamp("date_fin"));
            e.setPrix(rs.getFloat("prix"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesRestantes(rs.getInt("places_restantes"));
            e.setImage_url(rs.getString("image_url"));
            e.setStatut(rs.getString("statut"));
            e.setId_createur(rs.getInt("id_createur"));

            events.add(e);
        }
        return events;
    }


    public List<Events> trier(String column, String order) throws SQLException {
        List<Events> events = new ArrayList<>();
        List<String> allowedColumns = List.of("id_event","titre","categorie","date_debut","date_fin","prix","capacite_max","places_restantes");
        if (!allowedColumns.contains(column)) column = "id_event";
        if (!order.equalsIgnoreCase("ASC") && !order.equalsIgnoreCase("DESC")) order = "ASC";

        String sql = "SELECT * FROM events ORDER BY " + column + " " + order;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Events e = new Events();
            e.setId_event(rs.getInt("id_event"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setCategorie(rs.getString("categorie"));
            e.setLocation(rs.getString("location"));
            e.setDateDebut(rs.getTimestamp("date_debut"));
            e.setDateFin(rs.getTimestamp("date_fin"));
            e.setPrix(rs.getFloat("prix"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesRestantes(rs.getInt("places_restantes"));
            e.setImage_url(rs.getString("image_url"));
            e.setStatut(rs.getString("statut"));
            e.setId_createur(rs.getInt("id_createur"));

            events.add(e);
        }
        return events;
    }


    public void updatePlaces(int id_event, int Places_Restantes) throws SQLException{
        String sql = "UPDATE events SET places_restantes = ? WHERE id_event= ? ";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, Places_Restantes);
        ps.setInt(2, id_event);
        ps.executeUpdate();
    }

    private SearchService searchService = new SearchService();

    public List<Events> rechercherAvancee(String keyword){
        return searchService.search(keyword);
    }

    public void reindexerTout(){
        searchService.reindexAll();
    }
}