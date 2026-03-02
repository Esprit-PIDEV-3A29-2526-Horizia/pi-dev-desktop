package tn.esprit.services;

import tn.esprit.entities.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Service de gestion des Contrats et Factures pour Horizia
 */
public class ContratService {

    // ─── Informations de l'agence ────────────────────────────────
    private static final String NOM_AGENCE     = "Horizia";
    private static final String ADRESSE_AGENCE = "Tunis, Tunisie";
    private static final String TEL_AGENCE     = "+216 XX XXX XXX";
    private static final String EMAIL_AGENCE   = "horizia.agence@gmail.com";
    private static final String MF_AGENCE      = "MF: XXXXXXXX/A/M/000";

    // ─── Tarifs extras (TND/jour) ─────────────────────────────────
    public static final double TARIF_GPS             = 5.0;
    public static final double TARIF_SIEGE_BEBE      = 3.0;
    public static final double TARIF_ASSURANCE_COMP  = 15.0;
    public static final double TARIF_CHAUFFEUR       = 50.0;
    public static final double TARIF_CARBURANT       = 10.0;

    // ─── Pénalités ────────────────────────────────────────────────
    public static final double PENALITE_RETARD_PAR_HEURE  = 5.0;
    public static final double PENALITE_CARBURANT_MANQUANT = 20.0;
    public static final double PENALITE_DOMMAGE_LEGER     = 50.0;
    public static final double PENALITE_DOMMAGE_GRAVE     = 200.0;

    // ─────────────────────────────────────────────────────────────
    // CALCULS FINANCIERS
    // ─────────────────────────────────────────────────────────────

    public static long calculerNbJours(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) return 0;
        long jours = ChronoUnit.DAYS.between(debut, fin);
        return jours <= 0 ? 1 : jours;
    }

    public static double calculerMontantBase(double prixParJour, LocalDate debut, LocalDate fin) {
        return prixParJour * calculerNbJours(debut, fin);
    }

    public static double calculerMontantExtras(long nbJours, boolean gps, boolean siegeBebe,
                                               boolean assuranceComp, boolean chauffeur,
                                               boolean carburantPrepaye) {
        double total = 0;
        if (gps)             total += TARIF_GPS             * nbJours;
        if (siegeBebe)       total += TARIF_SIEGE_BEBE      * nbJours;
        if (assuranceComp)   total += TARIF_ASSURANCE_COMP  * nbJours;
        if (chauffeur)       total += TARIF_CHAUFFEUR        * nbJours;
        if (carburantPrepaye)total += TARIF_CARBURANT        * nbJours;
        return total;
    }

    public static double calculerSolde(double total, double avancePayee) {
        return Math.max(0, total - avancePayee);
    }

    public static double calculerPenaliteRetard(int heuresRetard, boolean carburantManquant,
                                                boolean dommagesLegers, boolean dommagesGraves) {
        double penalite = 0;
        if (heuresRetard > 0)   penalite += PENALITE_RETARD_PAR_HEURE * heuresRetard;
        if (carburantManquant)  penalite += PENALITE_CARBURANT_MANQUANT;
        if (dommagesLegers)     penalite += PENALITE_DOMMAGE_LEGER;
        if (dommagesGraves)     penalite += PENALITE_DOMMAGE_GRAVE;
        return penalite;
    }

    // ─────────────────────────────────────────────────────────────
    // GÉNÉRATION DE DOCUMENTS TEXTE
    // ─────────────────────────────────────────────────────────────

    /**
     * Génère le contenu texte du contrat de location
     * CORRECTION : utilise les vrais champs de Location.java
     */
    public static String genererContratTexte(Location location, boolean gps, boolean siegeBebe,
                                             boolean assuranceComp, boolean chauffeur,
                                             boolean carburantPrepaye) {
        if (location == null) return "";

        // ✅ Correction : getDateDebut() retourne Timestamp → toLocalDateTime().toLocalDate()
        LocalDate debut = location.getDateDebut() != null ?
                location.getDateDebut().toLocalDateTime().toLocalDate() : LocalDate.now();
        // ✅ Correction : getDateFinPrev() (pas getDateFin())
        LocalDate fin = location.getDateFinPrev() != null ?
                location.getDateFinPrev().toLocalDateTime().toLocalDate() : LocalDate.now().plusDays(1);

        long   nbJours       = calculerNbJours(debut, fin);
        // ✅ Correction : getPrixParJour() retourne double (pas BigDecimal)
        double prixJour      = location.getPrixParJour();
        double montantBase   = calculerMontantBase(prixJour, debut, fin);
        double montantExtras = calculerMontantExtras(nbJours, gps, siegeBebe, assuranceComp, chauffeur, carburantPrepaye);
        double montantTotal  = montantBase + montantExtras;
        // ✅ Correction : getAvance() (pas getMontantAvance())
        double avance        = location.getAvance();
        double solde         = calculerSolde(montantTotal, avance);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateAujourdhui = LocalDate.now().format(fmt);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("                   HORIZIA - CONTRAT DE LOCATION         \n");
        sb.append("           Votre partenaire de mobilité en Tunisie        \n");
        sb.append("═══════════════════════════════════════════════════════\n\n");

        // ✅ Correction : getIdLocation() (pas getId())
        sb.append("N° Contrat     : HOZ-").append(String.format("%04d", location.getIdLocation())).append("\n");
        sb.append("Date émission  : ").append(dateAujourdhui).append("\n\n");

        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("INFORMATIONS CLIENT\n");
        sb.append("───────────────────────────────────────────────────────\n");
        // ✅ Correction : getClientNomComplet() (pas getNomClient())
        sb.append("Nom            : ").append(location.getClientNomComplet() != null ? location.getClientNomComplet() : "—").append("\n");
        // ✅ Correction : getClientTelephone() (pas getTelClient())
        sb.append("Téléphone      : ").append(location.getClientTelephone() != null ? location.getClientTelephone() : "—").append("\n");
        // ✅ Correction : getClientCin() (pas getCinClient())
        sb.append("CIN            : ").append(location.getClientCin() != null ? location.getClientCin() : "—").append("\n\n");

        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("INFORMATIONS VÉHICULE\n");
        sb.append("───────────────────────────────────────────────────────\n");
        // ✅ Correction : getIdVehicule() — pas de getVehicule() dans Location
        sb.append("Véhicule ID    : ").append(location.getIdVehicule()).append("\n\n");

        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("PÉRIODE DE LOCATION\n");
        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("Date début     : ").append(debut.format(fmt)).append("\n");
        sb.append("Date fin prév. : ").append(fin.format(fmt)).append("\n");
        sb.append("Durée          : ").append(nbJours).append(" jour(s)\n\n");

        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("DÉTAIL FINANCIER\n");
        sb.append("───────────────────────────────────────────────────────\n");
        sb.append(String.format("Location       : %.3f TND x %d jours = %.3f TND%n", prixJour, nbJours, montantBase));

        if (gps)             sb.append(String.format("  + GPS           : %.3f x %d = %.3f TND%n", TARIF_GPS, nbJours, TARIF_GPS * nbJours));
        if (siegeBebe)       sb.append(String.format("  + Siège bébé    : %.3f x %d = %.3f TND%n", TARIF_SIEGE_BEBE, nbJours, TARIF_SIEGE_BEBE * nbJours));
        if (assuranceComp)   sb.append(String.format("  + Assurance     : %.3f x %d = %.3f TND%n", TARIF_ASSURANCE_COMP, nbJours, TARIF_ASSURANCE_COMP * nbJours));
        if (chauffeur)       sb.append(String.format("  + Chauffeur     : %.3f x %d = %.3f TND%n", TARIF_CHAUFFEUR, nbJours, TARIF_CHAUFFEUR * nbJours));
        if (carburantPrepaye)sb.append(String.format("  + Carburant     : %.3f x %d = %.3f TND%n", TARIF_CARBURANT, nbJours, TARIF_CARBURANT * nbJours));

        sb.append("─────────────────────────────────────\n");
        sb.append(String.format("TOTAL          : %.3f TND%n", montantTotal));
        sb.append(String.format("Avance payée   : %.3f TND%n", avance));
        sb.append(String.format("SOLDE RESTANT  : %.3f TND%n%n", solde));

        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("CONDITIONS GÉNÉRALES\n");
        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("• Le véhicule doit être restitué dans l'état de sa prise en charge.\n");
        sb.append("• Tout retard non signalé sera facturé 5 TND/heure.\n");
        sb.append("• Le client est responsable des infractions et amendes.\n");
        sb.append("• Le carburant est à la charge du client (sauf option prépayée).\n");
        sb.append("• Tout dommage sera facturé selon le barème de l'agence.\n\n");

        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("SIGNATURES\n");
        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("Client : ________________________   Agent : ________________________\n\n");

        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("  ").append(NOM_AGENCE).append(" | ").append(TEL_AGENCE)
                .append(" | ").append(EMAIL_AGENCE).append("\n");
        sb.append("  ").append(ADRESSE_AGENCE).append(" | ").append(MF_AGENCE).append("\n");
        sb.append("═══════════════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Génère le contenu texte de la facture finale (après retour)
     */
    public static String genererFactureTexte(Location location, double montantTotal,
                                             double avancePayee, double penalites,
                                             String notesRetour) {
        if (location == null) return "";

        double solde = calculerSolde(montantTotal + penalites, avancePayee);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateRetour = LocalDate.now().format(fmt);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("                   HORIZIA - FACTURE FINALE              \n");
        sb.append("═══════════════════════════════════════════════════════\n\n");

        sb.append("N° Facture     : FAC-").append(String.format("%04d", location.getIdLocation())).append("\n");
        sb.append("Date retour    : ").append(dateRetour).append("\n\n");

        sb.append("CLIENT         : ").append(location.getClientNomComplet() != null ? location.getClientNomComplet() : "—").append("\n");
        sb.append("VÉHICULE ID    : ").append(location.getIdVehicule()).append("\n\n");

        sb.append("───────────────────────────────────────────────────────\n");
        sb.append("RÉCAPITULATIF FINANCIER\n");
        sb.append("───────────────────────────────────────────────────────\n");
        sb.append(String.format("Montant location  : %.3f TND%n", montantTotal));
        if (penalites > 0)
            sb.append(String.format("Pénalités         : %.3f TND%n", penalites));
        sb.append(String.format("TOTAL GÉNÉRAL     : %.3f TND%n", montantTotal + penalites));
        sb.append(String.format("Avance payée      : %.3f TND%n", avancePayee));
        sb.append("─────────────────────────────────────\n");
        sb.append(String.format("MONTANT À PAYER   : %.3f TND%n%n", solde));

        if (notesRetour != null && !notesRetour.isBlank()) {
            sb.append("NOTES DE RETOUR   : ").append(notesRetour).append("\n\n");
        }

        sb.append("Merci de votre confiance ! À bientôt chez Horizia.\n\n");
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("  ").append(TEL_AGENCE).append(" | ").append(EMAIL_AGENCE).append("\n");
        sb.append("═══════════════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Sauvegarde le contrat dans un fichier texte
     */
    public static String sauvegarderContrat(String contenu, int locationId, String dossier) {
        File dir = new File(dossier);
        if (!dir.exists()) dir.mkdirs();

        String nomFichier = "Contrat_HOZ-" + String.format("%04d", locationId) + ".txt";
        File fichier = new File(dossier + File.separator + nomFichier);

        try (FileWriter writer = new FileWriter(fichier)) {
            writer.write(contenu);
            System.out.println("[ContratService] Contrat sauvegardé : " + fichier.getAbsolutePath());
            return fichier.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("[ContratService] Erreur sauvegarde : " + e.getMessage());
            return null;
        }
    }

    public static String formaterMontant(double montant) {
        return String.format("%.3f TND", montant);
    }
}