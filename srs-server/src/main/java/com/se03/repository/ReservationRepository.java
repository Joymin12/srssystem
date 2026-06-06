package com.se03.repository;

import com.se03.model.Reservation;

import java.util.List;

public interface ReservationRepository {
    Reservation findById(String reservationId);
    List<Reservation> findAll();
    List<Reservation> findByUser(String userId);
    List<Reservation> findPending();
    void save(Reservation reservation);
    void saveAll(List<Reservation> reservations);
}
