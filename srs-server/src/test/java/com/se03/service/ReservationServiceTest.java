package com.se03.service;

import com.se03.repository.ClassroomRepository;
import com.se03.repository.LectureScheduleRepository;
import com.se03.model.ReservationResult;
import com.se03.model.ReservationStatus;
import com.se03.model.ReservationRequest;
import com.se03.model.SearchCondition;
import com.se03.model.UserRole;
import com.se03.repository.FileClassroomRepository;
import com.se03.repository.FileDatabase;
import com.se03.repository.FileLectureScheduleRepository;
import com.se03.repository.FileNotificationRepository;
import com.se03.repository.FileReservationRepository;
import com.se03.repository.FileUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void findsAvailableRoomsByLectureAndReservationConflicts() throws Exception {
        ReservationService service = service();

        var rooms = service.findAvailableRooms(new SearchCondition("정보공학관", "", LocalDate.now().plusDays(1), "월", 4, 5));

        assertFalse(rooms.isEmpty());
        assertTrue(rooms.stream().anyMatch(c -> c.roomId().equals("912")));
    }

    @Test
    void rejectsStudentReservationLongerThanTwoHours() throws Exception {
        ReservationResult result = service().requestReservation(baseRequest(UserRole.STUDENT, 4, 6));

        assertFalse(result.success());
        assertTrue(result.message().contains("학생 예약은 최대 2시간"));
    }

    @Test
    void cancelChangesReservationStatus() throws Exception {
        ReservationService service = service();
        ReservationResult created = service.requestReservation(baseRequest(UserRole.STUDENT, 4, 5));

        ReservationResult canceled = service.cancelReservation("student1", created.reservationId(), "테스트 취소");

        assertTrue(canceled.success());
        assertEquals(ReservationStatus.CANCELED, repository.findById(created.reservationId()).status());
    }

    @Test
    void assistantApprovesPendingReservation() throws Exception {
        ReservationService service = service();
        ReservationResult created = service.requestReservation(baseRequest(UserRole.STUDENT, 4, 5));

        ReservationResult approved = service.approveReservation("assistant1", created.reservationId());

        assertTrue(approved.success());
        assertEquals(ReservationStatus.APPROVED, repository.findById(created.reservationId()).status());
    }

    @Test
    void rejectsApprovalWithoutAssistantAuthority() throws Exception {
        ReservationService service = service();
        ReservationResult created = service.requestReservation(baseRequest(UserRole.STUDENT, 4, 5));

        ReservationResult approved = service.approveReservation("student1", created.reservationId());

        assertFalse(approved.success());
        assertTrue(approved.message().contains("조교 권한"));
        assertEquals(ReservationStatus.PENDING, repository.findById(created.reservationId()).status());
    }

    @Test
    void listsReservationsByRequester() throws Exception {
        ReservationService service = service();
        ReservationResult created = service.requestReservation(baseRequest(UserRole.STUDENT, 4, 5));

        assertTrue(service.getReservationsByUser("student1").stream().anyMatch(r -> r.reservationId().equals(created.reservationId())));
        assertTrue(service.getReservationsByUser("student2").isEmpty());
    }

    @Test
    void assistantForceCancelsReservation() throws Exception {
        ReservationService service = service();
        ReservationResult created = service.requestReservation(baseRequest(UserRole.STUDENT, 4, 5));

        ReservationResult canceled = service.forceCancelReservation("assistant1", created.reservationId(), "관리자 취소");

        assertTrue(canceled.success());
        assertEquals(ReservationStatus.CANCELED, repository.findById(created.reservationId()).status());
    }

    @Test
    void rejectsConflictingLectureSchedule() throws Exception {
        var db = new FileDatabase(tempDir.resolve("store.tsv"));
        var classroomRepository = new FileClassroomRepository(db);
        var lectureRepository = new FileLectureScheduleRepository(db);
        var management = new ClassroomManagementService(classroomRepository, lectureRepository, new FileReservationRepository(db), new FileUserRepository(db));

        String result = management.saveLectureSchedule("assistant1", "", "정보공학관", "912", "월", 2, 4, "충돌 강의", "교수");

        assertTrue(result.contains("ERROR"));
        assertTrue(result.contains("충돌"));
    }

    @Test
    void refusesDeletingClassroomWithSchedule() throws Exception {
        var db = new FileDatabase(tempDir.resolve("store.tsv"));
        var management = new ClassroomManagementService(new FileClassroomRepository(db), new FileLectureScheduleRepository(db),
                new FileReservationRepository(db), new FileUserRepository(db));

        String result = management.deleteClassroom("assistant1", "912");

        assertTrue(result.contains("ERROR"));
        assertTrue(result.contains("삭제할 수 없습니다"));
    }

    @Test
    void backsUpAndRestoresYamlSections() throws Exception {
        var db = new FileDatabase(tempDir.resolve("store.tsv"));
        Path backup = tempDir.resolve("backup.yml");

        String backedUp = db.backup(backup);
        String yaml = Files.readString(backup);
        String restored = db.restore(backup);

        assertTrue(backedUp.contains("OK"));
        assertTrue(yaml.contains("classrooms:"));
        assertTrue(yaml.contains("schedules:"));
        assertTrue(yaml.contains("reservations:"));
        assertTrue(restored.contains("OK"));
    }

    private FileReservationRepository repository;

    private ReservationService service() throws Exception {
        FileDatabase db = new FileDatabase(tempDir.resolve("store.tsv"));
        ClassroomRepository classroomRepository = new FileClassroomRepository(db);
        LectureScheduleRepository lectureScheduleRepository = new FileLectureScheduleRepository(db);
        repository = new FileReservationRepository(db);
        NotificationService notificationService = new NotificationService(new FileNotificationRepository(db));
        return new ReservationService(classroomRepository, lectureScheduleRepository, repository, notificationService, new FileUserRepository(db));
    }

    private ReservationRequest baseRequest(UserRole role, int start, int end) {
        return new ReservationRequest("student1", role, "정보공학관", "912", LocalDate.now().plusDays(1), "월",
                start, end, "개인 학습", 2, "", "");
    }
}
