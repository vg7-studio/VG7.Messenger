package com.vg7.messenger;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class FullscreenImageDialog extends DialogFragment {

    private Uri imageUri;

    // Конструктор класу, приймає URI зображення для відображення
    public FullscreenImageDialog(Uri imageUri) {
        this.imageUri = imageUri;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Завантажуємо розмітку діалогового вікна з файлу XML
        View view = inflater.inflate(R.layout.fragment_fullscreen_image_dialog, null);

        // Знаходимо ImageView для відображення зображення
        ImageView imageView = view.findViewById(R.id.opened_image);
        // Використовуємо Glide для завантаження та відображення зображення за його URI
        Glide.with(requireContext())
                .load(imageUri)
                .into(imageView);

        // Налаштовуємо діалогове вікно, додаючи розмітку та кнопку "Закрити"
        builder.setView(view)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Закриваємо діалогове вікно при натисканні кнопки "Закрити"
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
