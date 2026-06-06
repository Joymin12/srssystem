package com.se03.api;

import java.util.Map;

public final class BackupApi {
    private final ApiClient apiClient;

    public BackupApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiResponse backup() throws Exception {
        return apiClient.post("/backup", Map.of());
    }

    public ApiResponse restore() throws Exception {
        return apiClient.post("/restore", Map.of());
    }
}
