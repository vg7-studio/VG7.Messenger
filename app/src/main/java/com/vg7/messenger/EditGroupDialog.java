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
import com.vg7.messenger.utils.FirebaseUtil;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EditGroupDialog extends DialogFragment {

    GroupChatroomModel model;

    ImageView groupImageView;
    EditText groupNameEditText;
    Button editGroupButton;
    ProgressBar editGroupProgressBar;

    Uri groupImageUri;

    FirebaseFirestore db;
    FirebaseStorage storage;
    FirebaseAuth auth;

    ActivityResultLauncher<Intent> imagePickLauncher;

    public EditGroupDialog(GroupChatroomModel model) {
        this.model = model;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_group_edit, null);

        groupImageView = view.findViewById(R.id.edit_group_image_view);
        groupNameEditText = view.findViewById(R.id.group_name);
        editGroupButton = view.findViewById(R.id.save_group_btn);
        editGroupProgressBar = view.findViewById(R.id.edit_group_progress_bar);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        groupImageUri = result.getData().getData();
                        AndroidUtil.setGroupPic(requireContext(), groupImageUri, groupImageView);
                    }
                }
        );

        loadGroupData();

        groupImageView.setOnClickListener(v -> openFileChooser());
        editGroupButton.setOnClickListener(v -> saveChanges());
        editGroupProgressBar.setVisibility(View.GONE);

        view.findViewById(R.id.back_btn).setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void loadGroupData() {
        groupNameEditText.setText(model.getGroupName());

        // Загрузка аватарки группового чата, если есть
        if (model.getGroupImageUrl() != null && !model.getGroupImageUrl().isEmpty()) {
            Uri uri = Uri.parse(model.getGroupImageUrl());
            AndroidUtil.setGroupPic(requireContext(), uri, groupImageView);
        }
    }

    private void saveChanges() {
        String newGroupName = groupNameEditText.getText().toString().trim();

        if (newGroupName.isEmpty()) {
            groupNameEditText.setError(getString(R.string.group_name_is_required));
            return;
        } else if (newGroupName.length() < 4) {
            groupNameEditText.setError(getString(R.string.group_name_is_required2));
            groupNameEditText.requestFocus();
            return;
        }

        // Показать индикатор загрузки
        editGroupButton.setVisibility(View.GONE);
        editGroupProgressBar.setVisibility(View.VISIBLE);

        // Обновить название группы в модели
        model.setGroupName(newGroupName);

        // Если выбрана новая аватарка группы, загрузить ее и обновить URL аватарки в модели
        if (groupImageUri != null) {
            uploadGroupImage();
        } else {
            // Если не выбрана новая аватарка, сохранить изменения в Firestore
            updateGroupData(null);
        }
    }

    private void uploadGroupImage() {
        StorageReference storageRef = storage.getReference("group_pic/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(groupImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> updateGroupData(uri.toString())))
                .addOnFailureListener(e -> {
                    editGroupProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGroupData(@Nullable String imageUrl) {

        if (imageUrl != null) {
            model.setGroupImageUrl(imageUrl);
        }

        // Обновление данных группы в Firestore
        db.collection("chatrooms").document(model.getChatroomId())
                .set(model)
                .addOnSuccessListener(aVoid -> {
                    // Успешно сохранено: закрыть диалог и показать сообщение об успехе
                    editGroupProgressBar.setVisibility(View.GONE);
                    AndroidUtil.showToast(getContext(), getString(R.string.updated_successfully));
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    // Ошибка при сохранении: показать сообщение об ошибке
                    editGroupProgressBar.setVisibility(View.GONE);
                    AndroidUtil.showToast(getContext(), getString(R.string.updated_failed));
                });
    }


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
