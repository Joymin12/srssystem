package com.se03.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class NotificationApi {
    private final ApiClient apiClient;

    public NotificationApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiResponse getNotifications(String userId) throws Exception {
        return apiClient.get("/notifications?userId=" + URLEncoder.encode(userId, StandardCharsets.UTF_8));
    }

    public ApiResponse markRead(String notificationId) throws Exception {
        return apiClient.post("/notifications/" + notificationId + "/read", Map.of());
    }
}
