package com.se03.model;

import com.se03.model.UserRole;

import java.time.LocalDate;

public final class Reservation {
    private String reservationId;
    private String requesterId;
    private UserRole requesterRole;
    private String buildingId;
    private String roomId;
    private LocalDate date;
    private String dayOfWeek;
    private int startPeriod;
    private int endPeriod;
    private String purpose;
    private int participantCount;
    private String companions;
    private ReservationStatus status;
    private String reason;

    public String reservationId() { return reservationId; }
    public String requesterId() { return requesterId; }
    public UserRole requesterRole() { return requesterRole; }
    public String buildingId() { return buildingId; }
    public String roomId() { return roomId; }
    public LocalDate date() { return date; }
    public String dayOfWeek() { return dayOfWeek; }
    public int startPeriod() { return startPeriod; }
    public int endPeriod() { return endPeriod; }
    public String purpose() { return purpose; }
    public int participantCount() { return participantCount; }
    public String companions() { return companions; }
    public ReservationStatus status() { return status; }
    public String reason() { return reason == null ? "" : reason; }

    public boolean isActive() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.APPROVED;
    }

    public boolean isOwnedBy(String userId) {
        return requesterId.equals(userId);
    }

    public boolean isProfessorReservation() {
        return requesterRole == UserRole.PROFESSOR;
    }

    public boolean conflictsWith(String roomId, LocalDate date, int startPeriod, int endPeriod) {
        return isActive() && this.roomId.equals(roomId) && this.date.equals(date) && this.startPeriod <= endPeriod && startPeriod <= this.endPeriod;
    }

    public void approve() {
        status = ReservationStatus.APPROVED;
        reason = "";
    }

    public void reject(String reason) {
        status = ReservationStatus.REJECTED;
        this.reason = reason == null ? "" : reason;
    }

    public void cancel(String reason) {
        status = ReservationStatus.CANCELED;
        this.reason = reason == null ? "" : reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Reservation r = new Reservation();

        public Builder reservationId(String value) { r.reservationId = value; return this; }
        public Builder requesterId(String value) { r.requesterId = value; return this; }
        public Builder requesterRole(UserRole value) { r.requesterRole = value; return this; }
        public Builder buildingId(String value) { r.buildingId = value; return this; }
        public Builder roomId(String value) { r.roomId = value; return this; }
        public Builder date(LocalDate value) { r.date = value; return this; }
        public Builder dayOfWeek(String value) { r.dayOfWeek = value; return this; }
        public Builder startPeriod(int value) { r.startPeriod = value; return this; }
        public Builder endPeriod(int value) { r.endPeriod = value; return this; }
        public Builder purpose(String value) { r.purpose = value; return this; }
        public Builder participantCount(int value) { r.participantCount = value; return this; }
        public Builder companions(String value) { r.companions = value == null ? "" : value; return this; }
        public Builder status(ReservationStatus value) { r.status = value; return this; }
        public Builder reason(String value) { r.reason = value == null ? "" : value; return this; }
        public Reservation build() { return r; }
    }
}
