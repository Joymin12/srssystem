package com.se03.model;

public record LectureSchedule(String scheduleId, String buildingId, String roomId, String dayOfWeek, int startPeriod, int endPeriod, String title, String professor) {
    public boolean conflictsWith(String dayOfWeek, int startPeriod, int endPeriod) {
        return this.dayOfWeek.equals(dayOfWeek) && this.startPeriod <= endPeriod && startPeriod <= this.endPeriod;
    }
}
