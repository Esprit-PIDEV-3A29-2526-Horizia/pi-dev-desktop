package tn.esprit.backend.services;

import tn.esprit.backend.utils.Database;
import tn.esprit.backend.entities.Utilisateur;  // ✅ Added
import tn.esprit.backend.entities.Role;          // ✅ Added
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private Connection conn;

    public UtilisateurService() {
        conn = Database.getInstance().getCnx();
    }

    // Inscription
    public void ajouter(Utilisateur u) {  // ✅ Fixed: utilisateur → Utilisateur
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, adresse, role, date_inscription, actif, image_profil) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, u.getNom());
            pst.setString(2, u.getPrenom());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getMotDePasse());
            pst.setString(5, u.getTelephone());
            pst.setString(6, u.getAdresse());
            pst.setString(7, u.getRole().name());
            pst.setTimestamp(8, Timestamp.valueOf(u.getDateInscription()));
            pst.setBoolean(9, u.isActif());
            pst.setString(10, u.getImageProfil());

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
    public void modifier(Utilisateur u) {  // ✅ Fixed
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, telephone=?, adresse=?, role=?, actif=?, image_profil=? WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, u.getNom());
            pst.setString(2, u.getPrenom());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getTelephone());
            pst.setString(5, u.getAdresse());
            pst.setString(6, u.getRole().name());
            pst.setBoolean(7, u.isActif());
            pst.setString(8, u.getImageProfil());
            pst.setInt(9, u.getId());

            pst.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur modification utilisateur: " + e.getMessage());
        }
    }

    // Supprimer
    public void supprimer(int id) {
        String sql = "DELETE FROM utilisateur WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur suppression utilisateur: " + e.getMessage());
        }
    }

    // Récupérer par ID
    public Utilisateur getById(int id) {  // ✅ Fixed
        String sql = "SELECT * FROM utilisateur WHERE id=?";

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
    public List<Utilisateur> getAll() {  // ✅ Fixed
        List<Utilisateur> list = new ArrayList<>();  // ✅ Fixed
        String sql = "SELECT * FROM utilisateur ORDER BY date_inscription DESC";

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
    public Utilisateur login(String email, String motDePasse) {  // ✅ Fixed
        String sql = "SELECT * FROM utilisateur WHERE email=? AND mot_de_passe=? AND actif=true";

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
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email=?";

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
    public List<Utilisateur> rechercher(String keyword) {  // ✅ Fixed
        List<Utilisateur> list = new ArrayList<>();  // ✅ Fixed
        String sql = "SELECT * FROM utilisateur WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";

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

    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {  // ✅ Fixed
        Utilisateur u = new Utilisateur();  // ✅ Fixed
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setTelephone(rs.getString("telephone"));
        u.setAdresse(rs.getString("adresse"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setDateInscription(rs.getTimestamp("date_inscription").toLocalDateTime());
        u.setActif(rs.getBoolean("actif"));
        u.setImageProfil(rs.getString("image_profil"));
        return u;
    }
}