package com.skillbarter.service;

import com.skillbarter.dao.NotificationDAO;
import com.skillbarter.model.Notification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * CONCEPT 7: Multithreading — polls AWS RDS for new notifications
 * every 15 seconds on a background thread.
 * UI registers a callback; this service calls it when new items arrive.
 */
@Service
public class NotificationService {

    private final NotificationDAO notificationDAO;
    private ScheduledExecutorService scheduler;
    private Consumer<List<Notification>> onNewNotification;
    private int watchedUserId = -1;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Start background polling for a user.
     * @param userId   the logged-in user's id
     * @param callback UI callback invoked on the Event Dispatch Thread
     */
    public void startPolling(int userId, Consumer<List<Notification>> callback) {
        this.watchedUserId     = userId;
        this.onNewNotification = callback;

        // CONCEPT 7: ScheduledExecutorService — background thread
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NotificationPoller");
            t.setDaemon(true);   // dies when main window closes
            return t;
        });

        scheduler.scheduleAtFixedRate(this::poll, 0, 15, TimeUnit.SECONDS);
        System.out.println("[Thread] NotificationPoller started for user " + userId);
    }

    private void poll() {
        try {
            List<Notification> unread = notificationDAO.findUnreadByUser(watchedUserId);
            if (!unread.isEmpty() && onNewNotification != null) {
                // Hand result back to Swing EDT safely
                javax.swing.SwingUtilities.invokeLater(() -> onNewNotification.accept(unread));
                notificationDAO.markAllRead(watchedUserId);
            }
        } catch (Exception e) {
            System.err.println("[Thread] Polling error: " + e.getMessage());
        }
    }

    public void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("[Thread] NotificationPoller stopped.");
        }
    }
}
