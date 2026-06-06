package com.se03.model;

import java.time.LocalDate;

public record SearchCondition(String buildingId, String roomId, LocalDate date, String dayOfWeek, int startPeriod, int endPeriod) {
}
