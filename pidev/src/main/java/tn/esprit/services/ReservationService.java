package tn.esprit.services;

import tn.esprit.entites.Reservation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {

    private final Connection cnx;

    public ReservationService() {
        cnx = MyDataBase.getInstance().getMyConnection();
    }
    @Override
    public void ajouter(Reservation r) {
        String qry = "INSERT INTO reservation (nbr_personnes, statut, id_voyage, id_user) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, r.getNbr_personnes());
            pstm.setString(2, r.getStatut());
            pstm.setInt(3, r.getIdVoyage());
            pstm.setInt(4, r.getIdUser());
            pstm.executeUpdate();
            System.out.println("Réservation effectuée !");
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la réservation : " + ex.getMessage());
        }
    }
    @Override
    public void modifier(Reservation r) {
        String qry = "UPDATE reservation SET nbr_personnes = ?, statut = ?, id_voyage = ?, id_user = ? WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, r.getNbr_personnes());
            pstm.setString(2, r.getStatut());
            pstm.setInt(3, r.getIdVoyage());
            pstm.setInt(4, r.getIdUser());
            pstm.setInt(5, r.getId());
            pstm.executeUpdate();
            System.out.println("Réservation mise à jour !");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    @Override
    public void supprimer(int id) {
        String qry = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            pstm.executeUpdate();
            System.out.println("Réservation supprimée !");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    @Override
    public List<Reservation> afficher() {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM reservation";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setDate_reservation(rs.getTimestamp("date_reservation"));
                r.setNbr_personnes(rs.getInt("nbr_personnes"));
                r.setStatut(rs.getString("statut"));
                r.setIdVoyage(rs.getInt("id_voyage"));
                r.setIdUser(rs.getInt("id_user"));
                reservations.add(r);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return reservations;
    }
    public void effectuerReservation(int idVoyage, int idUser, int nbrPersonnes) {
        String sqlRes = "INSERT INTO reservation (id_voyage, id_user, nbr_personnes, statut) VALUES (?, ?, ?, ?)";
        String sqlVoyage = "UPDATE voyage SET places_restantes = places_restantes - ? WHERE id = ?";
        try {
            cnx.setAutoCommit(false);
            try (PreparedStatement psRes = cnx.prepareStatement(sqlRes);
                 PreparedStatement psVoy = cnx.prepareStatement(sqlVoyage)) {
                psRes.setInt(1, idVoyage);
                psRes.setInt(2, idUser);
                psRes.setInt(3, nbrPersonnes);
                psRes.setString(4, "EN_ATTENTE");
                psRes.executeUpdate();
                psVoy.setInt(1, nbrPersonnes);
                psVoy.setInt(2, idVoyage);
                psVoy.executeUpdate();
                cnx.commit();
                System.out.println("Réservation réussie et places mises à jour !");
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            } finally {
                cnx.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Reservation> getReservationsParUtilisateur(int idUser) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = """
            SELECT r.id,
                   r.nbr_personnes,
                   r.statut,
                   r.id_voyage,
                   v.destination,
                   v.image_url
            FROM reservation r
            JOIN voyage v ON r.id_voyage = v.id
            WHERE r.id_user = ?
            ORDER BY r.date_reservation DESC
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setNbr_personnes(rs.getInt("nbr_personnes"));
                r.setStatut(rs.getString("statut"));
                r.setIdVoyage(rs.getInt("id_voyage"));
                r.setDestination(rs.getString("destination"));
                r.setImageUrl(rs.getString("image_url"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL Mes Réservations : " + e.getMessage());
        }
        return reservations;
    }
    public void annulerReservation(int reservationId) {
        String sqlGet = "SELECT id_voyage, nbr_personnes FROM reservation WHERE id = ?";
        String sqlDelete = "DELETE FROM reservation WHERE id = ?";
        String sqlUpdateVoyage = "UPDATE voyage SET places_restantes = places_restantes + ? WHERE id = ?";
        try {
            cnx.setAutoCommit(false);
            int idVoyage = -1;
            int nbr = 0;
            try (PreparedStatement psGet = cnx.prepareStatement(sqlGet)) {
                psGet.setInt(1, reservationId);
                ResultSet rs = psGet.executeQuery();
                if (rs.next()) {
                    idVoyage = rs.getInt("id_voyage");
                    nbr = rs.getInt("nbr_personnes");
                } else {
                    cnx.rollback();
                    System.err.println("Réservation introuvable id=" + reservationId);
                    return;
                }
            }
            try (PreparedStatement psDel = cnx.prepareStatement(sqlDelete);
                 PreparedStatement psUp = cnx.prepareStatement(sqlUpdateVoyage)) {
                psDel.setInt(1, reservationId);
                psDel.executeUpdate();
                psUp.setInt(1, nbr);
                psUp.setInt(2, idVoyage);
                psUp.executeUpdate();
                cnx.commit();
                System.out.println("Réservation annulée + places restaurées !");
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            } finally {
                cnx.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Reservation> getAllReservations() {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT r.*, v.destination FROM reservation r JOIN voyage v ON r.id_voyage = v.id";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setNbr_personnes(rs.getInt("nbr_personnes"));
                r.setDestination(rs.getString("destination"));
                r.setStatut(rs.getString("statut"));
                liste.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }
    public void confirmerReservation(int id) {
        String sql = "UPDATE reservation SET statut = 'Confirmée' WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}