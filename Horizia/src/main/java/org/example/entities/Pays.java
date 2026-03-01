package org.example.entities;

/**
 * Classe représentant un pays avec son indicatif téléphonique
 */
public class Pays {
    private String nom;
    private String indicatif;
    private String drapeau;
    private String formatExemple;

    public Pays(String nom, String indicatif, String drapeau, String formatExemple) {
        this.nom = nom;
        this.indicatif = indicatif;
        this.drapeau = drapeau;
        this.formatExemple = formatExemple;
    }

    public String getNom() {
        return nom;
    }

    public String getIndicatif() {
        return indicatif;
    }

    public String getDrapeau() {
        return drapeau;
    }

    public String getFormatExemple() {
        return formatExemple;
    }

    @Override
    public String toString() {
        return drapeau + " " + nom + " (" + indicatif + ")";
    }

    /**
     * Retourne le numéro de téléphone complet avec l'indicatif
     */
    public String formaterNumero(String numero) {
        // Nettoyer le numéro
        String numeroClean = numero.replaceAll("[\\s-]", "");

        // Si le numéro commence déjà par le bon indicatif, le retourner tel quel
        if (numeroClean.startsWith(indicatif)) {
            return numeroClean;
        }

        // Si le numéro commence par 0, le remplacer par l'indicatif
        if (numeroClean.startsWith("0")) {
            numeroClean = numeroClean.substring(1);
        }

        return indicatif + numeroClean;
    }

    /**
     * Liste des pays supportés
     */
    public static Pays[] getPaysSupportes() {
        return new Pays[]{
                new Pays("Tunisie", "+216", "🇹🇳", "98 123 456"),
                new Pays("France", "+33", "🇫🇷", "6 12 34 56 78"),
                new Pays("Maroc", "+212", "🇲🇦", "6 12 34 56 78"),
                new Pays("Algérie", "+213", "🇩🇿", "5 12 34 56 78"),
                new Pays("Belgique", "+32", "🇧🇪", "470 12 34 56"),
                new Pays("Suisse", "+41", "🇨🇭", "78 123 45 67"),
                new Pays("Canada", "+1", "🇨🇦", "514 123 4567"),
                new Pays("Allemagne", "+49", "🇩🇪", "151 12345678"),
                new Pays("Italie", "+39", "🇮🇹", "312 345 6789"),
                new Pays("Espagne", "+34", "🇪🇸", "612 34 56 78"),
                new Pays("Royaume-Uni", "+44", "🇬🇧", "7700 123456"),
                new Pays("Libye", "+218", "🇱🇾", "91 123 4567"),
                new Pays("Égypte", "+20", "🇪🇬", "100 123 4567"),
                new Pays("Arabie Saoudite", "+966", "🇸🇦", "50 123 4567"),
                new Pays("Émirats Arabes Unis", "+971", "🇦🇪", "50 123 4567"),
                new Pays("Qatar", "+974", "🇶🇦", "3312 3456"),
                new Pays("Koweït", "+965", "🇰🇼", "9123 4567"),
                new Pays("Autre", "+", "🌍", "XX XXX XXXX")
        };
    }
}