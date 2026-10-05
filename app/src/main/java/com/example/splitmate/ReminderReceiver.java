package com.example.splitmate;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "splitmate_payment_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title =
                intent.getStringExtra("title");

        String amount =
                intent.getStringExtra("amount");

        long reminderId =
                intent.getLongExtra("reminderId", 0);

        if (title == null) {
            title = "Payment Reminder";
        }

        String message =
                (amount != null && !amount.isEmpty())
                        ? "₹" + amount + " is due now"
                        : "You have a payment due";


        createChannel(context);


        Intent openApp =
                new Intent(context, HomeActivity.class);

        openApp.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent contentIntent =
                PendingIntent.getActivity(
                        context,
                        (int) reminderId,
                        openApp,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);


        // Android 13+ requires this runtime permission; if it hasn't
        // been granted we simply skip showing the notification rather
        // than crashing.
        if (Build.VERSION.SDK_INT >= 33 &&
                ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (manager != null) {
            manager.notify((int) reminderId, builder.build());
        }
    }


    private void createChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Payment Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Alerts you when a saved payment reminder is due"
            );

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
