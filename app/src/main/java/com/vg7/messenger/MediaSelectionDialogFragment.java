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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_media_selection_dialog, null);
        builder.setView(view);

        ImageButton selectPhotoButton = view.findViewById(R.id.select_photo_button);
        ImageButton selectVideoButton = view.findViewById(R.id.select_video_button);
        ImageButton selectFileButton = view.findViewById(R.id.select_file_button);

        selectPhotoButton.setOnClickListener(v -> onSelectMedia("photo"));
        selectVideoButton.setOnClickListener(v -> onSelectMedia("video"));
        selectFileButton.setOnClickListener(v -> onSelectMedia("file"));

        return builder.create();
    }

    private void onSelectMedia(String mediaType) {
        ((ChatActivity) requireActivity()).onSelectMedia(mediaType);
        dismiss();
    }
}

