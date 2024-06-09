package com.vg7.messenger;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.vg7.messenger.model.GroupChatroomModel;

public class DeleteGroupDialog extends DialogFragment {

    ImageView dialogIcon;
    TextView dialogText;
    Button yesBtn, noBtn;

    GroupChatroomModel group;

    public DeleteGroupDialog(GroupChatroomModel group) {
        this.group = group;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.yes_no_message_dialog, null);

        dialogIcon = view.findViewById(R.id.yesNoImage);
        dialogText = view.findViewById(R.id.yesNoMessage);
        yesBtn = view.findViewById(R.id.yesBtn);
        noBtn = view.findViewById(R.id.noBtn);

        dialogIcon.setBackgroundResource(R.drawable.circular_bg);
        dialogIcon.setBackgroundTintList(getResources().getColorStateList(R.color.my_primary));
        dialogIcon.setImageResource(R.drawable.delete_group_icon);
        dialogText.setText(R.string.delete_group_message);

        yesBtn.setOnClickListener(v -> deleteGroup());
        noBtn.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void deleteGroup() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Удаление группового чата из коллекции "chatrooms"
        db.collection("chatrooms").document(group.getChatroomId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    dismiss();
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error deleting group", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

}
