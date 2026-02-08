package tn.esprit.services;

import tn.esprit.entities.Status;
import tn.esprit.entities.reservationlog;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface IService <T>{

    void ajouter(T t)throws SQLException;
    void modifier(T t)throws SQLException;
    void supprimer (int id)throws SQLException;
    List<T> afficher()throws SQLException;
    List<T> rechercherParAttribut(String nomAttribut, Object valeurRecherchee) throws SQLException;
}
