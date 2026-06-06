package com.se03.service;

import com.se03.repository.ClassroomRepository;
import com.se03.repository.LectureScheduleRepository;
import com.se03.repository.ReservationRepository;
import com.se03.repository.UserRepository;
import com.se03.model.Classroom;
import com.se03.model.LectureSchedule;
import com.se03.model.UserRole;

import java.util.List;
import java.util.UUID;

public final class ClassroomManagementService {
    private final ClassroomRepository classroomRepository;
    private final LectureScheduleRepository lectureScheduleRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ClassroomManagementService(ClassroomRepository classroomRepository, LectureScheduleRepository lectureScheduleRepository,
                                      ReservationRepository reservationRepository, UserRepository userRepository) {
        this.classroomRepository = classroomRepository;
        this.lectureScheduleRepository = lectureScheduleRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    public List<Classroom> getClassrooms() {
        return classroomRepository.findAll();
    }

    public List<LectureSchedule> getLectureSchedules() {
        return lectureScheduleRepository.findAll();
    }

    public String saveClassroom(String assistantId, String buildingId, String roomId, int capacity) {
        if (!isAssistant(assistantId)) return error("조교 권한이 필요합니다.");
        if (blank(buildingId) || blank(roomId) || capacity <= 0) return error("강의실 입력값이 올바르지 않습니다.");
        classroomRepository.save(new Classroom(buildingId, roomId, capacity));
        return ok("강의실 정보를 저장했습니다.");
    }

    public String deleteClassroom(String assistantId, String roomId) {
        if (!isAssistant(assistantId)) return error("조교 권한이 필요합니다.");
        if (blank(roomId)) return error("삭제할 강의실 ID가 없습니다.");
        // SFR-809/810은 다이어그램 상세 대상 밖이지만, 삭제 실패 사유까지 요구하므로
        // Repository에서 조용히 지우지 않고 서비스에서 강의/예약 참조를 먼저 검사한다.
        if (lectureScheduleRepository.findAll().stream().anyMatch(s -> s.roomId().equals(roomId))) {
            return error("등록된 강의 시간이 있어 강의실을 삭제할 수 없습니다.");
        }
        if (reservationRepository.findAll().stream().anyMatch(r -> r.roomId().equals(roomId) && r.isActive())) {
            return error("진행 중인 예약이 있어 강의실을 삭제할 수 없습니다.");
        }
        classroomRepository.delete(roomId);
        return ok("강의실 정보를 삭제했습니다.");
    }

    public String saveLectureSchedule(String assistantId, String scheduleId, String buildingId, String roomId, String dayOfWeek,
                                      int startPeriod, int endPeriod, String title, String professor) {
        if (!isAssistant(assistantId)) return error("조교 권한이 필요합니다.");
        if (blank(buildingId) || blank(roomId) || blank(dayOfWeek) || blank(title) || startPeriod <= 0 || endPeriod < startPeriod) {
            return error("강의 시간 입력값이 올바르지 않습니다.");
        }
        Classroom classroom = classroomRepository.findById(roomId);
        if (classroom == null || !classroom.buildingId().equals(buildingId)) return error("강의실 정보를 찾을 수 없습니다.");
        // 강의 시간 충돌 검사는 예약 검증과 같은 서버 측 책임이다.
        // 화면마다 다른 기준이 생기지 않도록 관리 서비스에서 일관되게 막는다.
        boolean conflicts = lectureScheduleRepository.findAll().stream()
                .filter(s -> !s.scheduleId().equals(scheduleId))
                .anyMatch(s -> s.roomId().equals(roomId) && s.conflictsWith(dayOfWeek, startPeriod, endPeriod));
        if (conflicts) return error("기존 강의 시간과 충돌합니다.");
        String id = blank(scheduleId) ? "L-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : scheduleId;
        lectureScheduleRepository.save(new LectureSchedule(id, buildingId, roomId, dayOfWeek, startPeriod, endPeriod, title, professor == null ? "" : professor));
        return okWithId("강의 시간 정보를 저장했습니다.", id);
    }

    public String deleteLectureSchedule(String assistantId, String scheduleId) {
        if (!isAssistant(assistantId)) return error("조교 권한이 필요합니다.");
        if (blank(scheduleId)) return error("삭제할 강의 시간 ID가 없습니다.");
        lectureScheduleRepository.delete(scheduleId);
        return ok("강의 시간 정보를 삭제했습니다.");
    }

    private boolean isAssistant(String userId) {
        var user = userRepository.findById(userId);
        return user != null && user.role() == UserRole.ASSISTANT;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String ok(String message) {
        return "OK\nmessage=" + message + "\n";
    }

    private String okWithId(String message, String id) {
        return "OK\nmessage=" + message + "\nid=" + id + "\n";
    }

    private String error(String message) {
        return "ERROR\nmessage=" + message + "\n";
    }
}
