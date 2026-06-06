package com.se03.model;

import com.se03.model.UserRole;

import java.time.LocalDate;

public record ReservationRequest(String requesterId, UserRole requesterRole, String buildingId, String roomId, LocalDate date, String dayOfWeek,
                                 int startPeriod, int endPeriod, String purpose, int participantCount, String companions, String reason) {
}
