package com.vg7.messenger;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class VideoPlayerDialog extends DialogFragment {

    private Uri videoUri; // Uri-адрес відео

    public VideoPlayerDialog(Uri videoUri) { // Конструктор, який приймає Uri-адрес відео
        this.videoUri = videoUri;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Створити алерт-діалог
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_video_player_dialog, null); // Отримати інфлаєр і об'єкт View

        VideoView videoView = view.findViewById(R.id.opened_video); // Отримати огляд vidéoView
        videoView.setVideoURI(videoUri); // Встановити Uri-адрес відео

        MediaController mediaController = new MediaController(getContext()); // Створити об'єкт MediaController
        mediaController.setAnchorView(videoView); // Встановити огляд для MediaController
        videoView.setMediaController(mediaController); // Встановити MediaController для vidéoView

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(false); // Не повторювати відео
            }
        }); // Отримати зазначення на підготовку медіа-плеера

        videoView.start(); // Запустити відео

        builder.setView(view); // Встановити об'єкт View для діалогу
        return builder.create(); // Створити діалог
    }
}