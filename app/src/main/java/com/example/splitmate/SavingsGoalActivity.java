package com.example.splitmate;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SavingsGoalActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private String userEmail;

    private EditText goalNameEditText;
    private EditText goalAmountEditText;
    private TextView goalDateText;
    private Button saveGoalButton;
    private LinearLayout goalContainer;

    private Calendar selectedDate;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goal);

        MenuHelper.attach(this, findViewById(R.id.menuButton));

        databaseHelper = new DatabaseHelper(this);
        userEmail = SessionManager.resolveEmail(this);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        goalNameEditText = findViewById(R.id.goalNameEditText);
        goalAmountEditText = findViewById(R.id.goalAmountEditText);
        goalDateText = findViewById(R.id.goalDateText);
        saveGoalButton = findViewById(R.id.saveGoalButton);
        goalContainer = findViewById(R.id.goalContainer);

        goalDateText.setOnClickListener(v -> pickDate());
        saveGoalButton.setOnClickListener(v -> saveGoal());

        loadGoals();
    }


    private void pickDate() {

        Calendar base =
                selectedDate != null ? selectedDate : Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    goalDateText.setText(dateFormat.format(selectedDate.getTime()));
                },
                base.get(Calendar.YEAR),
                base.get(Calendar.MONTH),
                base.get(Calendar.DAY_OF_MONTH)
        ).show();
    }


    private void saveGoal() {

        String name = goalNameEditText.getText().toString().trim();
        String amountStr = goalAmountEditText.getText().toString().trim();

        if (name.isEmpty()) {
            goalNameEditText.setError("Enter a goal name");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                goalAmountEditText.setError("Enter a valid amount");
                return;
            }

        } catch (Exception e) {
            goalAmountEditText.setError("Enter a valid amount");
            return;
        }

        String targetDate =
                selectedDate != null
                        ? dateFormat.format(selectedDate.getTime())
                        : "";

        boolean saved =
                databaseHelper.setSavingsGoal(userEmail, name, amount, targetDate);

        if (saved) {

            Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show();

            goalNameEditText.setText("");
            goalAmountEditText.setText("");
            goalDateText.setText("Select date");
            selectedDate = null;

            loadGoals();

        } else {

            Toast.makeText(this, "Could not save goal", Toast.LENGTH_SHORT).show();
        }
    }


    // =========================================================
    // LOAD + RENDER ALL GOALS
    // =========================================================

    private void loadGoals() {

        goalContainer.removeAllViews();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            return;
        }

        Cursor cursor = databaseHelper.getSavingsGoals(userEmail);

        if (cursor == null) {
            return;
        }

        boolean hasGoals = false;

        while (cursor.moveToNext()) {
            hasGoals = true;

            long goalId = cursor.getLong(
                    cursor.getColumnIndexOrThrow("goal_id"));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("goal_name"));
            double target = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("target_amount"));
            double saved = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("saved_amount"));
            String targetDate = cursor.getString(
                    cursor.getColumnIndexOrThrow("target_date"));

            buildGoalCard(
                    goalId,
                    name,
                    target,
                    saved,
                    targetDate
            );
        }

        cursor.close();

        if (!hasGoals) {
            TextView empty = new TextView(this);
            empty.setText("No saving goals yet. Create your first goal above ✦");
            empty.setTextSize(14);
            empty.setTextColor(Color.rgb(107, 114, 117));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 25, 0, 25);
            goalContainer.addView(empty);
        }
    }

    private void buildGoalCard(
            long goalId,
            String name,
            double target,
            double saved,
            String targetDate) {

        CardView card = new CardView(this);
        card.setRadius(20);
        card.setCardElevation(4);
        card.setUseCompatPadding(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 4);
        card.setLayoutParams(cardParams);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(22, 20, 22, 20);

        // TITLE ROW

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView nameText = new TextView(this);
        nameText.setText("🎯 " + name);
        nameText.setTextSize(18);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setTextColor(Color.rgb(32, 33, 36));
        nameText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(nameText);

        TextView deleteIcon = new TextView(this);
        deleteIcon.setText("🗑");
        deleteIcon.setTextSize(18);
        deleteIcon.setPadding(20, 6, 6, 6);
        deleteIcon.setClickable(true);
        deleteIcon.setFocusable(true);
        deleteIcon.setOnClickListener(v -> confirmDeleteGoal(goalId));
        topRow.addView(deleteIcon);

        layout.addView(topRow);

        // PROGRESS TEXT

        int percent = target > 0 ? (int) Math.min(100, (saved / target) * 100) : 0;

        TextView progressText = new TextView(this);
        progressText.setText(
                "₹" + databaseHelper.formatAmount(saved) +
                        " saved of ₹" + databaseHelper.formatAmount(target) +
                        "  (" + percent + "%)"
        );
        progressText.setTextSize(14);
        progressText.setTextColor(Color.rgb(107, 114, 117));
        LinearLayout.LayoutParams progressTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progressTextParams.setMargins(0, 10, 0, 8);
        progressText.setLayoutParams(progressTextParams);
        layout.addView(progressText);

        // PROGRESS BAR (rounded track + rounded fill)

        FrameLayout track = new FrameLayout(this);

        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setColor(Color.rgb(230, 233, 234));
        trackBg.setCornerRadius(20);
        track.setBackground(trackBg);

        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                18
        );
        track.setLayoutParams(trackParams);

        View fill = new View(this);
        GradientDrawable fillBg = new GradientDrawable();
        fillBg.setColor(Color.rgb(35, 143, 131));
        fillBg.setCornerRadius(20);
        fill.setBackground(fillBg);

        // Draw the fill proportionally once the track has been measured
        track.post(() -> {
            int totalWidth = track.getWidth();
            FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(
                    Math.max(18, (int) (totalWidth * (percent / 100f))),
                    18
            );
            fill.setLayoutParams(fillParams);
        });

        track.addView(fill);
        layout.addView(track);

        // TARGET DATE

        if (targetDate != null && !targetDate.trim().isEmpty()) {

            TextView dateText = new TextView(this);
            dateText.setText("📅 Target: " + targetDate);
            dateText.setTextSize(12);
            dateText.setTextColor(Color.rgb(107, 114, 117));
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dateParams.setMargins(0, 10, 0, 0);
            dateText.setLayoutParams(dateParams);
            layout.addView(dateText);
        }

        // ADD MONEY BUTTON

        Button addMoneyButton = new Button(this);
        addMoneyButton.setText("＋ Add to Savings");
        addMoneyButton.setTextColor(Color.rgb(32, 33, 36));
        addMoneyButton.setTypeface(null, Typeface.BOLD);
        addMoneyButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#5EEAD4"))
        );
        LinearLayout.LayoutParams addButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addButtonParams.setMargins(0, 16, 0, 0);
        addMoneyButton.setLayoutParams(addButtonParams);
        addMoneyButton.setOnClickListener(v -> showAddMoneyDialog(goalId));
        layout.addView(addMoneyButton);

        card.addView(layout);
        goalContainer.addView(card);
    }

    private void showAddMoneyDialog(long goalId) {

        final EditText input = new EditText(this);
        input.setHint("₹ 0.00");
        input.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        input.setTextColor(Color.parseColor("#202124"));

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout frame = new FrameLayout(this);
        frame.setPadding(pad, pad / 2, pad, 0);
        frame.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Add to Savings")
                .setView(frame)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> {

                    String text = input.getText().toString().trim();

                    if (text.isEmpty()) {
                        return;
                    }

                    try {

                        double amount = Double.parseDouble(text);

                        if (amount > 0) {
                            databaseHelper.addToSavings(goalId, amount);
                            loadGoals();
                        }

                    } catch (NumberFormatException ignored) {
                    }
                })
                .show();
    }

    private void confirmDeleteGoal(long goalId) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("This will remove your savings goal and its progress.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    databaseHelper.deleteSavingsGoal(goalId);
                    loadGoals();
                })
                .show();
    }
}
