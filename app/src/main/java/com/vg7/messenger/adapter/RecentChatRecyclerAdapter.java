package com.vg7.messenger.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vg7.messenger.ChatActivity;
import com.vg7.messenger.R;
import com.vg7.messenger.model.ChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    // Конструктор адаптера останніх чатів
    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    // Метод для прив'язки даних до представлення RecyclerView
    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        // Отримати інформацію про користувача чату
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                        // Отримати модель користувача
                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                        // Отримати посилання на профільне зображення
                        FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful()) {
                                        Uri uri = t.getResult();
                                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                    }
                                });

                        // Встановити інформацію про користувача та останнє повідомлення
                        holder.usernameText.setText(otherUserModel.getUsername());
                        if (lastMessageSentByMe)
                            holder.lastMessageText.setText(holder.itemView.getContext().getString(R.string.you) + ": " + model.getLastMessage());
                        else
                            holder.lastMessageText.setText(model.getLastMessage());
                        holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                        // Обробка натискання на елемент
                        holder.itemView.setOnClickListener(v -> {
                            // Перехід до активності чату
                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    }
                });
    }

    // Метод для створення нового ViewHolder
    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Заповнити макет для рядка останніх чатів
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    public void updateChatroom(ChatroomModel updatedChatroom) {
        // Знайдіть позицію чату у списку за його ідентифікатором
        int index = -1;
        for (int i = 0; i < getSnapshots().size(); i++) {
            if (getItem(i).getChatroomId().equals(updatedChatroom.getChatroomId())) {
                index = i;
                break;
            }
        }

        // Оновіть дані чату у списку
        if (index != -1) {
            getSnapshots().getSnapshot(index).getReference().set(updatedChatroom);
        }
    }


    // ViewHolder для рядків останніх чатів
    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        // Конструктор ViewHolder
        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            // Знайти всі елементи макету
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
