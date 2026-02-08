package tn.esprit.services;

import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;  // Import the Status enum
import tn.esprit.utils.MyDataBase;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Servicereservationlog implements IService<reservationlog> {
    private Connection connection;

    public Servicereservationlog() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(reservationlog reservationlog) throws SQLException {
        String sql = "INSERT INTO `reservationlog`(`idlog`, `idc`, `date_debut`, `date_fin`, `montant`, `status`, `modalites`) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, reservationlog.getId_l());
        ps.setInt(2, reservationlog.getIdc());
        ps.setTimestamp(3, new Timestamp(reservationlog.getDate_debut().getTime()));
        ps.setTimestamp(4, new Timestamp(reservationlog.getDate_fin().getTime()));
        ps.setFloat(5, reservationlog.getMontant());
        ps.setString(6, reservationlog.getStatus().toString());  // Assumes enum toString() returns the name (e.g., "PENDING")
        ps.setString(7, reservationlog.getModalite());
        ps.executeUpdate();
    }

    @Override
    public void modifier(reservationlog reservationlog) throws SQLException {
        String sql = "UPDATE `reservationlog` SET `idlog`=?,`idc`=?,`date_debut`=?,`date_fin`=?,`montant`=?,`status`=?,`modalites`=? WHERE `idreslog`=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, reservationlog.getId_l());
        ps.setInt(2, reservationlog.getIdc());
        ps.setTimestamp(3, new Timestamp(reservationlog.getDate_debut().getTime()));
        ps.setTimestamp(4, new Timestamp(reservationlog.getDate_fin().getTime()));
        ps.setFloat(5, reservationlog.getMontant());
        ps.setString(6, reservationlog.getStatus().toString());
        ps.setString(7, reservationlog.getModalite());
        ps.setInt(8, reservationlog.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `reservationlog` WHERE `idreslog`=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<reservationlog> afficher() throws SQLException {
        List<reservationlog> reservations = new ArrayList<>();
        String sql = "SELECT * FROM `reservationlog`";
        try (Statement st = connection.createStatement();  // Added try-with-resources for safety
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                reservationlog rl = new reservationlog();
                rl.setId(rs.getInt("idreslog"));
                rl.setId_l(rs.getInt("idlog"));
                rl.setIdc(rs.getInt("idc"));
                rl.setDate_debut(rs.getTimestamp("date_debut"));
                rl.setDate_fin(rs.getTimestamp("date_fin"));
                rl.setMontant(rs.getFloat("montant"));
                rl.setStatus(Status.valueOf(rs.getString("status")));  // Fixed: Convert String to Status enum
                rl.setModalite(rs.getString("modalites"));
                reservations.add(rl);
            }
        }
        return reservations;
    }
    @Override
    public List<reservationlog> rechercherParAttribut(String nomAttribut, Object valeur) throws SQLException {
        List<reservationlog> toutesReservations = afficher();

        return toutesReservations.stream()
                .filter(reservation -> {
                    try {
                        // Utilisation de la réflexion pour accéder à l'attribut
                        Field champ = reservationlog.class.getDeclaredField(nomAttribut);
                        champ.setAccessible(true);
                        Object valeurChamp = champ.get(reservation);

                        // Comparaison selon le type de l'attribut
                        if (valeurChamp == null) {
                            return valeur == null;
                        }
                        return valeurChamp.equals(valeur);

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        System.err.println("Attribut non trouvé: " + nomAttribut);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }





}