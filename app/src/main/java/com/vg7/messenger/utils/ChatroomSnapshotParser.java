package com.vg7.messenger.utils;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.vg7.messenger.model.BaseChatroomModel;
import com.vg7.messenger.model.ChatroomModel;
import com.vg7.messenger.model.GroupChatroomModel;

import java.util.Objects;

public class ChatroomSnapshotParser implements SnapshotParser<BaseChatroomModel> {

    @NonNull
    @Override
    public BaseChatroomModel parseSnapshot(DocumentSnapshot snapshot) {
        String type = snapshot.getString("type");
        if ("group".equals(type)) {
            return Objects.requireNonNull(snapshot.toObject(GroupChatroomModel.class));
        } else {
            return Objects.requireNonNull(snapshot.toObject(ChatroomModel.class));
        }
    }
}

