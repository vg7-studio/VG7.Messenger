package com.vg7.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.hbb20.CountryCodePicker;

public class LoginPhoneNumberActivity extends AppCompatActivity {

    CountryCodePicker countryCodePicker;
    EditText phoneInput;
    Button sendOtpBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_number);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Ініціалізуємо елементи керування
        countryCodePicker = findViewById(R.id.login_countrycode);
        phoneInput = findViewById(R.id.login_mobile_number);
        sendOtpBtn = findViewById(R.id.send_otp_btn);
        progressBar = findViewById(R.id.login_progress_bar);

        // Приховуємо прогрес-бар на початку
        progressBar.setVisibility(View.GONE);

        // Реєструємо поле введення номера телефону для країнного коду
        countryCodePicker.registerCarrierNumberEditText(phoneInput);

        // Налаштовуємо прослуховувач кнопки відправки OTP-коду
        sendOtpBtn.setOnClickListener((v)->{
            // Перевіряємо, чи введено правильний номер телефону
            if(!countryCodePicker.isValidFullNumber()){
                phoneInput.setError(getString(R.string.phone_number_not_valid));
                return;
            }
            // Відправляємо номер телефону на активність для введення OTP-коду
            Intent intent = new Intent(LoginPhoneNumberActivity.this,LoginOtpActivity.class);
            intent.putExtra("phone",countryCodePicker.getFullNumberWithPlus());
            startActivity(intent);
        });
    }
}
