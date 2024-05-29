package com.vg7.messenger.model;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    private String message; // Текст повідомлення
    private String senderId; // Ідентифікатор відправника
    private Timestamp timestamp; // Мітка часу
    private String messageType; // Тип повідомлення: текст, зображення, відео
    private String mediaUrl; // URL мультимедійного вмісту

    // Конструктор за замовчуванням
    public ChatMessageModel() {
    }

    // Конструктор для текстового повідомлення
    public ChatMessageModel(String message, String senderId, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.messageType = "text"; // Задати тип текстового повідомлення
        this.mediaUrl = null;
    }

    // Конструктор для мультимедійного повідомлення
    public ChatMessageModel(String message, String senderId, Timestamp timestamp, String messageType, String messageUrl) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.messageType = messageType; // Задати тип мультимедійного повідомлення
        this.mediaUrl = messageUrl;
    }

    // Методи доступу до полів класу

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }
}
