package tn.esprit.backend.utils;

public class PartageService {
    private static final String BASE_URL = "horizia://publication/"; // ou https://...

    public static String genererLien(int publicationId) {
        return BASE_URL + publicationId;
    }
}