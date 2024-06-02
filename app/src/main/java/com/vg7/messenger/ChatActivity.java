package com.vg7.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vg7.messenger.adapter.ChatRecyclerAdapter;
import com.vg7.messenger.model.ChatMessageModel;
import com.vg7.messenger.model.ChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    EditText messageInput;
    ImageButton sendMediaBtn;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imagePicView;

    private static final int REQUEST_PICK_PHOTO = 1;
    private static final int REQUEST_PICK_VIDEO = 2;
    private static final int REQUEST_PICK_FILE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Отримати UserModel
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(),otherUser.getUserId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMediaBtn = findViewById(R.id.message_send_media_btn);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imagePicView = findViewById(R.id.profile_pic_image_view);

        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri  = t.getResult();
                        AndroidUtil.setProfilePic(this,uri,imagePicView);
                    }
                });

        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });

        imagePicView.setOnClickListener(v -> {
            openUserProfile(otherUser.getUserId(), otherUser.getUsername(), otherUser.getPhone(), otherUser.getStatus());
        });

        otherUsername.setText(otherUser.getUsername());
        otherUsername.setOnClickListener(v -> {
            openUserProfile(otherUser.getUserId(), otherUser.getUsername(), otherUser.getPhone(), otherUser.getStatus());
        });

        sendMediaBtn.setOnClickListener((v -> {
            showMediaSelectionDialog();
        }));
        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message);
        }));

        getOrCreateChatroomModel();
        setupChatRecyclerView();
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
                            sendMediaMessageToUser(selectedMediaUri, "image");
                            ImageView imageView = findViewById(R.id.right_chat_imageview); // Замініть R.id.imageView на ваш ID ImageView
                            imageView.setImageURI(selectedMediaUri);
                        }
                        break;
                    case REQUEST_PICK_VIDEO:
                        // Обробка вибору відео
                        String videoMimeType = getContentResolver().getType(selectedMediaUri);
                        if (videoMimeType != null && videoMimeType.startsWith("video")) {
                            sendMediaMessageToUser(selectedMediaUri, "video");
                        }
                        break;
                    case REQUEST_PICK_FILE:
                        // Обробка вибору файлу
                        sendMediaMessageToUser(selectedMediaUri, "file");
                        break;
                }
            }
        }
    }

    void setupChatRecyclerView(){
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options,getApplicationContext(), this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void sendMessageToUser(String message){

        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            messageInput.setText("");
                            sendNotification(message);
                        }
                    }
                });
    }

    void sendMediaMessageToUser(Uri fileUri, String messageType) {
        // Отримати посилання на Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Створити унікальне ім'я файлу
        String fileName = System.currentTimeMillis() +
                (messageType.equals("image") ? ".image" :
                (messageType.equals("video") ? ".video" : ".file"));

        StorageReference fileRef = storageRef.child("uploads/" + fileName);

        // Завантажте файл
        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            // Отримати URL завантаженого файлу
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String fileUrl = uri.toString();
                // Тепер відправте URL як частину вашого повідомлення
                sendMessageWithMedia(fileUrl, messageType);
            });
        }).addOnFailureListener(exception -> {
            // Обробка помилки завантаження
        });
    }

    void sendMessageWithMedia(String fileUrl, String messageType) {
        // Оновіть інформацію про останнє повідомлення в чаті
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage((messageType.equals("image") ? "Image" :
                (messageType.equals("video") ? "Video" : "File")));
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        // Створіть об'єкт повідомлення з мультимедіа
        ChatMessageModel chatMessageModel = new ChatMessageModel(fileUrl, FirebaseUtil.currentUserId(), Timestamp.now(), messageType, fileUrl);
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            messageInput.setText("");
                            sendNotification((messageType.equals("image") ? "New Image" :
                                    (messageType.equals("video") ? "New Video" : "New File")));
                        }
                    }
                });
    }

    void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if(chatroomModel==null){
                    // перший чат
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }

    void sendNotification(String message){

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try{
                    JSONObject jsonObject  = new JSONObject();

                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title",currentUser.getUsername());
                    notificationObj.put("body",message);

                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId",currentUser.getUserId());

                    jsonObject.put("notification",notificationObj);
                    jsonObject.put("data",dataObj);
                    jsonObject.put("to",otherUser.getFcmToken());

                    callApi(jsonObject);


                }
                catch (Exception e) {
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

    private void openUserProfile(String textImageUri, String textName, String textNumber, String textStatus) {
        ProfileDialog dialog = new ProfileDialog(textImageUri, textName, textNumber, textStatus);
        dialog.show(getSupportFragmentManager(), "open_profile");
    }

    public void openFullscreenImage(Uri imageUri) {
        FullscreenImageDialog dialog = new FullscreenImageDialog(imageUri);
        dialog.show(getSupportFragmentManager(), "open_image");
    }
    public void openVideoPlayer(Uri videoUri) {
        VideoPlayerDialog dialog = new VideoPlayerDialog(videoUri);
        dialog.show(getSupportFragmentManager(), "open_video");
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
