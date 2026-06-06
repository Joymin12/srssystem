package com.se03.model;

import com.se03.model.Reservation;

import java.util.List;

public record ScheduleViewResult(boolean success, String message, Classroom classroom, List<LectureSchedule> schedules, List<Reservation> reservations, ViewType viewType) {
    public static ScheduleViewResult fail(String message) {
        return new ScheduleViewResult(false, message, null, List.of(), List.of(), ViewType.WEEKLY);
    }

    public static ScheduleViewResult of(Classroom classroom, List<LectureSchedule> schedules, List<Reservation> reservations, ViewType viewType) {
        return new ScheduleViewResult(true, "조회 성공", classroom, schedules, reservations, viewType);
    }

    public String toWire() {
        StringBuilder out = new StringBuilder(success ? "OK\n" : "ERROR\n");
        out.append("message=").append(message).append('\n');
        if (classroom != null) {
            out.append("CLASSROOM\t").append(classroom.buildingId()).append('\t').append(classroom.roomId()).append('\t').append(classroom.capacity()).append('\n');
        }
        schedules.forEach(s -> out.append("LECTURE\t").append(s.scheduleId()).append('\t').append(s.buildingId()).append('\t').append(s.roomId()).append('\t')
                .append(s.dayOfWeek()).append('\t').append(s.startPeriod()).append('\t').append(s.endPeriod()).append('\t').append(s.title()).append('\t').append(s.professor()).append('\n'));
        reservations.forEach(r -> out.append("RESERVATION\t").append(r.reservationId()).append('\t').append(r.requesterId()).append('\t').append(r.requesterRole()).append('\t')
                .append(r.buildingId()).append('\t').append(r.roomId()).append('\t').append(r.date()).append('\t').append(r.dayOfWeek()).append('\t')
                .append(r.startPeriod()).append('\t').append(r.endPeriod()).append('\t').append(r.purpose()).append('\t').append(r.participantCount()).append('\t')
                .append(r.status()).append('\t').append(r.reason()).append('\n'));
        return out.toString();
    }
}
