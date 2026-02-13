package tn.esprit;

import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogementServicesTest {

    private static Servicelogement service;
    private static String testPrefix; // Pour identifier les données de test

    @BeforeAll
    static void setup() {
        service = new Servicelogement();
        testPrefix = "TEST_" + UUID.randomUUID().toString().substring(0, 8) + "_";
    }

    @Test
    @Order(1)
    void testAjouterLogement() throws SQLException {
        // Création avec un nom unique
        String uniqueNom = testPrefix + "Hotel Test";
        logement l = new logement("Hotel", uniqueNom, "https://example.com/image.jpg", "Test Adresse", 4, "WiFi, Piscine", 150.0f, true);
        service.ajouter(l); // l'objet a maintenant son ID renseigné (grâce à RETURN_GENERATED_KEYS)

        int idGenere = l.getId();
        assertTrue(idGenere > 0, "L'ID généré devrait être > 0");
        System.out.println("ID du logement test: " + idGenere);

        // Vérifier que le logement est bien présent
        List<logement> logements = service.afficher();
        boolean trouve = logements.stream().anyMatch(log -> log.getId() == idGenere);
        assertTrue(trouve, "Le logement ajouté n'a pas été trouvé dans la liste");
    }

    @Test
    @Order(2)
    void testModifierLogement() throws SQLException {
        // Ajouter un logement de test
        String uniqueNom = testPrefix + "Hotel Modify";
        logement l = new logement("Hotel", uniqueNom, "https://example.com/image.jpg", "Adresse Originale", 4, "WiFi", 100.0f, true);
        service.ajouter(l);
        int id = l.getId();

        // Modifier
        l.setNom(uniqueNom + " Modifié");
        l.setAdresse("Nouvelle Adresse");
        l.setTarif_nuit(200.0f);
        service.modifier(l);

        // Vérifier
        List<logement> logements = service.afficher();
        logement modifie = logements.stream().filter(log -> log.getId() == id).findFirst().orElse(null);
        assertNotNull(modifie, "Le logement modifié devrait exister");
        assertEquals("Nouvelle Adresse", modifie.getAdresse());
        assertEquals(200.0f, modifie.getTarif_nuit(), 0.001);
    }

    @Test
    @Order(3)
    void testSupprimerLogement() throws SQLException {
        // Ajouter un logement de test
        String uniqueNom = testPrefix + "Hotel Delete";
        logement l = new logement("Hotel", uniqueNom, "https://example.com/image.jpg", "Adresse Delete", 4, "WiFi", 100.0f, true);
        service.ajouter(l);
        int id = l.getId();

        // Supprimer
        service.supprimer(id);

        // Vérifier
        List<logement> logements = service.afficher();
        boolean existe = logements.stream().anyMatch(log -> log.getId() == id);
        assertFalse(existe, "Le logement aurait dû être supprimé");
    }

    @AfterAll
    static void cleanUpAll() throws SQLException {
        // Nettoyage final : supprimer tous les logements dont le nom commence par le préfixe de test
        List<logement> logements = service.afficher();
        for (logement l : logements) {
            if (l.getNom() != null && l.getNom().startsWith(testPrefix)) {
                service.supprimer(l.getId());
            }
        }
        System.out.println("Nettoyage des données de test terminé.");
    }
}