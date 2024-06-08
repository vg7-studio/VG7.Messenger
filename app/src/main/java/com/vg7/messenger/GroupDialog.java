package com.vg7.messenger;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vg7.messenger.adapter.AdminAdapter;
import com.vg7.messenger.adapter.MemberAdapter;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupDialog extends DialogFragment {

    private static final String TAG = "GroupDialog";

    GroupChatroomModel model;
    ImageButton backBtn;
    ImageView imageUri;
    TextView groupName, groupMembers;

    RecyclerView adminsRecyclerView;
    RecyclerView membersRecyclerView;
    TextView adminsTextView, membersTextView;

    private AdminAdapter adminAdapter;
    private MemberAdapter memberAdapter;

    List<UserModel> adminsList = new ArrayList<>();
    List<UserModel> membersList = new ArrayList<>();

    public GroupDialog(GroupChatroomModel model) {
        this.model = model;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_group_open_group, null);

        backBtn = view.findViewById(R.id.back_btn);
        imageUri = view.findViewById(R.id.profile_pic_image_view);
        groupName = view.findViewById(R.id.open_group_name);
        groupMembers = view.findViewById(R.id.open_group_members);

        backBtn.setOnClickListener((v -> dismiss()));

        // Инициализация RecyclerView и адаптеров
        initializeRecyclerView(view);

        // Загрузка данных группы
        loadGroupData();

        builder.setView(view);
        return builder.create();
    }

    private void initializeRecyclerView(View view) {
        adminsRecyclerView = view.findViewById(R.id.admins_recycler_view);
        membersRecyclerView = view.findViewById(R.id.members_recycler_view);
        adminsTextView = view.findViewById(R.id.admins_text_view);
        membersTextView = view.findViewById(R.id.members_text_view);

        adminsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adminAdapter = new AdminAdapter(getContext(), adminsList);
        memberAdapter = new MemberAdapter(getContext(), membersList);

        adminsRecyclerView.setAdapter(adminAdapter);
        membersRecyclerView.setAdapter(memberAdapter);
    }

    private void loadGroupData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupId = model.getChatroomId();

        DocumentReference groupRef = db.collection("chatrooms").document(groupId);
        groupRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String groupNameText = documentSnapshot.getString("groupName");
                    String membersText;
                    List<String> userIds = (List<String>) documentSnapshot.get("userIds");

                    if (documentSnapshot.getString("groupImageUrl") != null && !Objects.requireNonNull(documentSnapshot.getString("groupImageUrl")).isEmpty()) {
                        Uri uri = Uri.parse(documentSnapshot.getString("groupImageUrl"));
                        AndroidUtil.setProfilePic(getContext(), uri, imageUri);
                    }
                    int numMembers = userIds.size();

                    if (numMembers == 1) {
                        membersText = "1 " + getString(R.string.member);
                    } else if (numMembers % 10 == 1 && numMembers != 11) {
                        membersText = numMembers + " " + getString(R.string.member);
                    } else if (numMembers % 10 >= 2 && numMembers % 10 <= 4 && (numMembers < 10 || numMembers > 20)) {
                        membersText = numMembers + " " + getString(R.string.member2);
                    } else {
                        membersText = numMembers + " " + getString(R.string.member3);
                    }

                    groupName.setText(groupNameText);
                    groupMembers.setText(membersText);
                }
            }
        });

        // Сначала загружаем админов
        db.collection("chatrooms").document(groupId).collection("admins")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Failed to load admins", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            adminsList.clear();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                UserModel admin = doc.toObject(UserModel.class);
                                if (admin != null) {
                                    adminsList.add(admin);
                                }
                            }
                            adminAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded " + adminsList.size() + " admins");

                            // После загрузки админов загружаем участников
                            loadMembers(groupId);
                        }
                    }
                });
    }

    private void loadMembers(String groupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("chatrooms").document(groupId).collection("members")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Failed to load members", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            membersList.clear();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                UserModel member = doc.toObject(UserModel.class);
                                if (member != null && !isAdmin(member)) {
                                    membersList.add(member);
                                }
                            }
                            memberAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded " + membersList.size() + " members");
                        }
                    }
                });
    }

    private boolean isAdmin(UserModel user) {
        for (UserModel admin : adminsList) {
            if (admin.getUserId().equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }
}
