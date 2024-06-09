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
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    Context context;

    // Конструктор адаптера для пошуку користувачів
    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    // Метод для прив'язки даних до представлення RecyclerView
    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        // Встановити ім'я та телефон користувача
        holder.usernameText.setText(model.getUsername());
        holder.phoneText.setText(model.getPhone());

        // Якщо це поточний користувач, ігнор
        if (model.getUserId().equals(FirebaseUtil.currentUserId())) {
            holder.itemView.setVisibility(View.GONE);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }

        // Отримати посилання на профільне зображення користувача
        getProfilePicUri(model.getUserId(), uri -> {
            if (uri != null) {
                AndroidUtil.setProfilePic(context, uri, holder.profilePic);
            }
        });

        // Обробка натискання на елемент
        holder.itemView.setOnClickListener(v -> {
            // Перехід до активності чату
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("source", "SearchUserActivity");
            context.startActivity(intent);
        });
    }

    // Метод для створення нового ViewHolder
    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Заповнити макет для рядка пошуку користувачів
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    // Метод для отримання Uri профільного зображення користувача
    private void getProfilePicUri(String userId, ProfilePicCallback callback) {
        FirebaseUtil.getOtherProfilePicStorageRef(userId).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        callback.onCallback(t.getResult());
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    public interface ProfilePicCallback {
        void onCallback(Uri uri);
    }


    // ViewHolder для рядків пошуку користувачів
    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;

        // Конструктор ViewHolder
        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            // Знайти всі елементи макету
            usernameText = itemView.findViewById(R.id.user_name_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
