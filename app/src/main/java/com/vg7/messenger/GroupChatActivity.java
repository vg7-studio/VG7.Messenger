package com.vg7.messenger;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vg7.messenger.adapter.AdminAdapter;
import com.vg7.messenger.adapter.ChatRecyclerAdapter;
import com.vg7.messenger.adapter.GroupChatRecyclerAdapter;
import com.vg7.messenger.adapter.MemberAdapter;
import com.vg7.messenger.model.ChatMessageModel;
import com.vg7.messenger.model.ChatroomModel;
import com.vg7.messenger.model.GroupChatMessageModel;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    FirebaseFirestore db;
    FirebaseAuth auth;
    RecyclerView groupChatRecyclerView;
    EditText groupChatMessageInput;
    ImageButton messageSendBtn, mediaMessageSendBtn;
    ImageButton backBtn;
    ImageView groupImagePicView;
    TextView groupNameTextView;
    TextView groupMembersTextView;

    GroupChatRecyclerAdapter adapter;
    GroupChatroomModel chatroomModel;

    public String groupId;

    private AdminAdapter adminAdapter;
    private MemberAdapter memberAdapter;

    List<UserModel> adminsList = new ArrayList<>();
    List<UserModel> membersList = new ArrayList<>();

    private static final int REQUEST_PICK_PHOTO = 1;
    private static final int REQUEST_PICK_VIDEO = 2;
    private static final int REQUEST_PICK_FILE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Инициализация адаптеров
        adminAdapter = new AdminAdapter(this, adminsList);
        memberAdapter = new MemberAdapter(this, membersList);

        // Получение groupId из Intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("chatroomId");

        // Инициализация UI элементов
        backBtn = findViewById(R.id.back_btn);
        groupImagePicView = findViewById(R.id.profile_pic_image_view);
        groupChatRecyclerView = findViewById(R.id.group_chat_recycler_view);
        groupChatMessageInput = findViewById(R.id.group_chat_message_input);
        messageSendBtn = findViewById(R.id.message_send_btn);
        mediaMessageSendBtn = findViewById(R.id.message_send_media_btn);
        groupNameTextView = findViewById(R.id.group_name);
        groupMembersTextView = findViewById(R.id.group_members);

        // Загрузка данных группы
        loadGroupData();

        backBtn.setOnClickListener((v)-> {
            onBackPressed();
        });

        groupImagePicView.setOnClickListener((v -> {
            openGroup(chatroomModel);
        }));

        groupNameTextView.setOnClickListener((v -> {
            openGroup(chatroomModel);
        }));

        mediaMessageSendBtn.setOnClickListener((v -> {
            showMediaSelectionDialog();
        }));
        messageSendBtn.setOnClickListener((v -> {
            sendMessage(groupChatMessageInput.getText().toString().trim());
        }));

        getGroupChatroomModel();
        setupGroupChatRecyclerView();

        setupAdminListener();
        setupMemberListener();

        // Синхронизацiя даних користувачiв та групи
        syncUsersData();
        syncGroupData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                switch (requestCode) {
                    case REQUEST_PICK_PHOTO:
                        // Обробка вибору фото
                        String photoMimeType = getContentResolver().getType(selectedMediaUri);
                        if (photoMimeType != null && photoMimeType.startsWith("image")) {
                            sendMediaMessage(selectedMediaUri, "image");
                        }
                        break;
                    case REQUEST_PICK_VIDEO:
                        // Обробка вибору відео
                        String videoMimeType = getContentResolver().getType(selectedMediaUri);
                        if (videoMimeType != null && videoMimeType.startsWith("video")) {
                            sendMediaMessage(selectedMediaUri, "video");
                        }
                        break;
                    case REQUEST_PICK_FILE:
                        // Обробка вибору файлу
                        sendMediaMessage(selectedMediaUri, "file");
                        break;
                }
            }
        }
    }

    // Завантаження даних группового чату
    private void loadGroupData() {
        if (groupId == null) { // Повторне завантаження даних, в разi невдали получити ID групового чату
            DocumentReference groupIdRef = db.collection("chatrooms").document("groupId");
            groupIdRef.addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    groupId = documentSnapshot.getId();
                    loadGroupData();
                }
            });
        } else {
            DocumentReference groupRef = db.collection("chatrooms").document(groupId);
            groupRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String groupName = documentSnapshot.getString("groupName"); // Назва
                    String membersText;
                    List<String> userIds = (List<String>) documentSnapshot.get("userIds");

                    // Зображення групи
                    if (documentSnapshot.getString("groupImageUrl") != null && !Objects.requireNonNull(documentSnapshot.getString("groupImageUrl")).isEmpty()) {
                        Uri uri = Uri.parse(documentSnapshot.getString("groupImageUrl"));
                        AndroidUtil.setGroupPic(this, uri, groupImagePicView);
                    }
                    int numMembers = userIds.size(); // Кiл-ть учасникiв

                    if (numMembers == 1) {
                        membersText = "1 " + getString(R.string.member);
                    } else if (numMembers % 10 == 1 && numMembers != 11) {
                        membersText = numMembers + " " + getString(R.string.member);
                    } else if (numMembers % 10 >= 2 && numMembers % 10 <= 4 && (numMembers < 10 || numMembers > 20)) {
                        membersText = numMembers + " " + getString(R.string.member2);
                    } else {
                        membersText = numMembers + " " + getString(R.string.member3);
                    }

                    groupNameTextView.setText(groupName);
                    groupMembersTextView.setText(membersText);
                }
            }).addOnFailureListener(e -> {
            });
        }
    }

    void setupGroupChatRecyclerView(){
        Query query = FirebaseUtil.getChatroomMessageReference(groupId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<GroupChatMessageModel> options = new FirestoreRecyclerOptions.Builder<GroupChatMessageModel>()
                .setQuery(query,GroupChatMessageModel.class).build();

        adapter = new GroupChatRecyclerAdapter(options,getApplicationContext(), this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        groupChatRecyclerView.setLayoutManager(manager);
        groupChatRecyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                groupChatRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void getGroupChatroomModel(){
        FirebaseUtil.getChatroomReference(groupId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatroomModel = task.getResult().toObject(GroupChatroomModel.class);
                if(chatroomModel==null){
                    // перший чат
                    chatroomModel = new GroupChatroomModel(
                            groupId,
                            Arrays.asList(FirebaseUtil.currentUserId()),
                            Timestamp.now(),
                            "",
                            groupNameTextView.getText().toString()
                    );
                    FirebaseUtil.getChatroomReference(groupId).set(chatroomModel);
                }
            }
        });
    }

    // Завантаження списку адмiнiстраторiв
    private void setupAdminListener() {
        db.collection("chatrooms").document(groupId).collection("admins")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Failed to listen for admin updates", e);
                        return;
                    }
                    adminsList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            UserModel admin = doc.toObject(UserModel.class);
                            if (admin != null) {
                                adminsList.add(admin);
                            }
                        }
                    }
                    adminAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Updated " + adminsList.size() + " admins");
                });
    }

    // Завантаження списку учасникiв
    private void setupMemberListener() {
        db.collection("chatrooms").document(groupId).collection("members")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Failed to listen for member updates", e);
                        return;
                    }
                    membersList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            UserModel member = doc.toObject(UserModel.class);
                            if (member != null && !isAdmin(member)) {
                                membersList.add(member);
                            }
                        }
                    }
                    memberAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Updated " + membersList.size() + " members");
                });
    }

    // Синхронизацiя даних користувачiв
    private void syncUsersData() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<UserModel> allUsers = new ArrayList<>();
                            for (DocumentSnapshot doc : task.getResult()) {
                                UserModel user = doc.toObject(UserModel.class);
                                allUsers.add(user);
                            }

                            db.collection("chatrooms").document(groupId).collection("admins")
                                    .get()
                                    .addOnCompleteListener(adminTask -> {
                                        if (adminTask.isSuccessful() && adminTask.getResult() != null) {
                                            List<UserModel> adminUsers = new ArrayList<>();
                                            for (DocumentSnapshot doc : adminTask.getResult()) {
                                                UserModel user = doc.toObject(UserModel.class);
                                                // Перевіряємо наявність користувача в колекції "admins" групового чату за ідентифікатором
                                                if (userExists(user, allUsers)) {
                                                    adminUsers.add(user);
                                                }
                                            }
                                            Log.d("AddedList", String.valueOf(adminUsers.size()));
                                            updateCollection("admins", allUsers, adminUsers);
                                        }
                                    });

                            db.collection("chatrooms").document(groupId).collection("members")
                                    .get()
                                    .addOnCompleteListener(memberTask -> {
                                        if (memberTask.isSuccessful() && memberTask.getResult() != null) {
                                            List<UserModel> memberUsers = new ArrayList<>();
                                            for (DocumentSnapshot doc : memberTask.getResult()) {
                                                UserModel user = doc.toObject(UserModel.class);
                                                // Перевіряємо наявність користувача в колекції "members" групового чату за ідентифікатором
                                                if (userExists(user, allUsers)) {
                                                    memberUsers.add(user);
                                                }
                                            }
                                            updateCollection("members", allUsers, memberUsers);
                                        }
                                    });
                        }
                    }
                });
    }

    // Синхронизацiя даних групи
    private void syncGroupData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

                    // Обновляем UI в GroupDialog
                    if (chatroomModel != null && chatroomModel.getGroupImageUrl() != null && !chatroomModel.getGroupImageUrl().isEmpty()) {
                        Uri uri = Uri.parse(chatroomModel.getGroupImageUrl());
                        AndroidUtil.setGroupPic(GroupChatActivity.this, uri, groupImagePicView);
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

                    groupNameTextView.setText(groupNameText);
                    groupMembersTextView.setText(membersText);
                }
            }
        });
    }

    private void updateCollection(String collectionName, List<UserModel> allUsers, List<UserModel> specificUsers) {
        for (UserModel user : allUsers) {
            boolean userExists = false;
            for (UserModel specificUser : specificUsers) {
                if (user.getUserId().equals(specificUser.getUserId())) {
                    userExists = true;
                    break;
                }
            }
            if (userExists) {
                db.collection("chatrooms").document(groupId).collection(collectionName).document(user.getUserId()).set(user);
            }
        }
    }

    private boolean isAdmin(UserModel user) {
        for (UserModel admin : adminsList) {
            if (admin.getUserId().equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }

    private boolean userExists(UserModel user, List<UserModel> users) {
        for (UserModel u : users) {
            if (u.getUserId().equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }

    public void sendMessage(String messageText) {
        if (!messageText.isEmpty()) {
            chatroomModel.setLastMessageTimestamp(Timestamp.now());
            chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
            chatroomModel.setLastMessage(messageText);
            chatroomModel.setType("group");
            FirebaseUtil.getChatroomReference(groupId).set(chatroomModel);

            ChatMessageModel chatMessageModel = new ChatMessageModel(messageText, FirebaseUtil.currentUserId(), Timestamp.now());
            FirebaseUtil.getChatroomMessageReference(groupId).add(chatMessageModel)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                groupChatMessageInput.setText("");
                                sendNotification(messageText);
                            }
                        }
                    });
        }
    }

    void sendMediaMessage(Uri fileUri, String messageType) {
        // Отримати посилання на Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Створити унікальне ім'я файлу
        String fileName = FirebaseUtil.getFileName(this, fileUri);

        StorageReference fileRef = storageRef.child("uploads/" + fileName);

        // Завантажте файл
        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            // Отримати URL завантаженого файлу
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String fileUrl = uri.toString();
                // Тепер відправте URL як частину вашого повідомлення
                sendMessageWithMedia(fileName, fileUrl, messageType);
            });
        }).addOnFailureListener(exception -> {
            // Обробка помилки завантаження
        });
    }

    void sendMessageWithMedia(String fileName, String fileUrl, String messageType) {
        // Оновіть інформацію про останнє повідомлення в чаті
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage((messageType.equals("image") ? "Image" :
                (messageType.equals("video") ? "Video" : "File")));
        chatroomModel.setType("group");
        FirebaseUtil.getChatroomReference(groupId).set(chatroomModel);

        // Створіть об'єкт повідомлення з мультимедіа
        ChatMessageModel chatMessageModel = new ChatMessageModel(fileName, FirebaseUtil.currentUserId(), Timestamp.now(), messageType, fileUrl);
        FirebaseUtil.getChatroomMessageReference(groupId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            groupChatMessageInput.setText("");
                            sendNotification((messageType.equals("image") ? "New Image" :
                                    (messageType.equals("video") ? "New Video" : "New File")));
                        }
                    }
                });
    }

    private void showMediaSelectionDialog() {
        MediaSelectionDialogFragment dialog = new MediaSelectionDialogFragment();
        dialog.show(getSupportFragmentManager(), "media_selection_dialog");
    }
    public void onSelectMedia(String mediaType) {
        switch (mediaType) {
            case "photo":
                // Обробка вибору фото
                openPhotoPicker();
                break;
            case "video":
                // Обробка вибору відео
                openVideoPicker();
                break;
            case "file":
                // Обробка вибору файлу
                openFilePicker();
                break;
            default:
                // Обробка інших типів мультимедіа, якщо необхідно
                break;
        }
    }

    private void openPhotoPicker() {
        // Відкрити вікно вибору фото
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
    }
    private void openVideoPicker() {
        // Відкрити вікно вибору відео
        Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
        videoPickerIntent.setType("video/*");
        startActivityForResult(videoPickerIntent, REQUEST_PICK_VIDEO);
    }
    private void openFilePicker() {
        // Відкрити вікно вибору файлу
        Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filePickerIntent.setType("*/*");
        startActivityForResult(filePickerIntent, REQUEST_PICK_FILE);
    }

    private void openGroup(GroupChatroomModel model) {
        GroupDialog dialog = new GroupDialog(model, this);
        dialog.show(getSupportFragmentManager(), "open_group");
    }

    public void openFullscreenImage(Uri imageUri) {
        FullscreenImageDialog dialog = new FullscreenImageDialog(imageUri);
        dialog.show(getSupportFragmentManager(), "open_image");
    }
    public void openVideoPlayer(Uri videoUri) {
        VideoPlayerDialog dialog = new VideoPlayerDialog(videoUri);
        dialog.show(getSupportFragmentManager(), "open_video");
    }

    void sendNotification(String message){

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try{
                    JSONObject jsonObject  = new JSONObject();

                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title",groupNameTextView.getText());
                    notificationObj.put("body",message);

                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId",currentUser.getUserId());

                    jsonObject.put("notification",notificationObj);
                    jsonObject.put("data",dataObj);
                    jsonObject.put("to","/topics/" + groupId);

                    callApi(jsonObject);


                }
                catch (Exception e) {
                }

            }
        });

    }

    void callApi(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAAkVZnwjI:APA91bEuc-OnQnl2pMDlMC4q9RpFAImmcOOqfcGY54UbECGA7S5p-MXI5HR_P0q4tF1eMZmxFf06vHBsih-241eL0HbmbXpmKmuwkTIxD_ZFGhuFcyd5XQ4pUsxOSp0SSPsGymmgbF-R")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });

    }
}
