package com.se03.model;

public record User(String userId, String password, String name, UserRole role) {
}
