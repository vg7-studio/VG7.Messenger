package com.vg7.messenger;

import android.app.Activity;
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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.utils.FirebaseUtil;

public class LeaveGroupDialog extends DialogFragment {

    ImageView dialogIcon;
    TextView dialogText;
    Button yesBtn, noBtn;

    GroupChatroomModel group;

    public LeaveGroupDialog(GroupChatroomModel group) {
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
        dialogText.setText(R.string.leave_group_message);

        yesBtn.setOnClickListener(v -> leaveGroup());
        noBtn.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    // Метод залишення групи
    private void leaveGroup() {
        String userId = FirebaseUtil.currentUserId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Remove user from "members" collection
        DocumentReference memberRef = db.collection("chatrooms")
                .document(group.getChatroomId())
                .collection("members")
                .document(userId);

        memberRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove user from "userIds" array in group document
                    DocumentReference groupRef = db.collection("chatrooms").document(group.getChatroomId());
                    groupRef.update("userIds", FieldValue.arrayRemove(userId))
                            .addOnSuccessListener(aVoid2 -> {
                                dismiss();
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                            });
                })
                .addOnFailureListener(e -> {
                });
    }
}
