package com.vg7.messenger.model;

import com.google.firebase.Timestamp;

import java.util.List;

// Інтерфейс для базової моделі чату
public interface BaseChatroomModel {
    // Метод для отримання типу чату
    String getType();

    // Метод для отримання останнього повідомлення
    String getLastMessage();

    // Метод для отримання ID відправника останнього повідомлення
    String getLastMessageSenderId();

    // Метод для отримання часової позначки останнього повідомлення
    Timestamp getLastMessageTimestamp();

    // Метод для отримання списку ID користувачів у чаті
    List<String> getUserIds();

    // Метод для отримання ID чату
    String getChatroomId();
}
