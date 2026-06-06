package com.se03.repository;

import com.se03.model.Classroom;

import java.util.List;

public interface ClassroomRepository {
    Classroom findById(String roomId);
    List<Classroom> findByBuilding(String buildingId);
    List<Classroom> findAll();
    void save(Classroom classroom);
    void delete(String roomId);
}
