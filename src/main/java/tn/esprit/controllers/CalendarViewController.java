package tn.esprit.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.Events;
import tn.esprit.services.ServiceEvent;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.ResourceBundle;

public class CalendarViewController implements Initializable {

    @FXML private StackPane calendarContainer;

    private ServiceEvent serviceEvent;
    private List<Events> allEvents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        loadEvents();
        setupCalendar();
    }

    private void loadEvents() {
        try {
            allEvents = serviceEvent.afficher();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCalendar() {
        // Créer la vue calendrier
        CalendarView calendarView = new CalendarView();

        // Créer un calendrier pour les événements
        Calendar eventsCalendar = new Calendar("Événements");
        eventsCalendar.setStyle(Style.STYLE1); // Style bleu

        // Ajouter chaque événement de la base au calendrier
        if (allEvents != null) {
            for (Events event : allEvents) {
                if (event.getDateDebut() != null) {
                    // Créer une entrée de calendrier
                    Entry<Integer> entry = new Entry<>(event.getTitre());

                    // Convertir Timestamp en LocalDateTime
                    LocalDateTime debut = event.getDateDebut().toLocalDateTime();
                    LocalDateTime fin = event.getDateFin() != null ?
                            event.getDateFin().toLocalDateTime() :
                            debut.plusHours(2);

                    entry.setInterval(debut, fin);

                    if (event.getLocation() != null) {
                        entry.setLocation(event.getLocation());
                    }

                    // Stocker l'ID de l'événement (int)
                    entry.setUserObject(event.getId_event());

                    eventsCalendar.addEntry(entry);
                }
            }
        }

        // Créer une source de calendrier
        CalendarSource calendarSource = new CalendarSource("Mes Événements");
        calendarSource.getCalendars().add(eventsCalendar);

        // Ajouter la source à la vue
        calendarView.getCalendarSources().clear();
        calendarView.getCalendarSources().add(calendarSource);

        // Configurer la vue par défaut
        calendarView.setRequestedTime(LocalTime.now());

        // Thread pour mettre à jour l'heure
        startTimeUpdateThread(calendarView);

        // Ajouter au conteneur
        calendarContainer.getChildren().add(calendarView);
    }

    private void startTimeUpdateThread(CalendarView calendarView) {
        Thread updateTimeThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    calendarView.setToday(LocalDate.now());
                    calendarView.setTime(LocalTime.now());
                });

                try {
                    Thread.sleep(10000); // 10 secondes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();
    }
}