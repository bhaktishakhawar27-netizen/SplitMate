package com.example.splitmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonthlyDataActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout monthlyContainer;
    private String userEmail;
    private MonthlyBarChartView monthlyChart;

    private final int DARK = Color.rgb(32, 33, 36);
    private final int MINT = Color.rgb(47, 157, 143);
    private final int LIGHT_MINT = Color.rgb(217, 249, 243);
    private final int BACKGROUND = Color.rgb(245, 247, 247);
    private final int GREY = Color.rgb(108, 114, 117);
    private final int RED = Color.rgb(190, 60, 60);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_data);

        databaseHelper = new DatabaseHelper(this);
        monthlyContainer = findViewById(R.id.monthlyContainer);
        monthlyChart = findViewById(R.id.monthlyChart);
        userEmail = SessionManager.resolveEmail(this);

        if (userEmail == null || userEmail.trim().isEmpty()) {
            finish();
            return;
        }

        TextView backButton = findViewById(R.id.backButton);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        View menuButton = findViewById(R.id.menuButton);
        MenuHelper.attach(this, menuButton);

        Button addBudgetButton = findViewById(R.id.addExtraBudgetButton);
        addBudgetButton.setOnClickListener(v -> showExtraBudgetDialog());

        loadMonthlyData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (databaseHelper != null && userEmail != null) {
            loadMonthlyData();
        }
    }

    private void loadMonthlyData() {
        monthlyContainer.removeAllViews();

        Calendar current = Calendar.getInstance();
        current.set(Calendar.DAY_OF_MONTH, 1);

        List<String> labels = new ArrayList<>();
        List<Double> spentValues = new ArrayList<>();

        // Last 12 months, oldest -> newest for the chart.
        Calendar chartCalendar = (Calendar) current.clone();
        chartCalendar.add(Calendar.MONTH, -11);

        for (int i = 0; i < 12; i++) {
            int month = chartCalendar.get(Calendar.MONTH) + 1;
            int year = chartCalendar.get(Calendar.YEAR);

            labels.add(new SimpleDateFormat("MMM", Locale.ENGLISH)
                    .format(chartCalendar.getTime()));
            spentValues.add(getMonthlySpent(month, year));

            chartCalendar.add(Calendar.MONTH, 1);
        }

        monthlyChart.setData(labels, spentValues);

        int currentMonth = current.get(Calendar.MONTH) + 1;
        int currentYear = current.get(Calendar.YEAR);

        addSummaryCard(currentMonth, currentYear);
        addSectionTitle("Last 12 Months");

        // Newest month first in the detailed cards.
        Calendar cardCalendar = (Calendar) current.clone();
        for (int i = 0; i < 12; i++) {
            addMonthCard(
                    cardCalendar.get(Calendar.MONTH) + 1,
                    cardCalendar.get(Calendar.YEAR)
            );
            cardCalendar.add(Calendar.MONTH, -1);
        }
    }

    private void addSummaryCard(int month, int year) {
        double baseBudget = databaseHelper.getMonthlyBaseBudget(
                userEmail, month - 1, year
        );
        double extra = databaseHelper.getMonthlyBudgetAddition(
                userEmail, month - 1, year
        );
        double totalBudget = baseBudget + extra;
        double spent = getMonthlySpent(month, year);
        double received = databaseHelper.getMonthlyCredits(
                userEmail, month - 1, year
        );
        double remaining = totalBudget - spent + received;

        CardView card = createCard();

        LinearLayout content = verticalLayout(20);
        content.setBackgroundColor(Color.WHITE);

        TextView title = text(
                monthName(month) + " " + year,
                23,
                DARK,
                true
        );
        content.addView(title);

        TextView subtitle = text(
                "Your monthly money summary",
                13,
                GREY,
                false
        );
        content.addView(subtitle);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);
        stats.setPadding(0, 14, 0, 0);

        addStatRow(stats, "Total Budget", "₹" + money(totalBudget), DARK);
        addStatRow(stats, "Total Expense / Spent", "₹" + money(spent), DARK);
        addStatRow(stats, "Extra Budget Added", "₹" + money(extra), MINT);
        addStatRow(stats, "Money Received", "₹" + money(received), MINT);
        addStatRow(
                stats,
                remaining >= 0 ? "Remaining" : "Over Budget",
                "₹" + money(Math.abs(remaining)),
                remaining >= 0 ? MINT : RED
        );

        content.addView(stats);
        card.addView(content);
        monthlyContainer.addView(card);

        TextView graphTitle = text(
                "Monthly Spending",
                18,
                DARK,
                true
        );
        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        titleParams.setMargins(0, 6, 0, 8);
        graphTitle.setLayoutParams(titleParams);
        monthlyContainer.addView(graphTitle);
    }

    private void addStatRow(
            LinearLayout parent,
            String label,
            String value,
            int valueColor) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 6, 0, 6);

        TextView left = text(label, 14, GREY, false);
        left.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));

        TextView right = text(value, 15, valueColor, true);
        right.setGravity(Gravity.END);

        row.addView(left);
        row.addView(right);
        parent.addView(row);
    }

    private void addMonthCard(int month, int year) {
        String name = monthName(month);
        double baseBudget = databaseHelper.getMonthlyBaseBudget(
                userEmail, month - 1, year
        );
        double extra = databaseHelper.getMonthlyBudgetAddition(
                userEmail, month - 1, year
        );
        double budget = baseBudget + extra;
        double spent = getMonthlySpent(month, year);
        double received = databaseHelper.getMonthlyCredits(
                userEmail, month - 1, year
        );
        double remaining = budget - spent + received;

        CardView card = createCard();
        LinearLayout content = verticalLayout(18);

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text(name + " " + year, 19, DARK, true);
        title.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));
        heading.addView(title);

        TextView spentBadge = text(
                "₹" + money(spent),
                15,
                MINT,
                true
        );
        heading.addView(spentBadge);
        content.addView(heading);

        addMiniRow(content, "Budget", "₹" + money(budget));
        addMiniRow(content, "Total Expense / Spent", "₹" + money(spent));
        addMiniRow(content, "Extra Added", "₹" + money(extra));
        if (received > 0) {
            addMiniRow(content, "Received from split bills", "₹" + money(received));
        }

        TextView remainingText = text(
                remaining >= 0
                        ? "Remaining: ₹" + money(remaining)
                        : "Over budget: ₹" + money(Math.abs(remaining)),
                14,
                remaining >= 0 ? MINT : RED,
                true
        );
        LinearLayout.LayoutParams remParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        remParams.setMargins(0, 9, 0, 0);
        remainingText.setLayoutParams(remParams);
        content.addView(remainingText);

        card.addView(content);
        monthlyContainer.addView(card);
    }

    private void addMiniRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView left = text(label, 13, GREY, false);
        left.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));

        TextView right = text(value, 13, DARK, true);
        right.setGravity(Gravity.END);

        row.addView(left);
        row.addView(right);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 4, 0, 4);
        row.setLayoutParams(params);

        parent.addView(row);
    }

    private void addSectionTitle(String titleText) {
        TextView title = text(titleText, 18, DARK, true);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 8, 0, 8);
        title.setLayoutParams(params);
        monthlyContainer.addView(title);
    }

    private CardView createCard() {
        CardView card = new CardView(this);
        card.setRadius(20);
        card.setCardElevation(3);
        card.setUseCompatPadding(true);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 0, 0, 14);
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout verticalLayout(int padding) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, padding);
        return layout;
    }

    private TextView text(
            String value,
            float size,
            int color,
            boolean bold) {

        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setTypeface(null, bold
                ? android.graphics.Typeface.BOLD
                : android.graphics.Typeface.NORMAL);
        return tv;
    }

    private void showExtraBudgetDialog() {
        Calendar now = Calendar.getInstance();
        String monthYear = monthName(now.get(Calendar.MONTH) + 1)
                + " " + now.get(Calendar.YEAR);

        final EditText amountInput = new EditText(this);
        amountInput.setHint("e.g. 1000");
        amountInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        final EditText noteInput = new EditText(this);
        noteInput.setHint("Reason (optional)");
        noteInput.setSingleLine(true);

        LinearLayout form = verticalLayout(8);
        form.addView(amountInput);
        form.addView(noteInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Extra Budget")
                .setMessage("Add money on top of your " + monthYear + " budget.")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add Amount", null)
                .create();

        dialog.setOnShowListener(d -> {
                    AlertDialog alert = (AlertDialog) d;
                    alert.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener(v -> {
                                String raw = amountInput.getText().toString().trim();

                                if (raw.isEmpty()) {
                                    amountInput.setError("Enter an amount");
                                    return;
                                }

                                double amount;
                                try {
                                    amount = Double.parseDouble(raw);
                                } catch (Exception e) {
                                    amountInput.setError("Enter a valid amount");
                                    return;
                                }

                                if (amount <= 0) {
                                    amountInput.setError("Amount must be greater than 0");
                                    return;
                                }

                                String note = noteInput.getText().toString().trim();
                                String date = new SimpleDateFormat(
                                        "dd MMM yyyy",
                                        Locale.ENGLISH
                                ).format(new Date());

                                boolean saved = databaseHelper.addMonthlyBudgetAddition(
                                        userEmail,
                                        now.get(Calendar.MONTH),
                                        now.get(Calendar.YEAR),
                                        amount,
                                        note,
                                        date
                                );

                                if (saved) {
                                    Toast.makeText(
                                            this,
                                            "₹" + money(amount) + " added to this month's budget.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    loadMonthlyData();
                                    dialog.dismiss();
                                }
                            });
                });
        dialog.show();
    }

    private double getMonthlySpent(int month, int year) {
        double total = 0;
        Cursor cursor = null;

        try {
            cursor = databaseHelper.getAllExpenses(userEmail);
            if (cursor == null) return 0;

            int amountIndex = cursor.getColumnIndexOrThrow("amount");
            int dateIndex = cursor.getColumnIndexOrThrow("date");

            while (cursor.moveToNext()) {
                String date = cursor.getString(dateIndex);
                if (isDateInMonth(date, month, year)) {
                    total += cursor.getDouble(amountIndex);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    private boolean isDateInMonth(String date, int month, int year) {
        if (date == null || date.trim().isEmpty()) return false;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.ENGLISH
            );
            sdf.setLenient(false);
            Date parsed = sdf.parse(date);
            if (parsed == null) return false;

            Calendar c = Calendar.getInstance();
            c.setTime(parsed);

            return c.get(Calendar.MONTH) + 1 == month
                    && c.get(Calendar.YEAR) == year;
        } catch (Exception e) {
            return false;
        }
    }

    private String monthName(int month) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, month - 1);
        return new SimpleDateFormat(
                "MMMM",
                Locale.ENGLISH
        ).format(c.getTime());
    }

    private String money(double amount) {
        return databaseHelper.formatAmount(amount);
    }
}
