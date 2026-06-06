package com.se03.api;

import java.util.Map;

public final class ApprovalApi {
    private final ApiClient apiClient;
    public ApprovalApi(ApiClient apiClient) { this.apiClient = apiClient; }
    public ApiResponse getPendingReservations() throws Exception { return apiClient.get("/reservations/pending"); }
    public ApiResponse approveReservation(Map<String, String> request) throws Exception { return apiClient.post("/reservations/" + request.get("reservationId") + "/approve", request); }
    public ApiResponse rejectReservation(Map<String, String> request) throws Exception { return apiClient.post("/reservations/" + request.get("reservationId") + "/reject", request); }
}
