package tn.esprit.backend.entities;

public enum Role {
    ADMIN("Administrateur"),
    USER("Utilisateur");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

