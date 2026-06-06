package com.se03.handler;

import com.se03.controller.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public final class ManagementHandler implements HttpHandler {
    private final ManagementController controller;

    public ManagementHandler(ManagementController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        controller.handle(exchange);
    }
}
