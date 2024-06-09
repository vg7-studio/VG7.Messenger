package com.vg7.messenger;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vg7.messenger.adapter.SearchInGroupUserRecyclerAdapter;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

// Діалогове вікно для додавання користувачів до групи
public class AddMemberToGroupDialog extends DialogFragment {

    EditText searchInput; // Поле введення для пошуку користувачів
    ImageButton searchButton; // Кнопка пошуку
    ImageButton backButton; // Кнопка назад
    RecyclerView recyclerView; // RecyclerView для відображення результатів пошуку
    SearchInGroupUserRecyclerAdapter adapter; // Адаптер для RecyclerView
    GroupChatroomModel group; // Модель групи
    GroupChatActivity groupChatActivity; // Активність групового чату

    // Конструктор з параметрами групи та активності групового чату
    public AddMemberToGroupDialog(GroupChatroomModel group, GroupChatActivity groupChatActivity) {
        this.group = group;
        this.groupChatActivity = groupChatActivity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_member_to_group_dialog, null);

        // Ініціалізація елементів відображення
        searchInput = view.findViewById(R.id.search_username_input);
        searchButton = view.findViewById(R.id.search_user_btn);
        backButton = view.findViewById(R.id.back_btn);
        recyclerView = view.findViewById(R.id.search_user_recycler_view);

        searchInput.requestFocus(); // Фокус на полі введення для пошуку

        backButton.setOnClickListener(v -> dismiss()); // Обробник натискання на кнопку "назад"

        searchButton.setOnClickListener(v -> { // Обробник натискання на кнопку "пошук"
            String searchTerm = searchInput.getText().toString();
            if (searchTerm.isEmpty() || searchTerm.length() < 3) {
                searchInput.setError(getString(R.string.invalid_username));
                return;
            }
            setupSearchRecyclerView(searchTerm); // Налаштування RecyclerView для пошуку користувачів
        });

        // Ініціалізація RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        builder.setView(view); // Встановлення вигляду для діалогового вікна
        return builder.create(); // Створення діалогового вікна
    }

    // Метод налаштування RecyclerView для пошуку користувачів за введеним терміном пошуку
    void setupSearchRecyclerView(String searchTerm) {
        // Налаштування запиту до Firestore
        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("username", searchTerm)
                .whereLessThanOrEqualTo("username", searchTerm + '\uf8ff');

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        if (adapter != null) {
            adapter.stopListening();
        }

        // Ініціалізація та налаштування адаптера
        adapter = new SearchInGroupUserRecyclerAdapter(options, getContext(), groupChatActivity, group, getDialog());
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.startListening();
    }
}
