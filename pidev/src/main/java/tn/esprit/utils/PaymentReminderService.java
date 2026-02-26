package tn.esprit.utils;

import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.services.Servicereservationlog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PaymentReminderService {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Map<Integer, ReminderInfo> reminders = new ConcurrentHashMap<>();

    public static void scheduleReminders(int reservationId, Date dateLimite, String userEmail, String userPrenom,
                                         String logementNom, String userNom) {
        LocalDateTime limit = dateLimite.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        long totalSeconds = java.time.Duration.between(now, limit).getSeconds();
        if (totalSeconds <= 0) {
            cancelReservation(reservationId, userEmail, userPrenom, logementNom, userNom);
            return;
        }

        scheduleReminder(reservationId, totalSeconds - 2 * 3600, 2, 0, userEmail, userPrenom, logementNom);
        scheduleReminder(reservationId, totalSeconds - 3600, 1, 0, userEmail, userPrenom, logementNom);
        scheduleReminder(reservationId, totalSeconds - 1800, 0, 30, userEmail, userPrenom, logementNom);

        scheduler.schedule(() -> cancelReservation(reservationId, userEmail, userPrenom, logementNom, userNom),
                totalSeconds, TimeUnit.SECONDS);

        reminders.put(reservationId, new ReminderInfo(userEmail, userPrenom, logementNom, userNom, limit));
    }

    private static void scheduleReminder(int reservationId, long secondsBefore, long heures, long minutes,
                                         String email, String prenom, String logement) {
        if (secondsBefore > 0) {
            scheduler.schedule(() -> {
                try {
                    EmailService.sendReminderEmail(email, prenom, logement, heures, minutes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, secondsBefore, TimeUnit.SECONDS);
        }
    }

    private static void cancelReservation(int reservationId, String userEmail, String userPrenom,
                                          String logementNom, String userNom) {
        Servicereservationlog service = new Servicereservationlog();
        try {
            reservationlog r = service.rechercherParId(reservationId);
            if (r != null && r.getStatus() == Status.en_attente) {
                r.setStatus(Status.annulée);
                service.modifier(r);
                String dates = new java.text.SimpleDateFormat("dd/MM/yyyy").format(r.getDate_debut())
                        + " au " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(r.getDate_fin());
                EmailService.sendCancellationEmail(userEmail, userNom, userPrenom, logementNom, dates, r.getMontant());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        reminders.remove(reservationId);
    }

    /**
     * Retourne le temps restant en secondes pour une réservation, ou null si non trouvée.
     */
    public static Long getTimeLeftSeconds(int reservationId) {
        ReminderInfo info = reminders.get(reservationId);
        if (info == null) return null;
        return java.time.Duration.between(LocalDateTime.now(), info.getLimit()).getSeconds();
    }

    public static class ReminderInfo {
        private final String email;
        private final String prenom;
        private final String logement;
        private final String nom;
        private final LocalDateTime limit;

        public ReminderInfo(String email, String prenom, String logement, String nom, LocalDateTime limit) {
            this.email = email;
            this.prenom = prenom;
            this.logement = logement;
            this.nom = nom;
            this.limit = limit;
        }

        public LocalDateTime getLimit() {
            return limit;
        }
    }
}