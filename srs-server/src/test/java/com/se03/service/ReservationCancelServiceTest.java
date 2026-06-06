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

class ReservationCancelServiceTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("TC-UC06-01 사용자 본인 예약 취소 성공")
    void ownerCanCancelReservation() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-CANCEL", "student1", UserRole.STUDENT, ReservationStatus.PENDING));

        ReservationResult result = fixture.service.cancelReservation("student1", "R-CANCEL", "사용 취소");

        assertTrue(result.success());
        assertEquals(ReservationStatus.CANCELED, fixture.reservations.findById("R-CANCEL").status());
        assertEquals("사용 취소", fixture.reservations.findById("R-CANCEL").reason());
    }

    @Test
    @DisplayName("TC-UC06-02 타인 예약 취소 실패")
    void otherUsersReservationCannotBeCanceled() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-OTHER", "student1", UserRole.STUDENT, ReservationStatus.PENDING));

        ReservationResult result = fixture.service.cancelReservation("student2", "R-OTHER", "사용 취소");

        assertFalse(result.success());
        assertTrue(result.message().contains("자신의 예약만"));
        assertEquals(ReservationStatus.PENDING, fixture.reservations.findById("R-OTHER").status());
    }

    @Test
    @DisplayName("TC-UC06-03 존재하지 않는 예약 취소 실패")
    void missingReservationCannotBeCanceled() throws Exception {
        ReservationResult result = fixture().service.cancelReservation("student1", "UNKNOWN", "사용 취소");

        assertFalse(result.success());
        assertTrue(result.message().contains("취소 대상 예약"));
    }

    @Test
    @DisplayName("TC-UC06-04 이미 취소된 예약 취소 실패")
    void alreadyCanceledReservationCannotBeCanceledAgain() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-CANCELED", "student1", UserRole.STUDENT, ReservationStatus.CANCELED));

        ReservationResult result = fixture.service.cancelReservation("student1", "R-CANCELED", "다시 취소");

        assertFalse(result.success());
        assertTrue(result.message().contains("이미 취소"));
    }

    @Test
    @DisplayName("TC-UC06-05 조교 강제 취소 성공")
    void assistantCanForceCancelReservation() throws Exception {
        var fixture = fixture();
        fixture.reservations.save(reservation("R-FORCE", "student1", UserRole.STUDENT, ReservationStatus.APPROVED));

        ReservationResult result = fixture.service.forceCancelReservation("assistant1", "R-FORCE", "관리상 취소");

        assertTrue(result.success());
        assertEquals(ReservationStatus.CANCELED, fixture.reservations.findById("R-FORCE").status());
        assertEquals("관리상 취소", fixture.reservations.findById("R-FORCE").reason());
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
                .purpose("예약")
                .participantCount(2)
                .companions("")
                .status(status)
                .reason("")
                .build();
    }

    private record Fixture(ReservationService service, FileReservationRepository reservations) { }
}
