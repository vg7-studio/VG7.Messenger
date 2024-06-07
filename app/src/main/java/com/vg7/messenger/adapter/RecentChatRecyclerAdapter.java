package com.vg7.messenger.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.vg7.messenger.ChatActivity;
import com.vg7.messenger.GroupChatActivity;
import com.vg7.messenger.R;
import com.vg7.messenger.model.BaseChatroomModel;
import com.vg7.messenger.model.ChatroomModel;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<BaseChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    private Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<BaseChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull BaseChatroomModel model) {
        if (model instanceof ChatroomModel) {
            Log.d("RecentChatRecyclerAdapter", "Binding private chat");
            bindPrivateChat(holder, (ChatroomModel) model);
        } else if (model instanceof GroupChatroomModel) {
            Log.d("RecentChatRecyclerAdapter", "Binding group chat");
            bindGroupChat(holder, (GroupChatroomModel) model);
        } else {
            Log.e("RecentChatRecyclerAdapter", "Unknown model type: " + model.getClass().getName());
        }
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    private void bindPrivateChat(@NonNull ChatroomModelViewHolder holder, @NonNull ChatroomModel model) {
        try {
            FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            UserModel otherUserModel = task.getResult().toObject(UserModel.class);
                            if (otherUserModel != null) {
                                boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                                FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                        .addOnCompleteListener(t -> {
                                            if (t.isSuccessful() && t.getResult() != null) {
                                                Uri uri = t.getResult();
                                                AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                            } else {
                                                Log.e("RecentChatRecyclerAdapter", "Failed to get profile picture URL");
                                            }
                                        });

                                holder.usernameText.setText(otherUserModel.getUsername());
                                holder.lastMessageText.setText(lastMessageSentByMe
                                        ? holder.itemView.getContext().getString(R.string.you) + ": " + model.getLastMessage()
                                        : model.getLastMessage());
                                holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                                holder.itemView.setOnClickListener(v -> {
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("source", "MainActivity");
                                    context.startActivity(intent);
                                });
                            } else {
                                Log.e("RecentChatRecyclerAdapter", "UserModel is null");
                            }
                        } else {
                            Log.e("RecentChatRecyclerAdapter", "Failed to get user data", task.getException());
                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e("RecentChatRecyclerAdapter", "Error in getOtherUserFromChatroom: " + e.getMessage());
        }
    }

    private void bindGroupChat(@NonNull ChatroomModelViewHolder holder, @NonNull GroupChatroomModel model) {
        try {
            Log.d("GroupModel", model.getGroupName());
            holder.usernameText.setText(model.getGroupName());
            holder.lastMessageText.setText(model.getLastMessage() != null ? model.getLastMessage() : "No messages");
            holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

            if (model.getGroupImageUrl() != null && !model.getGroupImageUrl().isEmpty()) {
                Uri uri = Uri.parse(model.getGroupImageUrl());
                AndroidUtil.setProfilePic(context, uri, holder.profilePic);
            } else {
                holder.profilePic.setImageResource(R.drawable.group_icon);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("chatroomId", model.getChatroomId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        } catch (Exception e) {
            Log.e("RecentChatRecyclerAdapter", "Error in bindGroupChat: " + e.getMessage());
        }
    }

    public void updateChatroom(BaseChatroomModel updatedChatroom) {
        int index = -1;
        for (int i = 0; i < getSnapshots().size(); i++) {
            if (getItem(i).getChatroomId().equals(updatedChatroom.getChatroomId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            getSnapshots().getSnapshot(index).getReference().set(updatedChatroom);
        }
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
