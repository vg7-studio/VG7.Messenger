package com.vg7.messenger.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class ChatroomModel implements BaseChatroomModel {
    String chatroomId; // Ідентифікатор чату
    List<String> userIds; // Список ідентифікаторів користувачів у чаті
    Timestamp lastMessageTimestamp; // Мітка часу останнього повідомлення
    String lastMessageSenderId; // Ідентифікатор відправника останнього повідомлення
    String lastMessage; // Останнє повідомлення
    String type; // Тип чату

    // Конструктор за замовчуванням
    public ChatroomModel() {
    }

    // Конструктор для чату з вказаною інформацією
    public ChatroomModel(String chatroomId, List<String> userIds, Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
        this.type = "private";
    }

    // Методи доступу до полів класу

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
