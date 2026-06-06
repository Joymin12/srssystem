package com.se03.model;

public enum ViewType {
    DAILY,
    WEEKLY,
    MONTHLY;

    public static ViewType from(String value) {
        if (value == null || value.isBlank()) return WEEKLY;
        return switch (value.toLowerCase()) {
            case "daily", "day", "일별" -> DAILY;
            case "monthly", "month", "월별" -> MONTHLY;
            default -> WEEKLY;
        };
    }
}
