package com.se03.controller;

import com.se03.support.HttpSupport;

import com.se03.service.RoomStatusService;
import com.se03.model.ViewType;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public final class RoomStatusController {
    private final RoomStatusService roomStatusService;
    public RoomStatusController(RoomStatusService roomStatusService) { this.roomStatusService = roomStatusService; }

    public void showRoomStatus(HttpExchange exchange) throws IOException {
        Map<String, String> p = HttpSupport.params(exchange);
        String roomId = HttpSupport.pathId(exchange, 2);
        // 보고서의 API 경로는 유지하되, 실제 일/주/月 필터링을 위해 date query를 추가로 읽는다.
        HttpSupport.respond(exchange, roomStatusService.getRoomStatus(p.get("buildingId"), roomId, HttpSupport.dateValue(p, "date"), ViewType.from(p.get("viewType"))).toWire());
    }
}
