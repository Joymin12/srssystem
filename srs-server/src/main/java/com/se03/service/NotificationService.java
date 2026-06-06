package com.se03.service;

import com.se03.repository.NotificationRepository;
import com.se03.model.Notification;
import com.se03.model.Reservation;

import java.util.List;
import java.util.UUID;

public final class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyApproved(Reservation reservation) {
        save(reservation.requesterId(), "예약 " + reservation.reservationId() + "이 승인되었습니다.");
    }

    public void notifyRejected(Reservation reservation, String reason) {
        save(reservation.requesterId(), "예약 " + reservation.reservationId() + "이 거부되었습니다. 사유: " + reason);
    }

    public void notifyCanceled(Reservation reservation, String reason) {
        save(reservation.requesterId(), "예약 " + reservation.reservationId() + "이 취소되었습니다. 사유: " + reason);
    }

    public List<Notification> findByUser(String userId) {
        return notificationRepository.findByUser(userId);
    }

    public String read(String notificationId) {
        notificationRepository.markRead(notificationId);
        return "OK\nmessage=알림을 읽음 처리했습니다.\n";
    }

    private void save(String userId, String message) {
        notificationRepository.save(new Notification("N-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), userId, message, false));
    }
}
