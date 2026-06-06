package com.se03.service;

import com.se03.model.Reservation;
import com.se03.model.ReservationRequest;
import com.se03.model.ReservationResult;
import com.se03.model.ReservationStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationRequestServiceTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("TC-UC04-01 예약 신청 성공")
    void validReservationIsSavedAsPending() throws Exception {
        var fixture = fixture();

        ReservationResult result = fixture.service.requestReservation(request(UserRole.STUDENT, "912", LocalDate.now().plusDays(2), "목", 7, 8, "조별 학습", 2));

        assertTrue(result.success());
        assertNotNull(fixture.reservations.findById(result.reservationId()));
        assertEquals(ReservationStatus.PENDING, fixture.reservations.findById(result.reservationId()).status());
    }

    @Test
    @DisplayName("TC-UC04-02 필수 입력값 누락")
    void missingRequiredValueFails() throws Exception {
        ReservationResult result = fixture().service.requestReservation(request(UserRole.STUDENT, "912", LocalDate.now().plusDays(2), "목", 7, 8, "", 2));

        assertFalse(result.success());
        assertTrue(result.message().contains("필수 입력값"));
    }

    @Test
    @DisplayName("TC-UC04-03 강의 시간 충돌")
    void lectureConflictFails() throws Exception {
        ReservationResult result = fixture().service.requestReservation(request(UserRole.STUDENT, "912", LocalDate.now().plusDays(2), "월", 1, 2, "조별 학습", 2));

        assertFalse(result.success());
        assertTrue(result.message().contains("강의 시간"));
    }

    @Test
    @DisplayName("TC-UC04-04 기존 예약 충돌")
    void existingReservationConflictFails() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(existing("R-EXISTING", UserRole.STUDENT, ReservationStatus.APPROVED, "912", LocalDate.now().plusDays(2), "목", 7, 8, 2));

        ReservationResult result = fixture.service.requestReservation(request(UserRole.STUDENT, "912", LocalDate.now().plusDays(2), "목", 7, 8, "조별 학습", 2));

        assertFalse(result.success());
        assertTrue(result.message().contains("기존 예약"));
    }

    @Test
    @DisplayName("TC-UC04-05 학생 예약 시간 초과")
    void studentReservationLongerThanTwoHoursFails() throws Exception {
        ReservationResult result = fixture().service.requestReservation(request(UserRole.STUDENT, "912", LocalDate.now().plusDays(2), "목", 7, 9, "조별 학습", 2));

        assertFalse(result.success());
        assertTrue(result.message().contains("학생 예약은 최대 2시간"));
    }

    @Test
    @DisplayName("TC-UC04-06 교수 예약 시간 초과")
    void professorReservationLongerThanThreeHoursFails() throws Exception {
        ReservationResult result = fixture().service.requestReservation(request(UserRole.PROFESSOR, "912", LocalDate.now().plusDays(2), "목", 6, 9, "세미나", 10));

        assertFalse(result.success());
        assertTrue(result.message().contains("교수 예약은 최대 3시간"));
    }

    @Test
    @DisplayName("TC-UC04-07 학생 최소 하루 전 신청 위반")
    void studentSameDayReservationFails() throws Exception {
        ReservationResult result = fixture().service.requestReservation(request(UserRole.STUDENT, "912", LocalDate.now(), "목", 7, 8, "개인 학습", 2));

        assertFalse(result.success());
        assertTrue(result.message().contains("최소 하루 전"));
    }

    @Test
    @DisplayName("TC-UC04-08 학생 예약/대기 인원 50% 초과")
    void studentCapacityLimitFails() throws Exception {
        ReservationResult result = fixture().service.requestReservation(request(UserRole.STUDENT, "912", LocalDate.now().plusDays(2), "목", 7, 8, "조별 학습", 31));

        assertFalse(result.success());
        assertTrue(result.message().contains("50%"));
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

    private ReservationRequest request(UserRole role, String roomId, LocalDate date, String dayOfWeek, int startPeriod, int endPeriod, String purpose, int count) {
        String requesterId = role == UserRole.PROFESSOR ? "prof1" : "student1";
        return new ReservationRequest(requesterId, role, "정보공학관", roomId, date, dayOfWeek, startPeriod, endPeriod, purpose, count, "", "");
    }

    private Reservation existing(String id, UserRole role, ReservationStatus status, String roomId, LocalDate date, String dayOfWeek, int startPeriod, int endPeriod, int count) {
        return Reservation.builder()
                .reservationId(id)
                .requesterId(role == UserRole.PROFESSOR ? "prof1" : "student2")
                .requesterRole(role)
                .buildingId("정보공학관")
                .roomId(roomId)
                .date(date)
                .dayOfWeek(dayOfWeek)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .purpose("기존 예약")
                .participantCount(count)
                .companions("")
                .status(status)
                .reason("")
                .build();
    }

    private record Fixture(ReservationService service, FileReservationRepository reservations) { }
}
