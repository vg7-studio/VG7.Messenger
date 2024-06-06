package com.vg7.messenger.model;

import com.google.firebase.Timestamp;

import java.util.List;

public interface BaseChatroomModel {
    String getType();
    String getLastMessage();
    String getLastMessageSenderId();
    Timestamp getLastMessageTimestamp();
    List<String> getUserIds();
    String getChatroomId();
}