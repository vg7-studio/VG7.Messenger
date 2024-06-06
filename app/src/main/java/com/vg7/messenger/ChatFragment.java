package com.vg7.messenger;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.vg7.messenger.adapter.RecentChatRecyclerAdapter;
import com.vg7.messenger.model.BaseChatroomModel;
import com.vg7.messenger.utils.ChatroomSnapshotParser;
import com.vg7.messenger.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    RecentChatRecyclerAdapter adapter;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Повернення макету фрагмента
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recyler_view);
        // Налаштування списку чатів
        setupRecyclerView();
        // Налаштування слухача подій Firebase
        setupFirebaseListener();

        return view;
    }

    private void setupFirebaseListener() {
        FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatFragment", "Firebase listener error", error);
                        return;
                    }
                    // Перевірка наявності даних
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            BaseChatroomModel chatroom = new ChatroomSnapshotParser().parseSnapshot(snapshot);
                            Log.d("ChatFragment", "Chatroom received: " + chatroom.getChatroomId());
                            // Оновлення списку чатів за допомогою методу updateChatList()
                            updateChatList(chatroom);
                        }
                    } else {
                        Log.d("ChatFragment", "No chatrooms found");
                    }
                });
    }

    void setupRecyclerView() {
        // Запит на отримання списку чатів, в яких бере участь поточний користувач
        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        // Налаштування параметрів адаптера для списку чатів
        FirestoreRecyclerOptions<BaseChatroomModel> options = new FirestoreRecyclerOptions.Builder<BaseChatroomModel>()
                .setQuery(query, new ChatroomSnapshotParser()).build();

        // Створення адаптера та прикріплення його до списку
        adapter = new RecentChatRecyclerAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void updateChatList(BaseChatroomModel chatroom) {
        // Проверяем, что данные не пусты
        if (chatroom != null) {
            Log.d("ChatFragment", "Updating chat list with: " + chatroom.getChatroomId());
            // Обновляем данные в адаптере
            adapter.updateChatroom(chatroom);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Початок прослуховування змін в базі даних при старті фрагмента
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Зупинка прослуховування змін в базі даних при зупинці фрагмента
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Оновлення списку чатів при відновленні фрагмента
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }
}
