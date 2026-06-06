package com.se03.service;

import com.se03.repository.UserRepository;
import com.se03.model.User;

public final class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String userId, String password) {
        User user = userRepository.findById(userId);
        if (user == null || !user.password().equals(password)) {
            return null;
        }
        return user;
    }
}
