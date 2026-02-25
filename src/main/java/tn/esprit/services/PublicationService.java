package tn.esprit.services;

import tn.esprit.entities.Publication;
import tn.esprit.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationService {

    private Connection conn;

    public PublicationService() {
        conn = Database.getInstance().getCnx();
    }

    // ============================================
    // CRUD OPERATIONS
    // ============================================

    public void ajouter(Publication p) {
        String sql = "INSERT INTO publication (titre, description, image, date_publication, likes, utilisateur_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getTitre());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setTimestamp(4, Timestamp.valueOf(p.getDatePublication()));
            pst.setInt(5, p.getLikes());
            pst.setInt(6, p.getUtilisateurId());

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                p.setId(rs.getInt(1));
            }
            System.out.println("✅ Publication ajoutée avec ID: " + p.getId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout publication: " + e.getMessage());
        }
    }

    public void modifier(Publication p) {
        String sql = "UPDATE publication SET titre=?, description=?, image=?, likes=?, utilisateur_id=? WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, p.getTitre());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setInt(4, p.getLikes());
            pst.setInt(5, p.getUtilisateurId());
            pst.setInt(6, p.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Publication modifiée: " + p.getTitre());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur modification publication: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM publication WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Publication supprimée: ID " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression publication: " + e.getMessage());
        }
    }

    // ============================================
    // RECHERCHE ET RÉCUPÉRATION
    // ============================================

    public Publication getById(int id) {
        String sql = "SELECT * FROM publication WHERE id=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapPublication(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche publication par ID: " + e.getMessage());
        }
        return null;
    }

    // Récupérer toutes les publications (tri par défaut : plus récentes)
    public List<Publication> getAll() {
        return getAll("Plus récentes");
    }

    // Récupérer avec tri personnalisé
    public List<Publication> getAll(String tri) {
        List<Publication> list = new ArrayList<>();
        String sql;

        switch (tri) {
            case "Plus récentes":
                sql = "SELECT * FROM publication ORDER BY date_publication DESC";
                break;
            case "Plus anciennes":
                sql = "SELECT * FROM publication ORDER BY date_publication ASC";
                break;
            case "A-Z":
                sql = "SELECT * FROM publication ORDER BY titre ASC";
                break;
            case "Z-A":
                sql = "SELECT * FROM publication ORDER BY titre DESC";
                break;
            case "Plus aimées":
                sql = "SELECT * FROM publication ORDER BY likes DESC";
                break;
            default:
                sql = "SELECT * FROM publication ORDER BY date_publication DESC";
        }

        System.out.println("🔍 Tri: " + tri + " | Requête: " + sql);

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapPublication(rs));
            }
            System.out.println("📊 Total publications récupérées: " + list.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL getAll: " + e.getMessage());
        }

        return list;
    }

    // Rechercher par titre (contient le mot-clé)
    public List<Publication> rechercherParTitre(String titre) {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication WHERE titre LIKE ? ORDER BY date_publication DESC";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, "%" + titre + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(mapPublication(rs));
            }
            System.out.println("🔍 Recherche '" + titre + "': " + list.size() + " résultats");

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche publication: " + e.getMessage());
        }
        return list;
    }

    // Rechercher par utilisateur
    public List<Publication> getByUtilisateur(int utilisateurId) {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication WHERE utilisateur_id = ? ORDER BY date_publication DESC";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, utilisateurId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(mapPublication(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getByUtilisateur: " + e.getMessage());
        }
        return list;
    }

    // ============================================
    // MÉTHODES SPÉCIALES
    // ============================================

    // Incrémenter les likes d'une publication
    public void incrementerLikes(int publicationId) {
        String sql = "UPDATE publication SET likes = likes + 1 WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, publicationId);
            pst.executeUpdate();
            System.out.println("❤️ Like ajouté à la publication " + publicationId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur incrementer likes: " + e.getMessage());
        }
    }

    // Vérifier si un titre existe déjà (pour éviter les doublons)
    public boolean titreExiste(String titre) {
        return titreExiste(titre, -1); // -1 = ignorer l'ID
    }

    // Vérifier si un titre existe déjà (en excluant une publication spécifique)
    public boolean titreExiste(String titre, int excludeId) {
        String sql = "SELECT COUNT(*) FROM publication WHERE titre = ? AND id != ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, titre);
            pst.setInt(2, excludeId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification titre: " + e.getMessage());
        }
        return false;
    }

    // Compter le nombre total de publications
    public int count() {
        String sql = "SELECT COUNT(*) FROM publication";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur count publications: " + e.getMessage());
        }
        return 0;
    }

    // ============================================
    // MAPPING RESULTSET → ENTITÉ
    // ============================================

    private Publication mapPublication(ResultSet rs) throws SQLException {
        Publication p = new Publication();
        p.setId(rs.getInt("id"));
        p.setTitre(rs.getString("titre"));
        p.setDescription(rs.getString("description"));
        p.setImage(rs.getString("image"));
        p.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());

        // Gestion des nouveaux champs (avec try-catch pour compatibilité)
        try {
            p.setLikes(rs.getInt("likes"));
        } catch (SQLException e) {
            p.setLikes(0);
        }

        try {
            p.setUtilisateurId(rs.getInt("utilisateur_id"));
        } catch (SQLException e) {
            p.setUtilisateurId(0);
        }

        return p;
    }
}