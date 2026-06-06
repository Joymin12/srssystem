package com.se03.repository;

import com.se03.model.User;

public interface UserRepository {
    User findById(String userId);
}
