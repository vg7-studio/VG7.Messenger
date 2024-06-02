package com.vg7.messenger.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String phone; // Номер телефону користувача
    private String username; // Ім'я користувача
    private String status; // Статус користувача
    private Timestamp createdTimestamp; // Мітка часу створення облікового запису
    private String userId; // Ідентифікатор користувача
    private String fcmToken; // Токен Firebase Cloud Messaging (FCM)
    private Boolean hideNumber; // Значення функції "Приховати номер телефону"

    // Конструктор за замовчуванням
    public UserModel() {
    }

    // Конструктор для користувача з вказаною інформацією
    public UserModel(String phone, String username, String status, Timestamp createdTimestamp, String userId, Boolean hideNumber) {
        this.phone = phone;
        this.username = username;
        this.status = status;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
        this.hideNumber = hideNumber;
    }

    // Методи доступу до полів класу

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Boolean getHideNumberValue() { return hideNumber; }

    public void setHideNumberValue(Boolean hideNumber) {
        this.hideNumber = hideNumber;
    }
}
