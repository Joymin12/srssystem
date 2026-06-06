package com.se03.repository;

import com.se03.model.Reservation;
import com.se03.repository.ReservationRepository;
import com.se03.model.ReservationStatus;

import java.util.List;

public final class FileReservationRepository implements ReservationRepository {
    private final FileDatabase db;
    public FileReservationRepository(FileDatabase db) { this.db = db; }
    @Override public Reservation findById(String reservationId) {
        return db.reservations.stream().filter(r -> r.reservationId().equals(reservationId)).findFirst().orElse(null);
    }
    @Override public List<Reservation> findAll() { return db.reservations; }
    @Override public List<Reservation> findByUser(String userId) {
        return db.reservations.stream().filter(r -> r.requesterId().equals(userId)).toList();
    }
    @Override public List<Reservation> findPending() {
        return db.reservations.stream().filter(r -> r.status() == ReservationStatus.PENDING).toList();
    }
    @Override public void save(Reservation reservation) {
        db.reservations.removeIf(r -> r.reservationId().equals(reservation.reservationId()));
        db.reservations.add(reservation);
        db.save();
    }
    @Override public void saveAll(List<Reservation> reservations) {
        reservations.forEach(r -> db.reservations.removeIf(old -> old.reservationId().equals(r.reservationId())));
        db.reservations.addAll(reservations);
        db.save();
    }
}
