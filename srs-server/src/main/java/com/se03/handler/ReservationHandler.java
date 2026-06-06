package com.se03.handler;

import com.se03.controller.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public final class ReservationHandler implements HttpHandler {
    private final ReservationController controller;
    public ReservationHandler(ReservationController controller) { this.controller = controller; }
    @Override public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if ("/reservations".equals(path) && "GET".equalsIgnoreCase(method)) controller.listReservations(exchange);
        else if ("/reservations".equals(path) && "POST".equalsIgnoreCase(method)) controller.requestReservation(exchange);
        else if (path.matches("/reservations/[^/]+") && "GET".equalsIgnoreCase(method)) controller.getReservation(exchange);
        else controller.requestReservation(exchange);
    }
}
