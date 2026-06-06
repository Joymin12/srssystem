package com.se03.service;

import com.se03.repository.ClassroomRepository;
import com.se03.repository.LectureScheduleRepository;
import com.se03.repository.ReservationRepository;
import com.se03.model.Classroom;
import com.se03.model.LectureSchedule;
import com.se03.model.ScheduleViewResult;
import com.se03.model.ViewType;
import com.se03.model.Reservation;
import com.se03.model.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public final class RoomStatusService {
    private final ClassroomRepository classroomRepository;
    private final LectureScheduleRepository lectureScheduleRepository;
    private final ReservationRepository reservationRepository;

    public RoomStatusService(ClassroomRepository classroomRepository, LectureScheduleRepository lectureScheduleRepository, ReservationRepository reservationRepository) {
        this.classroomRepository = classroomRepository;
        this.lectureScheduleRepository = lectureScheduleRepository;
        this.reservationRepository = reservationRepository;
    }

    public ScheduleViewResult getRoomStatus(String buildingId, String roomId, LocalDate date, ViewType viewType) {
        // 초기 다이어그램은 viewType만 넘기지만, SFR-104/109의 일별/월별 조회를
        // 실제로 구분하려면 기준일이 필요하므로 date를 시스템 오퍼레이션에 포함했다.
        Classroom classroom = classroomRepository.findById(roomId);
        if (classroom == null || !classroom.buildingId().equals(buildingId)) {
            return ScheduleViewResult.fail("강의실 정보를 찾을 수 없습니다.");
        }
        LocalDate baseDate = date == null ? LocalDate.now() : date;
        List<LectureSchedule> schedules = lectureScheduleRepository.findByRoom(roomId).stream()
                .filter(s -> viewType != ViewType.DAILY || s.dayOfWeek().equals(koreanDay(baseDate)))
                .toList();
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> r.buildingId().equals(buildingId) && r.roomId().equals(roomId) && r.status() == ReservationStatus.APPROVED)
                .filter(r -> matchesView(r, baseDate, viewType))
                .toList();
        return ScheduleViewResult.of(classroom, schedules, reservations, viewType);
    }

    private boolean matchesView(Reservation reservation, LocalDate date, ViewType viewType) {
        return switch (viewType) {
            case DAILY -> reservation.date().equals(date);
            case MONTHLY -> reservation.date().getYear() == date.getYear() && reservation.date().getMonth() == date.getMonth();
            case WEEKLY -> !reservation.date().isBefore(date.minusDays(6)) && !reservation.date().isAfter(date.plusDays(6));
        };
    }

    private String koreanDay(LocalDate date) {
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
}
