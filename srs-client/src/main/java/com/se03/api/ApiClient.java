package com.se03.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ApiClient {
    private final String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    public ApiClient(String baseUrl) { this.baseUrl = baseUrl; }

    public ApiResponse get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return new ApiResponse(client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body());
    }

    public ApiResponse post(String path, Map<String, String> body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(encode(body), StandardCharsets.UTF_8))
                .build();
        return new ApiResponse(client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body());
    }

    public ApiResponse delete(String path, Map<String, String> body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(encode(body), StandardCharsets.UTF_8))
                .build();
        return new ApiResponse(client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body());
    }

    private String encode(Map<String, String> body) {
        StringBuilder sb = new StringBuilder();
        for (var e : body.entrySet()) {
            if (!sb.isEmpty()) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
