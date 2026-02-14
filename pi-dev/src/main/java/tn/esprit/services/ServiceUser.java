package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {

    private Connection connection;

    public ServiceUser() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public List<User> afficher() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT u.id, u.nom, u.prenom, u.email, u.password, u.telephone, u.addresse, " +
                "p.id as profil_id, p.type, p.statut " +
                "FROM user u " +
                "LEFT JOIN profil p ON u.profil_id = p.id";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("u.id"));
                user.setNom(rs.getString("u.nom"));
                user.setPrenom(rs.getString("u.prenom"));
                user.setEmail(rs.getString("u.email"));
                user.setPassword(rs.getString("u.password"));
                user.setTelephone(rs.getString("u.telephone"));
                user.setAddresse(rs.getString("u.addresse"));

                int profilId = rs.getInt("profil_id");
                if (!rs.wasNull()) {
                    Profil profil = new Profil();
                    profil.setId(profilId);
                    profil.setType(rs.getString("type"));
                    profil.setStatut(rs.getString("statut"));
                    user.setProfil(profil);
                }

                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans afficher():");
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (nom, prenom, email, password, telephone, addresse, profil_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getTelephone());
        ps.setString(6, user.getAddresse());

        if (user.getProfil() != null) {
            ps.setInt(7, user.getProfil().getId());
        } else {
            ps.setNull(7, Types.INTEGER);
        }

        ps.executeUpdate();
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE user SET nom=?, prenom=?, email=?, password=?, telephone=?, addresse=?, profil_id=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getTelephone());
        ps.setString(6, user.getAddresse());

        if (user.getProfil() != null) {
            ps.setInt(7, user.getProfil().getId());
        } else {
            ps.setNull(7, Types.INTEGER);
        }

        ps.setInt(8, user.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Dans ServiceUser.java
    public int compterParProfil(int profilId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM user WHERE profil_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, profilId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }
    public void testConnexion() {
    }
}