package tn.esprit.backend.utils;

import tn.esprit.backend.entities.Publication;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SelectedItem {
    private static final ObjectProperty<Publication> currentPublication = new SimpleObjectProperty<>();

    public static Publication getCurrentPublication() {
        return currentPublication.get();
    }

    public static void setCurrentPublication(Publication publication) {
        currentPublication.set(publication);
    }

    public static ObjectProperty<Publication> currentPublicationProperty() {
        return currentPublication;
    }

    public static void clear() {
        currentPublication.set(null);
    }
}