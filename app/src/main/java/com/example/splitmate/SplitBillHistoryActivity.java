package com.example.splitmate;

import android.database.Cursor;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SplitBillHistoryActivity extends AppCompatActivity {

    LinearLayout historyContainer;
    DatabaseHelper databaseHelper;

    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_split_bill_history);

        MenuHelper.attach(this, findViewById(R.id.menuButton));

        historyContainer = findViewById(R.id.historyContainer);

        databaseHelper = new DatabaseHelper(this);

        userEmail = SessionManager.resolveEmail(this);

        loadSplitBillHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (databaseHelper != null && userEmail != null) {
            loadSplitBillHistory();
        }
    }



    // =========================================================
    // PENDING BALANCE SUMMARY + REMINDER
    // =========================================================

    private void addPendingSummaryCard() {

        final double[] sum = databaseHelper.getPendingSplitSummary(userEmail);
        final double toReceive = sum[0];
        final double toPay = sum[1];
        final int count = (int) sum[2];

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setColor(android.graphics.Color.rgb(32, 33, 36));
        bg.setCornerRadius(dp(20));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(16);
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText("Split Status");
        title.setTextColor(android.graphics.Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView receive = new TextView(this);
        receive.setText("To receive: " + databaseHelper.formatAmount(toReceive));
        receive.setTextColor(android.graphics.Color.rgb(94, 234, 212));
        receive.setTextSize(15);
        receive.setPadding(0, dp(8), 0, 0);
        card.addView(receive);

        TextView pay = new TextView(this);
        pay.setText("To pay: " + databaseHelper.formatAmount(toPay));
        pay.setTextColor(android.graphics.Color.rgb(255, 160, 160));
        pay.setTextSize(15);
        pay.setPadding(0, dp(4), 0, 0);
        card.addView(pay);

        double net = toReceive - toPay;
        TextView balance = new TextView(this);
        balance.setText("Pending balance: "
                + (net < 0 ? "-" : "") + databaseHelper.formatAmount(Math.abs(net))
                + "  (" + count + " pending)");
        balance.setTextColor(android.graphics.Color.rgb(200, 205, 207));
        balance.setTextSize(13);
        balance.setPadding(0, dp(6), 0, 0);
        card.addView(balance);

        // Per-person pending list (max 6 rows)
        Cursor pc = databaseHelper.getPendingSettlements(userEmail);
        int shown = 0;
        while (pc.moveToNext() && shown < 6) {
            boolean isReceive = "RECEIVE".equals(pc.getString(2));
            TextView row = new TextView(this);
            row.setText((isReceive ? "↓ " : "↑ ") + pc.getString(0) + "  "
                    + databaseHelper.formatAmount(pc.getDouble(1))
                    + (isReceive ? "  owes you" : "  you owe"));
            row.setTextColor(android.graphics.Color.rgb(158, 164, 166));
            row.setTextSize(13);
            row.setPadding(0, dp(3), 0, 0);
            card.addView(row);
            shown++;
        }
        if (pc.getCount() > 6) {
            TextView more = new TextView(this);
            more.setText("+ " + (pc.getCount() - 6) + " more");
            more.setTextColor(android.graphics.Color.rgb(158, 164, 166));
            more.setTextSize(12);
            more.setPadding(0, dp(3), 0, 0);
            card.addView(more);
        }
        pc.close();

        Button remind = new Button(this);
        remind.setText(count == 0 ? "ALL SETTLED ✓" : "SEND REMINDER");
        remind.setEnabled(count > 0);
        remind.setTextColor(android.graphics.Color.rgb(32, 33, 36));
        remind.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.rgb(94, 234, 212)));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        bp.topMargin = dp(12);
        remind.setLayoutParams(bp);
        remind.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionIfNeeded();
                Toast.makeText(this,
                        "Allow notifications, then tap Send Reminder again",
                        Toast.LENGTH_LONG).show();
                return;
            }
            NotificationHelper.notifyPayment(
                    this,
                    "Split bill reminder",
                    "You have " + count + " pending settlement(s). To receive "
                            + databaseHelper.formatAmount(toReceive)
                            + ", to pay "
                            + databaseHelper.formatAmount(toPay) + ".");
            Toast.makeText(this, "Reminder sent", Toast.LENGTH_SHORT).show();
        });
        card.addView(remind);

        historyContainer.addView(card);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    501);
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    // =========================================================
    // LOAD SPLIT BILL HISTORY
    // =========================================================

    private void loadSplitBillHistory() {

        historyContainer.removeAllViews();

        addPendingSummaryCard();

        Cursor billCursor = databaseHelper.getReadableDatabase().query(
                "split_bills",
                null,
                "user_email=?",
                new String[]{userEmail},
                null,
                null,
                "bill_id DESC"
        );


        if (billCursor.getCount() == 0) {

            TextView emptyText = new TextView(this);

            emptyText.setText("No split bills yet 💸");
            emptyText.setTextSize(16);
            emptyText.setTextColor(
                    android.graphics.Color.rgb(107, 114, 117)
            );

            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(20, 50, 20, 50);

            historyContainer.addView(emptyText);

            billCursor.close();

            return;
        }


        while (billCursor.moveToNext()) {

            long billId =
                    billCursor.getLong(
                            billCursor.getColumnIndexOrThrow(
                                    "bill_id"
                            )
                    );

            String description =
                    billCursor.getString(
                            billCursor.getColumnIndexOrThrow(
                                    "description"
                            )
                    );

            double totalAmount =
                    billCursor.getDouble(
                            billCursor.getColumnIndexOrThrow(
                                    "total_amount"
                            )
                    );

            String paidBy =
                    billCursor.getString(
                            billCursor.getColumnIndexOrThrow(
                                    "paid_by"
                            )
                    );

            String date =
                    billCursor.getString(
                            billCursor.getColumnIndexOrThrow(
                                    "bill_date"
                            )
                    );

            String time =
                    billCursor.getString(
                            billCursor.getColumnIndexOrThrow(
                                    "bill_time"
                            )
                    );


            // =================================================
            // BILL CARD
            // =================================================

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
                    android.graphics.Color.WHITE
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
                    18
            );

            card.setLayoutParams(cardParams);


            // =================================================
            // DESCRIPTION
            // =================================================

            TextView title =
                    new TextView(this);

            title.setText(
                    description == null ||
                            description.trim().isEmpty()
                            ? "Split Bill"
                            : description
            );

            title.setTextSize(19);
            title.setTypeface(
                    null,
                    Typeface.BOLD
            );

            title.setTextColor(
                    android.graphics.Color.rgb(
                            32,
                            33,
                            36
                    )
            );


            card.addView(title);


            // =================================================
            // TOTAL AMOUNT
            // =================================================

            TextView amount =
                    new TextView(this);

            amount.setText(
                    "Total Bill: ₹" +
                            databaseHelper.formatAmount(
                                    totalAmount
                            )
            );

            amount.setTextSize(17);

            amount.setTypeface(
                    null,
                    Typeface.BOLD
            );

            amount.setTextColor(
                    android.graphics.Color.rgb(
                            35,
                            143,
                            131
                    )
            );

            amount.setPadding(
                    0,
                    8,
                    0,
                    4
            );

            card.addView(amount);


            // =================================================
            // PAID BY
            // =================================================

            TextView payer =
                    new TextView(this);

            payer.setText(
                    "Paid by: " + paidBy
            );

            payer.setTextSize(14);

            payer.setTextColor(
                    android.graphics.Color.rgb(
                            107,
                            114,
                            117
                    )
            );

            card.addView(payer);


            // =================================================
            // DATE + TIME
            // =================================================

            TextView dateTime =
                    new TextView(this);

            dateTime.setText(
                    "🕒 " + date + " • " + time
            );

            dateTime.setTextSize(13);

            dateTime.setTextColor(
                    android.graphics.Color.rgb(
                            107,
                            114,
                            117
                    )
            );

            dateTime.setPadding(
                    0,
                    4,
                    0,
                    12
            );

            card.addView(dateTime);


            // =================================================
            // MEMBERS TITLE
            // =================================================

            TextView membersTitle =
                    new TextView(this);

            membersTitle.setText(
                    "Members & Shares"
            );

            membersTitle.setTextSize(15);

            membersTitle.setTypeface(
                    null,
                    Typeface.BOLD
            );

            membersTitle.setTextColor(
                    android.graphics.Color.rgb(
                            32,
                            33,
                            36
                    )
            );

            membersTitle.setPadding(
                    0,
                    5,
                    0,
                    6
            );

            card.addView(membersTitle);


            // =================================================
            // GET MEMBERS
            // =================================================

            Cursor memberCursor =
                    databaseHelper.getReadableDatabase().query(

                            "split_members",

                            null,

                            "bill_id=?",

                            new String[]{
                                    String.valueOf(billId)
                            },

                            null,
                            null,

                            "member_id ASC"
                    );


            while (memberCursor.moveToNext()) {

                String memberName =
                        memberCursor.getString(
                                memberCursor.getColumnIndexOrThrow(
                                        "member_name"
                                )
                        );

                double memberAmount =
                        memberCursor.getDouble(
                                memberCursor.getColumnIndexOrThrow(
                                        "amount"
                                )
                        );

                double paidAmount =
                        memberCursor.getDouble(
                                memberCursor.getColumnIndexOrThrow(
                                        "paid_amount"
                                )
                        );


                LinearLayout memberRow =
                        new LinearLayout(this);

                memberRow.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                memberRow.setGravity(
                        Gravity.CENTER_VERTICAL
                );

                memberRow.setPadding(
                        0,
                        5,
                        0,
                        5
                );


                // MEMBER NAME

                TextView memberNameText =
                        new TextView(this);

                memberNameText.setText(
                        "• " + memberName
                );

                memberNameText.setTextSize(14);

                memberNameText.setTextColor(
                        android.graphics.Color.rgb(
                                32,
                                33,
                                36
                        )
                );


                LinearLayout.LayoutParams nameParams =
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        );

                memberNameText.setLayoutParams(
                        nameParams
                );


                memberRow.addView(
                        memberNameText
                );


                // MEMBER AMOUNT

                TextView memberAmountText =
                        new TextView(this);

                memberAmountText.setText(
                        "₹" +
                                databaseHelper.formatAmount(
                                        memberAmount
                                )
                );

                memberAmountText.setTextSize(14);

                memberAmountText.setTypeface(
                        null,
                        Typeface.BOLD
                );

                memberAmountText.setTextColor(
                        android.graphics.Color.rgb(
                                35,
                                143,
                                131
                        )
                );


                memberRow.addView(
                        memberAmountText
                );


                card.addView(
                        memberRow
                );
            }

            memberCursor.close();

            // =================================================
            // SETTLEMENT ACTIONS
            // =================================================

            Cursor settlementCursor =
                    databaseHelper.getSettlements(billId, userEmail);

            if (settlementCursor.moveToFirst()) {

                TextView settlementTitle = new TextView(this);
                settlementTitle.setText("Payment Status (Paid / Not Paid)");
                settlementTitle.setTextSize(15);
                settlementTitle.setTypeface(null, Typeface.BOLD);
                settlementTitle.setTextColor(
                        android.graphics.Color.rgb(32, 33, 36)
                );
                settlementTitle.setPadding(0, 14, 0, 6);
                card.addView(settlementTitle);

                do {
                    long settlementId =
                            settlementCursor.getLong(
                                    settlementCursor.getColumnIndexOrThrow(
                                            "settlement_id"
                                    )
                            );

                    String memberName =
                            settlementCursor.getString(
                                    settlementCursor.getColumnIndexOrThrow(
                                            "member_name"
                                    )
                            );

                    double settlementAmount =
                            settlementCursor.getDouble(
                                    settlementCursor.getColumnIndexOrThrow(
                                            "amount"
                                    )
                            );

                    String type =
                            settlementCursor.getString(
                                    settlementCursor.getColumnIndexOrThrow(
                                            "type"
                                    )
                            );

                    boolean done =
                            settlementCursor.getInt(
                                    settlementCursor.getColumnIndexOrThrow(
                                            "is_done"
                                    )
                            ) == 1;

                    LinearLayout settlementRow =
                            new LinearLayout(this);
                    settlementRow.setOrientation(
                            LinearLayout.HORIZONTAL
                    );
                    settlementRow.setGravity(
                            Gravity.CENTER_VERTICAL
                    );
                    settlementRow.setPadding(0, 5, 0, 5);

                    final boolean isReceive = "RECEIVE".equals(type);

                    CheckBox statusBox = new CheckBox(this);
                    statusBox.setChecked(done);
                    statusBox.setTextSize(14);
                    statusBox.setTextColor(
                            done
                                    ? android.graphics.Color.rgb(35, 143, 131)
                                    : android.graphics.Color.rgb(32, 33, 36)
                    );

                    String actionText =
                            isReceive
                                    ? memberName + " → you"
                                    : "You → " + memberName;

                    statusBox.setText(
                            actionText +
                                    "  ₹" +
                                    databaseHelper.formatAmount(
                                            settlementAmount
                                    ) +
                                    (done ? "   ✓ Paid" : "   • Not Paid")
                    );

                    LinearLayout.LayoutParams statusParams =
                            new LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1
                            );
                    statusBox.setLayoutParams(statusParams);

                    statusBox.setOnCheckedChangeListener((button, isChecked) -> {

                        boolean ok =
                                databaseHelper.setSettlementPaid(
                                        settlementId,
                                        isChecked,
                                        userEmail,
                                        description,
                                        date,
                                        time
                                );

                        Toast.makeText(
                                this,
                                ok
                                        ? "Balance updated ✓"
                                        : "Could not update payment",
                                Toast.LENGTH_SHORT
                        ).show();

                        historyContainer.post(this::loadSplitBillHistory);
                    });

                    settlementRow.addView(statusBox);

                    card.addView(settlementRow);

                } while (settlementCursor.moveToNext());
            }

            settlementCursor.close();


            // =================================================
            // DIVIDER
            // =================================================

            View divider =
                    new View(this);

            divider.setBackgroundColor(
                    android.graphics.Color.rgb(
                            225,
                            230,
                            230
                    )
            );


            LinearLayout.LayoutParams dividerParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );

            dividerParams.setMargins(
                    0,
                    12,
                    0,
                    0
            );

            divider.setLayoutParams(
                    dividerParams
            );

            card.addView(divider);

            // DELETE THIS SPLIT-BILL RECORD
            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE SPLIT BILL");
            deleteButton.setTextSize(13);
            deleteButton.setTextColor(android.graphics.Color.rgb(180, 55, 55));
            deleteButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.rgb(250, 235, 235)));
            LinearLayout.LayoutParams deleteParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(48));
            deleteParams.topMargin = dp(10);
            deleteButton.setLayoutParams(deleteParams);

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Split Bill?")
                        .setMessage(
                                "This will delete this history record and reverse any " +
                                "balance, received-money, or expense entries created by it."
                        )
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean deleted =
                                    databaseHelper.deleteSplitBill(
                                            billId,
                                            userEmail
                                    );

                            Toast.makeText(
                                    this,
                                    deleted
                                            ? "Split bill deleted and balance updated ✓"
                                            : "Could not delete split bill",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadSplitBillHistory();
                        })
                        .show();
            });

            card.addView(deleteButton);

            historyContainer.addView(card);
        }


        billCursor.close();
    }
}