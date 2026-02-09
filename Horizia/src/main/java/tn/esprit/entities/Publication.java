package tn.esprit.entities;

import java.time.LocalDate;

public class Publication {

    private int id;
    private String titre;
    private String description;
    private String destination;
    private LocalDate datePublication;

    public Publication() {}

    public Publication(String titre, String description, String destination, LocalDate datePublication) {
        this.titre = titre;
        this.description = description;
        this.destination = destination;
        this.datePublication = datePublication;
    }

    public Publication(int id, String titre, String description, String destination, LocalDate datePublication) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.destination = destination;
        this.datePublication = datePublication;
    }

    // getters & setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    @Override
    public String toString() {
        return "Publication{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", destination='" + destination + '\'' +
                ", date=" + datePublication +
                '}';
    }
}
