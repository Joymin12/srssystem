package com.se03.controller;

import com.se03.support.HttpSupport;

import com.se03.service.NotificationService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public final class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.matches("/notifications/[^/]+/read")) markRead(exchange);
        else list(exchange);
    }

    private void list(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        StringBuilder out = new StringBuilder("OK\n");
        notificationService.findByUser(p.get("userId")).forEach(n -> out.append("NOTIFICATION\t").append(n.notificationId()).append('\t')
                .append(n.userId()).append('\t').append(n.message()).append('\t').append(n.read()).append('\n'));
        HttpSupport.respond(exchange, out.toString());
    }

    private void markRead(HttpExchange exchange) throws IOException {
        HttpSupport.respond(exchange, notificationService.read(HttpSupport.pathId(exchange, 2)));
    }
}
