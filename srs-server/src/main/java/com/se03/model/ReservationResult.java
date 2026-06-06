package com.se03.model;

public record ReservationResult(boolean success, String reservationId, String message) {
    public static ReservationResult ok(String reservationId, String message) {
        return new ReservationResult(true, reservationId, message);
    }

    public static ReservationResult fail(String message) {
        return new ReservationResult(false, "", message);
    }

    public String toWire() {
        return (success ? "OK" : "ERROR") + "\nmessage=" + message + "\nreservationId=" + reservationId + "\n";
    }
}
