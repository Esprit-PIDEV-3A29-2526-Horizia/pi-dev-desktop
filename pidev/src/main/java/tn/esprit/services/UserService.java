package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.utils.MyDataBase;
import java.sql.*;

public class UserService {

    private Connection connection;

    public UserService() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, telephone = ?, addresse = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone());
            ps.setString(5, user.getAddresse());
            ps.setInt(6, user.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}