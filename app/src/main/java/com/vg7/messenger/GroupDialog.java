package com.vg7.messenger;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.vg7.messenger.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupDialog extends DialogFragment {

    private static final String TAG = "GroupDialog";

    GroupChatroomModel model;
    GroupChatActivity groupChatActivity;
    ImageButton backBtn, editBtn, moreBtn;
    ImageView imageUri;
    TextView groupName, groupMembers;

    RecyclerView adminsRecyclerView;
    RecyclerView membersRecyclerView;
    TextView adminsTextView, membersTextView;

    private AdminAdapter adminAdapter;
    private MemberAdapter memberAdapter;

    UserModel currentUser;
    List<UserModel> adminsList = new ArrayList<>();
    List<UserModel> membersList = new ArrayList<>();

    public GroupDialog(GroupChatroomModel model, GroupChatActivity groupChatActivity) {
        this.model = model;
        this.groupChatActivity = groupChatActivity;
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
        editBtn = view.findViewById(R.id.open_group_edit);
        moreBtn = view.findViewById(R.id.open_group_more);

        backBtn.setOnClickListener((v -> dismiss()));
        editBtn.setOnClickListener((v -> {
            editGroup(model);
        }));
        moreBtn.setOnClickListener(this::showPopupMenu);

        // Установка видимости кнопки редактирования группы по роли текущего пользователя
        FirebaseUtil.currentUserDetails().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(UserModel.class);

                    // Проверяем, что currentUser не null
                    if (currentUser != null) {
                        // Проверка роли текущего пользователя
                        boolean isAdmin = isAdmin(currentUser);
                        editBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                        editBtn.setEnabled(isAdmin);
                    } else {
                        Log.e(TAG, "Current user is null");
                        // Обработка ошибки, если необходимо
                    }
                } else {
                    Log.e(TAG, "Document does not exist or is null");
                    // Обработка ошибки, если необходимо
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to get current user details", e);
                // Обработка ошибки, если необходимо
            }
        });

        // Инициализация RecyclerView и адаптеров
        initializeRecyclerView(view);

        // Загрузка данных группы
        loadGroupData();

        syncUsersData(); // Синхронизация данных

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.group_dialog_menu, menu);
        configureMenuItems(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void initializeRecyclerView(View view) {
        adminsRecyclerView = view.findViewById(R.id.admins_recycler_view);
        membersRecyclerView = view.findViewById(R.id.members_recycler_view);
        adminsTextView = view.findViewById(R.id.admins_text_view);
        membersTextView = view.findViewById(R.id.members_text_view);

        adminsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adminAdapter = new AdminAdapter(getContext(), this, adminsList);
        memberAdapter = new MemberAdapter(getContext(), this, membersList);

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

    private void syncUsersData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Получаем идентификатор группы
        String groupId = model.getChatroomId();

        // Получаем админов группы
        db.collection("chatrooms").document(groupId).collection("admins")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
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
                });
    }

    private void loadMembers(String groupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Получаем участников группы
        db.collection("chatrooms").document(groupId).collection("members")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
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
                });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.group_dialog_menu, popupMenu.getMenu());

        // Проверка роли пользователя и настройка видимости элементов меню
        configureMenuItems(popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_member:
                        // Обработка добавления участника
                        addMemberDialog();
                        return true;
                    case R.id.action_delete_group:
                        // Обработка удаления группы
                        deleteGroupDialog();
                        return true;
                    case R.id.action_leave_group:
                        // Обработка выхода из группы
                        leaveGroupDialog();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void configureMenuItems(Menu menu) {
        // Получение текущего пользователя асинхронно
        FirebaseUtil.currentUserDetails().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(UserModel.class);

                    // Проверяем, что currentUser не null, перед вызовом метода getUsername()
                    if (currentUser != null) {
                        // Проверка роли текущего пользователя
                        boolean isAdmin = isAdmin(currentUser);
                        boolean isMember = isMember(currentUser);

                        // Настройка видимости и доступности элементов меню
                        menu.findItem(R.id.action_add_member).setVisible(isAdmin).setEnabled(isAdmin);
                        menu.findItem(R.id.action_delete_group).setVisible(isAdmin).setEnabled(isAdmin);
                        menu.findItem(R.id.action_leave_group).setVisible(isMember).setEnabled(isMember);
                    } else {
                        Log.e(TAG, "Current user is null");
                        // Обработка ошибки, если необходимо
                    }
                } else {
                    Log.e(TAG, "Document does not exist or is null");
                    // Обработка ошибки, если необходимо
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to get current user details", e);
                // Обработка ошибки, если необходимо
            }
        });
    }

    public void openUserProfile(UserModel model) {
        ProfileDialog dialog = new ProfileDialog(model);
        dialog.show(getParentFragmentManager(), "open_profile");
    }

    private void addMemberDialog() {
        AddMemberToGroupDialog dialog = new AddMemberToGroupDialog(model, groupChatActivity);
        dialog.show(getParentFragmentManager(), "add_member_to_group");
    }

    private void deleteGroupDialog() {
        DeleteGroupDialog dialog = new DeleteGroupDialog(model);
        dialog.show(getParentFragmentManager(), "delete_group");
    }

    private void leaveGroupDialog() {
        LeaveGroupDialog dialog = new LeaveGroupDialog(model);
        dialog.show(getParentFragmentManager(), "leave_group");
    }

    private boolean isAdmin(UserModel user) {
        for (UserModel admin : adminsList) {
            if (admin.getUserId().equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMember(UserModel user) {
        for (UserModel member : membersList) {
            if (member.getUserId().equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }

    private void editGroup(GroupChatroomModel model) {
        EditGroupDialog dialog = new EditGroupDialog(model);
        dialog.show(getParentFragmentManager(), "edit_group");
    }
}
