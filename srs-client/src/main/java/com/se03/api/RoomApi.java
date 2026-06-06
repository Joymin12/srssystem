package com.se03.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class RoomApi {
    private final ApiClient apiClient;
    public RoomApi(ApiClient apiClient) { this.apiClient = apiClient; }

    public ApiResponse showRoomStatus(Map<String, String> request) throws Exception {
        // 다이어그램의 showRoomStatus 흐름은 유지하고, 요구사항의 일별/월별 조회를
        // 정확히 처리하기 위해 기준일(date)만 query parameter로 확장했다.
        return apiClient.get("/rooms/" + enc(request.get("roomId")) + "/status?buildingId=" + enc(request.get("buildingId"))
                + "&date=" + enc(request.getOrDefault("date", ""))
                + "&viewType=" + enc(request.getOrDefault("viewType", "weekly")));
    }

    public ApiResponse getClassrooms() throws Exception {
        return apiClient.get("/admin/classrooms");
    }

    public ApiResponse searchAvailableRooms(Map<String, String> request) throws Exception {
        return apiClient.get("/rooms/available?buildingId=" + enc(request.get("buildingId")) + "&date=" + enc(request.get("date"))
                + "&dayOfWeek=" + enc(request.get("dayOfWeek")) + "&startPeriod=" + enc(request.get("startPeriod"))
                + "&endPeriod=" + enc(request.get("endPeriod")));
    }

    private String enc(String value) { return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8); }
}
