package com.vg7.messenger.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vg7.messenger.model.UserModel;

public class AndroidUtil {

    // Метод для відображення короткого повідомлення
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // Метод для передачі об'єкту UserModel через Intent
    public static void passUserModelAsIntent(Intent intent, UserModel model) {
        intent.putExtra("username", model.getUsername());
        intent.putExtra("phone", model.getPhone());
        intent.putExtra("status", model.getStatus());
        intent.putExtra("hideNumberValue", model.getHideNumberValue());
        intent.putExtra("userId", model.getUserId());
        intent.putExtra("fcmToken", model.getFcmToken());
    }

    // Метод для отримання об'єкту UserModel з Intent
    public static UserModel getUserModelFromIntent(Intent intent) {
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setStatus(intent.getStringExtra("status"));
        userModel.setHideNumberValue(intent.getBooleanExtra("hideNumberValue", false));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    // Метод для встановлення зображення профілю в ImageView
    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        // Використання бібліотеки Glide для завантаження та відображення зображення
        Glide.with(context)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform()) // Обрізка зображення у формі кола
                .into(imageView);
    }

    // Метод для встановлення зображення профілю в ImageView
    public static void setGroupPic(Context context, Uri imageUri, ImageView imageView) {
        // Використання бібліотеки Glide для завантаження та відображення зображення
        Glide.with(context)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform()) // Обрізка зображення у формі кола
                .into(imageView);
    }
}
