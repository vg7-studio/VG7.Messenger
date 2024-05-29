package com.vg7.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Створити діяльність
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Встановити орієнтацію екрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Перевіряти, чи є extras в intents
        if (getIntent().getExtras() != null) {
            // зagram з оповіщення
            String userId = getIntent().getExtras().getString("userId");
            FirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            // Якщо операція успішна
                            if (task.isSuccessful()) {
                                UserModel model = task.getResult().toObject(UserModel.class);

                                // Створити новий intent на MainActivity
                                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                // Запустити MainActivity
                                startActivity(mainIntent);

                                // Створити новий intent на ChatActivity
                                Intent intent = new Intent(SplashActivity.this, ChatActivity.class);
                                AndroidUtil.passUserModelAsIntent(intent, model);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // Запустити ChatActivity
                                startActivity(intent);
                                // Закрити цю діяльність
                                finish();
                            }
                        }
                    });
        } else {
            // Створити новий об'єкт Handler
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Перевіряти, чи користувач залогований
                    if (FirebaseUtil.isLoggedIn()) {
                        // Запустити MainActivity
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        // Запустити LoginPhoneNumberActivity
                        startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
                    }
                    // Закрити цю діяльність
                    finish();
                }
            }, 2000);
        }
    }
}