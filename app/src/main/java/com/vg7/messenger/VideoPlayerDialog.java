package com.vg7.messenger;

import android.app.Dialog;
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

    private Uri videoUri;

    public VideoPlayerDialog(Uri videoUri) {
        this.videoUri = videoUri;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_video_player_dialog, null);

        VideoView videoView = view.findViewById(R.id.opened_video);
        videoView.setVideoURI(videoUri);

        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> mp.setLooping(false));

        videoView.start();

        builder.setView(view);
        return builder.create();
    }
}
