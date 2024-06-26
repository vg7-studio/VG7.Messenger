package com.vg7.messenger.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vg7.messenger.GroupDialog;
import com.vg7.messenger.R;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    Context context; // Контекст для доступу до ресурсів та інших компонентів
    List<UserModel> adminList; // Список адміністраторів
    GroupDialog dialog; // Діалог для відображення профілю користувача

    // Конструктор для ініціалізації адаптера з контекстом та списком адміністраторів
    public AdminAdapter(Context context, List<UserModel> adminList) {
        this.context = context;
        this.adminList = adminList;
    }

    // Конструктор для ініціалізації адаптера з контекстом, діалогом та списком адміністраторів
    public AdminAdapter(Context context, GroupDialog dialog, List<UserModel> adminList) {
        this.context = context;
        this.dialog = dialog;
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Створення нового виду (View) для елемента списку
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_recycler_row, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        // Отримання адміністратора на поточній позиції
        UserModel admin = adminList.get(position);
        // Встановлення імені адміністратора
        holder.adminName.setText(admin.getUsername());

        // Встановлення номера телефону або приховування його
        if (!admin.getHideNumberValue()) {
            holder.adminPhoneNumber.setText(admin.getPhone());
        } else {
            holder.adminPhoneNumber.setText("*************");
        }

        // Обробка натискання на елемент списку
        holder.itemView.setOnClickListener(v -> {
            dialog.openUserProfile(admin); // Відкриття профілю користувача у діалозі
        });

        // Завантаження та встановлення профільної картинки адміністратора
        FirebaseUtil.getOtherProfilePicStorageRef(admin.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful() && t.getResult() != null) {
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.adminProfilePic);
                    } else {
                        Log.e("RecentChatRecyclerAdapter", "Failed to get profile picture URL");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return adminList.size(); // Повернення кількості елементів у списку адміністраторів
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        ImageView adminProfilePic; // Зображення профілю адміністратора
        TextView adminName; // Ім'я адміністратора
        TextView adminPhoneNumber; // Номер телефону адміністратора

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            adminProfilePic = itemView.findViewById(R.id.profile_pic_image_view); // Ініціалізація зображення профілю
            adminName = itemView.findViewById(R.id.user_name_text); // Ініціалізація імені адміністратора
            adminPhoneNumber = itemView.findViewById(R.id.phone_text); // Ініціалізація номера телефону адміністратора
        }
    }
}
