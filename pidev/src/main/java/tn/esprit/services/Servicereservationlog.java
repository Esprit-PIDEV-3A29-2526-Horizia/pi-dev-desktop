package tn.esprit.services;

import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
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

        System.out.println("=== AJOUT RÉSERVATION ===");
        System.out.println("ID Client: " + reservationlog.getIdc());
        System.out.println("ID Logement: " + reservationlog.getId_l());
        System.out.println("Date début: " + reservationlog.getDate_debut());
        System.out.println("Date fin: " + reservationlog.getDate_fin());
        System.out.println("Montant: " + reservationlog.getMontant());
        System.out.println("Statut: " + reservationlog.getStatus());
        System.out.println("Modalité: " + reservationlog.getModalite());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservationlog.getId_l());
            ps.setInt(2, reservationlog.getIdc());
            ps.setTimestamp(3, new Timestamp(reservationlog.getDate_debut().getTime()));
            ps.setTimestamp(4, new Timestamp(reservationlog.getDate_fin().getTime()));
            ps.setFloat(5, reservationlog.getMontant());
            ps.setString(6, reservationlog.getStatus().toString());
            ps.setString(7, reservationlog.getModalite());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservationlog.setId(generatedKeys.getInt(1));
                        System.out.println("Réservation ajoutée avec ID: " + reservationlog.getId());
                    }
                }
            }

            System.out.println("✅ Réservation ajoutée avec succès!");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'ajout: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(reservationlog reservationlog) throws SQLException {
        String sql = "UPDATE `reservationlog` SET `idlog`=?,`idc`=?,`date_debut`=?,`date_fin`=?,`montant`=?,`status`=?,`modalites`=? WHERE `idreslog`=?";

        System.out.println("=== MODIFICATION RÉSERVATION ID: " + reservationlog.getId() + " ===");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationlog.getId_l());
            ps.setInt(2, reservationlog.getIdc());
            ps.setTimestamp(3, new Timestamp(reservationlog.getDate_debut().getTime()));
            ps.setTimestamp(4, new Timestamp(reservationlog.getDate_fin().getTime()));
            ps.setFloat(5, reservationlog.getMontant());
            ps.setString(6, reservationlog.getStatus().toString());
            ps.setString(7, reservationlog.getModalite());
            ps.setInt(8, reservationlog.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Réservation modifiée avec succès!");
            } else {
                System.out.println("⚠️ Aucune réservation trouvée avec l'ID: " + reservationlog.getId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la modification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `reservationlog` WHERE `idreslog`=?";

        System.out.println("=== SUPPRESSION RÉSERVATION ID: " + id + " ===");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Réservation supprimée avec succès!");
            } else {
                System.out.println("⚠️ Aucune réservation trouvée avec l'ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la suppression: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<reservationlog> afficher() throws SQLException {
        List<reservationlog> reservations = new ArrayList<>();
        String sql = "SELECT * FROM `reservationlog`";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                reservationlog rl = new reservationlog();
                rl.setId(rs.getInt("idreslog"));
                rl.setId_l(rs.getInt("idlog"));
                rl.setIdc(rs.getInt("idc"));
                rl.setDate_debut(rs.getTimestamp("date_debut"));
                rl.setDate_fin(rs.getTimestamp("date_fin"));
                rl.setMontant(rs.getFloat("montant"));
                rl.setStatus(Status.valueOf(rs.getString("status")));
                rl.setModalite(rs.getString("modalites"));
                reservations.add(rl);
            }

            System.out.println("📊 " + reservations.size() + " réservations chargées");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'affichage: " + e.getMessage());
            throw e;
        }

        return reservations;
    }

    public List<reservationlog> rechercherParAttribut(String nomAttribut, Object valeur) throws SQLException {
        List<reservationlog> toutesReservations = afficher();

        return toutesReservations.stream()
                .filter(reservation -> {
                    try {
                        Field champ = reservationlog.class.getDeclaredField(nomAttribut);
                        champ.setAccessible(true);
                        Object valeurChamp = champ.get(reservation);

                        if (valeurChamp == null) {
                            return valeur == null;
                        }
                        return valeurChamp.equals(valeur);

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        System.err.println("❌ Attribut non trouvé: " + nomAttribut);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<reservationlog> trierParAttribut(String attribut, boolean ordreCroissant) throws SQLException {
        List<reservationlog> reservations = new ArrayList<>();

        List<String> attributsAutorises = List.of("date_debut", "date_fin", "montant", "status", "modalites");

        if (!attributsAutorises.contains(attribut)) {
            throw new IllegalArgumentException("Attribut de tri non valide : " + attribut);
        }

        String ordre = ordreCroissant ? "ASC" : "DESC";
        String sql = "SELECT * FROM `reservationlog` ORDER BY `" + attribut + "` " + ordre;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                reservationlog rl = new reservationlog();
                rl.setId(rs.getInt("idreslog"));
                rl.setId_l(rs.getInt("idlog"));
                rl.setIdc(rs.getInt("idc"));
                rl.setDate_debut(rs.getTimestamp("date_debut"));
                rl.setDate_fin(rs.getTimestamp("date_fin"));
                rl.setMontant(rs.getFloat("montant"));
                rl.setStatus(Status.valueOf(rs.getString("status")));
                rl.setModalite(rs.getString("modalites"));
                reservations.add(rl);
            }
        }
        return reservations;
    }

    /**
     * Récupère les réservations d'un client spécifique
     */
    public List<reservationlog> getReservationsByClientId(int clientId) throws SQLException {
        List<reservationlog> reservations = new ArrayList<>();
        String sql = "SELECT * FROM `reservationlog` WHERE `idc` = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservationlog rl = new reservationlog();
                    rl.setId(rs.getInt("idreslog"));
                    rl.setId_l(rs.getInt("idlog"));
                    rl.setIdc(rs.getInt("idc"));
                    rl.setDate_debut(rs.getTimestamp("date_debut"));
                    rl.setDate_fin(rs.getTimestamp("date_fin"));
                    rl.setMontant(rs.getFloat("montant"));
                    rl.setStatus(Status.valueOf(rs.getString("status")));
                    rl.setModalite(rs.getString("modalites"));
                    reservations.add(rl);
                }
            }
        }

        return reservations;
    }
    public void updateStatusById(int id, Status newStatus) throws SQLException {
        String sql = "UPDATE `reservationlog` SET `status`=? WHERE `idreslog`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus.toString());
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Statut mis à jour pour la réservation " + id + " : " + newStatus);
            }
        }
    }
    public reservationlog rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM `reservationlog` WHERE `idreslog` = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reservationlog rl = new reservationlog();
                    rl.setId(rs.getInt("idreslog"));
                    rl.setId_l(rs.getInt("idlog"));
                    rl.setIdc(rs.getInt("idc"));
                    rl.setDate_debut(rs.getTimestamp("date_debut"));
                    rl.setDate_fin(rs.getTimestamp("date_fin"));
                    rl.setMontant(rs.getFloat("montant"));
                    rl.setStatus(Status.valueOf(rs.getString("status")));
                    rl.setModalite(rs.getString("modalites"));
                    return rl;
                }
            }
        }
        return null;
    }
}