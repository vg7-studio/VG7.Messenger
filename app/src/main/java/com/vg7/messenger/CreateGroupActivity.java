package com.vg7.messenger;

import static com.vg7.messenger.utils.AndroidUtil.showToast;
import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vg7.messenger.model.GroupChatroomModel;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

// Активність для створення групи
public class CreateGroupActivity extends AppCompatActivity {

    ImageView groupImageView; // Зображення групи
    EditText groupNameEditText; // Поле введення назви групи
    Button createGroupButton; // Кнопка створення групи
    ProgressBar createGroupProgressBar; // Прогресбар створення групи

    Uri groupImageUri; // Uri зображення групи

    FirebaseFirestore db; // Екземпляр Firestore
    FirebaseStorage storage; // Екземпляр FirebaseStorage
    FirebaseAuth auth; // Екземпляр FirebaseAuth
    UserModel currentUserModel; // Поточна модель користувача
    ActivityResultLauncher<Intent> imagePickLauncher; // Лаунчер для вибору зображення

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Ініціалізація елементів відображення
        groupImageView = findViewById(R.id.create_group_image_view);
        groupNameEditText = findViewById(R.id.group_name);
        createGroupButton = findViewById(R.id.create_group_btn);
        createGroupProgressBar = findViewById(R.id.create_group_progress_bar);

        // Ініціалізація Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        // Лаунчер для вибору зображення
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        groupImageUri = result.getData().getData();
                        AndroidUtil.setGroupPic(CreateGroupActivity.this, groupImageUri, groupImageView);
                    }
                }
        );

        // Обробники подій для кнопок
        groupImageView.setOnClickListener(v -> openFileChooser());
        createGroupButton.setOnClickListener(v -> createGroup());
        createGroupProgressBar.setVisibility(View.GONE);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
    }

    // Метод для відкриття вибору зображення
    private void openFileChooser() {
        ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        imagePickLauncher.launch(intent);
                        return null;
                    }
                });
    }

    // Метод для створення групи
    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();

        // Перевірка введеної назви групи
        if (groupName.isEmpty()) {
            groupNameEditText.setError(getString(R.string.group_name_is_required));
            groupNameEditText.requestFocus();
            return;
        } else if (groupName.length() < 4) {
            groupNameEditText.setError(getString(R.string.group_name_is_required2));
            groupNameEditText.requestFocus();
            return;
        }

        // Показ прогресбару та приховання кнопки створення групи
        createGroupButton.setVisibility(View.GONE);
        createGroupProgressBar.setVisibility(View.VISIBLE);

        // Завантаження зображення групи та збереження групи в базі даних
        if (groupImageUri != null) {
            uploadGroupImage(groupName);
        } else {
            saveGroupToDatabase(groupName, null);
        }
    }

    // Метод для завантаження зображення групи
    private void uploadGroupImage(String groupName) {
        // Посилання на сховище Firebase для зображення групи
        StorageReference storageRef = storage.getReference("group_pic/" + System.currentTimeMillis() + ".jpg");

        // Завантаження зображення групи
        storageRef.putFile(groupImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Успішне завантаження зображення
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> saveGroupToDatabase(groupName, uri.toString()));
                })
                .addOnFailureListener(e -> {
                    // Помилка завантаження зображення
                    createGroupProgressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateGroupActivity.this, R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show();
                });
    }

    // Метод для збереження групи в базі даних
    private void saveGroupToDatabase(String groupName, @Nullable String imageUrl) {
        // Генерація унікального ID для групи
        String groupId = db.collection("chatrooms").document().getId();
        List<String> userIds = new ArrayList<>();
        String currentUserId = auth.getCurrentUser().getUid();
        userIds.add(currentUserId);

        // Отримання даних поточного користувача
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            currentUserModel = task.getResult().toObject(UserModel.class);

            // Створення об'єкту групи
            GroupChatroomModel group = new GroupChatroomModel(
                    groupId,
                    userIds,
                    Timestamp.now(),
                    currentUserId,
                    groupName
            );

            // Додавання URL зображення групи, якщо воно існує
            if (imageUrl != null) {
                group.setGroupImageUrl(imageUrl);
            }

            // Збереження групи в базі даних
            db.collection("chatrooms").document(groupId).set(group)
                    .addOnSuccessListener(aVoid -> {
                        // Успішне збереження групи
                        db.collection("chatrooms").document(groupId).collection("admins").document(currentUserId).set(currentUserModel)
                                .addOnSuccessListener(adminVoid -> {
                                    // Додавання адміністратора групи
                                    db.collection("chatrooms").document(groupId).collection("members").document(currentUserId).set(currentUserModel)
                                            .addOnSuccessListener(memberVoid -> {
                                                // Додавання користувача до групи
                                                createGroupProgressBar.setVisibility(View.GONE);

                                                showToast(this, "Success");

                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Помилка додавання користувача до групи
                                                createGroupProgressBar.setVisibility(View.GONE);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    // Помилка додавання адміністратора групи
                                    createGroupProgressBar.setVisibility(View.GONE);
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Помилка збереження групи
                        createGroupProgressBar.setVisibility(View.GONE);
                    });
        });
    }
}
