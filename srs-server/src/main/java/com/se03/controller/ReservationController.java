package com.se03.controller;

import com.se03.support.HttpSupport;

import com.se03.model.ReservationRequest;
import com.se03.service.ReservationService;
import com.se03.model.UserRole;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public final class ReservationController {
    private final ReservationService reservationService;
    public ReservationController(ReservationService reservationService) { this.reservationService = reservationService; }

    public void listReservations(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        StringBuilder out = new StringBuilder("OK\n");
        reservationService.getReservationsByUser(p.get("userId")).forEach(r -> appendReservation(out, r));
        HttpSupport.respond(exchange, out.toString());
    }

    public void getReservation(HttpExchange exchange) throws IOException {
        var reservation = reservationService.getReservation(HttpSupport.pathId(exchange, 2));
        if (reservation == null) {
            HttpSupport.respond(exchange, "ERROR\nmessage=예약 정보를 찾을 수 없습니다.\n");
            return;
        }
        StringBuilder out = new StringBuilder("OK\n");
        appendReservation(out, reservation);
        HttpSupport.respond(exchange, out.toString());
    }

    public void requestReservation(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        ReservationRequest request = new ReservationRequest(
                p.get("requesterId"), UserRole.valueOf(p.getOrDefault("requesterRole", "STUDENT")),
                p.get("buildingId"), p.get("roomId"), HttpSupport.dateValue(p, "date"), p.get("dayOfWeek"),
                HttpSupport.intValue(p, "startPeriod"), HttpSupport.intValue(p, "endPeriod"),
                p.get("purpose"), HttpSupport.intValue(p, "participantCount"), p.getOrDefault("companions", ""), p.getOrDefault("reason", "")
        );
        HttpSupport.respond(exchange, reservationService.requestReservation(request).toWire());
    }

    public void cancelReservation(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, reservationService.cancelReservation(p.get("requesterId"), HttpSupport.pathId(exchange, 2), p.getOrDefault("reason", "사용자 요청")).toWire());
    }

    public void forceCancelReservation(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, reservationService.forceCancelReservation(p.get("assistantId"), HttpSupport.pathId(exchange, 2), p.getOrDefault("reason", "조교 강제 취소")).toWire());
    }

    private void appendReservation(StringBuilder out, com.se03.model.Reservation r) {
        out.append("RESERVATION\t").append(r.reservationId()).append('\t').append(r.requesterId()).append('\t')
                .append(r.requesterRole()).append('\t').append(r.buildingId()).append('\t').append(r.roomId()).append('\t').append(r.date()).append('\t')
                .append(r.dayOfWeek()).append('\t').append(r.startPeriod()).append('\t').append(r.endPeriod()).append('\t').append(r.purpose()).append('\t')
                .append(r.participantCount()).append('\t').append(r.companions()).append('\t').append(r.status()).append('\t').append(r.reason()).append('\n');
    }
}
