package tn.esprit.services;

import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
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
        String sql = "INSERT INTO `logement`(`typelog`, `adresse`, `capacite`, `equipement`, `tarif_nuit`, `disponibilite`) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, logement.getType());
        ps.setString(2, logement.getAdresse());
        ps.setInt(3, logement.getCapacite());
        ps.setString(4, logement.getEquipement());
        ps.setFloat(5, logement.getTarif_nuit());
        ps.setBoolean(6, logement.isDisponibilite());
        ps.executeUpdate();
    }

    @Override
    public void modifier(logement logement) throws SQLException {
        String sql = "UPDATE `logement` SET `typelog`=?,`adresse`=?,`capacite`=?,`equipement`=?,`tarif_nuit`=?,`disponibilite`=? WHERE `idlog`=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, logement.getType());
        ps.setString(2, logement.getAdresse());
        ps.setInt(3, logement.getCapacite());
        ps.setString(4, logement.getEquipement());
        ps.setFloat(5, logement.getTarif_nuit());
        ps.setBoolean(6, logement.isDisponibilite());
        ps.setInt(7, logement.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `logement` WHERE `idlog`=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<logement> afficher() throws SQLException {
        List<logement> logements = new ArrayList<>();
        String sql = "SELECT * FROM `logement`";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            logement l =new logement();
            l.setId(rs.getInt("idlog"));
            l.setType(rs.getString("typelog"));
            l.setAdresse(rs.getString("adresse"));
            l.setCapacite(rs.getInt("capacite"));
            l.setEquipement(rs.getString("equipement"));
            l.setTarif_nuit(rs.getFloat("tarif_nuit"));
            l.setDisponibilite(rs.getBoolean("disponibilite"));
            logements.add(l);
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
}
