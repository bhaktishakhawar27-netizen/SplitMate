package com.example.splitmate;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

public class AllExpensesActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;

    LinearLayout expenseContainer;

    String userEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_expenses);

        MenuHelper.attach(this, findViewById(R.id.menuButton));

        databaseHelper =
                new DatabaseHelper(this);

        expenseContainer =
                findViewById(
                        R.id.expenseContainer
                );

        userEmail =
                SessionManager.resolveEmail(this);

        loadExpenses();
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (databaseHelper != null &&
                expenseContainer != null) {

            loadExpenses();
        }
    }


    private void loadExpenses() {

        expenseContainer.removeAllViews();


        if (userEmail == null ||
                userEmail.isEmpty()) {

            showMessage(
                    "User information not found."
            );

            return;
        }


        Cursor cursor =
                databaseHelper.getAllExpenses(
                        userEmail
                );


        if (cursor.getCount() == 0) {

            cursor.close();

            showMessage(
                    "No expenses added yet 💸"
            );

            return;
        }


        while (cursor.moveToNext()) {

            long expenseId =
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    "expense_id"
                            )
                    );

            String category =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "category"
                            )
                    );


            String otherCategory =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "other_category"
                            )
                    );


            String description =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "description"
                            )
                    );


            double amount =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "amount"
                            )
                    );


            String date =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "date"
                            )
                    );


            String time =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "time"
                            )
                    );


            addExpenseCard(
                    expenseId,
                    category,
                    otherCategory,
                    description,
                    amount,
                    date,
                    time
            );
        }


        cursor.close();
    }


    private void addExpenseCard(
            long expenseId,
            String category,
            String otherCategory,
            String description,
            double amount,
            String date,
            String time) {


        LinearLayout card =
                new LinearLayout(this);


        card.setOrientation(
                LinearLayout.VERTICAL
        );


        card.setPadding(
                20,
                18,
                20,
                18
        );


        card.setBackgroundColor(
                Color.WHITE
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                0,
                0,
                0,
                12
        );


        card.setLayoutParams(
                cardParams
        );


        // TOP ROW: AMOUNT (left) + DELETE ICON (right)

        LinearLayout topRow =
                new LinearLayout(this);

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        topRow.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );


        // AMOUNT

        TextView amountText =
                new TextView(this);


        amountText.setText(
                "₹" + formatAmount(amount)
        );


        amountText.setTextSize(
                23
        );


        amountText.setTextColor(
                Color.rgb(
                        32,
                        33,
                        36
                )
        );


        amountText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams amountParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );

        amountText.setLayoutParams(amountParams);

        topRow.addView(amountText);


        // DELETE ICON

        TextView deleteIcon =
                new TextView(this);

        deleteIcon.setText("🗑");
        deleteIcon.setTextSize(20);
        deleteIcon.setPadding(24, 12, 12, 12);
        deleteIcon.setClickable(true);
        deleteIcon.setFocusable(true);

        deleteIcon.setOnClickListener(
                v -> confirmDeleteExpense(expenseId)
        );

        topRow.addView(deleteIcon);

        card.addView(topRow);


        // CATEGORY

        TextView categoryText =
                new TextView(this);


        String displayCategory;


        if (category.equals("Other") &&
                otherCategory != null &&
                !otherCategory.isEmpty()) {

            displayCategory =
                    "Other • " +
                            otherCategory;

        } else {

            displayCategory =
                    category;
        }


        categoryText.setText(
                displayCategory
        );


        categoryText.setTextSize(
                15
        );


        categoryText.setTextColor(
                Color.rgb(
                        35,
                        143,
                        131
                )
        );


        categoryText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );


        LinearLayout.LayoutParams categoryParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        categoryParams.setMargins(
                0,
                6,
                0,
                0
        );


        categoryText.setLayoutParams(
                categoryParams
        );


        card.addView(
                categoryText
        );


        // DESCRIPTION

        TextView descriptionText =
                new TextView(this);


        descriptionText.setText(
                description
        );


        descriptionText.setTextSize(
                14
        );


        descriptionText.setTextColor(
                Color.rgb(
                        107,
                        114,
                        117
                )
        );


        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        descriptionParams.setMargins(
                0,
                5,
                0,
                0
        );


        descriptionText.setLayoutParams(
                descriptionParams
        );


        card.addView(
                descriptionText
        );


        // DATE + TIME

        TextView dateTimeText =
                new TextView(this);


        dateTimeText.setText(
                "🕒 " +
                        date +
                        " • " +
                        time
        );


        dateTimeText.setTextSize(
                12
        );


        dateTimeText.setTextColor(
                Color.GRAY
        );


        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        dateParams.setMargins(
                0,
                10,
                0,
                0
        );


        dateTimeText.setLayoutParams(
                dateParams
        );


        card.addView(
                dateTimeText
        );


        expenseContainer.addView(
                card
        );
    }


    private void confirmDeleteExpense(long expenseId) {

        new AlertDialog.Builder(this)

                .setTitle("Delete Expense")

                .setMessage(
                        "Are you sure you want to delete this expense? " +
                                "This cannot be undone."
                )

                .setNegativeButton("Cancel", null)

                .setPositiveButton("Delete", (dialog, which) -> {

                    boolean deleted =
                            databaseHelper.deleteExpense(expenseId);

                    if (deleted) {

                        android.widget.Toast.makeText(
                                this,
                                "Expense deleted",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();

                        loadExpenses();

                    } else {

                        android.widget.Toast.makeText(
                                this,
                                "Could not delete expense",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .show();
    }


    private void showMessage(
            String message) {

        TextView text =
                new TextView(this);


        text.setText(
                message
        );


        text.setTextSize(
                16
        );


        text.setTextColor(
                Color.GRAY
        );


        text.setGravity(
                Gravity.CENTER
        );


        text.setPadding(
                20,
                50,
                20,
                50
        );


        expenseContainer.addView(
                text
        );
    }


    private String formatAmount(
            double amount) {

        if (amount == (long) amount) {

            return String.valueOf(
                    (long) amount
            );
        }


        return String.format(
                "%.2f",
                amount
        );
    }
}