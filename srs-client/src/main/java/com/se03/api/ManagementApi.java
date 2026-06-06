package com.se03.api;

import java.util.Map;

public final class ManagementApi {
    private final ApiClient apiClient;

    public ManagementApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiResponse getClassrooms() throws Exception { return apiClient.get("/admin/classrooms"); }
    public ApiResponse saveClassroom(Map<String, String> body) throws Exception { return apiClient.post("/admin/classrooms", body); }
    public ApiResponse deleteClassroom(Map<String, String> body) throws Exception { return apiClient.delete("/admin/classrooms/" + body.get("roomId"), body); }
    public ApiResponse getSchedules() throws Exception { return apiClient.get("/admin/schedules"); }
    public ApiResponse saveSchedule(Map<String, String> body) throws Exception { return apiClient.post("/admin/schedules", body); }
    public ApiResponse deleteSchedule(Map<String, String> body) throws Exception { return apiClient.delete("/admin/schedules/" + body.get("scheduleId"), body); }
}
