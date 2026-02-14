package tn.esprit.services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {

    void ajouter(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    void updatePlaces(int id_event, int Places_Restantes) throws SQLException;
    List<T> afficher() throws SQLException;

    List<T> rechercher(String keyword) throws SQLException;
    List<T> trier(String column, String order) throws SQLException;
}
