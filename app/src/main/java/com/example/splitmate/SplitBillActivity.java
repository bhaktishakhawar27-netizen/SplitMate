package com.example.splitmate;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SplitBillActivity extends AppCompatActivity {

    EditText amountEditText;
    EditText descriptionEditText;
    EditText paidByEditText;
    EditText membersEditText;
    EditText valuesEditText;

    Spinner splitTypeSpinner;

    Button calculateButton;

    CheckBox excludeMeCheckBox;

    LinearLayout settlementContainer;

    // Details of the bill currently shown in the Paid / Not Paid list
    private long currentBillId = -1;
    private String currentDescription = "";
    private String currentDate = "";
    private String currentTime = "";

    TextView resultText;

    DatabaseHelper databaseHelper;

    String userEmail;

    private String lastSavedSignature = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_split_bill
        );


        
        MenuHelper.attach(this, findViewById(R.id.menuButton));
databaseHelper =
                new DatabaseHelper(this);


        userEmail =
                SessionManager.resolveEmail(this);


        amountEditText =
                findViewById(
                        R.id.amountEditText
                );


        descriptionEditText =
                findViewById(
                        R.id.descriptionEditText
                );


        paidByEditText =
                findViewById(
                        R.id.paidByEditText
                );


        membersEditText =
                findViewById(
                        R.id.membersEditText
                );


        valuesEditText =
                findViewById(
                        R.id.valuesEditText
                );


        splitTypeSpinner =
                findViewById(
                        R.id.splitTypeSpinner
                );


        calculateButton =
                findViewById(
                        R.id.calculateButton
                );


        resultText =
                findViewById(
                        R.id.resultText
                );


        excludeMeCheckBox =
                findViewById(
                        R.id.excludeMeCheckBox
                );


        settlementContainer =
                findViewById(
                        R.id.settlementContainer
                );


        String[] splitTypes = {

                "Equal Split",
                "Custom Amount",
                "Percentage"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        splitTypes
                );


        splitTypeSpinner.setAdapter(
                adapter
        );


        splitTypeSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        if (position == 0) {

                            valuesEditText.setHint(
                                    "Not required for Equal"
                            );

                        } else if (position == 1) {

                            valuesEditText.setHint(
                                    "Example: 300, 300, 300, 300"
                            );

                        } else {

                            valuesEditText.setHint(
                                    "Example: 25, 25, 25, 25"
                            );
                        }
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {

                    }
                }
        );


        calculateButton.setOnClickListener(
                v -> calculateSplit()
        );
    }


    private void calculateSplit() {


        String amountText =
                amountEditText
                        .getText()
                        .toString()
                        .trim();


        String description =
                descriptionEditText
                        .getText()
                        .toString()
                        .trim();


        String paidBy =
                paidByEditText
                        .getText()
                        .toString()
                        .trim();


        String membersText =
                membersEditText
                        .getText()
                        .toString()
                        .trim();


        String valuesText =
                valuesEditText
                        .getText()
                        .toString()
                        .trim();


        // VALIDATION

        if (amountText.isEmpty()) {

            amountEditText.setError(
                    "Enter bill amount"
            );

            return;
        }


        if (description.isEmpty()) {

            descriptionEditText.setError(
                    "Enter description"
            );

            return;
        }


        if (paidBy.isEmpty()) {

            paidByEditText.setError(
                    "Enter who paid"
            );

            return;
        }


        if (membersText.isEmpty()) {

            membersEditText.setError(
                    "Enter members"
            );

            return;
        }


        double totalAmount;


        try {

            totalAmount =
                    Double.parseDouble(
                            amountText
                    );

        } catch (Exception e) {

            amountEditText.setError(
                    "Enter valid amount"
            );

            return;
        }


        if (totalAmount <= 0) {

            amountEditText.setError(
                    "Amount must be greater than 0"
            );

            return;
        }


        // MEMBERS

        String[] memberArray =
                membersText.split(",");


        ArrayList<String> members =
                new ArrayList<>();


        for (String member :
                memberArray) {

            String name =
                    member.trim();


            if (!name.isEmpty()) {

                members.add(name);
            }
        }


        if (members.size() < 2) {

            membersEditText.setError(
                    "Enter at least 2 members"
            );

            return;
        }


        double[] amounts =
                new double[members.size()];


        int splitType =
                splitTypeSpinner
                        .getSelectedItemPosition();


        // =========================
        // EQUAL SPLIT
        // =========================

        if (splitType == 0) {

            double each =
                    totalAmount /
                            members.size();


            for (int i = 0;
                 i < members.size();
                 i++) {

                amounts[i] = each;
            }
        }


        // =========================
        // CUSTOM AMOUNT
        // =========================

        else if (splitType == 1) {

            if (valuesText.isEmpty()) {

                valuesEditText.setError(
                        "Enter amounts"
                );

                return;
            }


            String[] valueArray =
                    valuesText.split(",");


            if (valueArray.length !=
                    members.size()) {

                valuesEditText.setError(
                        "Enter one amount for each member"
                );

                return;
            }


            double sum = 0;


            try {

                for (int i = 0;
                     i < valueArray.length;
                     i++) {

                    amounts[i] =
                            Double.parseDouble(
                                    valueArray[i].trim()
                            );


                    if (amounts[i] < 0) {

                        valuesEditText.setError(
                                "Amount cannot be negative"
                        );

                        return;
                    }


                    sum += amounts[i];
                }

            } catch (Exception e) {

                valuesEditText.setError(
                        "Enter valid amounts"
                );

                return;
            }


            if (Math.abs(
                    sum - totalAmount
            ) > 0.01) {

                valuesEditText.setError(
                        "Amounts must total ₹" +
                                formatAmount(
                                        totalAmount
                                )
                );

                return;
            }
        }


        // =========================
        // PERCENTAGE
        // =========================

        else {

            if (valuesText.isEmpty()) {

                valuesEditText.setError(
                        "Enter percentages"
                );

                return;
            }


            String[] valueArray =
                    valuesText.split(",");


            if (valueArray.length !=
                    members.size()) {

                valuesEditText.setError(
                        "Enter one percentage for each member"
                );

                return;
            }


            double percentageSum = 0;


            try {

                for (int i = 0;
                     i < valueArray.length;
                     i++) {

                    double percentage =
                            Double.parseDouble(
                                    valueArray[i].trim()
                            );


                    if (percentage < 0) {

                        valuesEditText.setError(
                                "Percentage cannot be negative"
                        );

                        return;
                    }


                    percentageSum +=
                            percentage;


                    amounts[i] =
                            totalAmount *
                                    percentage /
                                    100;
                }

            } catch (Exception e) {

                valuesEditText.setError(
                        "Enter valid percentages"
                );

                return;
            }


            if (Math.abs(
                    percentageSum - 100
            ) > 0.01) {

                valuesEditText.setError(
                        "Percentages must total 100%"
                );

                return;
            }
        }


        // =========================
        // SETTLEMENT
        // =========================

        final boolean excludeMe =
                excludeMeCheckBox.isChecked();

        final String myNameForResult =
                databaseHelper.getUserName(userEmail);

        StringBuilder result =
                new StringBuilder();


        result.append(
                "SETTLEMENT\n\n"
        );


        result.append(
                "Total Bill: ₹"
        );


        result.append(
                formatAmount(
                        totalAmount
                )
        );


        result.append(
                "\nPaid By: "
        );


        result.append(
                paidBy
        );


        result.append(
                "\n\n"
        );


        boolean foundSettlement =
                false;


        for (int i = 0;
             i < members.size();
             i++) {


            String member =
                    members.get(i);


            double owed =
                    amounts[i];


            // Payer does not owe himself/herself

            if (member.equalsIgnoreCase(
                    paidBy
            )) {

                continue;
            }


            if (excludeMe && isMe(member, myNameForResult)) {

                result.append(member);
                result.append(" (excluded – nothing to pay)\n");

                continue;
            }


            if (owed > 0) {

                result.append(
                        member
                );


                result.append(
                        " → "
                );


                result.append(
                        paidBy
                );


                result.append(
                        " ₹"
                );


                result.append(
                        formatAmount(
                                owed
                        )
                );


                result.append(
                        "\n"
                );


                foundSettlement = true;
            }
        }


        if (!foundSettlement) {

            result.append(
                    "No settlement required."
            );
        }


        resultText.setText(
                result.toString()
        );


        // =========================
        // SAVE BILL
        // =========================

        if (userEmail == null ||
                userEmail.isEmpty()) {

            Toast.makeText(
                    this,
                    "User information not found!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String date =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.ENGLISH
                ).format(
                        new Date()
                );


        String time =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.ENGLISH
                ).format(
                        new Date()
                );


        String signature =
                amountText + "|" + description + "|" + paidBy + "|" +
                        membersText + "|" + valuesText + "|" + splitType + "|" + excludeMe;

        if (signature.equals(lastSavedSignature)) {

            Toast.makeText(
                    this,
                    "This split bill is already saved",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        long billId =
                databaseHelper.createSplitBill(
                        userEmail,
                        description,
                        totalAmount,
                        paidBy,
                        date,
                        time
                );


        if (billId == -1) {

            Toast.makeText(
                    this,
                    "Could not save split bill!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        for (int i = 0;
             i < members.size();
             i++) {

            double paidAmount = 0;


            if (members.get(i)
                    .equalsIgnoreCase(
                            paidBy
                    )) {

                paidAmount =
                        totalAmount;
            }


            databaseHelper.addSplitMember(
                    billId,
                    members.get(i),
                    amounts[i],
                    paidAmount
            );
        }


        lastSavedSignature = signature;

        updateMyBalance(
                billId,
                description,
                paidBy,
                members,
                amounts,
                totalAmount,
                date,
                time,
                excludeMe
        );

        currentBillId = billId;
        currentDescription = description;
        currentDate = date;
        currentTime = time;

        renderSettlements();

        Toast.makeText(
                this,
                "Split Bill saved successfully! 💸",
                Toast.LENGTH_SHORT
        ).show();
    }


    // =========================================================
    // UPDATE LOGGED-IN USER'S BALANCE
    // =========================================================

    private boolean isMe(String name, String myName) {

        if (name == null) {
            return false;
        }

        String n = name.trim();

        return n.equalsIgnoreCase("me") ||
                n.equalsIgnoreCase("myself") ||
                (myName != null && n.equalsIgnoreCase(myName.trim()));
    }

    private void updateMyBalance(
            long billId,
            String description,
            String paidBy,
            ArrayList<String> members,
            double[] amounts,
            double totalAmount,
            String date,
            String time,
            boolean excludeMe) {

        String myName = databaseHelper.getUserName(userEmail);

        boolean iPaid = isMe(paidBy, myName);

        double myShare = 0;
        double othersShare = 0;

        for (int i = 0; i < members.size(); i++) {

            if (isMe(members.get(i), myName)) {
                myShare += amounts[i];
            } else {
                othersShare += amounts[i];
            }
        }

        if (iPaid) {

            // The full amount I paid leaves my balance...
            // ...except my own share when "Exclude Me" is ticked.
            double myExpense = excludeMe
                    ? Math.max(0, totalAmount - myShare)
                    : totalAmount;

            if (myExpense > 0) {
                databaseHelper.addSplitBillExpense(
                        billId,
                        userEmail,
                        myExpense,
                        "Split Bill",
                        "",
                        "Split: " + description + " (paid by you)",
                        date,
                        time
                );
            }

            // Money owed to you is NOT added to the balance yet.
            // It becomes available only when you mark the payment as received.
            for (int i = 0; i < members.size(); i++) {
                if (!isMe(members.get(i), myName) && amounts[i] > 0) {
                    databaseHelper.addSettlement(
                            billId,
                            userEmail,
                            members.get(i),
                            amounts[i],
                            "RECEIVE"
                    );
                }
            }

            NotificationHelper.notifyPayment(
                    this,
                    "Split bill saved",
                    "₹" + formatAmount(totalAmount) + " was paid by you. " +
                            "Mark each person's payment as received when it actually arrives."
            );

        } else if (myShare > 0 && !excludeMe) {

            // Someone else paid. Your balance is reduced only when you
            // actually pay your share, not merely when the bill is created.
            databaseHelper.addSettlement(
                    billId,
                    userEmail,
                    paidBy,
                    myShare,
                    "PAY"
            );

            NotificationHelper.notifyPayment(
                    this,
                    "Split bill saved",
                    "You need to pay " + paidBy + " ₹" + formatAmount(myShare) +
                            ". Mark it as paid from Split Bill History."
            );
        }
    }


    // =========================================================
    // PAID / NOT PAID LIST + RECEIVE / PAY SUMMARY
    // =========================================================

    private int dp(int v) {
        return Math.round(
                v * getResources().getDisplayMetrics().density
        );
    }

    private void renderSettlements() {

        settlementContainer.removeAllViews();

        if (currentBillId == -1) {
            settlementContainer.setVisibility(View.GONE);
            return;
        }

        settlementContainer.setVisibility(View.VISIBLE);

        Cursor c = databaseHelper.getSettlements(currentBillId, userEmail);

        double toReceive = 0;   // still to receive (not paid yet)
        double toPay = 0;       // still to pay (not paid yet)
        double received = 0;    // already received
        double paidOut = 0;     // already paid

        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);

        while (c.moveToNext()) {

            final long settlementId = c.getLong(
                    c.getColumnIndexOrThrow("settlement_id"));
            String member = c.getString(
                    c.getColumnIndexOrThrow("member_name"));
            double amount = c.getDouble(
                    c.getColumnIndexOrThrow("amount"));
            String type = c.getString(
                    c.getColumnIndexOrThrow("type"));
            boolean done = c.getInt(
                    c.getColumnIndexOrThrow("is_done")) == 1;

            boolean isReceive = "RECEIVE".equals(type);

            if (isReceive) {
                if (done) received += amount; else toReceive += amount;
            } else {
                if (done) paidOut += amount; else toPay += amount;
            }

            CheckBox box = new CheckBox(this);
            box.setChecked(done);
            box.setTextSize(14);
            box.setTextColor(Color.rgb(32, 33, 36));
            box.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.rgb(35, 143, 131)));

            String who = isReceive
                    ? member + " → you"
                    : "You → " + member;

            box.setText(
                    who + "  ₹" + formatAmount(amount) +
                            "   " + (done ? "✓ Paid" : "• Not Paid")
            );

            box.setOnCheckedChangeListener((button, isChecked) -> {

                boolean ok = databaseHelper.setSettlementPaid(
                        settlementId,
                        isChecked,
                        userEmail,
                        currentDescription,
                        currentDate,
                        currentTime
                );

                if (!ok) {
                    Toast.makeText(
                            this,
                            "Could not update payment",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                // Rebuild list + totals so they always match the database
                settlementContainer.post(this::renderSettlements);
            });

            rows.addView(box);
        }

        c.close();

        // ---------- card ----------

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView title = new TextView(this);
        title.setText("Paid / Not Paid");
        title.setTextSize(19);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(32, 33, 36));
        card.addView(title);

        TextView hint = new TextView(this);
        hint.setText("Tick a person once they have paid. " +
                "Your balance updates automatically.");
        hint.setTextSize(12);
        hint.setTextColor(Color.rgb(107, 114, 117));
        hint.setPadding(0, dp(4), 0, dp(8));
        card.addView(hint);

        if (rows.getChildCount() == 0) {

            TextView none = new TextView(this);
            none.setText("Nothing to receive or pay for this bill.");
            none.setTextSize(14);
            none.setTextColor(Color.rgb(107, 114, 117));
            card.addView(none);

        } else {
            card.addView(rows);
        }

        // ---------- receive / pay summary ----------

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(225, 230, 230));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(0, dp(10), 0, dp(10));
        line.setLayoutParams(lp);
        card.addView(line);

        TextView receiveText = new TextView(this);
        receiveText.setText(
                "💰 Money I need to RECEIVE:  ₹" + formatAmount(toReceive));
        receiveText.setTextSize(16);
        receiveText.setTypeface(null, Typeface.BOLD);
        receiveText.setTextColor(Color.rgb(35, 143, 131));
        card.addView(receiveText);

        TextView payText = new TextView(this);
        payText.setText(
                "💸 Money I need to PAY:  ₹" + formatAmount(toPay));
        payText.setTextSize(16);
        payText.setTypeface(null, Typeface.BOLD);
        payText.setTextColor(Color.rgb(200, 60, 60));
        payText.setPadding(0, dp(4), 0, 0);
        card.addView(payText);

        TextView doneText = new TextView(this);
        doneText.setText(
                "Already received: ₹" + formatAmount(received) +
                        "   •   Already paid: ₹" + formatAmount(paidOut));
        doneText.setTextSize(13);
        doneText.setTextColor(Color.rgb(107, 114, 117));
        doneText.setPadding(0, dp(6), 0, 0);
        card.addView(doneText);

        settlementContainer.addView(card);
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