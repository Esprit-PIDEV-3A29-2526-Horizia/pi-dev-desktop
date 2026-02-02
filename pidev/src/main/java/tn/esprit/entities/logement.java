package tn.esprit.entities;

public class logement {
    private int id;
    private String type;
    private String adresse;
    private int capacite;
    private String equipement;
    private float tarif_nuit;
    private boolean disponibilite;

    public logement() {
    }

    public logement(int id, String type, String adresse, int capacite, String equipement, float tarif_nuit, boolean disponibilite) {
        this.id = id;
        this.type = type;
        this.adresse = adresse;
        this.capacite = capacite;
        this.equipement = equipement;
        this.tarif_nuit = tarif_nuit;
        this.disponibilite = disponibilite;
    }
    public logement( String type, String adresse,int capacite, String equipement, float tarif_nuit, boolean disponibilite) {
        this.type = type;
        this.adresse = adresse;
        this.capacite = capacite;
        this.equipement = equipement;
        this.tarif_nuit = tarif_nuit;
        this.disponibilite = disponibilite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }



    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getEquipement() {
        return equipement;
    }

    public void setEquipement(String equipement) {
        this.equipement = equipement;
    }

    public float getTarif_nuit() {
        return tarif_nuit;
    }

    public void setTarif_nuit(float tarif_nuit) {
        this.tarif_nuit = tarif_nuit;
    }

    public boolean isDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }

    @Override
    public String toString() {
        return "logement{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", adresse='" + adresse + '\'' +
                ", capacite=" + capacite +
                ", equipement='" + equipement + '\'' +
                ", tarif_nuit=" + tarif_nuit +
                ", disponibilite=" + disponibilite +
                '}';
    }
}
