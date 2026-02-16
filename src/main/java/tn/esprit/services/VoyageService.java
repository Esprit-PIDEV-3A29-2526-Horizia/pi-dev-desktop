package tn.esprit.services;

import tn.esprit.entites.Voyage;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageService implements IService<Voyage> {

    private Connection cnx;

    public VoyageService() {
        // Récupération de l'instance unique de connexion (Singleton)
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Voyage v) {
        String qry = "INSERT INTO voyage (destination, description, prix, date_depart, date_retour, image_url, id_categorie) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, v.getDestination());
            pstm.setString(2, v.getDescription());
            pstm.setDouble(3, v.getPrix());
            pstm.setDate(4, v.getDateDepart());
            pstm.setDate(5, v.getDateRetour());
            pstm.setString(6, v.getImageUrl());
            pstm.setInt(7, v.getIdCategorie());

            pstm.executeUpdate();
            System.out.println("Voyage vers " + v.getDestination() + " ajouté avec succès !");
        } catch (SQLException ex) {
            System.err.println("Erreur lors de l'ajout du voyage : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Voyage v) {
        String qry = "UPDATE voyage SET destination = ?, description = ?, prix = ?, date_depart = ?, date_retour = ?, image_url = ?, id_categorie = ? WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, v.getDestination());
            pstm.setString(2, v.getDescription());
            pstm.setDouble(3, v.getPrix());
            pstm.setDate(4, v.getDateDepart());
            pstm.setDate(5, v.getDateRetour());
            pstm.setString(6, v.getImageUrl());
            pstm.setInt(7, v.getIdCategorie());
            pstm.setInt(8, v.getId());

            int rowsUpdated = pstm.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Voyage ID " + v.getId() + " mis à jour !");
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la modification : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String qry = "DELETE FROM voyage WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            int rowsDeleted = pstm.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Voyage supprimé avec succès !");
            } else {
                System.out.println("Aucun voyage trouvé avec l'ID : " + id);
            }
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
                v.setDateDepart(rs.getDate("date_depart"));
                v.setDateRetour(rs.getDate("date_retour"));
                v.setImageUrl(rs.getString("image_url"));
                v.setIdCategorie(rs.getInt("id_categorie"));

                voyages.add(v);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des voyages : " + ex.getMessage());
        }
        return voyages;
    }
}