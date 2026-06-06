package com.se03.controller;

import com.se03.support.HttpSupport;

import com.se03.service.ReservationService;
import com.se03.model.SearchCondition;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public final class AvailableRoomController {
    private final ReservationService reservationService;
    public AvailableRoomController(ReservationService reservationService) { this.reservationService = reservationService; }

    public void searchAvailableRooms(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        SearchCondition condition = new SearchCondition(p.get("buildingId"), "", HttpSupport.dateValue(p, "date"), p.get("dayOfWeek"),
                HttpSupport.intValue(p, "startPeriod"), HttpSupport.intValue(p, "endPeriod")).normalized();
        if (!condition.isValidForSearch()) {
            HttpSupport.respond(exchange, "ERROR\nmessage=조회 조건이 올바르지 않습니다.\n");
            return;
        }
        StringBuilder out = new StringBuilder("OK\n");
        var rooms = reservationService.findAvailableRooms(condition);
        out.append("message=").append(rooms.isEmpty() ? "예약 가능한 강의실이 없습니다." : "예약 가능한 강의실 조회 성공").append('\n');
        rooms.forEach(c -> out.append("CLASSROOM\t").append(c.buildingId()).append('\t').append(c.roomId()).append('\t').append(c.capacity()).append('\n'));
        HttpSupport.respond(exchange, out.toString());
    }
}
