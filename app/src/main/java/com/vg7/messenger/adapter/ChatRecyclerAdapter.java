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
import com.vg7.messenger.ChatActivity;
import com.vg7.messenger.R;
import com.vg7.messenger.VideoPlayerDialog;
import com.vg7.messenger.model.ChatMessageModel;
import com.vg7.messenger.utils.FirebaseUtil;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    private Context mContext;
    private ChatActivity mChatActivity;

    // Конструктор адаптера чату
    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context, ChatActivity chatActivity) {
        super(options);
        mContext = context;
        mChatActivity = chatActivity;
    }

    // Метод для прив'язки даних до представлення RecyclerView
    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        Log.i("ChatRecyclerAdapter", "Прив'язка виду на позиції: " + position);

        // Приховати всі елементи повідомлення перед тим, як вони будуть заповнені даними
        holder.leftChatTextview.setVisibility(View.GONE);
        holder.leftChatImageview.setVisibility(View.GONE);
        holder.leftChatVideoview.setVisibility(View.GONE);
        holder.leftChatFileLayout.setVisibility(View.GONE);
        holder.leftChatFileTextview.setVisibility(View.GONE);
        holder.rightChatTextview.setVisibility(View.GONE);
        holder.rightChatImageview.setVisibility(View.GONE);
        holder.rightChatVideoview.setVisibility(View.GONE);
        holder.rightChatFileLayout.setVisibility(View.GONE);
        holder.rightChatFileTextview.setVisibility(View.GONE);

        // Визначити відправника повідомлення
        boolean isSender = model.getSenderId().equals(FirebaseUtil.currentUserId());

        // Визначити активний макет для повідомлення
        LinearLayout activeLayout = isSender ? holder.rightChatLayout : holder.leftChatLayout;
        TextView activeTextView = isSender ? holder.rightChatTextview : holder.leftChatTextview;
        ImageView activeImageView = isSender ? holder.rightChatImageview : holder.leftChatImageview;
        VideoView activeVideoView = isSender ? holder.rightChatVideoview : holder.leftChatVideoview;
        LinearLayout activeFileLayout = isSender ? holder.rightChatFileLayout : holder.leftChatFileLayout;
        TextView activeFileText = isSender ? holder.rightChatFileTextview : holder.leftChatFileTextview;

        // Відобразити активний макет відповідно до відправника
        if (isSender) {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
        } else {
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
        }

        // Обробити різні типи повідомлень
        String messageType = model.getMessageType();
        if ("text".equals(messageType)) {
            // Відобразити текстове повідомлення
            activeLayout.setBackgroundTintList(ContextCompat.getColorStateList(mContext, isSender ? R.color.chat_color_receiver : R.color.chat_color_sender));
            activeTextView.setText(model.getMessage());
            activeTextView.setVisibility(View.VISIBLE);
        } else if ("image".equals(messageType)) {
            // Відобразити зображення
            activeLayout.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            Uri imageUri = Uri.parse(model.getMediaUrl());
            Log.d("ChatRecyclerAdapter", "Завантаження зображення: " + imageUri.toString());
            Glide.with(mContext)
                    .load(imageUri)
                    .into(activeImageView);
            activeImageView.setVisibility(View.VISIBLE);

            // Обробити натискання на зображення
            activeImageView.setOnClickListener(v -> mChatActivity.openFullscreenImage(imageUri));
        } else if ("video".equals(messageType)) {
            // Відобразити відео
            activeLayout.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            Uri videoUri = Uri.parse(model.getMediaUrl());
            Log.d("ChatRecyclerAdapter", "Завантаження відео: " + videoUri.toString());
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            if (thumbnail != null) {
                Drawable drawable = new BitmapDrawable(mContext.getResources(), thumbnail);
                activeVideoView.setBackground(drawable);
            }
            activeVideoView.setVideoURI(videoUri);
            activeVideoView.setVisibility(View.VISIBLE);

            // Обробити натискання на відео
            activeVideoView.setOnClickListener(v -> mChatActivity.openVideoPlayer(videoUri));
        } else if ("file".equals(messageType)) {
            // Відобразити файл
            activeLayout.setBackgroundTintList(ContextCompat.getColorStateList(mContext, isSender ? R.color.chat_color_receiver : R.color.chat_color_sender));
            activeFileText.setText(model.getMessage());
            activeFileLayout.setVisibility(View.VISIBLE);
            activeFileText.setVisibility(View.VISIBLE);

            // Обробити натискання на файл
            activeFileLayout.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(model.getMediaUrl()), "*/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            });
        }
    }

    // Створити новий ViewHolder
    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Заповнити макет для повідомлення чату
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    // ViewHolder для повідомлень чату
    class ChatModelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout, leftChatFileLayout, rightChatFileLayout;
        TextView leftChatTextview, rightChatTextview, leftChatFileTextview, rightChatFileTextview;
        ImageView leftChatImageview, rightChatImageview;
        VideoView leftChatVideoview, rightChatVideoview;

        // Конструктор ViewHolder
        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);

            // Знайти всі елементи макету
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatImageview = itemView.findViewById(R.id.left_chat_imageview);
            rightChatImageview = itemView.findViewById(R.id.right_chat_imageview);
            leftChatVideoview = itemView.findViewById(R.id.left_chat_videoview);
            rightChatVideoview = itemView.findViewById(R.id.right_chat_videoview);
            leftChatFileLayout = itemView.findViewById(R.id.left_chat_file);
            leftChatFileTextview = itemView.findViewById(R.id.left_chat_file_textview);
            rightChatFileLayout = itemView.findViewById(R.id.right_chat_file);
            rightChatFileTextview = itemView.findViewById(R.id.right_chat_file_textview);
        }
    }
}
