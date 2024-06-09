package com.vg7.messenger;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class LeaveGroupDialog extends DialogFragment {

    ImageView dialogIcon;
    TextView dialogText;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.yes_no_message_dialog, null);

        dialogIcon = view.findViewById(R.id.yesNoImage);
        dialogText = view.findViewById(R.id.yesNoMessage);

        dialogIcon.setBackgroundResource(R.drawable.circular_bg);
        dialogIcon.setBackgroundTintList(getResources().getColorStateList(R.color.my_primary));
        dialogIcon.setImageResource(R.drawable.delete_group_icon);
        dialogText.setText(R.string.leave_group_message);

        builder.setView(view);
        return builder.create();
    }
}
