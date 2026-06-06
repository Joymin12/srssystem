package com.se03.repository;

import com.se03.model.User;
import com.se03.repository.UserRepository;

public final class FileUserRepository implements UserRepository {
    private final FileDatabase db;
    public FileUserRepository(FileDatabase db) { this.db = db; }
    @Override public User findById(String userId) {
        return db.users.stream().filter(u -> u.userId().equals(userId)).findFirst().orElse(null);
    }
}
