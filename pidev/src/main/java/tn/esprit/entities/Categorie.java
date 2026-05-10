package tn.esprit.entities;

public enum Categorie {
    TOUS("Tous", "📌"),
    PLAGE("Plage", "🏖️"),
    MONTAGNE("Montagne", "⛰️"),
    VILLE("Ville", "🏙️"),
    DESERT("Désert", "🏜️"),
    CAMPAGNE("Campagne", "🌾"),
    HISTORIQUE("Historique", "🏛️");

    private final String label;
    private final String emoji;

    Categorie(String label, String emoji) {
        this.label = label;
        this.emoji = emoji;
    }

    public String getLabel() { return label; }
    public String getEmoji() { return emoji; }
    public String getDisplay() { return emoji + " " + label; }

    /**
     * Convertit une chaîne de caractères (provenant de la base de données) en valeur de l'énumération.
     * Accepte à la fois les noms d'énumération (ex: "VILLE") et les libellés (ex: "Ville").
     * @param value la valeur à convertir
     * @return la valeur de l'énumération correspondante, ou TOUS si non trouvée
     */
    public static Categorie fromString(String value) {
        if (value == null) return TOUS;

        // Essayer d'abord de matcher avec les noms d'énumération
        try {
            return Categorie.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Ignorer et continuer
        }

        // Essayer de matcher avec les libellés
        switch (value) {
            case "Tous": return TOUS;
            case "Plage": return PLAGE;
            case "Montagne": return MONTAGNE;
            case "Ville": return VILLE;
            case "Désert": return DESERT;
            case "Campagne": return CAMPAGNE;
            case "Historique": return HISTORIQUE;
            default: return TOUS;
        }
    }
}