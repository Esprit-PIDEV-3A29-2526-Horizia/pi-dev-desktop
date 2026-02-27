package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.utils.MyDataBase;

import java.sql.*;

public class AuthService {

    private Connection connection;

    public AuthService() {
        // Utiliser votre singleton
        connection = MyDataBase.getInstance().getMyConnection();
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT u.id, u.nom, u.prenom, u.email, u.password, u.telephone, u.addresse, " +
                "p.id as profil_id, p.type, p.statut " +
                "FROM user u " +
                "LEFT JOIN profil p ON u.profil_id = p.id " +
                "WHERE u.email = ? AND u.password = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("u.id"));
            user.setNom(rs.getString("u.nom"));
            user.setPrenom(rs.getString("u.prenom"));
            user.setEmail(rs.getString("u.email"));
            user.setPassword(rs.getString("u.password"));
            user.setTelephone(rs.getString("u.telephone"));
            user.setAddresse(rs.getString("u.addresse"));

            // Récupérer le profil
            int profilId = rs.getInt("profil_id");
            if (!rs.wasNull()) {
                Profil profil = new Profil();
                profil.setId(profilId);
                profil.setType(rs.getString("type"));
                profil.setStatut(rs.getString("statut"));
                user.setProfil(profil);
            }

            return user;
        }

        return null;
    }
}