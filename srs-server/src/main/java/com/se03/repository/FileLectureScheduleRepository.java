package com.se03.repository;

import com.se03.model.LectureSchedule;
import com.se03.repository.LectureScheduleRepository;

import java.util.List;

public final class FileLectureScheduleRepository implements LectureScheduleRepository {
    private final FileDatabase db;
    public FileLectureScheduleRepository(FileDatabase db) { this.db = db; }
    @Override public List<LectureSchedule> findByRoom(String roomId) {
        return db.schedules.stream().filter(s -> s.roomId().equals(roomId)).toList();
    }
    @Override public List<LectureSchedule> findAll() { return db.schedules; }
    @Override public void save(LectureSchedule lectureSchedule) {
        db.schedules.removeIf(s -> s.scheduleId().equals(lectureSchedule.scheduleId()));
        db.schedules.add(lectureSchedule);
        db.save();
    }
    @Override public void delete(String scheduleId) {
        db.schedules.removeIf(s -> s.scheduleId().equals(scheduleId));
        db.save();
    }
}
