package com.se03.model;

public record Notification(String notificationId, String userId, String message, boolean read) {
}
