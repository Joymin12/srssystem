package com.se03.handler;

import com.se03.controller.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public final class RoomStatusHandler implements HttpHandler {
    private final RoomStatusController controller;
    public RoomStatusHandler(RoomStatusController controller) { this.controller = controller; }
    @Override public void handle(HttpExchange exchange) throws IOException { controller.showRoomStatus(exchange); }
}
