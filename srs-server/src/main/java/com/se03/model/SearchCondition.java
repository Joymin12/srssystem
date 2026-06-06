package com.se03.model;

import java.time.LocalDate;

public record SearchCondition(String buildingId, String roomId, LocalDate date, String dayOfWeek, int startPeriod, int endPeriod) {
    public boolean hasBuilding() {
        return buildingId != null && !buildingId.isBlank();
    }

    public boolean hasDate() {
        return date != null;
    }

    public boolean hasValidPeriod() {
        return startPeriod > 0 && endPeriod >= startPeriod;
    }

    public boolean isValidForSearch() {
        return hasBuilding() && hasDate() && hasValidPeriod();
    }

    public String normalizedDayOfWeek() {
        if (dayOfWeek != null && !dayOfWeek.isBlank()) return dayOfWeek.trim();
        if (date == null) return "";
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    public SearchCondition normalized() {
        return new SearchCondition(trim(buildingId), trim(roomId), date, normalizedDayOfWeek(), startPeriod, endPeriod);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
