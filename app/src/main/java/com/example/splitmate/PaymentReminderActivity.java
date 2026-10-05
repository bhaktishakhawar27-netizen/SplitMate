package com.example.splitmate;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PaymentReminderActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private String userEmail;

    private EditText titleEditText;
    private EditText amountEditText;
    private EditText notesEditText;
    private TextView dateText;
    private TextView timeText;
    private Button saveButton;
    private LinearLayout reminderContainer;

    private Calendar selectedDate;
    private Calendar selectedTime;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("hh:mm a", Locale.ENGLISH);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_reminder);

        MenuHelper.attach(this, findViewById(R.id.menuButton));

        databaseHelper = new DatabaseHelper(this);
        userEmail = SessionManager.resolveEmail(this);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        titleEditText = findViewById(R.id.reminderTitleEditText);
        amountEditText = findViewById(R.id.reminderAmountEditText);
        notesEditText = findViewById(R.id.reminderNotesEditText);
        dateText = findViewById(R.id.reminderDateText);
        timeText = findViewById(R.id.reminderTimeText);
        saveButton = findViewById(R.id.saveReminderButton);
        reminderContainer = findViewById(R.id.reminderContainer);

        requestNotificationPermissionIfNeeded();

        dateText.setOnClickListener(v -> pickDate());
        timeText.setOnClickListener(v -> pickTime());
        saveButton.setOnClickListener(v -> saveReminder());

        loadReminders();
    }


    // =========================================================
    // NOTIFICATION PERMISSION (Android 13+)
    // =========================================================

    private void requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT >= 33 &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    101
            );
        }
    }


    // =========================================================
    // DATE / TIME PICKERS
    // =========================================================

    private void pickDate() {

        Calendar base =
                selectedDate != null ? selectedDate : Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    dateText.setText(dateFormat.format(selectedDate.getTime()));
                },
                base.get(Calendar.YEAR),
                base.get(Calendar.MONTH),
                base.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickTime() {

        Calendar base =
                selectedTime != null ? selectedTime : Calendar.getInstance();

        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    timeText.setText(timeFormat.format(selectedTime.getTime()));
                },
                base.get(Calendar.HOUR_OF_DAY),
                base.get(Calendar.MINUTE),
                false
        ).show();
    }


    // =========================================================
    // SAVE REMINDER
    // =========================================================

    private void saveReminder() {

        String title = titleEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Enter a title");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                amountEditText.setError("Enter a valid amount");
                return;
            }

        } catch (Exception e) {
            amountEditText.setError("Enter a valid amount");
            return;
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime == null) {
            Toast.makeText(this, "Please select a due time", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar dueDateTime = Calendar.getInstance();
        dueDateTime.set(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                0
        );

        long reminderId = databaseHelper.addReminder(
                userEmail,
                title,
                amount,
                dateFormat.format(selectedDate.getTime()),
                timeFormat.format(selectedTime.getTime()),
                notes
        );

        if (reminderId == -1) {
            Toast.makeText(this, "Could not save reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        scheduleAlarm(reminderId, title, databaseHelper.formatAmount(amount), dueDateTime);

        Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show();

        titleEditText.setText("");
        amountEditText.setText("");
        notesEditText.setText("");
        dateText.setText("Select date");
        timeText.setText("Select time");
        selectedDate = null;
        selectedTime = null;

        loadReminders();
    }


    // =========================================================
    // ALARM SCHEDULING
    // =========================================================

    private void scheduleAlarm(
            long reminderId,
            String title,
            String amountFormatted,
            Calendar dueDateTime) {

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("amount", amountFormatted);
        intent.putExtra("reminderId", reminderId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {

            // Using set() (not setExact()) so no special "exact alarm"
            // permission is required on Android 12+. The reminder may
            // fire a little late, which is fine for a bill reminder.
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    dueDateTime.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private void cancelAlarm(long reminderId) {

        Intent intent = new Intent(this, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


    // =========================================================
    // LOAD + RENDER REMINDER LIST
    // =========================================================

    private void loadReminders() {

        reminderContainer.removeAllViews();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            return;
        }

        Cursor cursor = databaseHelper.getReminders(userEmail);

        if (cursor == null || cursor.getCount() == 0) {

            showEmpty();

            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        int idIndex = cursor.getColumnIndexOrThrow("reminder_id");
        int titleIndex = cursor.getColumnIndexOrThrow("title");
        int amountIndex = cursor.getColumnIndexOrThrow("amount");
        int dateIndex = cursor.getColumnIndexOrThrow("due_date");
        int timeIndex = cursor.getColumnIndexOrThrow("due_time");
        int notesIndex = cursor.getColumnIndexOrThrow("notes");

        while (cursor.moveToNext()) {

            addReminderCard(
                    cursor.getLong(idIndex),
                    cursor.getString(titleIndex),
                    cursor.getDouble(amountIndex),
                    cursor.getString(dateIndex),
                    cursor.getString(timeIndex),
                    cursor.getString(notesIndex)
            );
        }

        cursor.close();
    }

    private void addReminderCard(
            long reminderId,
            String title,
            double amount,
            String date,
            String time,
            String notes) {

        CardView card = new CardView(this);
        card.setRadius(18);
        card.setCardElevation(3);
        card.setUseCompatPadding(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 12);
        card.setLayoutParams(cardParams);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(18, 16, 18, 16);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(16);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setTextColor(Color.rgb(32, 33, 36));
        titleText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(titleText);

        TextView deleteIcon = new TextView(this);
        deleteIcon.setText("🗑");
        deleteIcon.setTextSize(18);
        deleteIcon.setPadding(20, 6, 6, 6);
        deleteIcon.setClickable(true);
        deleteIcon.setFocusable(true);
        deleteIcon.setOnClickListener(v -> confirmDelete(reminderId));
        topRow.addView(deleteIcon);

        layout.addView(topRow);

        TextView amountText = new TextView(this);
        amountText.setText("₹" + databaseHelper.formatAmount(amount));
        amountText.setTextSize(19);
        amountText.setTypeface(null, Typeface.BOLD);
        amountText.setTextColor(Color.rgb(35, 143, 131));
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        amountParams.setMargins(0, 4, 0, 4);
        amountText.setLayoutParams(amountParams);
        layout.addView(amountText);

        TextView dueText = new TextView(this);
        dueText.setText("⏰ Due: " + date + " • " + time);
        dueText.setTextSize(13);
        dueText.setTextColor(Color.rgb(107, 114, 117));
        layout.addView(dueText);

        if (notes != null && !notes.trim().isEmpty()) {

            TextView notesText = new TextView(this);
            notesText.setText(notes);
            notesText.setTextSize(13);
            notesText.setTextColor(Color.rgb(107, 114, 117));
            LinearLayout.LayoutParams notesParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            notesParams.setMargins(0, 6, 0, 0);
            notesText.setLayoutParams(notesParams);
            layout.addView(notesText);
        }

        card.addView(layout);
        reminderContainer.addView(card);
    }

    private void confirmDelete(long reminderId) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Remove this reminder?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    cancelAlarm(reminderId);
                    databaseHelper.deleteReminder(reminderId);
                    loadReminders();
                })
                .show();
    }

    private void showEmpty() {

        TextView text = new TextView(this);
        text.setText("No reminders yet 🔔");
        text.setTextSize(15);
        text.setTextColor(Color.GRAY);
        text.setGravity(Gravity.CENTER);
        text.setPadding(10, 40, 10, 40);
        reminderContainer.addView(text);
    }
}
