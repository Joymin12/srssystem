package com.se03.repository;

import com.se03.model.LectureSchedule;

import java.util.List;

public interface LectureScheduleRepository {
    List<LectureSchedule> findByRoom(String roomId);
    List<LectureSchedule> findAll();
    void save(LectureSchedule lectureSchedule);
    void delete(String scheduleId);
}
