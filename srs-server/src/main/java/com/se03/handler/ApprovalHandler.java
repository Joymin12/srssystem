package com.se03.handler;

import com.se03.controller.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public final class ApprovalHandler implements HttpHandler {
    private final ApprovalController controller;
    public ApprovalHandler(ApprovalController controller) { this.controller = controller; }
    @Override public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/approve")) controller.approveReservation(exchange);
        else if (path.endsWith("/reject")) controller.rejectReservation(exchange);
        else controller.getPendingReservations(exchange);
    }
}
