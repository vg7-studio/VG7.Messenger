package com.vg7.messenger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class MediaSelectionDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Створення нового діалогового вікна
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Надуваємо макет для вікна вибору мультимедіа
        View view = inflater.inflate(R.layout.fragment_media_selection_dialog, null);
        builder.setView(view);

        // Знаходимо кнопки вибору мультимедіа
        ImageButton selectPhotoButton = view.findViewById(R.id.select_photo_button);
        ImageButton selectVideoButton = view.findViewById(R.id.select_video_button);
        ImageButton selectFileButton = view.findViewById(R.id.select_file_button);

        // Встановлюємо слухачів натискання на кнопки
        selectPhotoButton.setOnClickListener(v -> onSelectMedia("photo"));
        selectVideoButton.setOnClickListener(v -> onSelectMedia("video"));
        selectFileButton.setOnClickListener(v -> onSelectMedia("file"));

        return builder.create();
    }

    // Метод, який викликається при виборі мультимедіа
    private void onSelectMedia(String mediaType) {
        // Викликаємо метод onSelectMedia активності ChatActivity та закриваємо діалогове вікно
        ((ChatActivity) requireActivity()).onSelectMedia(mediaType);
        dismiss();
    }
}
