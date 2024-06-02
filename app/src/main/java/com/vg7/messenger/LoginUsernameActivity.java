package com.vg7.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginUsernameActivity extends AppCompatActivity {

    EditText usernameInput;
    Button letMeInBtn;
    ProgressBar progressBar;
    String phoneNumber;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_username);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Ініціалізуємо елементи керування
        usernameInput = findViewById(R.id.login_username);
        letMeInBtn = findViewById(R.id.login_let_me_in_btn);
        progressBar =findViewById(R.id.login_progress_bar);

        // Отримуємо номер телефону з попередньої активності
        phoneNumber = getIntent().getExtras().getString("phone");

        // Налаштовуємо кнопку для збереження імені користувача
        letMeInBtn.setOnClickListener((v -> {
            setUsername();
        }));

        // Отримуємо або встановлюємо ім'я користувача при завантаженні активності
        getUsername();
    }

    // Метод для збереження імені користувача
    void setUsername(){

        // Отримуємо введене ім'я користувача з поля введення
        String username = usernameInput.getText().toString();

        // Перевіряємо, чи введено правильне ім'я користувача
        if (username.isEmpty() || username.length()<3) {
            usernameInput.setError("Username length should be at least 3 chars");
            return;
        }

        // Встановлюємо прогрес під час збереження
        setInProgress(true);

        // Створюємо або оновлюємо модель користувача з новим іменем
        if (userModel!=null) {
            userModel.setUsername(username);
        }
        else {
            userModel = new UserModel(phoneNumber, username, "", Timestamp.now(), FirebaseUtil.currentUserId(), false);
        }

        // Зберігаємо дані користувача в базі даних Firebase Firestore
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    // Переходимо до головного екрану після успішного збереження
                    Intent intent = new Intent(LoginUsernameActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity(intent);
                }
            }
        });

    }

    // Метод для отримання імені користувача з бази даних
    void getUsername(){
        // Встановлюємо прогрес під час завантаження
        setInProgress(true);

        // Отримуємо дані користувача з бази даних Firebase Firestore
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    userModel =    task.getResult().toObject(UserModel.class);
                    if(userModel!=null){
                        // Заповнюємо поле введення імені користувача з бази даних
                        usernameInput.setText(userModel.getUsername());
                    }
                }
            }
        });
    }

    // Метод для відображення або приховання прогресу
    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            letMeInBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            letMeInBtn.setVisibility(View.VISIBLE);
        }
    }
}
