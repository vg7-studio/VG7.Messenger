package com.vg7.messenger;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

public class ProfileDialog extends DialogFragment {

    // Оголошення змінних для елементів інтерфейсу
    private ImageView imageUri;
    private TextView other_name, other_number, other_status;
    private Button btnClose;
    private String textImageUri, textName, textNumber, textStatus;

    // Конструктор для передачі даних
    public ProfileDialog (String textImageUri, String textName, String textNumber, String textStatus) {
        this.textImageUri = textImageUri;
        this.textName = textName;
        this.textNumber = textNumber;
        this.textStatus = textStatus;
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
        btnClose = view.findViewById(R.id.closeBtn);

        // Завантаження URL зображення профілю з Firebase і встановлення його в ImageView
        FirebaseUtil.getOtherProfilePicStorageRef(textImageUri).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri  = t.getResult();
                        AndroidUtil.setProfilePic(requireContext(),uri,imageUri);
                    }
                });

        // Встановлення тексту для текстових полів
        other_name.setText(textName);
        other_number.setText(textNumber);
        other_status.setText(textStatus);

        // Додавання обробника подій для кнопки закриття
        btnClose.setOnClickListener(v -> { dismiss(); });

        // Встановлення користувацького виду для діалогу
        builder.setView(view);

        return builder.create();
    }
}
