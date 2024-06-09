package com.vg7.messenger.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vg7.messenger.ChatActivity;
import com.vg7.messenger.R;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchInGroupUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchInGroupUserRecyclerAdapter.UserModelViewHolder> {

    private static final String TAG = "SearchInGroupUserRecyclerAdapter";
    Context context;
    GroupChatroomModel group;
    Dialog dialog;

    // Конструктор адаптера для пошуку користувачів
    public SearchInGroupUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context, GroupChatroomModel group, Dialog dialog) {
        super(options);
        this.context = context;
        this.group = group;
        this.dialog = dialog;
    }

    // Метод для прив'язки даних до представлення RecyclerView
    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        // Получаем список идентификаторов пользователей, уже участвующих в групповом чате
        getMembersIds(membersIds -> {
            // Проверяем, содержится ли текущий пользователь в списке участников группового чата
            if (membersIds.contains(model.getUserId())) {
                // Если пользователь уже участвует в чате, прячем элемент RecyclerView
                holder.itemView.setVisibility(View.GONE);
                return;
            } else {
                // Показываем элемент RecyclerView если пользователь не участвует в чате
                holder.itemView.setVisibility(View.VISIBLE);
            }

            // Встановити ім'я та телефон користувача
            holder.usernameText.setText(model.getUsername());
            holder.phoneText.setText(model.getPhone());

            // Отримати посилання на профільне зображення користувача
            FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                    .addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            Uri uri = t.getResult();
                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                        }
                    });

            // Обробка натискання на елемент
            holder.itemView.setOnClickListener(v -> {
                addUserToGroupChat(model);
            });
        });
    }


    // Метод для створення нового ViewHolder
    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Заповнити макет для рядка пошуку користувачів
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    private void getMembersIds(MembersIdsCallback callback) {
        List<String> membersIds = new ArrayList<>();
        Log.d(TAG, group.getChatroomId());

        // Получаем ссылку на коллекцию "members"
        CollectionReference membersCollectionRef = FirebaseFirestore.getInstance().collection("chatrooms").document(group.getChatroomId()).collection("members");

        // Получаем все документы из коллекции "members"
        membersCollectionRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Failed to load members", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    UserModel member = doc.toObject(UserModel.class);
                    membersIds.add(member.getUserId());
                }
                // Передаем список идентификаторов пользователей в колбэк
                callback.onCallback(membersIds);
            }
        });
    }

    public interface MembersIdsCallback {
        void onCallback(List<String> membersIds);
    }

    // Метод для додавання користувача до групового чату
    private void addUserToGroupChat(UserModel model) {
        CollectionReference membersCollectionRef = FirebaseFirestore.getInstance().collection("chatrooms")
                .document(group.getChatroomId()).collection("members");

        membersCollectionRef.document(model.getUserId()).set(model)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "User \"" + model.getUsername() + "\" added", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding user to group chat", e);
                    Toast.makeText(context, "Failed to add user to group", Toast.LENGTH_SHORT).show();
                });
    }

    // ViewHolder для рядків пошуку користувачів
    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;

        // Конструктор ViewHolder
        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            // Знайти всі елементи макету
            usernameText = itemView.findViewById(R.id.user_name_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
