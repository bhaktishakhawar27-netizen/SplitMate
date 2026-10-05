package com.example.splitmate;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_PAYMENTS = "splitmate_payments";

    private static void createChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_PAYMENTS,
                    "Payments",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Alerts for expenses, payments and split bills");

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Shows a payment notification. Silently does nothing if the user
     * has not granted the notification permission (Android 13+).
     */
    public static void notifyPayment(Context context, String title, String message) {

        if (Build.VERSION.SDK_INT >= 33 &&
                ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createChannel(context);

        Intent open = new Intent(context, HomeActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                id,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_PAYMENTS)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pi);

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.notify(id, builder.build());
        }
    }
}
