package com.se03.service;

import com.se03.model.Classroom;
import com.se03.repository.ClassroomRepository;
import com.se03.model.LectureSchedule;
import com.se03.repository.LectureScheduleRepository;
import com.se03.service.NotificationService;
import com.se03.repository.ReservationRepository;
import com.se03.repository.UserRepository;
import com.se03.model.Reservation;
import com.se03.model.ReservationRequest;
import com.se03.model.ReservationResult;
import com.se03.model.ReservationStatus;
import com.se03.model.SearchCondition;
import com.se03.model.ValidationResult;
import com.se03.model.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ReservationService {
    private final ClassroomRepository classroomRepository;
    private final LectureScheduleRepository lectureScheduleRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public ReservationService(ClassroomRepository classroomRepository, LectureScheduleRepository lectureScheduleRepository,
                              ReservationRepository reservationRepository, NotificationService notificationService, UserRepository userRepository) {
        this.classroomRepository = classroomRepository;
        this.lectureScheduleRepository = lectureScheduleRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public List<Classroom> findAvailableRooms(SearchCondition condition) {
        List<Classroom> classrooms = classroomRepository.findByBuilding(condition.buildingId());
        List<LectureSchedule> schedules = lectureScheduleRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();
        return classrooms.stream()
                .filter(c -> !hasLectureConflict(c, condition, schedules))
                .filter(c -> !hasApprovedReservationConflict(c, condition, reservations))
                .toList();
    }

    public ReservationResult requestReservation(ReservationRequest request) {
        ValidationResult validation = validateReservationRequest(request);
        if (!validation.valid()) return ReservationResult.fail(validation.reason());

        Reservation reservation = Reservation.builder()
                .reservationId("R-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .requesterId(request.requesterId())
                .requesterRole(request.requesterRole())
                .buildingId(request.buildingId())
                .roomId(request.roomId())
                .date(request.date())
                .dayOfWeek(request.dayOfWeek())
                .startPeriod(request.startPeriod())
                .endPeriod(request.endPeriod())
                .purpose(request.purpose())
                .participantCount(request.participantCount())
                .companions(request.companions())
                .status(ReservationStatus.PENDING)
                .reason("")
                .build();
        reservationRepository.save(reservation);
        return ReservationResult.ok(reservation.reservationId(), "예약 신청이 완료되었습니다.");
    }

    public ReservationResult cancelReservation(String userId, String reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null) return ReservationResult.fail("취소 대상 예약이 없습니다.");
        if (!reservation.isOwnedBy(userId)) return ReservationResult.fail("자신의 예약만 취소할 수 있습니다.");
        if (reservation.status() == ReservationStatus.CANCELED) return ReservationResult.fail("이미 취소된 예약입니다.");
        reservation.cancel(reason);
        reservationRepository.save(reservation);
        notificationService.notifyCanceled(reservation, reason);
        return ReservationResult.ok(reservationId, "예약이 취소되었습니다.");
    }

    public ReservationResult forceCancelReservation(String assistantId, String reservationId, String reason) {
        if (!isAssistant(assistantId)) return ReservationResult.fail("조교 권한이 필요합니다.");
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null) return ReservationResult.fail("취소 대상 예약이 없습니다.");
        if (reservation.status() == ReservationStatus.CANCELED) return ReservationResult.fail("이미 취소된 예약입니다.");
        reservation.cancel(reason);
        reservationRepository.save(reservation);
        notificationService.notifyCanceled(reservation, reason);
        return ReservationResult.ok(reservationId, "예약이 강제 취소되었습니다.");
    }

    public List<Reservation> getPendingReservations() {
        return reservationRepository.findPending();
    }

    public List<Reservation> getReservationsByUser(String userId) {
        return reservationRepository.findByUser(userId);
    }

    public Reservation getReservation(String reservationId) {
        return reservationRepository.findById(reservationId);
    }

    public ReservationResult approveReservation(String assistantId, String reservationId) {
        if (!isAssistant(assistantId)) return ReservationResult.fail("조교 권한이 필요합니다.");
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null) return ReservationResult.fail("대상 예약이 없습니다.");
        if (reservation.status() != ReservationStatus.PENDING) return ReservationResult.fail("대기 상태 예약만 승인할 수 있습니다.");
        List<Reservation> changed = new ArrayList<>();
        if (reservation.isProfessorReservation()) {
            for (Reservation conflict : findStudentConflicts(reservation)) {
                conflict.cancel("교수 예약 승인으로 예약이 취소되었습니다.");
                notificationService.notifyCanceled(conflict, conflict.reason());
                changed.add(conflict);
            }
        }
        reservation.approve();
        changed.add(reservation);
        reservationRepository.saveAll(changed);
        notificationService.notifyApproved(reservation);
        return ReservationResult.ok(reservationId, "예약이 승인되었습니다.");
    }

    public ReservationResult rejectReservation(String assistantId, String reservationId, String reason) {
        if (!isAssistant(assistantId)) return ReservationResult.fail("조교 권한이 필요합니다.");
        if (reason == null || reason.isBlank()) return ReservationResult.fail("거부 사유를 입력해야 합니다.");
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null) return ReservationResult.fail("대상 예약이 없습니다.");
        if (reservation.status() != ReservationStatus.PENDING) return ReservationResult.fail("대기 상태 예약만 거부할 수 있습니다.");
        reservation.reject(reason);
        reservationRepository.save(reservation);
        notificationService.notifyRejected(reservation, reason);
        return ReservationResult.ok(reservationId, "예약이 거부되었습니다.");
    }

    private ValidationResult validateReservationRequest(ReservationRequest request) {
        // 보고서의 checkTimeLimit/checkAdvanceRule/checkCapacityLimit 등 개념 검증은
        // 호출 순서와 실패 사유를 한곳에서 보장하려고 단일 검증 메서드 안에 모았다.
        if (blank(request.requesterId()) || blank(request.buildingId()) || blank(request.roomId()) || request.date() == null || blank(request.purpose())) {
            return ValidationResult.fail("필수 입력값이 누락되었습니다.");
        }
        if (request.startPeriod() <= 0 || request.endPeriod() < request.startPeriod()) return ValidationResult.fail("교시 범위가 올바르지 않습니다.");
        Classroom classroom = classroomRepository.findById(request.roomId());
        if (classroom == null || !classroom.buildingId().equals(request.buildingId())) return ValidationResult.fail("강의실 정보를 찾을 수 없습니다.");
        SearchCondition condition = new SearchCondition(request.buildingId(), request.roomId(), request.date(), request.dayOfWeek(), request.startPeriod(), request.endPeriod());
        if (hasLectureConflict(classroom, condition, lectureScheduleRepository.findAll())) return ValidationResult.fail("강의 시간과 충돌합니다.");
        if (hasBlockingReservationConflict(classroom, request, reservationRepository.findAll())) return ValidationResult.fail("기존 예약과 충돌합니다.");
        int hours = request.endPeriod() - request.startPeriod() + 1;
        if (request.requesterRole() == UserRole.STUDENT) {
            if (hours > 2) return ValidationResult.fail("학생 예약은 최대 2시간입니다.");
            if (!request.date().isAfter(LocalDate.now())) return ValidationResult.fail("학생 예약은 최소 하루 전에 신청해야 합니다.");
            int occupied = reservationRepository.findAll().stream()
                    .filter(r -> r.requesterRole() == UserRole.STUDENT && r.conflictsWith(request.roomId(), request.date(), request.startPeriod(), request.endPeriod()))
                    .mapToInt(Reservation::participantCount).sum();
            if (occupied + request.participantCount() > classroom.capacity() / 2) return ValidationResult.fail("수용 인원의 50%를 초과하여 예약할 수 없습니다.");
        }
        if (request.requesterRole() == UserRole.PROFESSOR && hours > 3) return ValidationResult.fail("교수 예약은 최대 3시간입니다.");
        return ValidationResult.ok();
    }

    private boolean hasLectureConflict(Classroom classroom, SearchCondition condition, List<LectureSchedule> schedules) {
        return schedules.stream().anyMatch(s -> s.roomId().equals(classroom.roomId()) && s.conflictsWith(condition.dayOfWeek(), condition.startPeriod(), condition.endPeriod()));
    }

    private boolean hasApprovedReservationConflict(Classroom classroom, SearchCondition condition, List<Reservation> reservations) {
        return reservations.stream().anyMatch(r -> r.status() == ReservationStatus.APPROVED && r.conflictsWith(classroom.roomId(), condition.date(), condition.startPeriod(), condition.endPeriod()));
    }

    private boolean hasBlockingReservationConflict(Classroom classroom, ReservationRequest request, List<Reservation> reservations) {
        // 교수 예약은 요구사항상 학생 예약/대기와 충돌해도 신청을 허용하고,
        // 승인 시점에 학생 예약을 취소 처리한다. 교수 예약끼리의 충돌은 여기서 막는다.
        return reservations.stream()
                .filter(Reservation::isActive)
                .filter(r -> r.conflictsWith(classroom.roomId(), request.date(), request.startPeriod(), request.endPeriod()))
                .anyMatch(r -> request.requesterRole() != UserRole.PROFESSOR || r.requesterRole() != UserRole.STUDENT);
    }

    private List<Reservation> findStudentConflicts(Reservation reservation) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.requesterRole() == UserRole.STUDENT)
                .filter(r -> r.conflictsWith(reservation.roomId(), reservation.date(), reservation.startPeriod(), reservation.endPeriod()))
                .toList();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isAssistant(String userId) {
        var user = userRepository.findById(userId);
        return user != null && user.role() == UserRole.ASSISTANT;
    }
}
