package com.example.splitmate;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PaymentHistoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout historyContainer;

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_payment_history
        );

        
        MenuHelper.attach(this, findViewById(R.id.menuButton));
databaseHelper =
                new DatabaseHelper(this);

        userEmail =
                SessionManager.resolveEmail(this);

        historyContainer =
                findViewById(
                        R.id.paymentHistoryContainer
                );

        findViewById(R.id.backButton)
                .setOnClickListener(
                        v -> finish()
                );

        loadPaymentHistory();
    }


    private void loadPaymentHistory() {

        historyContainer.removeAllViews();

        Cursor cursor = null;

        boolean found = false;

        try {

            cursor =
                    databaseHelper.getAllExpenses(
                            userEmail
                    );

            if (cursor == null) {
                showEmpty();
                return;
            }

            int amountIndex =
                    cursor.getColumnIndexOrThrow(
                            "amount"
                    );

            int categoryIndex =
                    cursor.getColumnIndexOrThrow(
                            "category"
                    );

            int descriptionIndex =
                    cursor.getColumnIndexOrThrow(
                            "description"
                    );

            int dateIndex =
                    cursor.getColumnIndexOrThrow(
                            "date"
                    );

            int timeIndex =
                    cursor.getColumnIndexOrThrow(
                            "time"
                    );

            int otherCategoryIndex =
                    cursor.getColumnIndex(
                            "other_category"
                    );


            int expenseIdIndex =
                    cursor.getColumnIndexOrThrow(
                            "expense_id"
                    );

            while (cursor.moveToNext()) {

                found = true;

                long expenseId =
                        cursor.getLong(
                                expenseIdIndex
                        );

                double amount =
                        cursor.getDouble(
                                amountIndex
                        );

                String category =
                        cursor.getString(
                                categoryIndex
                        );

                String description =
                        cursor.getString(
                                descriptionIndex
                        );

                String date =
                        cursor.getString(
                                dateIndex
                        );

                String time =
                        cursor.getString(
                                timeIndex
                        );

                String otherCategory = "";

                if (otherCategoryIndex != -1) {

                    otherCategory =
                            cursor.getString(
                                    otherCategoryIndex
                            );
                }


                addPaymentCard(
                        expenseId,
                        amount,
                        category,
                        description,
                        date,
                        time,
                        otherCategory
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            showEmpty();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }


        if (!found) {
            showEmpty();
        }
    }


    private void addPaymentCard(
            long expenseId,
            double amount,
            String category,
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


        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                18,
                16,
                18,
                16
        );


        // DESCRIPTION + DELETE ICON ROW

        LinearLayout titleRow =
                new LinearLayout(this);

        titleRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                new TextView(this);

        title.setText(
                description == null ||
                        description.trim().isEmpty()
                        ? "Payment"
                        : description
        );

        title.setTextSize(17);
        title.setTextColor(
                Color.rgb(32, 33, 36)
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        title.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        titleRow.addView(title);

        TextView deleteIcon =
                new TextView(this);

        deleteIcon.setText("🗑");
        deleteIcon.setTextSize(18);
        deleteIcon.setPadding(20, 8, 8, 8);
        deleteIcon.setClickable(true);
        deleteIcon.setFocusable(true);

        deleteIcon.setOnClickListener(
                v -> confirmDeleteExpense(expenseId)
        );

        titleRow.addView(deleteIcon);

        layout.addView(titleRow);


        // AMOUNT

        TextView amountText =
                new TextView(this);

        amountText.setText(
                "₹" +
                        formatAmount(amount)
        );

        amountText.setTextSize(21);

        amountText.setTextColor(
                Color.rgb(32, 33, 36)
        );

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
                6,
                0,
                6
        );

        amountText.setLayoutParams(
                amountParams
        );

        layout.addView(amountText);


        // CATEGORY

        String categoryTextValue =
                category;

        if (category != null &&
                category.equalsIgnoreCase("Other") &&
                otherCategory != null &&
                !otherCategory.trim().isEmpty()) {

            categoryTextValue =
                    "Other • " +
                            otherCategory;
        }


        TextView categoryText =
                createInfoText(
                        "Category: " +
                                categoryTextValue
                );

        layout.addView(categoryText);


        // DATE

        TextView dateText =
                createInfoText(
                        "📅 Date: " +
                                safeText(date)
                );

        layout.addView(dateText);


        // TIME

        TextView timeText =
                createInfoText(
                        "⏰ Time: " +
                                safeText(time)
                );

        layout.addView(timeText);


        card.addView(layout);

        historyContainer.addView(card);
    }


    private TextView createInfoText(
            String text
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);

        view.setTextSize(14);

        view.setTextColor(
                Color.rgb(108, 114, 117)
        );

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

        view.setLayoutParams(params);

        return view;
    }


    private void confirmDeleteExpense(long expenseId) {

        new AlertDialog.Builder(this)

                .setTitle("Delete Payment")

                .setMessage(
                        "Are you sure you want to delete this record? " +
                                "This cannot be undone."
                )

                .setNegativeButton("Cancel", null)

                .setPositiveButton("Delete", (dialog, which) -> {

                    boolean deleted =
                            databaseHelper.deleteExpense(expenseId);

                    Toast.makeText(
                            this,
                            deleted ? "Payment deleted" : "Could not delete payment",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadPaymentHistory();
                })

                .show();
    }


    private void showEmpty() {

        TextView text =
                new TextView(this);

        text.setText(
                "No payment history yet 💳"
        );

        text.setTextSize(16);

        text.setTextColor(
                Color.rgb(108, 114, 117)
        );

        text.setGravity(
                Gravity.CENTER
        );

        text.setPadding(
                10,
                50,
                10,
                50
        );

        historyContainer.addView(text);
    }


    private String safeText(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "-";
        }

        return value;
    }


    private String formatAmount(
            double amount
    ) {

        if (amount == (long) amount) {

            return String.valueOf(
                    (long) amount
            );

        }

        return String.format(
                java.util.Locale.ENGLISH,
                "%.2f",
                amount
        );
    }
}