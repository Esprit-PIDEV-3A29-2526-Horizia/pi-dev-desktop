package tn.esprit.services;

import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.utils.MyDataBase;

import java.sql.*;

public class AuthService {

    private Connection connection;

    public AuthService() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT u.id, u.nom, u.prenom, u.email, u.password, u.telephone, u.addresse, u.face_descriptor, " +
                "p.id as profil_id, p.type, p.statut " +
                "FROM user u " +
                "LEFT JOIN profil p ON u.profil_id = p.id " +
                "WHERE u.email = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String dbPassword = rs.getString("u.password");

            if (dbPassword.startsWith("$2a$")) {
                if (BCrypt.checkpw(password, dbPassword)) {
                    return createUserFromResultSet(rs);
                }
            } else {
                if (password.equals(dbPassword)) {
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    updatePasswordHash(email, hashedPassword);
                    return createUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private void updatePasswordHash(String email, String hashedPassword) {
        String sql = "UPDATE user SET password = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("u.id"));
        user.setNom(rs.getString("u.nom"));
        user.setPrenom(rs.getString("u.prenom"));
        user.setEmail(rs.getString("u.email"));

        // ✅ Vérifier si la colonne password existe
        try {
            user.setPassword(rs.getString("u.password"));
        } catch (SQLException e) {
            // La colonne n'existe pas dans cette requête (getUserByEmail)
            System.out.println("ℹ️ Colonne password non disponible dans cette requête");
        }

        user.setTelephone(rs.getString("u.telephone"));
        user.setAddresse(rs.getString("u.addresse"));
        user.setFaceDescriptor(rs.getString("u.face_descriptor"));

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
    public boolean checkEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT u.id, u.nom, u.prenom, u.email, u.telephone, u.addresse, u.face_descriptor, " +
                "p.id as profil_id, p.type, p.statut " +
                "FROM user u " +
                "LEFT JOIN profil p ON u.profil_id = p.id " +
                "WHERE u.email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT u.id, u.nom, u.prenom, u.email, u.password, u.telephone, u.addresse, u.face_descriptor, " +
                "p.id as profil_id, p.type, p.statut " +
                "FROM user u " +
                "LEFT JOIN profil p ON u.profil_id = p.id " +
                "WHERE u.id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateFaceDescriptor(int userId, String faceDescriptor) {
        String sql = "UPDATE user SET face_descriptor = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, faceDescriptor);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasFaceRegistered(int userId) {
        String sql = "SELECT face_descriptor FROM user WHERE id = ? AND face_descriptor IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean createUserFromGoogle(User user) {
        String sql = "INSERT INTO user (email, nom, prenom, password, profil_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getNom());
            ps.setString(3, user.getPrenom());
            ps.setString(4, ""); // Pas de mot de passe
            ps.setInt(5, 2); // ID du profil CLIENT (à adapter selon ta base)

            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}