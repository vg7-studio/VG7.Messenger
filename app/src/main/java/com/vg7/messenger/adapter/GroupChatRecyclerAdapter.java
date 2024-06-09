package com.vg7.messenger.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vg7.messenger.GroupChatActivity;
import com.vg7.messenger.R;
import com.vg7.messenger.model.GroupChatMessageModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

public class GroupChatRecyclerAdapter extends FirestoreRecyclerAdapter<GroupChatMessageModel, GroupChatRecyclerAdapter.GroupChatViewHolder> {

    private Context context;
    private GroupChatActivity groupChatActivity;

    public GroupChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<GroupChatMessageModel> options, Context context, GroupChatActivity groupChatActivity) {
        super(options);
        this.context = context;
        this.groupChatActivity = groupChatActivity;
    }

    @Override
    protected void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position, @NonNull GroupChatMessageModel model) {
        Log.i("GroupChatMessageModel", "Прив'язка виду на позиції: " + position);

        // Приховати всі елементи повідомлення перед тим, як вони будуть заповнені даними
        holder.leftChatUsername.setVisibility(View.GONE);
        holder.leftChatTextView.setVisibility(View.GONE);
        holder.leftChatImageView.setVisibility(View.GONE);
        holder.leftChatVideoView.setVisibility(View.GONE);
        holder.leftChatFileLayout.setVisibility(View.GONE);
        holder.leftChatFileTextView.setVisibility(View.GONE);
        holder.rightChatTextView.setVisibility(View.GONE);
        holder.rightChatImageView.setVisibility(View.GONE);
        holder.rightChatVideoView.setVisibility(View.GONE);
        holder.rightChatFileLayout.setVisibility(View.GONE);
        holder.rightChatFileTextView.setVisibility(View.GONE);

        DocumentReference senderDocRef = FirebaseFirestore.getInstance().collection("chatrooms")
                .document(groupChatActivity.groupId)
                .collection("members")
                .document(model.getSenderId());

        // Визначити відправника повідомлення
        boolean isSender = model.getSenderId().equals(FirebaseUtil.currentUserId());

        // Визначити активний макет для повідомлення
        LinearLayout activeLayout = isSender ? holder.rightChatLayout : holder.leftChatLayout;
        TextView activeTextView = isSender ? holder.rightChatTextView : holder.leftChatTextView;
        ImageView activeImageView = isSender ? holder.rightChatImageView : holder.leftChatImageView;
        VideoView activeVideoView = isSender ? holder.rightChatVideoView : holder.leftChatVideoView;
        LinearLayout activeFileLayout = isSender ? holder.rightChatFileLayout : holder.leftChatFileLayout;
        TextView activeFileText = isSender ? holder.rightChatFileTextView : holder.leftChatFileTextView;

        // Відобразити активний макет відповідно до відправника
        if (isSender) {
            holder.leftChatUsername.setVisibility(View.GONE);
            holder.leftChatUsername.setText("");
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
        } else {
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatUsername.setVisibility(View.VISIBLE);
            // Визначити ім'я відправника повідомлення
            senderDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String senderName = documentSnapshot.getString("username");
                    if (senderName != null) {
                        Log.d("Username", "Username: " + senderName);
                        holder.leftChatUsername.setText(senderName);
                    } else {
                        Log.d("Username", "Username not found");
                    }
                }
            });
            holder.leftChatLayout.setVisibility(View.VISIBLE);
        }

        // Обробити різні типи повідомлень
        String messageType = model.getMessageType();
        if ("text".equals(messageType)) {
            // Відобразити текстове повідомлення
            activeLayout.setBackgroundTintList(ContextCompat.getColorStateList(context, isSender ? R.color.chat_color_receiver : R.color.chat_color_sender));
            activeTextView.setText(model.getMessage());
            activeTextView.setVisibility(View.VISIBLE);
        } else if ("image".equals(messageType)) {
            // Відобразити зображення
            activeLayout.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            Uri imageUri = Uri.parse(model.getMediaUrl());
            Log.d("ChatRecyclerAdapter", "Завантаження зображення: " + imageUri.toString());
            Glide.with(context)
                    .load(imageUri)
                    .into(activeImageView);
            activeImageView.setVisibility(View.VISIBLE);

            // Обробити натискання на зображення
            activeImageView.setOnClickListener(v -> groupChatActivity.openFullscreenImage(imageUri));
        } else if ("video".equals(messageType)) {
            // Відобразити відео
            activeLayout.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            Uri videoUri = Uri.parse(model.getMediaUrl());
            Log.d("ChatRecyclerAdapter", "Завантаження відео: " + videoUri.toString());
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            if (thumbnail != null) {
                Drawable drawable = new BitmapDrawable(context.getResources(), thumbnail);
                activeVideoView.setBackground(drawable);
            }
            activeVideoView.setVideoURI(videoUri);
            activeVideoView.setVisibility(View.VISIBLE);

            // Обробити натискання на відео
            activeVideoView.setOnClickListener(v -> groupChatActivity.openVideoPlayer(videoUri));
        } else if ("file".equals(messageType)) {
            // Відобразити файл
            activeLayout.setBackgroundTintList(ContextCompat.getColorStateList(context, isSender ? R.color.chat_color_receiver : R.color.chat_color_sender));
            activeFileText.setText(model.getMessage());
            activeFileLayout.setVisibility(View.VISIBLE);
            activeFileText.setVisibility(View.VISIBLE);

            // Обробити натискання на файл
            activeFileLayout.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(model.getMediaUrl()), "*/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_chat_message_recycler_row, parent, false);
        return new GroupChatViewHolder(view);
    }

    static class GroupChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout;
        TextView leftChatUsername;
        ImageView leftChatImageView;
        VideoView leftChatVideoView;
        LinearLayout leftChatFileLayout;
        TextView leftChatFileTextView;
        TextView leftChatTextView;

        LinearLayout rightChatLayout;
        ImageView rightChatImageView;
        VideoView rightChatVideoView;
        LinearLayout rightChatFileLayout;
        TextView rightChatFileTextView;
        TextView rightChatTextView;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            leftChatUsername = itemView.findViewById(R.id.left_chat_username);
            leftChatImageView = itemView.findViewById(R.id.left_chat_imageview);
            leftChatVideoView = itemView.findViewById(R.id.left_chat_videoview);
            leftChatFileLayout = itemView.findViewById(R.id.left_chat_file);
            leftChatFileTextView = itemView.findViewById(R.id.left_chat_file_textview);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);

            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            rightChatImageView = itemView.findViewById(R.id.right_chat_imageview);
            rightChatVideoView = itemView.findViewById(R.id.right_chat_videoview);
            rightChatFileLayout = itemView.findViewById(R.id.right_chat_file);
            rightChatFileTextView = itemView.findViewById(R.id.right_chat_file_textview);
            rightChatTextView = itemView.findViewById(R.id.right_chat_textview);
        }
    }
}
