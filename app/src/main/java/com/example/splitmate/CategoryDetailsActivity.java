package com.example.splitmate;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Locale;

public class CategoryDetailsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout expenseContainer;

    private String userEmail;
    private String selectedCategory;

    private TextView categoryTitle;
    private TextView categoryTotal;

    private final int DARK = Color.rgb(32, 33, 36);
    private final int GREY = Color.rgb(108, 114, 117);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category_details);

        MenuHelper.attach(this, findViewById(R.id.menuButton));

        databaseHelper = new DatabaseHelper(this);

        userEmail = SessionManager.resolveEmail(this);
        selectedCategory = getIntent().getStringExtra("category");

        if (userEmail == null || userEmail.trim().isEmpty()
                || selectedCategory == null
                || selectedCategory.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Category information not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        categoryTitle = findViewById(R.id.categoryTitle);
        categoryTotal = findViewById(R.id.categoryTotal);
        expenseContainer = findViewById(R.id.expenseContainer);

        categoryTitle.setText(
                selectedCategory + " Expenses"
        );

        findViewById(R.id.backButton).setOnClickListener(
                v -> finish()
        );

        findViewById(R.id.menuButton).setOnClickListener(
                this::showMenu
        );

        loadCategoryExpenses();
    }


    // =========================================================
    // LOAD CATEGORY EXPENSES
    // =========================================================

    private void loadCategoryExpenses() {

        expenseContainer.removeAllViews();

        double total = 0;
        int count = 0;

        Cursor cursor = null;

        try {

            cursor = databaseHelper.getAllExpenses(userEmail);

            if (cursor == null) {
                showEmptyMessage();
                categoryTotal.setText("Total Spent: ₹0");
                return;
            }

            int amountIndex =
                    cursor.getColumnIndexOrThrow("amount");

            int categoryIndex =
                    cursor.getColumnIndexOrThrow("category");

            int descriptionIndex =
                    cursor.getColumnIndexOrThrow("description");

            int dateIndex =
                    cursor.getColumnIndexOrThrow("date");

            int timeIndex =
                    cursor.getColumnIndexOrThrow("time");

            int otherCategoryIndex =
                    cursor.getColumnIndex("other_category");


            while (cursor.moveToNext()) {

                String category =
                        cursor.getString(categoryIndex);

                if (category == null ||
                        !category.equalsIgnoreCase(
                                selectedCategory
                        )) {

                    continue;
                }

                double amount =
                        cursor.getDouble(amountIndex);

                String description =
                        cursor.getString(descriptionIndex);

                String date =
                        cursor.getString(dateIndex);

                String time =
                        cursor.getString(timeIndex);

                String otherCategory = "";

                if (otherCategoryIndex != -1) {

                    otherCategory =
                            cursor.getString(
                                    otherCategoryIndex
                            );
                }


                total += amount;
                count++;


                addExpenseCard(
                        amount,
                        description,
                        date,
                        time,
                        otherCategory
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Unable to load expenses",
                    Toast.LENGTH_SHORT
            ).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }


        categoryTotal.setText(
                "Total Spent: ₹" +
                        formatAmount(total)
        );


        if (count == 0) {

            showEmptyMessage();
        }
    }


    // =========================================================
    // ADD EXPENSE CARD
    // =========================================================

    private void addExpenseCard(
            double amount,
            String description,
            String date,
            String time,
            String otherCategory
    ) {

        CardView card =
                new CardView(this);

        card.setRadius(18);
        card.setCardElevation(4);
        card.setUseCompatPadding(true);


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                14
        );

        card.setLayoutParams(cardParams);


        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                18,
                16,
                18,
                16
        );


        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        TextView descriptionText =
                new TextView(this);

        if (description == null ||
                description.trim().isEmpty()) {

            descriptionText.setText(
                    "Expense"
            );

        } else {

            descriptionText.setText(
                    description
            );
        }

        descriptionText.setTextSize(17);
        descriptionText.setTextColor(DARK);
        descriptionText.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(
                descriptionText
        );


        // -----------------------------------------------------
        // AMOUNT
        // -----------------------------------------------------

        TextView amountText =
                new TextView(this);

        amountText.setText(
                "₹" +
                        formatAmount(amount)
        );

        amountText.setTextSize(21);
        amountText.setTextColor(DARK);
        amountText.setTypeface(
                null,
                Typeface.BOLD
        );


        LinearLayout.LayoutParams amountParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        amountParams.setMargins(
                0,
                7,
                0,
                5
        );

        amountText.setLayoutParams(
                amountParams
        );

        content.addView(
                amountText
        );


        // -----------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------

        String categoryDisplay =
                selectedCategory;

        if (selectedCategory.equalsIgnoreCase("Other")
                && otherCategory != null
                && !otherCategory.trim().isEmpty()) {

            categoryDisplay =
                    "Other • " +
                            otherCategory;
        }


        TextView categoryText =
                createSmallText(
                        "Category: " +
                                categoryDisplay
                );

        content.addView(
                categoryText
        );


        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        TextView dateText =
                createSmallText(
                        "📅 " +
                                safeText(date)
                );

        content.addView(
                dateText
        );


        // -----------------------------------------------------
        // TIME
        // -----------------------------------------------------

        TextView timeText =
                createSmallText(
                        "⏰ " +
                                safeText(time)
                );

        content.addView(
                timeText
        );


        card.addView(
                content
        );

        expenseContainer.addView(
                card
        );
    }


    // =========================================================
    // SMALL TEXT
    // =========================================================

    private TextView createSmallText(
            String text
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setTextSize(14);
        textView.setTextColor(GREY);


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                3,
                0,
                0
        );

        textView.setLayoutParams(
                params
        );

        return textView;
    }


    // =========================================================
    // EMPTY MESSAGE
    // =========================================================

    private void showEmptyMessage() {

        TextView emptyText =
                new TextView(this);

        emptyText.setText(
                "No expenses found in this category 💸"
        );

        emptyText.setTextSize(16);
        emptyText.setTextColor(GREY);
        emptyText.setGravity(
                Gravity.CENTER
        );

        emptyText.setPadding(
                10,
                35,
                10,
                35
        );

        expenseContainer.addView(
                emptyText
        );
    }


    // =========================================================
    // MENU
    // =========================================================

    private void showMenu(View anchor) {

        PopupMenu popupMenu =
                new PopupMenu(
                        this,
                        anchor
                );


        popupMenu.getMenu().add(
                "Home"
        );

        popupMenu.getMenu().add(
                "Monthly Data"
        );

        popupMenu.getMenu().add(
                "Split Bill History"
        );

        popupMenu.getMenu().add(
                "All Expenses"
        );

        popupMenu.getMenu().add(
                "Logout"
        );


        popupMenu.setOnMenuItemClickListener(
                item -> {

                    String selected =
                            item.getTitle()
                                    .toString();


                    // -------------------------------------------------
                    // HOME
                    // -------------------------------------------------

                    if (selected.equals("Home")) {

                        Intent intent =
                                new Intent(
                                        this,
                                        HomeActivity.class
                                );

                        intent.putExtra(
                                "email",
                                userEmail
                        );

                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                        );

                        startActivity(intent);

                        finish();

                        return true;
                    }


                    // -------------------------------------------------
                    // MONTHLY DATA
                    // -------------------------------------------------

                    if (selected.equals(
                            "Monthly Data"
                    )) {

                        Intent intent =
                                new Intent(
                                        this,
                                        MonthlyDataActivity.class
                                );

                        intent.putExtra(
                                "email",
                                userEmail
                        );

                        startActivity(intent);

                        return true;
                    }


                    // -------------------------------------------------
                    // SPLIT BILL HISTORY
                    // -------------------------------------------------

                    if (selected.equals(
                            "Split Bill History"
                    )) {

                        Intent intent =
                                new Intent(
                                        this,
                                        SplitBillHistoryActivity.class
                                );

                        intent.putExtra(
                                "email",
                                userEmail
                        );

                        startActivity(intent);

                        return true;
                    }


                    // -------------------------------------------------
                    // ALL EXPENSES
                    // -------------------------------------------------

                    if (selected.equals(
                            "All Expenses"
                    )) {

                        Intent intent =
                                new Intent(
                                        this,
                                        AllExpensesActivity.class
                                );

                        intent.putExtra(
                                "email",
                                userEmail
                        );

                        startActivity(intent);

                        return true;
                    }


                    // -------------------------------------------------
                    // LOGOUT
                    // -------------------------------------------------

                    if (selected.equals(
                            "Logout"
                    )) {

                        showLogoutDialog();

                        return true;
                    }


                    return false;
                }
        );


        popupMenu.show();
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private void showLogoutDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this)

                .setTitle(
                        "Logout"
                )

                .setMessage(
                        "Are you sure you want to logout?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> {

                            Intent intent =
                                    new Intent(
                                            this,
                                            LoginActivity.class
                                    );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(
                                    intent
                            );

                            finish();
                        }
                )

                .show();
    }


    // =========================================================
    // FORMAT AMOUNT
    // =========================================================

    private String formatAmount(
            double amount
    ) {

        if (amount == (long) amount) {

            return String.format(
                    Locale.ENGLISH,
                    "%d",
                    (long) amount
            );

        } else {

            return String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    amount
            );
        }
    }


    // =========================================================
    // SAFE TEXT
    // =========================================================

    private String safeText(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "-";
        }

        return value;
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (databaseHelper != null &&
                userEmail != null &&
                selectedCategory != null) {

            loadCategoryExpenses();
        }
    }
}