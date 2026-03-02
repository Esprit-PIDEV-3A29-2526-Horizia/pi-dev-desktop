package tn.esprit.entities;
import java.sql.Timestamp;
public class Location {

    private int idLocation;
    private int idVehicule;
    private String clientNomComplet;
    private String clientTelephone;
    private String clientCin;

    // Champs de géolocalisation
    private String clientAdresse;
    private String clientVille;
    private String clientCodePostal;
    private Double clientLatitude;
    private Double clientLongitude;

    private Timestamp dateDebut;
    private Timestamp dateFinPrev;
    private Timestamp dateFinReelle;
    private int kilometrageDebut;
    private Integer kilometrageRetour; // null si pas encore retourné
    private double prixParJour;
    private double montantTotal;
    private double avance;
    private String statut;
    private String notes;

    public Location() {}

    public Location(int idVehicule, String clientNomComplet, String clientTelephone,
                    Timestamp dateDebut, Timestamp dateFinPrev, int kilometrageDebut,
                    double prixParJour, double montantTotal, String statut) {
        this.idVehicule = idVehicule;
        this.clientNomComplet = clientNomComplet;
        this.clientTelephone = clientTelephone;
        this.dateDebut = dateDebut;
        this.dateFinPrev = dateFinPrev;
        this.kilometrageDebut = kilometrageDebut;
        this.prixParJour = prixParJour;
        this.montantTotal = montantTotal;
        this.statut = statut;
    }

    public int getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(int idLocation) {
        this.idLocation = idLocation;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getClientNomComplet() {
        return clientNomComplet;
    }

    public void setClientNomComplet(String clientNomComplet) {
        this.clientNomComplet = clientNomComplet;
    }

    public String getClientTelephone() {
        return clientTelephone;
    }

    public void setClientTelephone(String clientTelephone) {
        this.clientTelephone = clientTelephone;
    }

    public String getClientCin() {
        return clientCin;
    }

    public void setClientCin(String clientCin) {
        this.clientCin = clientCin;
    }

    public Timestamp getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Timestamp dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Timestamp getDateFinPrev() {
        return dateFinPrev;
    }

    public void setDateFinPrev(Timestamp dateFinPrev) {
        this.dateFinPrev = dateFinPrev;
    }

    public Timestamp getDateFinReelle() {
        return dateFinReelle;
    }

    public void setDateFinReelle(Timestamp dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
    }

    public int getKilometrageDebut() {
        return kilometrageDebut;
    }

    public void setKilometrageDebut(int kilometrageDebut) {
        this.kilometrageDebut = kilometrageDebut;
    }

    public Integer getKilometrageRetour() {
        return kilometrageRetour;
    }

    public void setKilometrageRetour(Integer kilometrageRetour) {
        this.kilometrageRetour = kilometrageRetour;
    }

    public double getPrixParJour() {
        return prixParJour;
    }

    public void setPrixParJour(double prixParJour) {
        this.prixParJour = prixParJour;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public double getAvance() {
        return avance;
    }

    public void setAvance(double avance) {
        this.avance = avance;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Getters et Setters pour la géolocalisation
    public String getClientAdresse() {
        return clientAdresse;
    }

    public void setClientAdresse(String clientAdresse) {
        this.clientAdresse = clientAdresse;
    }

    public String getClientVille() {
        return clientVille;
    }

    public void setClientVille(String clientVille) {
        this.clientVille = clientVille;
    }

    public String getClientCodePostal() {
        return clientCodePostal;
    }

    public void setClientCodePostal(String clientCodePostal) {
        this.clientCodePostal = clientCodePostal;
    }

    public Double getClientLatitude() {
        return clientLatitude;
    }

    public void setClientLatitude(Double clientLatitude) {
        this.clientLatitude = clientLatitude;
    }

    public Double getClientLongitude() {
        return clientLongitude;
    }

    public void setClientLongitude(Double clientLongitude) {
        this.clientLongitude = clientLongitude;
    }

    @Override
    public String toString() {
        return "Location #" + idLocation + " - Client: " + clientNomComplet +
                " - Voiture: " + idVehicule + " - Statut: " + statut;
    }
}