package tn.esprit.services;

import tn.esprit.entities.Profil;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProfil {

    private Connection connection;

    public ServiceProfil() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    public int ajouter(Profil p) throws SQLException {
        String sql = "INSERT INTO profil(type, statut) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, p.getType());
        ps.setString(2, p.getStatut());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int id = rs.getInt(1);
            p.setId(id);
            return id;
        }
        return -1;
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

    public List<Profil> rechercherParType(String type) throws SQLException {
        List<Profil> resultats = new ArrayList<>();
        String sql = "SELECT * FROM profil WHERE type = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, type);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Profil p = new Profil(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("statut")
            );
            resultats.add(p);
        }
        return resultats;
    }

    public void modifier(Profil p) throws SQLException {
        String sql = "UPDATE profil SET type=?, statut=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getType());
        ps.setString(2, p.getStatut());
        ps.setInt(3, p.getId());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM profil WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}