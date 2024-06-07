package com.vg7.messenger;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.vg7.messenger.model.GroupChatroomModel;

public class GroupDialog extends DialogFragment {

    // Оголошення змінних для елементів інтерфейсу
    GroupChatroomModel model;

    public GroupDialog(GroupChatroomModel model) { this.model = model; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Створення діалогового вікна з використанням AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_group_open_group, null);

        // Встановлення користувацького виду для діалогу
        builder.setView(view);

        return builder.create();
    }
}
