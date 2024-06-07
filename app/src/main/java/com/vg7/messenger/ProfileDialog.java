package com.vg7.messenger;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

public class ProfileDialog extends DialogFragment {

    // Оголошення змінних для елементів інтерфейсу
    private ImageView imageUri;
    private TextView other_name, other_number, other_status, other_hide_phone_number_title;
    private Button btnClose;
    private UserModel model;

    // Конструктор для передачі даних
    public ProfileDialog (UserModel model) {
        this.model = model;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Створення діалогового вікна з використанням AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_chat_open_profile, null);

        // Ініціалізація елементів інтерфейсу
        imageUri = view.findViewById(R.id.other_show_profile_image);
        other_name = view.findViewById(R.id.other_show_profile_username);
        other_number = view.findViewById(R.id.other_show_profile_phone_number);
        other_status = view.findViewById(R.id.other_show_profile_status);
        other_hide_phone_number_title = view.findViewById(R.id.other_show_profile_phone_number_title);
        btnClose = view.findViewById(R.id.closeBtn);

        loadProfilePicture();

        // Встановлення тексту для текстових полів
        other_name.setText(model.getUsername());
        if (model.getHideNumberValue()) {
            other_number.setText("");
            other_hide_phone_number_title.setVisibility(View.GONE);
            other_number.setVisibility(View.GONE);
        } else {
            other_number.setText(model.getPhone());
            other_hide_phone_number_title.setVisibility(View.VISIBLE);
            other_number.setVisibility(View.VISIBLE);
        }
        other_status.setText(model.getStatus());

        // Додавання обробника подій для кнопки закриття
        btnClose.setOnClickListener(v -> { dismiss(); });

        // Додати слухач змін для оновлення даних співрозмовника у реальному часі
        FirebaseUtil.getUserDocumentReference(model.getUserId()).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("ProfileDialog", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                UserModel updatedUser = snapshot.toObject(UserModel.class);
                if (updatedUser != null) {
                    model = updatedUser;
                    loadProfilePicture();
                    other_name.setText(model.getUsername());
                    if (model.getHideNumberValue()) {
                        other_number.setText("");
                        other_hide_phone_number_title.setVisibility(View.GONE);
                        other_number.setVisibility(View.GONE);
                    } else {
                        other_number.setText(model.getPhone());
                        other_hide_phone_number_title.setVisibility(View.VISIBLE);
                        other_number.setVisibility(View.VISIBLE);
                    }
                    if (model.getStatus().trim().isEmpty())
                        other_status.setText("—");
                    else
                        other_status.setText(model.getStatus());
                }
            } else {
                Log.d("ProfileDialog", "Current data: null");
            }
        });

        // Встановлення користувацького виду для діалогу
        builder.setView(view);

        return builder.create();
    }

    private void loadProfilePicture() {
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Uri uri = t.getResult();
                        // Використовуємо метод requireActivity().runOnUiThread для оновлення UI у головному потоці
                        requireActivity().runOnUiThread(() -> {
                            // Очищаємо існуючий кеш для imageView, щоб гарантувати завантаження останнього зображення
                            Glide.with(requireContext()).clear(imageUri);
                            AndroidUtil.setProfilePic(requireContext(), uri, imageUri);
                        });
                    }
                });
    }
}
