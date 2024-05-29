package com.vg7.messenger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {

    // Інтерфейс для відображення профілю
    ImageView profilePic;
    // Ввідні поля для зміни імені користувача
    EditText usernameInput;
    // Ввідні поля для зміни телефону користувача
    EditText phoneInput;
    // Ввідні поля для зміни статусу користувача
    EditText statusInput;
    // Кнопка для збереження змін
    Button updateProfileBtn;
    // ProgressBar для відображення процесу змін
    ProgressBar progressBar;
    // Кнопка для виходу з системи
    TextView logoutBtn;

    // Модель користувача, який зараз залогований
    UserModel currentUserModel;
    // Запустовка ланчер для вибору зображення
    ActivityResultLauncher<Intent> imagePickLauncher;
    // URI вибраного зображення
    Uri selectedImageUri;

    public ProfileFragment() {
        // Конструктор класу
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Створити ланчер для вибору зображення
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            AndroidUtil.setProfilePic(getContext(), selectedImageUri, profilePic);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Отримати інфлятор для відображення відокремленого блоку
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        statusInput = view.findViewById(R.id.profile_status);
        updateProfileBtn = view.findViewById(R.id.profle_update_btn);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.logout_btn);

        // Отримати дані користувача
        getUserData();

        // Слушатися за кліком на кнопку "Обновить профіль"
        updateProfileBtn.setOnClickListener((v -> {
            updateBtnClick();
        }));

        // Слушатися за кліком на кнопку "Вихід"
        logoutBtn.setOnClickListener((v) -> {
            Dialog dialog = new Dialog(getContext(), R.style.dialogue);
            dialog.setContentView(R.layout.layout_logout_dialog);

            Button no, yes;
            yes = dialog.findViewById(R.id.yesBtn);
            no = dialog.findViewById(R.id.noBtn);

            yes.setOnClickListener((b) -> {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseUtil.logout();
                            Intent intent = new Intent(getContext(), SplashActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                });
            });
            no.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        });

        // Слушатися за кліком на фото профілю
        profilePic.setOnClickListener((v)->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

        return view;
    }

    void updateBtnClick(){
        // Отримати нове ім'я користувача
        String newUsername = usernameInput.getText().toString();
        // Отримати новий статус користувача
        String newStatus = statusInput.getText().toString();
        if(newUsername.isEmpty() || newUsername.length() < 3){
            usernameInput.setError(getString(R.string.username_length_should_be_at_least_3_chars));
            return;
        }
        // Змінити дані користувача в модельному об'єкті
        currentUserModel.setUsername(newUsername);
        currentUserModel.setStatus(newStatus);
        setInProgress(true);

        if(selectedImageUri!=null){
            // Загрузити вибране зображення в Firebase Storage
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri)
                    .addOnCompleteListener(task -> {
                        updateToFirestore();
                    });
        }else{
            updateToFirestore();
        }
    }

    void updateToFirestore(){
        // Оновити дані користувача в Firebase Firestore
        FirebaseUtil.currentUserDetails().set(currentUserModel)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if(task.isSuccessful()){
                        AndroidUtil.showToast(getContext(),getString(R.string.updated_successfully));
                    }else{
                        AndroidUtil.showToast(getContext(),getString(R.string.updated_failed));
                    }
                });
    }

    void getUserData(){
        setInProgress(true);

        // Отримати зображення профілю з Firebase Storage
        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Uri uri  = task.getResult();
                        AndroidUtil.setProfilePic(getContext(),uri,profilePic);
                    }
                });

        // Отримати дані користувача з Firebase Firestore
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            currentUserModel = task.getResult().toObject(UserModel.class);
            usernameInput.setText(currentUserModel.getUsername());
            phoneInput.setText(currentUserModel.getPhone());
            statusInput.setText(currentUserModel.getStatus());
        });
    }

    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            updateProfileBtn.setVisibility(View.VISIBLE);
        }
    }
}