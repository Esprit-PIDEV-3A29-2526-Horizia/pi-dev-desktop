package tn.esprit.services;

import tn.esprit.entities.Profil;
import tn.esprit.utils.MyDataBase;
import java.util.Comparator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProfil {

    private Connection connection;

    public ServiceProfil() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    public void ajouter(Profil p) throws SQLException {
        String sql = "INSERT INTO profil(type, statut) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getType());
        ps.setString(2, p.getStatut());
        ps.executeUpdate();
    }

    public List<Profil> afficher() throws SQLException {
        List<Profil> profils = new ArrayList<>();
        String sql = "SELECT * FROM profil";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Profil p = new Profil(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("statut")
            );
            profils.add(p);
        }
        return profils;
    }

    public void modifier(Profil p) throws SQLException {
        String sql = "UPDATE profil SET type=?, statut=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getType());
        ps.setString(2, p.getStatut());
        ps.setInt(3, p.getId());
        ps.executeUpdate();
    }

    // DELETE
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM profil WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Profil> rechercherParType(String type) throws SQLException {
        return afficher().stream()
                .filter(p -> p.getType().equalsIgnoreCase(type))
                .toList();
    }

    public List<Profil> rechercherParStatut(String statut) throws SQLException {
        return afficher().stream()
                .filter(p -> p.getStatut().equalsIgnoreCase(statut))
                .toList();
    }

    public List<Profil> trierParType() throws SQLException {
        return afficher().stream()
                .sorted(Comparator.comparing(Profil::getType))
                .toList();
    }

}
