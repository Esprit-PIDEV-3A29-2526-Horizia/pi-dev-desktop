package tn.esprit.services;

import tn.esprit.entites.Voyage;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageService implements IService<Voyage> {

    private Connection cnx;

    public VoyageService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Voyage v) {
        // Ajout des colonnes places_total et places_restantes (9 points d'interrogation)
        String sql = "INSERT INTO voyage (destination, description, prix, date_depart, date_retour, image_url, id_categorie, places_total, places_restantes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, v.getDestination());
            ps.setString(2, v.getDescription());
            ps.setDouble(3, v.getPrix());
            ps.setDate(4, v.getDate_depart());
            ps.setDate(5, v.getDate_retour());
            ps.setString(6, v.getImage_url());
            ps.setInt(7, v.getId_categorie());
            ps.setInt(8, v.getPlaces_total());     // Nouveau
            ps.setInt(9, v.getPlaces_restantes()); // Nouveau

            ps.executeUpdate();
            System.out.println("Voyage ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Voyage v) {
        // Requête SQL basée sur votre structure de table
        String sql = "UPDATE voyage SET destination=?, description=?, prix=?, date_depart=?, " +
                "date_retour=?, image_url=?, id_categorie=?, places_total=?, places_restantes=? " +
                "WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, v.getDestination());
            ps.setString(2, v.getDescription());
            ps.setDouble(3, v.getPrix());
            ps.setDate(4, v.getDate_depart());
            ps.setDate(5, v.getDate_retour());
            ps.setString(6, v.getImage_url());
            ps.setInt(7, v.getId_categorie());
            ps.setInt(8, v.getPlaces_total());
            ps.setInt(9, v.getPlaces_restantes());
            ps.setInt(10, v.getId()); // L'ID indispensable pour trouver la ligne à modifier

            ps.executeUpdate();
            System.out.println("Modification enregistrée en base de données !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String qry = "DELETE FROM voyage WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            pstm.executeUpdate();
            System.out.println("Voyage supprimé avec succès !");
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la suppression : " + ex.getMessage());
        }
    }

    @Override
    public List<Voyage> afficher() {
        List<Voyage> voyages = new ArrayList<>();
        String qry = "SELECT * FROM voyage";
        try (Statement stm = cnx.createStatement(); ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                Voyage v = new Voyage();
                v.setId(rs.getInt("id"));
                v.setDestination(rs.getString("destination"));
                v.setDescription(rs.getString("description"));
                v.setPrix(rs.getDouble("prix"));
                v.setDate_depart(rs.getDate("date_depart"));
                v.setDate_retour(rs.getDate("date_retour"));
                v.setImage_url(rs.getString("image_url"));
                v.setId_categorie(rs.getInt("id_categorie"));
                v.setPlaces_total(rs.getInt("places_total"));         // Nouveau
                v.setPlaces_restantes(rs.getInt("places_restantes")); // Nouveau

                voyages.add(v);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération : " + ex.getMessage());
        }
        return voyages;
    }
}