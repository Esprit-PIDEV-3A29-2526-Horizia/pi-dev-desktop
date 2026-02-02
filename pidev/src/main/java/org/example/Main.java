package org.example;

import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;

import java.sql.SQLException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Servicelogement sl=new Servicelogement();
        try{
            sl.ajouter(new logement("hotel yassine","Tunis",5,"connexion,piscine",1000,true));
           System.out.println("logement ajoutée");
           //sl.modifier(new logement(3,"hotel Syfax","Sfax",12,"connexion,piscine",1000,true));
         // System.out.println("logement modifier");
            //sl.supprimer(2);
           // System.out.println("logement supprime");
            System.out.println(sl.afficher());

        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
}