package com.se03.repository;

import com.se03.model.Classroom;
import com.se03.model.LectureSchedule;
import com.se03.model.Notification;
import com.se03.model.Reservation;
import com.se03.model.ReservationStatus;
import com.se03.model.User;
import com.se03.model.UserRole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class FileDatabase {
    private final Path file;
    final List<User> users = new ArrayList<>();
    final List<Classroom> classrooms = new ArrayList<>();
    final List<LectureSchedule> schedules = new ArrayList<>();
    final List<Reservation> reservations = new ArrayList<>();
    final List<Notification> notifications = new ArrayList<>();

    public FileDatabase(Path file) throws IOException {
        this.file = file;
        if (Files.exists(file)) load();
        else seed();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            users.forEach(u -> lines.add(String.join("\t", "USER", u.userId(), u.password(), u.name(), u.role().name())));
            classrooms.forEach(c -> lines.add(String.join("\t", "CLASSROOM", c.buildingId(), c.roomId(), String.valueOf(c.capacity()))));
            schedules.forEach(s -> lines.add(String.join("\t", "LECTURE", s.scheduleId(), s.buildingId(), s.roomId(), s.dayOfWeek(), String.valueOf(s.startPeriod()), String.valueOf(s.endPeriod()), s.title(), s.professor())));
            reservations.forEach(r -> lines.add(String.join("\t", "RESERVATION", r.reservationId(), r.requesterId(), r.requesterRole().name(), r.buildingId(), r.roomId(),
                    r.date().toString(), r.dayOfWeek(), String.valueOf(r.startPeriod()), String.valueOf(r.endPeriod()), r.purpose(),
                    String.valueOf(r.participantCount()), r.companions(), r.status().name(), r.reason())));
            notifications.forEach(n -> lines.add(String.join("\t", "NOTIFICATION", n.notificationId(), n.userId(), n.message(), String.valueOf(n.read()))));
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public synchronized String backup(Path path) {
        // 보고서의 "YAML 백업" 요구를 맞추기 위해 백업 파일은 도메인별 YAML 섹션으로 쓴다.
        // 운영 저장소는 단순 TSV로 유지하여 수업 과제 범위의 파일 입출력 코드를 작게 유지한다.
        save();
        try {
            Files.createDirectories(path.getParent());
            List<String> out = new ArrayList<>();
            out.add("users:");
            users.forEach(u -> {
                out.add("  - userId: \"" + yaml(u.userId()) + "\"");
                out.add("    password: \"" + yaml(u.password()) + "\"");
                out.add("    name: \"" + yaml(u.name()) + "\"");
                out.add("    role: \"" + u.role().name() + "\"");
            });
            out.add("classrooms:");
            classrooms.forEach(c -> {
                out.add("  - buildingId: \"" + yaml(c.buildingId()) + "\"");
                out.add("    roomId: \"" + yaml(c.roomId()) + "\"");
                out.add("    capacity: " + c.capacity());
            });
            out.add("schedules:");
            schedules.forEach(s -> {
                out.add("  - scheduleId: \"" + yaml(s.scheduleId()) + "\"");
                out.add("    buildingId: \"" + yaml(s.buildingId()) + "\"");
                out.add("    roomId: \"" + yaml(s.roomId()) + "\"");
                out.add("    dayOfWeek: \"" + yaml(s.dayOfWeek()) + "\"");
                out.add("    startPeriod: " + s.startPeriod());
                out.add("    endPeriod: " + s.endPeriod());
                out.add("    title: \"" + yaml(s.title()) + "\"");
                out.add("    professor: \"" + yaml(s.professor()) + "\"");
            });
            out.add("reservations:");
            reservations.forEach(r -> {
                out.add("  - reservationId: \"" + yaml(r.reservationId()) + "\"");
                out.add("    requesterId: \"" + yaml(r.requesterId()) + "\"");
                out.add("    requesterRole: \"" + r.requesterRole().name() + "\"");
                out.add("    buildingId: \"" + yaml(r.buildingId()) + "\"");
                out.add("    roomId: \"" + yaml(r.roomId()) + "\"");
                out.add("    date: \"" + r.date() + "\"");
                out.add("    dayOfWeek: \"" + yaml(r.dayOfWeek()) + "\"");
                out.add("    startPeriod: " + r.startPeriod());
                out.add("    endPeriod: " + r.endPeriod());
                out.add("    purpose: \"" + yaml(r.purpose()) + "\"");
                out.add("    participantCount: " + r.participantCount());
                out.add("    companions: \"" + yaml(r.companions()) + "\"");
                out.add("    status: \"" + r.status().name() + "\"");
                out.add("    reason: \"" + yaml(r.reason()) + "\"");
            });
            out.add("notifications:");
            notifications.forEach(n -> {
                out.add("  - notificationId: \"" + yaml(n.notificationId()) + "\"");
                out.add("    userId: \"" + yaml(n.userId()) + "\"");
                out.add("    message: \"" + yaml(n.message()) + "\"");
                out.add("    read: " + n.read());
            });
            Files.write(path, out, StandardCharsets.UTF_8);
            return "OK\nmessage=백업 파일을 저장했습니다: " + path + "\n";
        } catch (IOException e) {
            return "ERROR\nmessage=백업 실패: " + e.getMessage() + "\n";
        }
    }

    public synchronized String restore(Path path) {
        // 백업 파일은 사용자가 다루는 제출/복구 포맷이므로 YAML 구조를 기준으로 복구한다.
        // TSV 로드는 서버 내부 영구 저장소 호환용으로만 남겨 둔다.
        try {
            loadYaml(Files.readAllLines(path, StandardCharsets.UTF_8));
            save();
            return "OK\nmessage=백업 파일에서 복구했습니다.\n";
        } catch (IOException e) {
            return "ERROR\nmessage=복구 실패: " + e.getMessage() + "\n";
        } catch (RuntimeException e) {
            return "ERROR\nmessage=복구 실패: " + e.getMessage() + "\n";
        }
    }

    private void loadYaml(List<String> lines) {
        users.clear(); classrooms.clear(); schedules.clear(); reservations.clear(); notifications.clear();
        String section = "";
        java.util.Map<String, String> item = new java.util.LinkedHashMap<>();
        for (String line : lines) {
            if (line.isBlank()) continue;
            if (!line.startsWith(" ") && line.endsWith(":")) {
                addYamlItem(section, item);
                item.clear();
                section = line.substring(0, line.length() - 1);
            } else if (line.startsWith("  - ")) {
                addYamlItem(section, item);
                item.clear();
                putYaml(item, line.substring(4));
            } else if (line.startsWith("    ")) {
                putYaml(item, line.substring(4));
            }
        }
        addYamlItem(section, item);
    }

    private void putYaml(java.util.Map<String, String> item, String line) {
        int idx = line.indexOf(':');
        if (idx < 0) return;
        item.put(line.substring(0, idx), unyaml(line.substring(idx + 1).trim()));
    }

    private void addYamlItem(String section, java.util.Map<String, String> item) {
        if (item.isEmpty()) return;
        switch (section) {
            case "users" -> users.add(new User(item.get("userId"), item.get("password"), item.get("name"), UserRole.valueOf(item.get("role"))));
            case "classrooms" -> classrooms.add(new Classroom(item.get("buildingId"), item.get("roomId"), Integer.parseInt(item.get("capacity"))));
            case "schedules" -> schedules.add(new LectureSchedule(item.get("scheduleId"), item.get("buildingId"), item.get("roomId"), item.get("dayOfWeek"),
                    Integer.parseInt(item.get("startPeriod")), Integer.parseInt(item.get("endPeriod")), item.get("title"), item.get("professor")));
            case "reservations" -> reservations.add(Reservation.builder()
                    .reservationId(item.get("reservationId")).requesterId(item.get("requesterId")).requesterRole(UserRole.valueOf(item.get("requesterRole")))
                    .buildingId(item.get("buildingId")).roomId(item.get("roomId")).date(LocalDate.parse(item.get("date"))).dayOfWeek(item.get("dayOfWeek"))
                    .startPeriod(Integer.parseInt(item.get("startPeriod"))).endPeriod(Integer.parseInt(item.get("endPeriod"))).purpose(item.get("purpose"))
                    .participantCount(Integer.parseInt(item.get("participantCount"))).companions(item.get("companions")).status(ReservationStatus.valueOf(item.get("status")))
                    .reason(item.getOrDefault("reason", "")).build());
            case "notifications" -> notifications.add(new Notification(item.get("notificationId"), item.get("userId"), item.get("message"), Boolean.parseBoolean(item.get("read"))));
            default -> { }
        }
    }

    private String yaml(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unyaml(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private void seed() {
        users.add(new User("student1", "1234", "김학생", UserRole.STUDENT));
        users.add(new User("student2", "1234", "이학생", UserRole.STUDENT));
        users.add(new User("prof1", "1234", "박교수", UserRole.PROFESSOR));
        users.add(new User("assistant1", "1234", "최조교", UserRole.ASSISTANT));
        classrooms.add(new Classroom("정보공학관", "908", 30));
        classrooms.add(new Classroom("정보공학관", "911", 40));
        classrooms.add(new Classroom("정보공학관", "912", 60));
        classrooms.add(new Classroom("정보공학관", "913", 40));
        classrooms.add(new Classroom("정보공학관", "914", 50));
        classrooms.add(new Classroom("정보공학관", "915", 45));
        classrooms.add(new Classroom("정보공학관", "916", 45));
        classrooms.add(new Classroom("정보공학관", "918", 35));
        schedules.add(new LectureSchedule("L-1001", "정보공학관", "912", "월", 1, 3, "소프트웨어공학", "박교수"));
        schedules.add(new LectureSchedule("L-1002", "정보공학관", "913", "화", 4, 6, "데이터베이스", "정교수"));
        schedules.add(new LectureSchedule("L-1003", "정보공학관", "914", "수", 2, 4, "운영체제", "한교수"));
        save();
    }

    private void load() throws IOException {
        load(Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    private void load(List<String> lines) {
        users.clear(); classrooms.clear(); schedules.clear(); reservations.clear(); notifications.clear();
        for (String line : lines) {
            if (line.isBlank()) continue;
            String[] f = line.split("\t", -1);
            switch (f[0]) {
                case "USER" -> users.add(new User(f[1], f[2], f[3], UserRole.valueOf(f[4])));
                case "CLASSROOM" -> classrooms.add(new Classroom(f[1], f[2], Integer.parseInt(f[3])));
                case "LECTURE" -> schedules.add(new LectureSchedule(f[1], f[2], f[3], f[4], Integer.parseInt(f[5]), Integer.parseInt(f[6]), f[7], f[8]));
                case "RESERVATION" -> reservations.add(Reservation.builder()
                        .reservationId(f[1]).requesterId(f[2]).requesterRole(UserRole.valueOf(f[3])).buildingId(f[4]).roomId(f[5])
                        .date(LocalDate.parse(f[6])).dayOfWeek(f[7]).startPeriod(Integer.parseInt(f[8])).endPeriod(Integer.parseInt(f[9]))
                        .purpose(f[10]).participantCount(Integer.parseInt(f[11])).companions(f[12]).status(ReservationStatus.valueOf(f[13]))
                        .reason(f.length > 14 ? f[14] : "").build());
                case "NOTIFICATION" -> notifications.add(new Notification(f[1], f[2], f[3], Boolean.parseBoolean(f[4])));
                default -> { }
            }
        }
    }
}
