package com.se03.support;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class HttpSupport {
    private HttpSupport() {}

    public static Map<String, String> params(HttpExchange exchange) throws IOException {
        String raw = exchange.getRequestURI().getRawQuery();
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) || "DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String body = reader.readLine();
                if (body != null && !body.isBlank()) raw = raw == null ? body : raw + "&" + body;
            }
        }
        Map<String, String> params = new HashMap<>();
        if (raw == null || raw.isBlank()) return params;
        for (String part : raw.split("&")) {
            int idx = part.indexOf('=');
            String key = idx >= 0 ? part.substring(0, idx) : part;
            String value = idx >= 0 ? part.substring(idx + 1) : "";
            params.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return params;
    }

    public static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static int intValue(Map<String, String> p, String key) {
        try { return Integer.parseInt(p.getOrDefault(key, "0")); }
        catch (NumberFormatException e) { return 0; }
    }

    public static LocalDate dateValue(Map<String, String> p, String key) {
        try { return LocalDate.parse(p.get(key)); }
        catch (Exception e) { return null; }
    }

    public static String pathId(HttpExchange exchange, int index) {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        return parts.length > index ? parts[index] : "";
    }
}
