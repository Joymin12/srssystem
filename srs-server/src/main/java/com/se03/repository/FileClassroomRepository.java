package com.se03.repository;

import com.se03.model.Classroom;
import com.se03.repository.ClassroomRepository;

import java.util.List;

public final class FileClassroomRepository implements ClassroomRepository {
    private final FileDatabase db;
    public FileClassroomRepository(FileDatabase db) { this.db = db; }
    @Override public Classroom findById(String roomId) {
        return db.classrooms.stream().filter(c -> c.roomId().equals(roomId)).findFirst().orElse(null);
    }
    @Override public List<Classroom> findByBuilding(String buildingId) {
        return db.classrooms.stream().filter(c -> c.buildingId().equals(buildingId)).toList();
    }
    @Override public List<Classroom> findAll() { return db.classrooms; }
    @Override public void save(Classroom classroom) {
        db.classrooms.removeIf(c -> c.roomId().equals(classroom.roomId()));
        db.classrooms.add(classroom);
        db.save();
    }
    @Override public void delete(String roomId) {
        db.classrooms.removeIf(c -> c.roomId().equals(roomId));
        db.schedules.removeIf(s -> s.roomId().equals(roomId));
        db.save();
    }
}
