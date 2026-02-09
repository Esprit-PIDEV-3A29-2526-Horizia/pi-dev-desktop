package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.utils.MyDataBase;
import java.util.Comparator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {
    private Connection connection;

    public ServiceUser() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO `user`(`nom`, `prenom`, `email`, `password`, `telephone`, `addresse`) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getTelephone());
        ps.setString(6, user.getAdresse());
        ps.executeUpdate();
    }

    public List<User> rechercherParNom(String nom) throws SQLException {
        return afficher().stream()
                .filter(u -> u.getNom().toLowerCase().contains(nom.toLowerCase()))
                .toList();
    }

    public List<User> rechercherParEmail(String email) throws SQLException {
        return afficher().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .toList();
    }

    public List<User> trierParNom() throws SQLException {
        return afficher().stream()
                .sorted(Comparator.comparing(User::getNom))
                .toList();
    }

    public List<User> trierParIdDesc() throws SQLException {
        return afficher().stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();
    }


    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE `user` SET `nom`=?, `prenom`=?, `email`=?, `password`=?, `telephone`=?, `addresse`=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getAdresse());
        ps.setString(5, user.getTelephone());
        ps.setString(6, user.getPassword());
        ps.setInt(7, user.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `user` WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM `user`";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            User u = new User();
            u.setId(resultSet.getInt("id"));
            u.setNom(resultSet.getString("nom"));
            u.setPrenom(resultSet.getString("prenom"));
            u.setEmail(resultSet.getString("email"));
            u.setAdresse(resultSet.getString("password"));
            u.setTelephone(resultSet.getString("telephone"));
            u.setPassword(resultSet.getString("addresse"));
            users.add(u);
        }
        return users;
    }
}
