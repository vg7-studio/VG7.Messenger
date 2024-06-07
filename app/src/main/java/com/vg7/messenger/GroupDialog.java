package com.vg7.messenger;

import androidx.fragment.app.DialogFragment;

import com.vg7.messenger.model.GroupChatroomModel;

public class GroupDialog extends DialogFragment {

    // Оголошення змінних для елементів інтерфейсу
    GroupChatroomModel model;

    public GroupDialog(GroupChatroomModel model) { this.model = model; }
}
