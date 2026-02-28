package tn.esprit.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private static NotificationService instance;
    private List<Notification> notifications = new ArrayList<>();
    private List<NotificationListener> listeners = new ArrayList<>();
    private List<ToastListener> toastListeners = new ArrayList<>();

    private NotificationService() {}

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void addNotification(String title, String message, NotificationType type, int relatedId, String relatedType) {
        Notification notif = new Notification(
                notifications.size() + 1,
                title,
                message,
                type,
                LocalDateTime.now(),
                false,
                relatedId,
                relatedType
        );
        notifications.add(0, notif);

        // Notifier les listeners pour la liste
        notifyListeners();

        // Notifier pour le toast
        notifyToastListeners(notif);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public int getUnreadCount() {
        return (int) notifications.stream().filter(n -> !n.isRead()).count();
    }

    public void markAsRead(int id) {
        notifications.stream()
                .filter(n -> n.getId() == id)
                .findFirst()
                .ifPresent(n -> n.setRead(true));
        notifyListeners();
    }

    public void markAllAsRead() {
        notifications.forEach(n -> n.setRead(true));
        notifyListeners();
    }

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void addToastListener(ToastListener listener) {
        toastListeners.add(listener);
    }

    private void notifyListeners() {
        for (NotificationListener listener : listeners) {
            listener.onNotificationChanged();
        }
    }

    private void notifyToastListeners(Notification notif) {
        for (ToastListener listener : toastListeners) {
            listener.onNewNotification(notif);
        }
    }

    // Classes internes
    public static class Notification {
        private int id;
        private String title;
        private String message;
        private NotificationType type;
        private LocalDateTime timestamp;
        private boolean read;
        private int relatedId;
        private String relatedType;

        public Notification(int id, String title, String message, NotificationType type,
                            LocalDateTime timestamp, boolean read, int relatedId, String relatedType) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.read = read;
            this.relatedId = relatedId;
            this.relatedType = relatedType;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
        public int getRelatedId() { return relatedId; }
        public String getRelatedType() { return relatedType; }

        public String getFormattedTime() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM");
            return timestamp.format(formatter);
        }
    }

    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }

    public interface NotificationListener {
        void onNotificationChanged();
    }

    public interface ToastListener {
        void onNewNotification(Notification notif);
    }
}