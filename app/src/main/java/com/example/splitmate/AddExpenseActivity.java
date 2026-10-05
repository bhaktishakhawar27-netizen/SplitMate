package com.example.splitmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    EditText amountEditText;
    EditText otherCategoryEditText;
    EditText descriptionEditText;

    Spinner categorySpinner;

    TextView otherCategoryLabel;
    TextView dateTextView;
    TextView menuButton;

    Button saveExpenseButton;

    DatabaseHelper databaseHelper;

    String userEmail;

    Calendar selectedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_expense);

        MenuHelper.attach(this, findViewById(R.id.menuButton));


        amountEditText =
                findViewById(R.id.amountEditText);

        categorySpinner =
                findViewById(R.id.categorySpinner);

        otherCategoryLabel =
                findViewById(R.id.otherCategoryLabel);

        otherCategoryEditText =
                findViewById(R.id.otherCategoryEditText);

        descriptionEditText =
                findViewById(R.id.descriptionEditText);

        dateTextView =
                findViewById(R.id.dateTextView);

        menuButton =
                findViewById(R.id.menuButton);

        saveExpenseButton =
                findViewById(R.id.saveExpenseButton);


        databaseHelper =
                new DatabaseHelper(this);



        userEmail =
                SessionManager.resolveEmail(this);



        selectedDate = Calendar.getInstance();

        updateDateText();



        dateTextView.setOnClickListener(v -> {

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            AddExpenseActivity.this,

                            (view, year, month, dayOfMonth) -> {

                                selectedDate.set(
                                        year,
                                        month,
                                        dayOfMonth
                                );

                                updateDateText();
                            },

                            selectedDate.get(
                                    Calendar.YEAR
                            ),

                            selectedDate.get(
                                    Calendar.MONTH
                            ),

                            selectedDate.get(
                                    Calendar.DAY_OF_MONTH
                            )
                    );

            datePickerDialog.show();
        });



        menuButton.setOnClickListener(v -> {

            PopupMenu popupMenu =
                    new PopupMenu(
                            AddExpenseActivity.this,
                            menuButton
                    );

            popupMenu.getMenu().add("🏠 Home");
            popupMenu.getMenu().add("📋 All Expenses");
            popupMenu.getMenu().add("💸 Split Bill History");
            popupMenu.getMenu().add("🚪 Logout");


            popupMenu.setOnMenuItemClickListener(item -> {

                String title =
                        item.getTitle().toString();


                if (title.contains("Home")) {

                    Intent intent =
                            new Intent(
                                    AddExpenseActivity.this,
                                    HomeActivity.class
                            );

                    intent.putExtra(
                            "email",
                            userEmail
                    );

                    startActivity(intent);

                    finish();

                    return true;
                }



                if (title.contains("All Expenses")) {

                    Intent intent =
                            new Intent(
                                    AddExpenseActivity.this,
                                    AllExpensesActivity.class
                            );

                    intent.putExtra(
                            "email",
                            userEmail
                    );

                    startActivity(intent);

                    return true;
                }



                if (title.contains(
                        "Split Bill History"
                )) {

                    Intent intent =
                            new Intent(
                                    AddExpenseActivity.this,
                                    SplitBillHistoryActivity.class
                            );

                    intent.putExtra(
                            "email",
                            userEmail
                    );

                    startActivity(intent);

                    return true;
                }


                if (title.contains("Logout")) {

                    Intent intent =
                            new Intent(
                                    AddExpenseActivity.this,
                                    LoginActivity.class
                            );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);

                    finish();

                    return true;
                }


                return false;
            });

            popupMenu.show();
        });


        String[] categories = {

                "Food",
                "Canteen",
                "Travel",
                "Shopping",
                "Education",
                "Entertainment",
                "Bills",
                "Other"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories
                );


        categorySpinner.setAdapter(adapter);



        otherCategoryLabel.setVisibility(
                View.GONE
        );

        otherCategoryEditText.setVisibility(
                View.GONE
        );


        categorySpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        String selectedCategory =
                                parent.getItemAtPosition(
                                        position
                                ).toString();


                        if (selectedCategory.equals(
                                "Other"
                        )) {

                            otherCategoryLabel.setVisibility(
                                    View.VISIBLE
                            );

                            otherCategoryEditText.setVisibility(
                                    View.VISIBLE
                            );

                        } else {

                            otherCategoryLabel.setVisibility(
                                    View.GONE
                            );

                            otherCategoryEditText.setVisibility(
                                    View.GONE
                            );

                            otherCategoryEditText.setText("");
                        }
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {

                    }
                }
        );


        saveExpenseButton.setOnClickListener(v -> {


            String amountText =
                    amountEditText
                            .getText()
                            .toString()
                            .trim();


            String category =
                    categorySpinner
                            .getSelectedItem()
                            .toString();


            String otherCategory = "";


            if (category.equals("Other")) {

                otherCategory =
                        otherCategoryEditText
                                .getText()
                                .toString()
                                .trim();
            }


            String description =
                    descriptionEditText
                            .getText()
                            .toString()
                            .trim();


            if (userEmail == null ||
                    userEmail.isEmpty()) {

                Toast.makeText(
                        AddExpenseActivity.this,
                        "User information not found!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            if (amountText.isEmpty()) {

                amountEditText.setError(
                        "Enter amount"
                );

                return;
            }


            double amount;


            try {

                amount =
                        Double.parseDouble(
                                amountText
                        );

            } catch (NumberFormatException e) {

                amountEditText.setError(
                        "Enter valid amount"
                );

                return;
            }


            if (amount <= 0) {

                amountEditText.setError(
                        "Amount must be greater than 0"
                );

                return;
            }


            if (category.equals("Other") &&
                    otherCategory.isEmpty()) {

                otherCategoryEditText.setError(
                        "Enter category name"
                );

                return;
            }


            if (description.isEmpty()) {

                descriptionEditText.setError(
                        "Enter description"
                );

                return;
            }


            // =========================
            // DATE
            // =========================

            String date =
                    new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.ENGLISH
                    ).format(
                            selectedDate.getTime()
                    );


            // =========================
            // TIME
            // =========================

            String time =
                    new SimpleDateFormat(
                            "hh:mm a",
                            Locale.ENGLISH
                    ).format(
                            new Date()
                    );


            // =========================
            // SAVE DATABASE
            // =========================

            boolean success =
                    databaseHelper.addExpense(

                            userEmail,

                            amount,

                            category,

                            otherCategory,

                            description,

                            date,

                            time
                    );


            // =========================
            // RESULT
            // =========================

            if (success) {

                Toast.makeText(
                        AddExpenseActivity.this,
                        "Expense saved successfully! 💸",
                        Toast.LENGTH_SHORT
                ).show();

                NotificationHelper.notifyPayment(
                        AddExpenseActivity.this,
                        "Payment recorded",
                        "₹" + databaseHelper.formatAmount(amount) +
                                " spent on " + category +
                                (description != null && !description.trim().isEmpty()
                                        ? " - " + description : "")
                );


                /*
                 * Go back to Home.
                 *
                 * HomeActivity.onResume()
                 * will reload:
                 * Budget
                 * Spent
                 * Balance
                 * Category totals
                 * Pie chart
                 */

                Intent intent =
                        new Intent(
                                AddExpenseActivity.this,
                                HomeActivity.class
                        );

                intent.putExtra(
                        "email",
                        userEmail
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                startActivity(intent);

                finish();


            } else {

                Toast.makeText(
                        AddExpenseActivity.this,
                        "Failed to save expense!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    // =========================
    // UPDATE DATE TEXT
    // =========================

    private void updateDateText() {

        String formattedDate =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.ENGLISH
                ).format(
                        selectedDate.getTime()
                );

        dateTextView.setText(
                formattedDate + "   📅"
        );
    }
}