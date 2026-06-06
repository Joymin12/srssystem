package com.se03.repository;

import com.se03.model.Notification;
import com.se03.repository.NotificationRepository;

import java.util.List;

public final class FileNotificationRepository implements NotificationRepository {
    private final FileDatabase db;
    public FileNotificationRepository(FileDatabase db) { this.db = db; }
    @Override public void save(Notification notification) {
        db.notifications.add(notification);
        db.save();
    }
    @Override public List<Notification> findByUser(String userId) {
        return db.notifications.stream().filter(n -> n.userId().equals(userId)).toList();
    }
    @Override public void markRead(String notificationId) {
        for (int i = 0; i < db.notifications.size(); i++) {
            Notification n = db.notifications.get(i);
            if (n.notificationId().equals(notificationId)) {
                db.notifications.set(i, new Notification(n.notificationId(), n.userId(), n.message(), true));
                db.save();
                return;
            }
        }
    }
}
