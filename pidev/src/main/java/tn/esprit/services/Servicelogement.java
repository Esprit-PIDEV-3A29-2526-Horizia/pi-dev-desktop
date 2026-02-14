package tn.esprit.services;

import tn.esprit.entities.logement;
import tn.esprit.utils.MyDataBase;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Servicelogement implements IService<logement> {
    private Connection connection;

    public Servicelogement() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(logement logement) throws SQLException {
        String sql = "INSERT INTO `logement`(`type`, `nom`, `image`, `adresse`, `capacite`, `equipement`, `tarif_nuit`, `disponibilite`) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, logement.getType());
            ps.setString(2, logement.getNom());
            ps.setString(3, logement.getImage());
            ps.setString(4, logement.getAdresse());
            ps.setInt(5, logement.getCapacite());
            ps.setString(6, logement.getEquipement());
            ps.setFloat(7, logement.getTarif_nuit());
            ps.setBoolean(8, logement.isDisponibilite());
            ps.executeUpdate();

            // Récupérer l'ID généré et le setter dans l'objet (optionnel)
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    logement.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(logement logement) throws SQLException {
        String sql = "UPDATE `logement` SET `type`=?, `nom`=?, `image`=?, `adresse`=?, `capacite`=?, `equipement`=?, `tarif_nuit`=?, `disponibilite`=? WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, logement.getType());
            ps.setString(2, logement.getNom());
            ps.setString(3, logement.getImage());
            ps.setString(4, logement.getAdresse());
            ps.setInt(5, logement.getCapacite());
            ps.setString(6, logement.getEquipement());
            ps.setFloat(7, logement.getTarif_nuit());
            ps.setBoolean(8, logement.isDisponibilite());
            ps.setInt(9, logement.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `logement` WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<logement> afficher() throws SQLException {
        List<logement> logements = new ArrayList<>();
        String sql = "SELECT * FROM `logement`";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                logement l = new logement();
                l.setId(rs.getInt("id"));
                l.setType(rs.getString("type"));
                l.setNom(rs.getString("nom"));
                l.setImage(rs.getString("image"));
                l.setAdresse(rs.getString("adresse"));
                l.setCapacite(rs.getInt("capacite"));
                l.setEquipement(rs.getString("equipement"));
                l.setTarif_nuit(rs.getFloat("tarif_nuit"));
                l.setDisponibilite(rs.getBoolean("disponibilite"));
                logements.add(l);
            }
        }
        return logements;
    }

    // Méthode utilitaire pour obtenir le dernier ID inséré (alternative)
    public int getLastInsertId() throws SQLException {
        String sql = "SELECT LAST_INSERT_ID() AS last_id";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("last_id");
            }
        }
        return -1;
    }

    // Nouvelle méthode pour rechercher dans la base de données (optimisée)
    public List<logement> rechercher(String keyword) throws SQLException {
        List<logement> logements = new ArrayList<>();
        String sql = "SELECT * FROM `logement` WHERE `nom` LIKE ? OR `adresse` LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword + "%";
            ps.setString(1, likeKeyword);
            ps.setString(2, likeKeyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logement l = new logement();
                    l.setId(rs.getInt("id"));
                    l.setType(rs.getString("type"));
                    l.setNom(rs.getString("nom"));
                    l.setImage(rs.getString("image"));
                    l.setAdresse(rs.getString("adresse"));
                    l.setCapacite(rs.getInt("capacite"));
                    l.setEquipement(rs.getString("equipement"));
                    l.setTarif_nuit(rs.getFloat("tarif_nuit"));
                    l.setDisponibilite(rs.getBoolean("disponibilite"));
                    logements.add(l);
                }
            }
        }
        return logements;
    }

    @Override
    public List<logement> rechercherParAttribut(String nomAttribut, Object valeurRecherchee) throws SQLException {
        List<logement> touteslogements = afficher();

        return touteslogements.stream()
                .filter(log -> {
                    try {
                        Field champ = logement.class.getDeclaredField(nomAttribut);
                        champ.setAccessible(true);
                        Object valeurChamp = champ.get(log);

                        // Gestion spéciale pour les comparaisons de nombres flottants
                        if (valeurChamp instanceof Float && valeurRecherchee instanceof Float) {
                            float valeurFloat = (Float) valeurChamp;
                            float rechercheFloat = (Float) valeurRecherchee;
                            return Math.abs(valeurFloat - rechercheFloat) < 0.001; // Tolérance pour les floats
                        }

                        // Gestion spéciale pour les booléens
                        if (valeurChamp instanceof Boolean && valeurRecherchee instanceof String) {
                            Boolean boolValue = (Boolean) valeurChamp;
                            String stringValue = (String) valeurRecherchee;
                            return boolValue.toString().equalsIgnoreCase(stringValue);
                        }

                        // Gestion spéciale pour les chaînes de caractères : recherche partielle et insensible à la casse
                        if (valeurChamp instanceof String && valeurRecherchee instanceof String) {
                            String stringValue = (String) valeurChamp;
                            String searchValue = (String) valeurRecherchee;
                            return stringValue.toLowerCase().contains(searchValue.toLowerCase());
                        }

                        // Comparaison par défaut
                        if (valeurChamp == null) {
                            return valeurRecherchee == null;
                        }
                        return valeurChamp.equals(valeurRecherchee);

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        System.err.println("Attribut non trouvé: " + nomAttribut + " - " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<logement> trierParAttribut(String attribut, boolean ordreCroissant) throws SQLException {
        List<logement> logements = new ArrayList<>();

        // Updated allowed attributes to match DB column names: removed non-matching ones like "ville", "pays", "id_proprietaire" (not in schema), added "nom", "image"
        List<String> attributsAutorises = List.of("type", "nom", "image", "adresse", "capacite", "equipement", "tarif_nuit", "disponibilite");

        if (!attributsAutorises.contains(attribut)) {
            throw new IllegalArgumentException("Attribut de tri non valide : " + attribut);
        }

        String ordre = ordreCroissant ? "ASC" : "DESC";
        String sql = "SELECT * FROM `logement` ORDER BY `" + attribut + "` " + ordre;

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            logement l = new logement();
            // Updated column names in ResultSet getters
            l.setId(rs.getInt("id"));
            l.setType(rs.getString("type"));
            l.setNom(rs.getString("nom"));
            l.setImage(rs.getString("image"));
            l.setAdresse(rs.getString("adresse"));
            l.setCapacite(rs.getInt("capacite"));
            l.setEquipement(rs.getString("equipement"));
            l.setTarif_nuit(rs.getFloat("tarif_nuit"));
            l.setDisponibilite(rs.getBoolean("disponibilite"));
            logements.add(l);
        }
        return logements;
    }
}