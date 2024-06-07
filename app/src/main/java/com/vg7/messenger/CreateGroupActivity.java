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

public class CreateGroupActivity extends AppCompatActivity {

    ImageView groupImageView;
    EditText groupNameEditText;
    Button createGroupButton;
    ProgressBar createGroupProgressBar;

    Uri groupImageUri;

    FirebaseFirestore db;
    FirebaseStorage storage;
    FirebaseAuth auth;
    UserModel currentUserModel;
    ActivityResultLauncher<Intent> imagePickLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        groupImageView = findViewById(R.id.create_group_image_view);
        groupNameEditText = findViewById(R.id.group_name);
        createGroupButton = findViewById(R.id.create_group_btn);
        createGroupProgressBar = findViewById(R.id.create_group_progress_bar);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        groupImageUri = result.getData().getData();
                        AndroidUtil.setGroupPic(CreateGroupActivity.this, groupImageUri, groupImageView);
                    }
                }
        );

        groupImageView.setOnClickListener(v -> openFileChooser());
        createGroupButton.setOnClickListener(v -> createGroup());
        createGroupProgressBar.setVisibility(View.GONE);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
    }

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

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();

        if (groupName.isEmpty()) {
            groupNameEditText.setError(getString(R.string.group_name_is_required));
            groupNameEditText.requestFocus();
            return;
        } else if (groupName.length() < 4) {
            groupNameEditText.setError(getString(R.string.group_name_is_required2));
            groupNameEditText.requestFocus();
            return;
        }

        createGroupButton.setVisibility(View.GONE);
        createGroupProgressBar.setVisibility(View.VISIBLE);

        if (groupImageUri != null) {
            uploadGroupImage(groupName);
        } else {
            saveGroupToDatabase(groupName, null);
        }
    }

    private void uploadGroupImage(String groupName) {
        StorageReference storageRef = storage.getReference("group_pic/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(groupImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> saveGroupToDatabase(groupName, uri.toString())))
                .addOnFailureListener(e -> {
                    createGroupProgressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateGroupActivity.this, R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show();
                });
    }

    private void saveGroupToDatabase(String groupName, @Nullable String imageUrl) {
        String groupId = db.collection("chatrooms").document().getId();
        List<String> userIds = new ArrayList<>();
        String currentUserId = auth.getCurrentUser().getUid();
        userIds.add(currentUserId);

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            currentUserModel = task.getResult().toObject(UserModel.class);
        });

        GroupChatroomModel group = new GroupChatroomModel(
                groupId,
                userIds,
                Timestamp.now(),
                currentUserId,
                groupName
        );

        if (imageUrl != null) {
            group.setGroupImageUrl(imageUrl);
        }

        db.collection("chatrooms").document(groupId).set(group)
                .addOnSuccessListener(aVoid -> {
                    db.collection("chatrooms").document(groupId).collection("admins").document(currentUserId).set(currentUserModel)
                            .addOnSuccessListener(adminVoid -> {
                                db.collection("chatrooms").document(groupId).collection("members").document(currentUserId).set(currentUserModel)
                                        .addOnSuccessListener(memberVoid -> {
                                            createGroupProgressBar.setVisibility(View.GONE);

                                            showToast(this, "Success");

                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            createGroupProgressBar.setVisibility(View.GONE);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                createGroupProgressBar.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    createGroupProgressBar.setVisibility(View.GONE);
                });
    }
}
