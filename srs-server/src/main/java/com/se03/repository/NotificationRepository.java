package com.se03.repository;

import com.se03.model.Notification;

import java.util.List;

public interface NotificationRepository {
    void save(Notification notification);
    List<Notification> findByUser(String userId);
    void markRead(String notificationId);
}
