package com.se03.api;

import java.util.Map;

public final class ReservationApi {
    private final ApiClient apiClient;
    public ReservationApi(ApiClient apiClient) { this.apiClient = apiClient; }
    public ApiResponse getReservations(String userId) throws Exception { return apiClient.get("/reservations?userId=" + java.net.URLEncoder.encode(userId, java.nio.charset.StandardCharsets.UTF_8)); }
    public ApiResponse getReservation(String reservationId) throws Exception { return apiClient.get("/reservations/" + reservationId); }
    public ApiResponse requestReservation(Map<String, String> request) throws Exception { return apiClient.post("/reservations", request); }
    public ApiResponse cancelReservation(Map<String, String> request) throws Exception { return apiClient.post("/reservations/" + request.get("reservationId") + "/cancel", request); }
    public ApiResponse forceCancelReservation(Map<String, String> request) throws Exception { return apiClient.post("/reservations/" + request.get("reservationId") + "/force-cancel", request); }
}
