package com.se03.service;

import com.se03.model.Reservation;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationApprovalServiceTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("TC-UC07-01 대기 예약 승인 성공")
    void pendingReservationCanBeApproved() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-APPROVE", "student1", UserRole.STUDENT, ReservationStatus.PENDING));

        ReservationResult result = fixture.service.approveReservation("assistant1", "R-APPROVE");

        assertTrue(result.success());
        assertEquals(ReservationStatus.APPROVED, fixture.reservations.findById("R-APPROVE").status());
    }

    @Test
    @DisplayName("TC-UC07-02 대기 예약 거부 성공")
    void pendingReservationCanBeRejectedWithReason() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-REJECT", "student1", UserRole.STUDENT, ReservationStatus.PENDING));

        ReservationResult result = fixture.service.rejectReservation("assistant1", "R-REJECT", "시간 중복 우려");

        assertTrue(result.success());
        assertEquals(ReservationStatus.REJECTED, fixture.reservations.findById("R-REJECT").status());
        assertEquals("시간 중복 우려", fixture.reservations.findById("R-REJECT").reason());
    }

    @Test
    @DisplayName("TC-UC07-03 대기 상태가 아닌 예약 승인 실패")
    void nonPendingReservationCannotBeApproved() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-APPROVED", "student1", UserRole.STUDENT, ReservationStatus.APPROVED));

        ReservationResult result = fixture.service.approveReservation("assistant1", "R-APPROVED");

        assertFalse(result.success());
        assertTrue(result.message().contains("대기 상태"));
    }

    @Test
    @DisplayName("TC-UC07-04 거부 사유 누락 실패")
    void rejectWithoutReasonFails() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-NO-REASON", "student1", UserRole.STUDENT, ReservationStatus.PENDING));

        ReservationResult result = fixture.service.rejectReservation("assistant1", "R-NO-REASON", "");

        assertFalse(result.success());
        assertTrue(result.message().contains("거부 사유"));
        assertEquals(ReservationStatus.PENDING, fixture.reservations.findById("R-NO-REASON").status());
    }

    @Test
    @DisplayName("TC-UC07-05 교수 예약 승인 시 학생 예약 취소")
    void approvingProfessorReservationCancelsConflictingStudentReservation() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-PROF", "prof1", UserRole.PROFESSOR, ReservationStatus.PENDING));
        fixture.reservations.save(reservation("R-STUDENT", "student1", UserRole.STUDENT, ReservationStatus.APPROVED));

        ReservationResult result = fixture.service.approveReservation("assistant1", "R-PROF");

        assertTrue(result.success());
        assertEquals(ReservationStatus.APPROVED, fixture.reservations.findById("R-PROF").status());
        assertEquals(ReservationStatus.CANCELED, fixture.reservations.findById("R-STUDENT").status());
        assertTrue(fixture.reservations.findById("R-STUDENT").reason().contains("교수 예약 승인"));
    }

    private Fixture fixture() throws Exception {
        FileDatabase db = new FileDatabase(tempDir.resolve("store.tsv"));
        FileReservationRepository reservations = new FileReservationRepository(db);
        NotificationService notifications = new NotificationService(new FileNotificationRepository(db));
        ReservationService service = new ReservationService(new FileClassroomRepository(db), new FileLectureScheduleRepository(db), reservations, notifications, new FileUserRepository(db));
        return new Fixture(service, reservations);
    }

    private Reservation reservation(String id, String requesterId, UserRole role, ReservationStatus status) {
        return Reservation.builder()
                .reservationId(id)
                .requesterId(requesterId)
                .requesterRole(role)
                .buildingId("정보공학관")
                .roomId("912")
                .date(LocalDate.now().plusDays(2))
                .dayOfWeek("목")
                .startPeriod(7)
                .endPeriod(8)
                .purpose(role == UserRole.PROFESSOR ? "세미나" : "조별 학습")
                .participantCount(role == UserRole.PROFESSOR ? 10 : 2)
                .companions("")
                .status(status)
                .reason("")
                .build();
    }

    private record Fixture(ReservationService service, FileReservationRepository reservations) { }
}
