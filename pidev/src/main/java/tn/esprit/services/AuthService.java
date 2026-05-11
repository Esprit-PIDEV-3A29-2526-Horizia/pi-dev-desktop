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
    // À ajouter dans AuthService.java

    public String getAccountStatusMessage(String email) {
        String sql = "SELECT p.statut FROM user u LEFT JOIN profil p ON u.profil_id = p.id WHERE u.email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String statut = rs.getString("statut");
                if (statut == null) return "Profil non défini.";
                if (!statut.equalsIgnoreCase("ACTIF")) {
                    return "❌ Votre compte est " + statut.toLowerCase() + ". Vous ne pouvez pas vous connecter.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // compte actif ou inexistant
    }
    // ==================== LOGIN (compatible Symfony $2y$ + vérification statut) ====================
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
            // ------------------------------------------------------------
            // VÉRIFICATION DU STATUT DU PROFIL
            // ------------------------------------------------------------
            String profilStatut = rs.getString("p.statut");
            if (profilStatut == null || !profilStatut.equalsIgnoreCase("ACTIF")) {
                // Compte inactif, bloqué ou sans profil -> refus connexion
                System.out.println("Connexion refusée pour " + email + " : statut = " + profilStatut);
                return null;
            }

            String dbPassword = rs.getString("u.password");

            // Compatibilité Symfony ($2y$)
            if (dbPassword.startsWith("$2y$")) {
                String convertedHash = "$2a$" + dbPassword.substring(4);
                if (BCrypt.checkpw(password, convertedHash)) {
                    // Migrer vers $2a$
                    String newHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    updatePasswordHash(email, newHash);
                    return createUserFromResultSet(rs);
                }
            }
            // Format Java ($2a$)
            else if (dbPassword.startsWith("$2a$")) {
                if (BCrypt.checkpw(password, dbPassword)) {
                    return createUserFromResultSet(rs);
                }
            }
            // Ancien mot de passe en clair
            else {
                if (password.equals(dbPassword)) {
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    updatePasswordHash(email, hashedPassword);
                    return createUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // ==================== CHANGER LE MOT DE PASSE ====================
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String sql = "SELECT password FROM user WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                // Vérifier avec BCrypt (supporte $2a$ et $2y$)
                if (storedHash.startsWith("$2y$")) {
                    String converted = "$2a$" + storedHash.substring(4);
                    if (!BCrypt.checkpw(oldPassword, converted)) {
                        return false;
                    }
                } else if (storedHash.startsWith("$2a$")) {
                    if (!BCrypt.checkpw(oldPassword, storedHash)) {
                        return false;
                    }
                } else {
                    if (!oldPassword.equals(storedHash)) {
                        return false;
                    }
                }

                String newHashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                String updateSql = "UPDATE user SET password = ? WHERE id = ?";
                try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                    updatePs.setString(1, newHashed);
                    updatePs.setInt(2, userId);
                    return updatePs.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== MISE À JOUR DU PROFIL (sans mot de passe) ====================
    public boolean updateUserProfile(User user) {
        String sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, telephone = ?, addresse = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone());
            ps.setString(5, user.getAddresse());
            ps.setInt(6, user.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== UTILITAIRES ====================
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
        try {
            user.setPassword(rs.getString("u.password"));
        } catch (SQLException e) { /* ignore */ }
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

    // ==================== AUTRES MÉTHODES EXISTANTES ====================
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
            ps.setString(4, "");
            ps.setInt(5, 2);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}