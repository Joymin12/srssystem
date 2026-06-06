package com.se03.service;

import com.se03.model.Reservation;
import com.se03.model.ReservationStatus;
import com.se03.model.ScheduleViewResult;
import com.se03.model.UserRole;
import com.se03.model.ViewType;
import com.se03.repository.FileClassroomRepository;
import com.se03.repository.FileDatabase;
import com.se03.repository.FileLectureScheduleRepository;
import com.se03.repository.FileReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomStatusServiceTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("TC-UC02-01 강의실 현황 조회 성공")
    void roomStatusReturnsSchedulesAndApprovedReservations() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-APPROVED", "student1", ReservationStatus.APPROVED, LocalDate.now()));

        ScheduleViewResult result = fixture.service.getRoomStatus("정보공학관", "912", LocalDate.now(), ViewType.WEEKLY);

        assertTrue(result.success());
        assertEquals("912", result.classroom().roomId());
        assertFalse(result.schedules().isEmpty());
        assertTrue(result.reservations().stream().anyMatch(r -> r.reservationId().equals("R-APPROVED")));
    }

    @Test
    @DisplayName("TC-UC02-02 존재하지 않는 강의실 조회 실패")
    void unknownRoomReturnsFailure() throws Exception {
        var fixture = fixture();

        ScheduleViewResult result = fixture.service.getRoomStatus("정보공학관", "UNKNOWN", LocalDate.now(), ViewType.WEEKLY);

        assertFalse(result.success());
        assertEquals("강의실 정보를 찾을 수 없습니다.", result.message());
    }

    @Test
    @DisplayName("TC-UC02-03 승인 예약만 현황에 표시")
    void roomStatusIncludesOnlyApprovedReservations() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-APPROVED", "student1", ReservationStatus.APPROVED, LocalDate.now()));
        fixture.reservations.save(reservation("R-PENDING", "student2", ReservationStatus.PENDING, LocalDate.now()));
        fixture.reservations.save(reservation("R-CANCELED", "student2", ReservationStatus.CANCELED, LocalDate.now()));

        ScheduleViewResult result = fixture.service.getRoomStatus("정보공학관", "912", LocalDate.now(), ViewType.WEEKLY);

        assertEquals(1, result.reservations().size());
        assertEquals("R-APPROVED", result.reservations().get(0).reservationId());
    }

    @Test
    @DisplayName("TC-UC02-04 강의 시간만 존재하는 경우")
    void roomStatusCanReturnLectureOnly() throws Exception {
        var fixture = fixture();

        ScheduleViewResult result = fixture.service.getRoomStatus("정보공학관", "912", LocalDate.now(), ViewType.WEEKLY);

        assertTrue(result.success());
        assertFalse(result.schedules().isEmpty());
        assertTrue(result.reservations().isEmpty());
    }

    private Fixture fixture() throws Exception {
        FileDatabase db = new FileDatabase(tempDir.resolve("store.tsv"));
        FileReservationRepository reservations = new FileReservationRepository(db);
        RoomStatusService service = new RoomStatusService(new FileClassroomRepository(db), new FileLectureScheduleRepository(db), reservations);
        return new Fixture(service, reservations);
    }

    private Reservation reservation(String id, String requesterId, ReservationStatus status, LocalDate date) {
        return Reservation.builder()
                .reservationId(id)
                .requesterId(requesterId)
                .requesterRole(UserRole.STUDENT)
                .buildingId("정보공학관")
                .roomId("912")
                .date(date)
                .dayOfWeek("월")
                .startPeriod(4)
                .endPeriod(5)
                .purpose("조별 학습")
                .participantCount(2)
                .companions("")
                .status(status)
                .reason("")
                .build();
    }

    private record Fixture(RoomStatusService service, FileReservationRepository reservations) { }
}
