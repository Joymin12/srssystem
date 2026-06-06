package com.se03.controller;

import com.se03.support.HttpSupport;

import com.se03.service.ClassroomManagementService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public final class ManagementController {
    private final ClassroomManagementService managementService;

    public ManagementController(ClassroomManagementService managementService) {
        this.managementService = managementService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if ("/admin/classrooms".equals(path) && "GET".equalsIgnoreCase(method)) listClassrooms(exchange);
        else if ("/admin/classrooms".equals(path) && "POST".equalsIgnoreCase(method)) saveClassroom(exchange);
        else if (path.matches("/admin/classrooms/[^/]+") && "DELETE".equalsIgnoreCase(method)) deleteClassroom(exchange);
        else if ("/admin/schedules".equals(path) && "GET".equalsIgnoreCase(method)) listSchedules(exchange);
        else if ("/admin/schedules".equals(path) && "POST".equalsIgnoreCase(method)) saveSchedule(exchange);
        else if (path.matches("/admin/schedules/[^/]+") && "DELETE".equalsIgnoreCase(method)) deleteSchedule(exchange);
        else HttpSupport.respond(exchange, "ERROR\nmessage=지원하지 않는 관리 요청입니다.\n");
    }

    private void listClassrooms(HttpExchange exchange) throws IOException {
        StringBuilder out = new StringBuilder("OK\n");
        managementService.getClassrooms().forEach(c -> out.append("CLASSROOM\t").append(c.buildingId()).append('\t').append(c.roomId()).append('\t').append(c.capacity()).append('\n'));
        HttpSupport.respond(exchange, out.toString());
    }

    private void saveClassroom(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, managementService.saveClassroom(p.get("assistantId"), p.get("buildingId"), p.get("roomId"), HttpSupport.intValue(p, "capacity")));
    }

    private void deleteClassroom(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, managementService.deleteClassroom(p.get("assistantId"), HttpSupport.pathId(exchange, 3)));
    }

    private void listSchedules(HttpExchange exchange) throws IOException {
        StringBuilder out = new StringBuilder("OK\n");
        managementService.getLectureSchedules().forEach(s -> out.append("LECTURE\t").append(s.scheduleId()).append('\t').append(s.buildingId()).append('\t')
                .append(s.roomId()).append('\t').append(s.dayOfWeek()).append('\t').append(s.startPeriod()).append('\t').append(s.endPeriod()).append('\t')
                .append(s.title()).append('\t').append(s.professor()).append('\n'));
        HttpSupport.respond(exchange, out.toString());
    }

    private void saveSchedule(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, managementService.saveLectureSchedule(p.get("assistantId"), p.get("scheduleId"), p.get("buildingId"), p.get("roomId"),
                p.get("dayOfWeek"), HttpSupport.intValue(p, "startPeriod"), HttpSupport.intValue(p, "endPeriod"), p.get("title"), p.get("professor")));
    }

    private void deleteSchedule(HttpExchange exchange) throws IOException {
        var p = HttpSupport.params(exchange);
        HttpSupport.respond(exchange, managementService.deleteLectureSchedule(p.get("assistantId"), HttpSupport.pathId(exchange, 3)));
    }
}
