package tn.esprit.entities;

import java.time.LocalDate;

public class Commentaire {

    private int id;
    private String contenu;
    private LocalDate dateCommentaire;
    private int publicationId;

    public Commentaire() {}

    public Commentaire(String contenu, LocalDate dateCommentaire, int publicationId) {
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
        this.publicationId = publicationId;
    }

    // getters & setters
}
