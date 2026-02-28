package tn.esprit.backend.services;

import java.util.List;

// EnlÃ¨ve le "public" ou renomme l'interface en IService
public interface IService<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(int id);
    T getById(int id);
    List<T> getAll();
}

