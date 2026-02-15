package tn.esprit.services;

import tn.esprit.entities.user;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Serviceuser {
    private Connection connection;

    public Serviceuser() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    // Méthode pour rechercher par ID
    public user rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user u = new user();
                    u.setId(rs.getInt("id"));
                    u.setNom(rs.getString("nom"));
                    u.setPrenom(rs.getString("prenom"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setTelephone(rs.getString("telephone"));
                    u.setAddresse(rs.getString("addresse"));
                    u.setProfil_id(rs.getInt("profil_id"));
                    return u;
                }
            }
        }
        return null;
    }

    // Autres méthodes si nécessaires (ajouter, afficher, etc.) - ajoutez-les si besoin
    public List<user> afficher() throws SQLException {
        List<user> users = new ArrayList<>();
        String sql = "SELECT * FROM `user`";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                user u = new user();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setTelephone(rs.getString("telephone"));
                u.setAddresse(rs.getString("addresse"));
                u.setProfil_id(rs.getInt("profil_id"));
                users.add(u);
            }
        }
        return users;
    }
}