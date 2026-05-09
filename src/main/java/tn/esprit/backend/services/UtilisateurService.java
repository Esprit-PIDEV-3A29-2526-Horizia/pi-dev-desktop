package tn.esprit.backend.services;

import tn.esprit.backend.utils.Database;
import tn.esprit.backend.entities.Utilisateur;
import tn.esprit.backend.entities.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private Connection conn;

    public UtilisateurService() {
        conn = Database.getInstance().getCnx();
    }

    // Inscription (table user)
    public void ajouter(Utilisateur u) {
        String sql = "INSERT INTO user (nom, prenom, email, password, telephone, addresse) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, u.getNom());
            pst.setString(2, u.getPrenom());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getMotDePasse());
            pst.setString(5, u.getTelephone());
            pst.setString(6, u.getAdresse());

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                u.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            System.err.println("Erreur ajout utilisateur: " + e.getMessage());
        }
    }

    // Modifier
    public void modifier(Utilisateur u) {
        String sql = "UPDATE user SET nom=?, prenom=?, email=?, telephone=?, addresse=? WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, u.getNom());
            pst.setString(2, u.getPrenom());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getTelephone());
            pst.setString(5, u.getAdresse());
            pst.setInt(6, u.getId());

            pst.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur modification utilisateur: " + e.getMessage());
        }
    }

    // Supprimer
    public void supprimer(int id) {
        String sql = "DELETE FROM user WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur suppression utilisateur: " + e.getMessage());
        }
    }

    // Récupérer par ID
    public Utilisateur getById(int id) {
        String sql = "SELECT * FROM user WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche utilisateur: " + e.getMessage());
        }
        return null;
    }

    // Récupérer tous
    public List<Utilisateur> getAll() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur liste utilisateurs: " + e.getMessage());
        }
        return list;
    }

    // Connexion (login)
    public Utilisateur login(String email, String motDePasse) {
        String sql = "SELECT * FROM user WHERE email=? AND password=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            pst.setString(2, motDePasse);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur login: " + e.getMessage());
        }
        return null;
    }

    // Vérifier si email existe déjà
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification email: " + e.getMessage());
        }
        return false;
    }

    // Rechercher par nom ou email
    public List<Utilisateur> rechercher(String keyword) {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            String search = "%" + keyword + "%";
            pst.setString(1, search);
            pst.setString(2, search);
            pst.setString(3, search);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche utilisateurs: " + e.getMessage());
        }
        return list;
    }

    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("password"));  // colonne "password"
        u.setTelephone(rs.getString("telephone"));
        u.setAdresse(rs.getString("addresse"));     // colonne "addresse" (double d)

        // Déterminer le rôle (admin si email contient "admin")
        if (rs.getString("email") != null && rs.getString("email").contains("admin")) {
            u.setRole(Role.ADMIN);
        } else {
            u.setRole(Role.USER);
        }

        u.setActif(true);
        u.setDateInscription(java.time.LocalDateTime.now());
        return u;
    }
}