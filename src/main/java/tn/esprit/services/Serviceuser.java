package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Serviceuser implements IService<User> {

    private Connection connection;

    public Serviceuser() {
        connection = MyDataBase.getInstance().getMyConnection();
        testConnexion();
    }

    @Override
    public List<User> afficher() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT u.id, u.nom, u.prenom, u.email, u.password, u.telephone, u.addresse, " +
                "p.id as profil_id, p.type as profil_type, p.statut as profil_statut " +
                "FROM user u " +
                "LEFT JOIN profil p ON u.profil_id = p.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setTelephone(rs.getString("telephone"));
                user.setAddresse(rs.getString("addresse"));

                int profilId = rs.getInt("profil_id");
                if (!rs.wasNull()) {
                    Profil profil = new Profil();
                    profil.setId(profilId);
                    profil.setType(rs.getString("profil_type"));
                    profil.setStatut(rs.getString("profil_statut"));
                    user.setProfil(profil);
                }

                users.add(user);
            }
            System.out.println("✅ " + count + " utilisateurs trouvés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans afficher(): " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }
    public User rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
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

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (nom, prenom, email, password, telephone, addresse, profil_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE user SET nom=?, prenom=?, email=?, password=?, telephone=?, addresse=?, profil_id=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int compterParProfil(int profilId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM user WHERE profil_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, profilId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    public void testConnexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Connexion DB établie");
            } else {
                System.err.println("❌ Connexion DB non établie");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur test connexion: " + e.getMessage());
        }
    }
    public boolean emailExiste(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Version sans exception à utiliser dans les contrôleurs
     */
    public boolean emailExiste(String email, boolean silent) {
        try {
            return emailExiste(email);
        } catch (SQLException e) {
            if (!silent) {
                System.err.println("❌ Erreur vérification email: " + e.getMessage());
            }
            return false;
        }
    }
}