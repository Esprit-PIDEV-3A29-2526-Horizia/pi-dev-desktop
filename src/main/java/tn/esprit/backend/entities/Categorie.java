package tn.esprit.backend.entities;

public enum Categorie {
    TOUS("Tous", "ðŸ“°"),
    PLAGE("Plage", "ðŸ–ï¸"),
    MONTAGNE("Montagne", "ðŸ”ï¸"),
    VILLE("Ville", "ðŸ™ï¸"),
    DESERT("DÃ©sert", "ðŸœï¸"),
    CAMPAGNE("Campagne", "ðŸŒ¾"),
    HISTORIQUE("Historique", "ðŸ›ï¸");

    private final String label;
    private final String emoji;

    Categorie(String label, String emoji) {
        this.label = label;
        this.emoji = emoji;
    }

    public String getLabel() { return label; }
    public String getEmoji() { return emoji; }
    public String getDisplay() { return emoji + " " + label; }
}

