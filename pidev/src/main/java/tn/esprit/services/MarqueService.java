package tn.esprit.services;

import tn.esprit.entities.Marque;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarqueService {

    private Connection getConn() throws SQLException {
        return MyDataBase.getInstance().getMyConnection();
    }

    // ═══════════════════════════════════════════════════════
    // Lister toutes les marques (avec tri alphabétique)
    // ═══════════════════════════════════════════════════════
    public List<Marque> getAllMarques(String tri) {
        List<Marque> marques = new ArrayList<>();

        String orderBy = "ASC";
        if ("DESC".equalsIgnoreCase(tri)) {
            orderBy = "DESC";
        }

        // ← IMPORTANT : Sélectionner aussi la colonne "logo"
        String sql = "SELECT id_marque, nom_marque, logo FROM marque ORDER BY nom_marque " + orderBy;

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Marque m = new Marque();
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomMarque(rs.getString("nom_marque"));
                m.setLogo(rs.getString("logo"));  // ← RÉCUPÉRER LE LOGO
                marques.add(m);
            }

            if (marques.isEmpty()) {
                System.out.println("Aucune marque présente dans la base.");
            } else {
                System.out.println("Marques trouvées (" + marques.size() + ") - triées " + orderBy);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération marques : " + e.getMessage());
        }

        return marques;
    }

    public List<Marque> getAllMarquesAlphabetique() {
        return getAllMarques("ASC");
    }

    public List<Marque> getAllMarquesInverseAlphabetique() {
        return getAllMarques("DESC");
    }

    // ═══════════════════════════════════════════════════════
    // Rechercher une marque par nom (partiel)
    // ═══════════════════════════════════════════════════════
    public List<Marque> rechercherMarqueParNom(String recherche) {
        List<Marque> resultat = new ArrayList<>();

        if (recherche == null || recherche.trim().isEmpty()) {
            return getAllMarques("ASC");
        }

        String sql = "SELECT id_marque, nom_marque, logo FROM marque WHERE LOWER(nom_marque) LIKE LOWER(?) ORDER BY nom_marque ASC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + recherche.trim() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Marque m = new Marque();
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomMarque(rs.getString("nom_marque"));
                m.setLogo(rs.getString("logo"));  // ← RÉCUPÉRER LE LOGO
                resultat.add(m);
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche marque : " + e.getMessage());
        }

        return resultat;
    }

    // ═══════════════════════════════════════════════════════
    // Récupérer une marque par ID
    // ═══════════════════════════════════════════════════════
    public Marque getMarqueById(int id) {
        if (id <= 0) {
            System.out.println("ID invalide.");
            return null;
        }

        String sql = "SELECT id_marque, nom_marque, logo FROM marque WHERE id_marque = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Marque m = new Marque();
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomMarque(rs.getString("nom_marque"));
                m.setLogo(rs.getString("logo"));  // ← RÉCUPÉRER LE LOGO
                return m;
            }
        } catch (SQLException e) {
            System.err.println("Erreur getMarqueById : " + e.getMessage());
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════
    // Ajouter une marque
    // ═══════════════════════════════════════════════════════
    public boolean ajouterMarque(Marque marque) {
        if (marque == null || marque.getNomMarque() == null || marque.getNomMarque().trim().isEmpty()) {
            System.out.println("Erreur : Nom de marque invalide.");
            return false;
        }

        String sql = "INSERT INTO marque (nom_marque, logo) VALUES (?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, marque.getNomMarque().trim());
            ps.setString(2, marque.getLogo());  // ← INSÉRER LE LOGO

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    marque.setIdMarque(rs.getInt(1));
                }
                System.out.println("Succès : Marque ajoutée (ID: " + marque.getIdMarque() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout marque : " + e.getMessage());
        }
        return false;
    }
}