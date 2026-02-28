package tn.esprit.services;

import tn.esprit.entites.Voyage;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VoyageService implements IService<Voyage> {

    private final Connection cnx;
    public VoyageService() {
        cnx = MyDataBase.getInstance().getMyConnection();
    }
    @Override
    public void ajouter(Voyage v) {
        String sql = "INSERT INTO voyage (titre, destination, description, prix, date_depart, date_retour, image_url, id_categorie, places_total, places_restantes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, v.getTitre());
            ps.setString(2, v.getDestination());
            ps.setString(3, v.getDescription());
            ps.setDouble(4, v.getPrix());
            ps.setDate(5, v.getDate_depart());
            ps.setDate(6, v.getDate_retour());
            ps.setString(7, v.getImage_url());
            ps.setInt(8, v.getId_categorie());
            ps.setInt(9, v.getPlaces_total());
            ps.setInt(10, v.getPlaces_restantes());
            ps.executeUpdate();
            System.out.println("Voyage ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout voyage : " + e.getMessage());
        }
    }
    @Override
    public void modifier(Voyage v) {
        String sql = "UPDATE voyage SET titre=?, destination=?, description=?, prix=?, date_depart=?, date_retour=?, image_url=?, id_categorie=?, places_total=?, places_restantes=? " +
                "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, v.getTitre());
            ps.setString(2, v.getDestination());
            ps.setString(3, v.getDescription());
            ps.setDouble(4, v.getPrix());
            ps.setDate(5, v.getDate_depart());
            ps.setDate(6, v.getDate_retour());
            ps.setString(7, v.getImage_url());
            ps.setInt(8, v.getId_categorie());
            ps.setInt(9, v.getPlaces_total());
            ps.setInt(10, v.getPlaces_restantes());
            ps.setInt(11, v.getId());
            ps.executeUpdate();
            System.out.println("Voyage modifié avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification voyage : " + e.getMessage());
        }
    }
    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM voyage WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Voyage supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression voyage : " + e.getMessage());
        }
    }
    @Override
    public List<Voyage> afficher() {
        List<Voyage> voyages = new ArrayList<>();
        String sql = "SELECT * FROM voyage";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                voyages.add(mapperVoyage(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage voyages : " + e.getMessage());
        }
        return voyages;
    }
    public Voyage getById(int id) {
        String sql = "SELECT * FROM voyage WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperVoyage(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById : " + e.getMessage());
        }
        return null;
    }

    public List<Voyage> rechercherStream(String keyword) {
        String k = (keyword == null) ? "" : keyword.trim().toLowerCase();
        return afficher().stream()
                .filter(v -> containsIgnoreCase(v.getTitre(), k) || containsIgnoreCase(v.getDestination(), k))
                .toList();
    }
    public List<Voyage> trierParPrixAscStream(List<Voyage> base) {
        return safeList(base).stream()
                .sorted(Comparator.comparingDouble(Voyage::getPrix))
                .toList();
    }
    public List<Voyage> trierParPrixDescStream(List<Voyage> base) {
        return safeList(base).stream()
                .sorted(Comparator.comparingDouble(Voyage::getPrix).reversed())
                .toList();
    }
    public List<Voyage> trierParDestinationAZStream(List<Voyage> base) {
        return safeList(base).stream()
                .sorted(Comparator.comparing(v -> safeStr(v.getDestination()).toLowerCase()))
                .toList();
    }
    public List<Voyage> trierParPlacesRestantesDescStream(List<Voyage> base) {
        return safeList(base).stream()
                .sorted(Comparator.comparingInt(Voyage::getPlaces_restantes).reversed())
                .toList();
    }
    public List<Voyage> filtrerParCategorieStream(List<Voyage> base, int idCategorie) {
        return safeList(base).stream()
                .filter(v -> v.getId_categorie() == idCategorie)
                .toList();
    }
    private boolean containsIgnoreCase(String text, String keywordLower) {
        if (keywordLower == null || keywordLower.isBlank()) return true;
        return safeStr(text).toLowerCase().contains(keywordLower);
    }
    private String safeStr(String s) {
        return (s == null) ? "" : s;
    }
    private List<Voyage> safeList(List<Voyage> list) {
        return (list == null) ? List.of() : list;
    }
    private Voyage mapperVoyage(ResultSet rs) throws SQLException {
        Voyage v = new Voyage();
        v.setId(rs.getInt("id"));
        v.setTitre(rs.getString("titre"));
        v.setDestination(rs.getString("destination"));
        v.setDescription(rs.getString("description"));
        v.setPrix(rs.getDouble("prix"));
        v.setDate_depart(rs.getDate("date_depart"));
        v.setDate_retour(rs.getDate("date_retour"));
        v.setImage_url(rs.getString("image_url"));
        v.setId_categorie(rs.getInt("id_categorie"));
        v.setPlaces_total(rs.getInt("places_total"));
        v.setPlaces_restantes(rs.getInt("places_restantes"));
        return v;
    }
}
