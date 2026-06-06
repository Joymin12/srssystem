package com.se03.handler;

import com.se03.controller.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public final class CancelReservationHandler implements HttpHandler {
    private final ReservationController controller;
    public CancelReservationHandler(ReservationController controller) { this.controller = controller; }
    @Override public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().endsWith("/force-cancel")) controller.forceCancelReservation(exchange);
        else controller.cancelReservation(exchange);
    }
}
