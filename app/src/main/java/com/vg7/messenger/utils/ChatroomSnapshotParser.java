package com.vg7.messenger.utils;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.vg7.messenger.model.BaseChatroomModel;
import com.vg7.messenger.model.ChatroomModel;
import com.vg7.messenger.model.GroupChatroomModel;

import java.util.Objects;

// Клас для розбору знімка чату
public class ChatroomSnapshotParser implements SnapshotParser<BaseChatroomModel> {

    // Перевизначений метод для розбору знімка
    @NonNull
    @Override
    public BaseChatroomModel parseSnapshot(DocumentSnapshot snapshot) {
        // Отримати тип чату зі знімка
        String type = snapshot.getString("type");

        // Якщо це груповий чат, повернути об'єкт типу GroupChatroomModel
        if ("group".equals(type)) {
            return Objects.requireNonNull(snapshot.toObject(GroupChatroomModel.class));
        }
        // Якщо це особистий чат, повернути об'єкт типу ChatroomModel
        else {
            return Objects.requireNonNull(snapshot.toObject(ChatroomModel.class));
        }
    }
}


