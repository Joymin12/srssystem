package com.se03.service;

import com.se03.model.Classroom;
import com.se03.model.Reservation;
import com.se03.model.ReservationStatus;
import com.se03.model.SearchCondition;
import com.se03.model.UserRole;
import com.se03.repository.FileClassroomRepository;
import com.se03.repository.FileDatabase;
import com.se03.repository.FileLectureScheduleRepository;
import com.se03.repository.FileNotificationRepository;
import com.se03.repository.FileReservationRepository;
import com.se03.repository.FileUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvailableRoomServiceTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("TC-UC03-01 빈 강의실 조회 성공")
    void findsAvailableRoomsWhenNoLectureOrReservationConflicts() throws Exception {
        ReservationService service = service();

        List<Classroom> rooms = service.findAvailableRooms(condition("목", 7, 8));

        assertFalse(rooms.isEmpty());
    }

    @Test
    @DisplayName("TC-UC03-02 강의 시간 충돌 강의실 제외")
    void excludesRoomWithLectureConflict() throws Exception {
        ReservationService service = service();

        List<Classroom> rooms = service.findAvailableRooms(condition("월", 1, 2));

        assertTrue(rooms.stream().noneMatch(room -> room.roomId().equals("912")));
    }

    @Test
    @DisplayName("TC-UC03-03 승인 예약 충돌 강의실 제외")
    void excludesRoomWithApprovedReservationConflict() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-APPROVED", ReservationStatus.APPROVED));

        List<Classroom> rooms = fixture.service.findAvailableRooms(condition("목", 7, 8));

        assertTrue(rooms.stream().noneMatch(room -> room.roomId().equals("912")));
    }

    @Test
    @DisplayName("TC-UC03-04 시작 교시 오류")
    void invalidPeriodReturnsNoAvailableRooms() throws Exception {
        ReservationService service = service();

        List<Classroom> rooms = service.findAvailableRooms(condition("목", 8, 7));

        assertTrue(rooms.isEmpty());
    }

    @Test
    @DisplayName("TC-UC03-05 날짜 기준 요일 자동 보정")
    void blankDayOfWeekUsesDateValue() throws Exception {
        ReservationService service = service();

        List<Classroom> rooms = service.findAvailableRooms(condition("", 7, 8));

        assertFalse(rooms.isEmpty());
    }

    @Test
    @DisplayName("TC-UC03-06 필수 조건 누락 시 빈 결과 반환")
    void invalidSearchConditionReturnsEmptyResult() throws Exception {
        ReservationService service = service();

        List<Classroom> rooms = service.findAvailableRooms(new SearchCondition("", "", LocalDate.now().plusDays(2), "목", 7, 8));

        assertTrue(rooms.isEmpty());
    }

    private ReservationService service() throws Exception {
        return fixture().service;
    }

    private Fixture fixture() throws Exception {
        FileDatabase db = new FileDatabase(tempDir.resolve("store.tsv"));
        FileClassroomRepository classrooms = new FileClassroomRepository(db);
        FileLectureScheduleRepository schedules = new FileLectureScheduleRepository(db);
        FileReservationRepository reservations = new FileReservationRepository(db);
        NotificationService notifications = new NotificationService(new FileNotificationRepository(db));
        ReservationService service = new ReservationService(classrooms, schedules, reservations, notifications, new FileUserRepository(db));
        return new Fixture(service, reservations);
    }

    private SearchCondition condition(String dayOfWeek, int startPeriod, int endPeriod) {
        return new SearchCondition("정보공학관", "", LocalDate.now().plusDays(2), dayOfWeek, startPeriod, endPeriod);
    }

    private Reservation reservation(String id, ReservationStatus status) {
        return Reservation.builder()
                .reservationId(id)
                .requesterId("student1")
                .requesterRole(UserRole.STUDENT)
                .buildingId("정보공학관")
                .roomId("912")
                .date(LocalDate.now().plusDays(2))
                .dayOfWeek("목")
                .startPeriod(7)
                .endPeriod(8)
                .purpose("개인 학습")
                .participantCount(2)
                .companions("")
                .status(status)
                .reason("")
                .build();
    }

    private record Fixture(ReservationService service, FileReservationRepository reservations) { }
}
