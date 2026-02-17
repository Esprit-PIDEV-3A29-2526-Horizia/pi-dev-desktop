package tn.esprit.services;

import tn.esprit.entites.Reservation;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {

    private Connection cnx;

    public ReservationService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Reservation r) {
        // La date_reservation est mise à CURRENT_TIMESTAMP par défaut dans MySQL
        String qry = "INSERT INTO reservation (nb_personnes, statut, id_voyage, id_utilisateur) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, r.getNbPersonnes());
            pstm.setString(2, r.getStatut());
            pstm.setInt(3, r.getIdVoyage());
            pstm.setInt(4, r.getIdUtilisateur());

            pstm.executeUpdate();
            System.out.println("Réservation effectuée !");
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la réservation : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Reservation r) {
        String qry = "UPDATE reservation SET nb_personnes = ?, statut = ?, id_voyage = ?, id_utilisateur = ? WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, r.getNbPersonnes());
            pstm.setString(2, r.getStatut());
            pstm.setInt(3, r.getIdVoyage());
            pstm.setInt(4, r.getIdUtilisateur());
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
            System.out.println("Réservation annulée/supprimée !");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public List<Reservation> afficher() {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM reservation";
        try (Statement stm = cnx.createStatement(); ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                reservations.add(new Reservation(
                        rs.getInt("id"),
                        rs.getTimestamp("date_reservation"),
                        rs.getInt("nb_personnes"),
                        rs.getString("statut"),
                        rs.getInt("id_voyage"),
                        rs.getInt("id_utilisateur")
                ));
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return reservations;
    }
    // Dans ReservationService.java
    public void effectuerReservation(int idVoyage, int idUser, int nbPlaces) {
        // Requête pour insérer la réservation
        String sqlRes = "INSERT INTO reservation (id_voyage, id_user, nbr_personnes) VALUES (?, ?, ?)";
        // Requête pour mettre à jour les places restantes du voyage
        String sqlVoyage = "UPDATE voyage SET places_restantes = places_restantes - ? WHERE id = ?";

        try {
            // Il est préférable d'utiliser une transaction ici
            PreparedStatement psRes = cnx.prepareStatement(sqlRes);
            psRes.setInt(1, idVoyage);
            psRes.setInt(2, idUser);
            psRes.setInt(3, nbPlaces);
            psRes.executeUpdate();

            PreparedStatement psVoy = cnx.prepareStatement(sqlVoyage);
            psVoy.setInt(1, nbPlaces);
            psVoy.setInt(2, idVoyage);
            psVoy.executeUpdate();

            System.out.println("✅ Réservation réussie et places mises à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}