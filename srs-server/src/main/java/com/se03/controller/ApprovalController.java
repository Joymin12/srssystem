package com.se03.controller;

import com.se03.support.HttpSupport;

import com.se03.service.ReservationService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public final class ApprovalController {
    private final ReservationService reservationService;
    public ApprovalController(ReservationService reservationService) { this.reservationService = reservationService; }

    public void getPendingReservations(HttpExchange exchange) throws IOException {
        StringBuilder out = new StringBuilder("OK\n");
        reservationService.getPendingReservations().forEach(r -> out.append("RESERVATION\t").append(r.reservationId()).append('\t').append(r.requesterId()).append('\t')
                .append(r.requesterRole()).append('\t').append(r.buildingId()).append('\t').append(r.roomId()).append('\t').append(r.date()).append('\t')
                .append(r.dayOfWeek()).append('\t').append(r.startPeriod()).append('\t').append(r.endPeriod()).append('\t').append(r.purpose()).append('\t')
                .append(r.participantCount()).append('\t').append(r.status()).append('\t').append(r.reason()).append('\n'));
        HttpSupport.respond(exchange, out.toString());
    }

    public void approveReservation(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, reservationService.approveReservation(p.get("assistantId"), HttpSupport.pathId(exchange, 2)).toWire());
    }

    public void rejectReservation(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, reservationService.rejectReservation(p.get("assistantId"), HttpSupport.pathId(exchange, 2), p.get("reason")).toWire());
    }
}
