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
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.vg7.messenger.ChatActivity;
import com.vg7.messenger.GroupChatActivity;
import com.vg7.messenger.R;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchInGroupUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchInGroupUserRecyclerAdapter.UserModelViewHolder> {

    private static final String TAG = "SearchInGroupUserRecyclerAdapter";
    private Context context;
    private GroupChatActivity groupChatActivity;
    private GroupChatroomModel group;
    private Dialog dialog;

    // Конструктор адаптера для пошуку користувачів
    public SearchInGroupUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context, GroupChatActivity groupChatActivity, GroupChatroomModel group, Dialog dialog) {
        super(options);
        this.context = context;
        this.groupChatActivity = groupChatActivity;
        this.group = group;
        this.dialog = dialog;
    }

    // Метод для прив'язки даних до представлення RecyclerView
    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        getMembersIds(membersIds -> {
            if (membersIds != null && membersIds.contains(model.getUserId())) {
                holder.itemView.setVisibility(View.GONE);
                return;
            }

            holder.usernameText.setText(model.getUsername());
            holder.phoneText.setText(model.getPhone());

            FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                    .addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            Uri uri = t.getResult();
                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                        }
                    });

            holder.itemView.setOnClickListener(v -> {
                addUserToGroupChat(model);
            });
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    // Метод для отримання ID учасників групи
    private void getMembersIds(FirebaseFirestoreCallback callback) {
        List<String> membersIds = new ArrayList<>();

        CollectionReference membersCollectionRef = FirebaseFirestore.getInstance()
                .collection("chatrooms").document(group.getChatroomId()).collection("members");

        membersCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    UserModel member = document.toObject(UserModel.class);
                    membersIds.add(Objects.requireNonNull(member).getUserId());
                }
                callback.onCallback(membersIds);
            } else {
                Log.e(TAG, "Error getting members IDs: ", task.getException());
            }
        });
    }

    private interface FirebaseFirestoreCallback {
        void onCallback(List<String> membersIds);
    }

    // Метод для додавання користувача до групового чату
    private void addUserToGroupChat(UserModel model) {
        CollectionReference membersCollectionRef = FirebaseFirestore.getInstance().collection("chatrooms")
                .document(group.getChatroomId()).collection("members");

        membersCollectionRef.document(model.getUserId()).set(model)
                .addOnSuccessListener(aVoid -> {
                    FirebaseFirestore.getInstance().collection("chatrooms")
                            .document(group.getChatroomId())
                            .update("userIds", FieldValue.arrayUnion(model.getUserId()))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(context, model.getUsername() + " added to the group chat", Toast.LENGTH_SHORT).show();
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();  // Закрити діалогове вікно
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating userIds in group chat", e);
                                Toast.makeText(context, "Failed to update group chat", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding user to group chat", e);
                    Toast.makeText(context, "Failed to add " + model.getUsername(), Toast.LENGTH_SHORT).show();
                });
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
