package com.vg7.messenger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.utils.AndroidUtil;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EditGroupDialog extends DialogFragment {

    GroupChatroomModel model;

    ImageView groupImageView; // Зображення групи
    EditText groupNameEditText; // Поле введення нового імені групи
    Button editGroupButton; // Кнопка збереження змін
    ProgressBar editGroupProgressBar; // Прогрес-бар під час збереження змін

    Uri groupImageUri; // URI зображення групи

    FirebaseFirestore db; // Об'єкт бази даних Firestore
    FirebaseStorage storage; // Об'єкт зберігання Firebase
    FirebaseAuth auth; // Об'єкт аутентифікації Firebase

    ActivityResultLauncher<Intent> imagePickLauncher; // Менеджер результатів запиту на вибір зображення

    // Конструктор класу, приймає модель групи
    public EditGroupDialog(GroupChatroomModel model) {
        this.model = model;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_group_edit, null);

        // Ініціалізація елементів інтерфейсу
        groupImageView = view.findViewById(R.id.edit_group_image_view);
        groupNameEditText = view.findViewById(R.id.group_name);
        editGroupButton = view.findViewById(R.id.save_group_btn);
        editGroupProgressBar = view.findViewById(R.id.edit_group_progress_bar);

        // Ініціалізація об'єктів Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        // Ініціалізація менеджера результатів запиту на вибір зображення
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        groupImageUri = result.getData().getData();
                        AndroidUtil.setGroupPic(requireContext(), groupImageUri, groupImageView);
                    }
                }
        );

        // Завантаження даних групи
        loadGroupData();

        // Обробники подій кнопок
        groupImageView.setOnClickListener(v -> openFileChooser());
        editGroupButton.setOnClickListener(v -> saveChanges());
        editGroupProgressBar.setVisibility(View.GONE);

        // Обробник події для кнопки "Назад"
        view.findViewById(R.id.back_btn).setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    // Метод для завантаження даних групи
    private void loadGroupData() {
        groupNameEditText.setText(model.getGroupName());

        // Завантаження зображення групи, якщо воно існує
        if (model.getGroupImageUrl() != null && !model.getGroupImageUrl().isEmpty()) {
            Uri uri = Uri.parse(model.getGroupImageUrl());
            AndroidUtil.setGroupPic(requireContext(), uri, groupImageView);
        }
    }

    // Метод для збереження змін
    private void saveChanges() {
        String newGroupName = groupNameEditText.getText().toString().trim();

        if (newGroupName.isEmpty()) {
            groupNameEditText.setError(getString(R.string.group_name_is_required)); // Встановлення помилки для поля вводу
            return;
        } else if (newGroupName.length() < 4) {
            groupNameEditText.setError(getString(R.string.group_name_is_required2)); // Встановлення помилки для поля вводу
            groupNameEditText.requestFocus();
            return;
        }

        // Показ прогресу під час збереження
        editGroupButton.setVisibility(View.GONE);
        editGroupProgressBar.setVisibility(View.VISIBLE);

        // Оновлення назви группи в моделі
        model.setGroupName(newGroupName);

        // Якщо вибрано нове зображення групи, відправити його на сервер і оновити URL
        if (groupImageUri != null) {
            uploadGroupImage();
        } else {
            // Якщо нове зображення не вибрано, оновити дані групи в Firestore
            updateGroupData(null);
        }
    }

    // Метод для завантаження зображення групи на сервер Firebase Storage
    private void uploadGroupImage() {
        StorageReference storageRef = storage.getReference("group_pic/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(groupImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> updateGroupData(uri.toString())))
                .addOnFailureListener(e -> {
                    editGroupProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show();
                });
    }

    // Метод для оновлення даних групи в базі Firestore
    private void updateGroupData(@Nullable String imageUrl) {

        if (imageUrl != null) {
            model.setGroupImageUrl(imageUrl);
        }

        // Оновлення даних групи в Firestore
        db.collection("chatrooms").document(model.getChatroomId())
                .set(model)
                .addOnSuccessListener(aVoid -> {
                    // Успішно збережено: закрити діалог і показати повідомлення про успіх
                    editGroupProgressBar.setVisibility(View.GONE);
                    AndroidUtil.showToast(getContext(), getString(R.string.updated_successfully));
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    // Помилка під час збереження: показати повідомлення про помилку
                    editGroupProgressBar.setVisibility(View.GONE);
                    AndroidUtil.showToast(getContext(), getString(R.string.updated_failed));
                });
    }


    // Метод для відкриття вибору зображення
    private void openFileChooser() {
        ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        imagePickLauncher.launch(intent);
                        return null;
                    }
                });
    }
}
