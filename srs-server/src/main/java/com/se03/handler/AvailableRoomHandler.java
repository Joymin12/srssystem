package com.se03.handler;

import com.se03.controller.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public final class AvailableRoomHandler implements HttpHandler {
    private final AvailableRoomController controller;
    public AvailableRoomHandler(AvailableRoomController controller) { this.controller = controller; }
    @Override public void handle(HttpExchange exchange) throws IOException { controller.searchAvailableRooms(exchange); }
}
