package com.se03.handler;

import com.se03.controller.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public final class NotificationHandler implements HttpHandler {
    private final NotificationController controller;

    public NotificationHandler(NotificationController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        controller.handle(exchange);
    }
}
