package tn.esprit.services;

import tn.esprit.entites.Categorie;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {

    private Connection cnx;

    public CategorieService() {
        // On récupère l'unique instance de connexion
        cnx = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Categorie c) {
        String qry = "INSERT INTO categorie (nom, description) VALUES (?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, c.getNom());
            pstm.setString(2, c.getDescription());
            pstm.executeUpdate();
            System.out.println("Catégorie '" + c.getNom() + "' ajoutée avec succès !");
        } catch (SQLException ex) {
            System.err.println("Erreur lors de l'ajout : " + ex.getMessage());
        }
    }

    @Override
    public List<Categorie> afficher() {
        List<Categorie> categories = new ArrayList<>();
        String qry = "SELECT * FROM categorie";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                categories.add(c);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de l'affichage : " + ex.getMessage());
        }
        return categories;
    }

    @Override
    public void modifier(Categorie c) {
        String qry = "UPDATE categorie SET nom = ?, description = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, c.getNom());
            pstm.setString(2, c.getDescription());
            pstm.setInt(3, c.getId());
            pstm.executeUpdate();
            System.out.println("Catégorie modifiée !");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String qry = "DELETE FROM categorie WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            pstm.executeUpdate();
            System.out.println("Catégorie supprimée !");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
}