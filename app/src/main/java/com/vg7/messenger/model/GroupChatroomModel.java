package com.vg7.messenger.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class GroupChatroomModel implements BaseChatroomModel {
    String groupId; // Ідентифікатор групи
    List<String> userIds; // Список ідентифікаторів користувачів у групі
    Timestamp lastMessageTimestamp; // Мітка часу останнього повідомлення
    String lastMessageSenderId; // Ідентифікатор відправника останнього повідомлення
    String lastMessage; // Останнє повідомлення
    String type; // Тип чату
    String groupName; // Назва групи
    String groupImageUrl; // Зображення групи

    // Конструктор за замовчуванням
    public GroupChatroomModel() {
    }

    // Конструктор для чату з вказаною інформацією
    public GroupChatroomModel(String groupId, List<String> userIds, Timestamp lastMessageTimestamp, String lastMessageSenderId, String groupName) {
        this.groupId = groupId;
        this.userIds = userIds;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
        this.type = "group";
        this.groupName = groupName;
        this.groupImageUrl = "";
    }

    // Методи доступу до полів класу

    public String getChatroomId() {
        return groupId;
    }

    public void setChatroomId(String groupId) {
        this.groupId = groupId;
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

    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public void setGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}