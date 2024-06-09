package com.vg7.messenger.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import androidx.core.app.NotificationManagerCompat;

import com.vg7.messenger.R;

// Утиліта для роботи з повідомленнями
public class NotificationUtils {

    // Перевірити дозвіл на повідомлення
    public static void checkNotificationPermission(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        // Якщо повідомлення вимкнено, відобразити діалогове вікно
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            showNotificationPermissionDialog(context);
        }
    }

    // Відобразити діалогове вікно про дозвіл на повідомлення
    private static void showNotificationPermissionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.enable_notifications) // Заголовок діалогового вікна
                .setMessage(R.string.notifications_are_disabled_message) // Повідомлення діалогового вікна
                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Відкрити налаштування додатку для дозволу на повідомлення
                        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss(); // Закрити діалогове вікно
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show(); // Показати діалогове вікно
    }
}
