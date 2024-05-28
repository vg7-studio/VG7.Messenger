package com.vg7.messenger.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.vg7.messenger.R;
import com.vg7.messenger.model.ChatMessageModel;
import com.vg7.messenger.utils.FirebaseUtil;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    private Context mContext;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
        super(options);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        Log.i("ChatRecyclerAdapter", "Binding view at position: " + position);

        // Скрыть все элементы перед настройкой
        holder.leftChatTextview.setVisibility(View.GONE);
        holder.leftChatImageview.setVisibility(View.GONE);
        holder.leftChatVideoview.setVisibility(View.GONE);
        holder.rightChatTextview.setVisibility(View.GONE);
        holder.rightChatImageview.setVisibility(View.GONE);
        holder.rightChatVideoview.setVisibility(View.GONE);

        boolean isSender = model.getSenderId().equals(FirebaseUtil.currentUserId());

        LinearLayout activeLayout = isSender ? holder.rightChatLayout : holder.leftChatLayout;
        TextView activeTextView = isSender ? holder.rightChatTextview : holder.leftChatTextview;
        ImageView activeImageView = isSender ? holder.rightChatImageview : holder.leftChatImageview;
        VideoView activeVideoView = isSender ? holder.rightChatVideoview : holder.leftChatVideoview;

        if (isSender) {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
        } else {
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
        }

        String messageType = model.getMessageType();
        if ("text".equals(messageType)) {
            activeLayout.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(isSender ? R.color.chat_color_receiver : R.color.chat_color_sender)));
            activeTextView.setText(model.getMessage());
            activeTextView.setVisibility(View.VISIBLE);
        } else if ("image".equals(messageType)) {
            activeLayout.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            Uri imageUri = Uri.parse(model.getMediaUrl());
            Log.d("ChatRecyclerAdapter", "Loading image: " + imageUri.toString());
            Glide.with(mContext)
                    .load(imageUri)
                    .into(activeImageView);
            activeImageView.setVisibility(View.VISIBLE);
        } else if ("video".equals(messageType)) {
            activeLayout.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            Uri videoUri = Uri.parse(model.getMediaUrl());
            Log.d("ChatRecyclerAdapter", "Loading video: " + videoUri.toString());
            activeVideoView.setVideoURI(videoUri);
            activeVideoView.setVisibility(View.VISIBLE);
            activeVideoView.start();
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextview, rightChatTextview;
        ImageView leftChatImageview, rightChatImageview;
        VideoView leftChatVideoview, rightChatVideoview;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatImageview = itemView.findViewById(R.id.left_chat_imageview);
            rightChatImageview = itemView.findViewById(R.id.right_chat_imageview);
            leftChatVideoview = itemView.findViewById(R.id.left_chat_videoview);
            rightChatVideoview = itemView.findViewById(R.id.right_chat_videoview);
        }
    }
}