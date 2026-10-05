package com.example.splitmate;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private String userEmail;

    private TextView greetingText;
    private TextView menuButton;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView navHeaderName;
    private TextView navHeaderEmail;
    private TextView navHeaderAvatar;
    private android.widget.ImageView navHeaderPhoto;

    private TextView budgetText;
    private TextView budgetDateText;
    private TextView spentText;
    private TextView balanceText;
    private TextView userNameText;
    private TextView userEmailText;
    //private TextView menuButton;
    private TextView foodAmountText;
    private TextView canteenAmountText;
    private TextView travelAmountText;
    private TextView shoppingAmountText;
    private TextView educationAmountText;
    private TextView entertainmentAmountText;
    private TextView billsAmountText;
    private TextView otherAmountText;

    private Button expenseButton;
    private Button groupsButton;
    private Button smartTipButton;

    private PieChart spendingPieChart;

    private Calendar selectedMonth;
    private Calendar currentMonth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        menuButton = findViewById(R.id.menuButton);

        setupDrawer();

        databaseHelper = new DatabaseHelper(this);

        userEmail = SessionManager.resolveEmail(this);

        if (userEmail == null || userEmail.trim().isEmpty()) {

            // No valid session: send the user to Login instead of failing
            SessionManager.logout(this);

            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(loginIntent);
            finish();
            return;
        }

        requestNotificationPermission();

        initializeViews();

        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        selectedMonth = (Calendar) currentMonth.clone();

        setupListeners();

        loadUserData();
        loadMonthlyData();
    }

    // =========================================================
    // SIDE DRAWER (PhonePe style)
    // =========================================================

    private void setupDrawer() {

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Hamburger icon opens the drawer
        menuButton.setOnClickListener(
                v -> drawerLayout.openDrawer(GravityCompat.START)
        );

        // Views inside the drawer header (avatar + name + email)
        View headerView = navigationView.getHeaderView(0);
        navHeaderAvatar = headerView.findViewById(R.id.navHeaderAvatar);
        navHeaderPhoto = headerView.findViewById(R.id.navHeaderPhoto);
        navHeaderName = headerView.findViewById(R.id.navHeaderName);
        navHeaderEmail = headerView.findViewById(R.id.navHeaderEmail);

        navigationView.setNavigationItemSelectedListener(
                this::handleDrawerItemClick
        );

        // Close the drawer on back press instead of leaving the screen
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                }
        );
    }

    private boolean handleDrawerItemClick(MenuItem item) {

        int id = item.getItemId();

        drawerLayout.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_account) {

            Intent accountIntent = new Intent(
                    HomeActivity.this,
                    ProfileActivity.class
            );

            accountIntent.putExtra("email", userEmail);
            startActivity(accountIntent);

            return true;
        }

        if (id == R.id.nav_payment_history) {

            Intent intent = new Intent(
                    HomeActivity.this,
                    PaymentHistoryActivity.class
            );

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_monthly_data) {

            Intent intent = new Intent(
                    HomeActivity.this,
                    MonthlyDataActivity.class
            );

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_all_expenses) {

            Intent intent = new Intent(
                    HomeActivity.this,
                    AllExpensesActivity.class
            );

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_split_bill_history) {

            Intent intent = new Intent(
                    HomeActivity.this,
                    SplitBillHistoryActivity.class
            );

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_payment_reminder) {

            Intent intent = new Intent(
                    HomeActivity.this,
                    PaymentReminderActivity.class
            );

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_savings_goal) {

            Intent intent = new Intent(
                    HomeActivity.this,
                    SavingsGoalActivity.class
            );

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_todo) {

            Intent intent = new Intent(
                    HomeActivity.this,
                    TodoActivity.class
            );

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_logout) {

            showLogoutDialog();
            return true;
        }

        return false;
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        greetingText = findViewById(R.id.greetingText);
        menuButton = findViewById(R.id.menuButton);

        budgetText = findViewById(R.id.budgetText);
        budgetDateText = findViewById(R.id.budgetDateText);

        spentText = findViewById(R.id.spentText);
        balanceText = findViewById(R.id.balanceText);

        foodAmountText = findViewById(R.id.foodAmountText);
        canteenAmountText = findViewById(R.id.canteenAmountText);
        travelAmountText = findViewById(R.id.travelAmountText);
        shoppingAmountText = findViewById(R.id.shoppingAmountText);
        educationAmountText = findViewById(R.id.educationAmountText);

        entertainmentAmountText =
                findViewById(R.id.entertainmentAmountText);

        billsAmountText =
                findViewById(R.id.billsAmountText);

        otherAmountText =
                findViewById(R.id.otherAmountText);

        expenseButton =
                findViewById(R.id.expenseButton);

        groupsButton =
                findViewById(R.id.groupsButton);

        smartTipButton =
                findViewById(R.id.smartTipButton);

        spendingPieChart =
                findViewById(R.id.spendingPieChart);
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        // -----------------------------------------------------
        // ADD EXPENSE
        // -----------------------------------------------------

        expenseButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            AddExpenseActivity.class
                    );

            intent.putExtra(
                    "email",
                    userEmail
            );

            startActivity(intent);
        });


        // -----------------------------------------------------
        // SPLIT BILL
        // -----------------------------------------------------

        groupsButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            SplitBillActivity.class
                    );

            intent.putExtra(
                    "email",
                    userEmail
            );

            startActivity(intent);
        });


        // -----------------------------------------------------
        // SMART MONEY CHECK
        // -----------------------------------------------------

        smartTipButton.setOnClickListener(
                v -> showSmartMoneyCheck()
        );


        // -----------------------------------------------------
        // MONTH SELECTOR
        // -----------------------------------------------------

        budgetDateText.setOnClickListener(
                v -> showMonthSelector()
        );

        budgetText.setOnClickListener(
                v -> showEditBudgetDialog()
        );


        // =====================================================
        // CATEGORY CLICK
        // =====================================================
        // अब category पर AlertDialog नहीं खुलेगा.
        // Separate CategoryDetailsActivity खुलेगी.
        // =====================================================

        foodAmountText.setOnClickListener(
                v -> openCategoryDetails("Food")
        );

        canteenAmountText.setOnClickListener(
                v -> openCategoryDetails("Canteen")
        );

        travelAmountText.setOnClickListener(
                v -> openCategoryDetails("Travel")
        );

        shoppingAmountText.setOnClickListener(
                v -> openCategoryDetails("Shopping")
        );

        educationAmountText.setOnClickListener(
                v -> openCategoryDetails("Education")
        );

        entertainmentAmountText.setOnClickListener(
                v -> openCategoryDetails("Entertainment")
        );

        billsAmountText.setOnClickListener(
                v -> openCategoryDetails("Bills")
        );

        otherAmountText.setOnClickListener(
                v -> openCategoryDetails("Other")
        );
    }


    // =========================================================
    // OPEN CATEGORY DETAILS PAGE
    // =========================================================

    private void openCategoryDetails(String category) {

        Intent intent =
                new Intent(
                        HomeActivity.this,
                        CategoryDetailsActivity.class
                );

        intent.putExtra(
                "email",
                userEmail
        );

        intent.putExtra(
                "category",
                category
        );

        startActivity(intent);
    }


    // =========================================================
    // USER DATA
    // =========================================================

    private void loadUserData() {

        try {

            String name =
                    databaseHelper.getUserName(
                            userEmail
                    );

            boolean hasName = name != null && !name.trim().isEmpty();
            String displayName = hasName ? name : "User";

            if (hasName) {

                greetingText.setText(
                        "Hello, " + name + " 👋"
                );

            } else {

                greetingText.setText(
                        "Hello 👋"
                );
            }

            // Top bar (visible on the left, next to the menu icon)
            userNameText.setText(displayName);
            userEmailText.setText(userEmail);

            // Drawer header
            navHeaderName.setText(displayName);
            navHeaderEmail.setText(userEmail);
            navHeaderAvatar.setText(
                    displayName.substring(0, 1).toUpperCase()
            );

            String[] profile = databaseHelper.getUserProfile(userEmail);
            ProfileImageHelper.show(
                    profile == null ? null : profile[4],
                    navHeaderPhoto,
                    navHeaderAvatar
            );


            double budget =
                    databaseHelper.getMonthlyBudget(
                            userEmail,
                            selectedMonth.get(Calendar.MONTH),
                            selectedMonth.get(Calendar.YEAR)
                    );

            budgetText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    budget
                            )
            );

        } catch (Exception e) {

            greetingText.setText(
                    "Hello 👋"
            );

            budgetText.setText(
                    "₹0"
            );
        }
    }


    // =========================================================
    // MONTHLY DATA
    // =========================================================

    private void loadMonthlyData() {

        try {

            int month =
                    selectedMonth.get(
                            Calendar.MONTH
                    );

            int year =
                    selectedMonth.get(
                            Calendar.YEAR
                    );


            // -------------------------------------------------
            // MONTH NAME
            // -------------------------------------------------

            String monthName =
                    new SimpleDateFormat(
                            "MMMM yyyy",
                            Locale.ENGLISH
                    ).format(
                            selectedMonth.getTime()
                    );

            budgetDateText.setText(
                    monthName +
                            " • Tap to change"
            );


            // -------------------------------------------------
            // BUDGET
            // -------------------------------------------------

            double budget =
                    databaseHelper.getMonthlyBudget(
                            userEmail,
                            month,
                            year
                    );

            budgetText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    budget
                            )
            );


            // -------------------------------------------------
            // TOTAL SPENT
            // -------------------------------------------------

            double totalSpent =
                    getMonthlyTotalFromDatabase(
                            month,
                            year
                    );

            spentText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    totalSpent
                            )
            );


            // -------------------------------------------------
            // BALANCE
            // -------------------------------------------------

            // Money to receive from split bills is added to the balance
            double credits =
                    databaseHelper.getMonthlyCredits(
                            userEmail,
                            month,
                            year
                    );

            double balance =
                    budget - totalSpent + credits;

            balanceText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    balance
                            )
            );


            // -------------------------------------------------
            // CATEGORY TOTALS
            // -------------------------------------------------

            double food =
                    getCategoryTotal(
                            "Food",
                            month,
                            year
                    );

            double canteen =
                    getCategoryTotal(
                            "Canteen",
                            month,
                            year
                    );

            double travel =
                    getCategoryTotal(
                            "Travel",
                            month,
                            year
                    );

            double shopping =
                    getCategoryTotal(
                            "Shopping",
                            month,
                            year
                    );

            double education =
                    getCategoryTotal(
                            "Education",
                            month,
                            year
                    );

            double entertainment =
                    getCategoryTotal(
                            "Entertainment",
                            month,
                            year
                    );

            double bills =
                    getCategoryTotal(
                            "Bills",
                            month,
                            year
                    );

            double other =
                    getCategoryTotal(
                            "Other",
                            month,
                            year
                    );


            // -------------------------------------------------
            // DISPLAY CATEGORY AMOUNTS
            // -------------------------------------------------

            foodAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    food
                            )
            );

            canteenAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    canteen
                            )
            );

            travelAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    travel
                            )
            );

            shoppingAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    shopping
                            )
            );

            educationAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    education
                            )
            );

            entertainmentAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    entertainment
                            )
            );

            billsAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    bills
                            )
            );

            otherAmountText.setText(
                    "₹" +
                            databaseHelper.formatAmount(
                                    other
                            )
            );


            // -------------------------------------------------
            // PIE CHART
            // -------------------------------------------------

            setupPieChart(
                    food,
                    canteen,
                    travel,
                    shopping,
                    education,
                    entertainment,
                    bills,
                    other
            );

        } catch (Exception e) {

            spentText.setText("₹0");

            try {

                balanceText.setText(
                        "₹" +
                                databaseHelper.formatAmount(
                                        databaseHelper.getUserBudget(
                                                userEmail
                                        )
                                )
                );

            } catch (Exception ignored) {

                balanceText.setText("₹0");
            }

            foodAmountText.setText("₹0");
            canteenAmountText.setText("₹0");
            travelAmountText.setText("₹0");
            shoppingAmountText.setText("₹0");
            educationAmountText.setText("₹0");
            entertainmentAmountText.setText("₹0");
            billsAmountText.setText("₹0");
            otherAmountText.setText("₹0");

            if (spendingPieChart != null) {

                spendingPieChart.clear();

                spendingPieChart.setNoDataText(
                        "No spending data"
                );

                spendingPieChart.invalidate();
            }
        }
    }


    // =========================================================
    // GET MONTHLY TOTAL
    // =========================================================

    private double getMonthlyTotalFromDatabase(
            int month,
            int year
    ) {

        double total = 0;

        Cursor cursor = null;

        try {

            cursor =
                    databaseHelper.getAllExpenses(
                            userEmail
                    );

            if (cursor == null) {
                return 0;
            }

            int dateIndex =
                    cursor.getColumnIndexOrThrow(
                            "date"
                    );

            int amountIndex =
                    cursor.getColumnIndexOrThrow(
                            "amount"
                    );


            while (cursor.moveToNext()) {

                String date =
                        cursor.getString(
                                dateIndex
                        );

                if (isDateInSelectedMonth(
                        date,
                        month,
                        year
                )) {

                    double amount =
                            cursor.getDouble(
                                    amountIndex
                            );

                    total += amount;
                }
            }

        } catch (Exception e) {

            total = 0;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }


    // =========================================================
    // CATEGORY TOTAL
    // =========================================================

    private double getCategoryTotal(
            String category,
            int month,
            int year
    ) {

        double total = 0;

        Cursor cursor = null;

        try {

            cursor =
                    databaseHelper.getAllExpenses(
                            userEmail
                    );

            if (cursor == null) {
                return 0;
            }

            int categoryIndex =
                    cursor.getColumnIndexOrThrow(
                            "category"
                    );

            int dateIndex =
                    cursor.getColumnIndexOrThrow(
                            "date"
                    );

            int amountIndex =
                    cursor.getColumnIndexOrThrow(
                            "amount"
                    );


            while (cursor.moveToNext()) {

                String savedCategory =
                        cursor.getString(
                                categoryIndex
                        );

                String date =
                        cursor.getString(
                                dateIndex
                        );


                if (savedCategory != null &&
                        savedCategory.equalsIgnoreCase(
                                category
                        ) &&
                        isDateInSelectedMonth(
                                date,
                                month,
                                year
                        )) {

                    double amount =
                            cursor.getDouble(
                                    amountIndex
                            );

                    total += amount;
                }
            }

        } catch (Exception e) {

            total = 0;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }


    // =========================================================
    // DATE CHECK
    // =========================================================

    private boolean isDateInSelectedMonth(
            String dateString,
            int month,
            int year
    ) {

        if (dateString == null ||
                dateString.trim().isEmpty()) {

            return false;
        }

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.ENGLISH
                    );

            format.setLenient(false);

            Date date =
                    format.parse(
                            dateString
                    );

            if (date == null) {
                return false;
            }

            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(date);

            return calendar.get(
                    Calendar.MONTH
            ) == month
                    &&
                    calendar.get(
                            Calendar.YEAR
                    ) == year;

        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // PIE CHART
    // =========================================================

    private void setupPieChart(
            double food,
            double canteen,
            double travel,
            double shopping,
            double education,
            double entertainment,
            double bills,
            double other
    ) {

        if (spendingPieChart == null) {
            return;
        }

        ArrayList<PieEntry> entries =
                new ArrayList<>();

        ArrayList<Integer> colors =
                new ArrayList<>();


        // FOOD
        if (food > 0) {

            entries.add(
                    new PieEntry(
                            (float) food,
                            "Food"
                    )
            );

            colors.add(
                    Color.rgb(
                            255,
                            99,
                            132
                    )
            );
        }


        // CANTEEN
        if (canteen > 0) {

            entries.add(
                    new PieEntry(
                            (float) canteen,
                            "Canteen"
                    )
            );

            colors.add(
                    Color.rgb(
                            255,
                            193,
                            7
                    )
            );
        }


        // TRAVEL
        if (travel > 0) {

            entries.add(
                    new PieEntry(
                            (float) travel,
                            "Travel"
                    )
            );

            colors.add(
                    Color.rgb(
                            54,
                            162,
                            235
                    )
            );
        }


        // SHOPPING
        if (shopping > 0) {

            entries.add(
                    new PieEntry(
                            (float) shopping,
                            "Shopping"
                    )
            );

            colors.add(
                    Color.rgb(
                            153,
                            102,
                            255
                    )
            );
        }


        // EDUCATION
        if (education > 0) {

            entries.add(
                    new PieEntry(
                            (float) education,
                            "Education"
                    )
            );

            colors.add(
                    Color.rgb(
                            75,
                            192,
                            120
                    )
            );
        }


        // ENTERTAINMENT
        if (entertainment > 0) {

            entries.add(
                    new PieEntry(
                            (float) entertainment,
                            "Entertainment"
                    )
            );

            colors.add(
                    Color.rgb(
                            255,
                            159,
                            64
                    )
            );
        }


        // BILLS
        if (bills > 0) {

            entries.add(
                    new PieEntry(
                            (float) bills,
                            "Bills"
                    )
            );

            colors.add(
                    Color.rgb(
                            100,
                            100,
                            100
                    )
            );
        }


        // OTHER
        if (other > 0) {

            entries.add(
                    new PieEntry(
                            (float) other,
                            "Other"
                    )
            );

            colors.add(
                    Color.rgb(
                            180,
                            180,
                            180
                    )
            );
        }


        // CLEAR OLD CHART

        spendingPieChart.clear();


        // NO DATA

        if (entries.isEmpty()) {

            spendingPieChart.setNoDataText(
                    "No expenses for this month"
            );

            spendingPieChart.setNoDataTextColor(
                    Color.GRAY
            );

            spendingPieChart.invalidate();

            return;
        }


        // DATA SET

        PieDataSet dataSet =
                new PieDataSet(
                        entries,
                        ""
                );

        dataSet.setColors(colors);

        dataSet.setValueTextSize(
                12f
        );

        dataSet.setValueTextColor(
                Color.WHITE
        );


        PieData pieData =
                new PieData(
                        dataSet
                );

        spendingPieChart.setData(
                pieData
        );

        spendingPieChart.setUsePercentValues(
                false
        );


        // CHART SETTINGS

        spendingPieChart
                .getDescription()
                .setEnabled(false);

        spendingPieChart.setDrawHoleEnabled(
                true
        );

        spendingPieChart.setHoleRadius(
                55f
        );

        spendingPieChart
                .setTransparentCircleRadius(
                        60f
                );


        spendingPieChart.setCenterText(
                "Spending"
        );

        spendingPieChart.setCenterTextSize(
                16f
        );

        spendingPieChart.setCenterTextColor(
                Color.rgb(
                        32,
                        33,
                        36
                )
        );


        spendingPieChart.setEntryLabelColor(
                Color.WHITE
        );

        spendingPieChart.setEntryLabelTextSize(
                11f
        );


        spendingPieChart
                .getLegend()
                .setEnabled(true);

        spendingPieChart
                .getLegend()
                .setTextSize(11f);


        spendingPieChart.animateY(
                700
        );

        spendingPieChart.invalidate();
    }


    // =========================================================
    // MONTH SELECTOR
    // =========================================================

    private void showMonthSelector() {

        ArrayList<String> months =
                new ArrayList<>();

        ArrayList<Calendar> monthValues =
                new ArrayList<>();


        Calendar temp =
                (Calendar) currentMonth.clone();


        // CURRENT + PREVIOUS 12 MONTHS

        for (int i = 0; i <= 12; i++) {

            Calendar month =
                    (Calendar) temp.clone();

            month.add(
                    Calendar.MONTH,
                    -i
            );

            monthValues.add(month);


            String monthText =
                    new SimpleDateFormat(
                            "MMMM yyyy",
                            Locale.ENGLISH
                    ).format(
                            month.getTime()
                    );

            months.add(monthText);
        }


        String[] monthArray =
                months.toArray(
                        new String[0]
                );


        int selectedIndex = 0;


        for (int i = 0;
             i < monthValues.size();
             i++) {

            Calendar month =
                    monthValues.get(i);

            if (month.get(
                    Calendar.YEAR
            )
                    ==
                    selectedMonth.get(
                            Calendar.YEAR
                    )
                    &&
                    month.get(
                            Calendar.MONTH
                    )
                            ==
                            selectedMonth.get(
                                    Calendar.MONTH
                            )) {

                selectedIndex = i;
                break;
            }
        }


        AlertDialog dialog =
                new AlertDialog.Builder(this)

                        .setTitle(
                                "Select Month"
                        )

                        .setSingleChoiceItems(
                                monthArray,
                                selectedIndex,
                                (d, which) -> {

                                    selectedMonth =
                                            (Calendar)
                                                    monthValues
                                                            .get(
                                                                    which
                                                            )
                                                            .clone();

                                    loadMonthlyData();

                                    d.dismiss();
                                }
                        )

                        .create();


        dialog.show();
    }


    // =========================================================
    // EDIT BUDGET (per selected month)
    // =========================================================

    private void showEditBudgetDialog() {

        String monthLabel =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.ENGLISH
                ).format(selectedMonth.getTime());

        final EditText input = new EditText(this);
        input.setHint("₹ 0.00");
        input.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        input.setTextColor(android.graphics.Color.parseColor("#202124"));

        double currentBudget =
                databaseHelper.getMonthlyBaseBudget(
                        userEmail,
                        selectedMonth.get(Calendar.MONTH),
                        selectedMonth.get(Calendar.YEAR)
                );

        if (currentBudget > 0) {
            input.setText(databaseHelper.formatAmount(currentBudget));
        }

        int pad = (int) (20 * getResources().getDisplayMetrics().density);

        new AlertDialog.Builder(this)

                .setTitle("Set Budget for " + monthLabel)

                .setView(wrapWithPadding(input, pad))

                .setNegativeButton("Cancel", null)

                .setPositiveButton("Save", (dialog, which) -> {

                    String text = input.getText().toString().trim();

                    if (text.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Enter an amount",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    try {

                        double amount = Double.parseDouble(text);

                        databaseHelper.setMonthlyBudget(
                                userEmail,
                                selectedMonth.get(Calendar.MONTH),
                                selectedMonth.get(Calendar.YEAR),
                                amount
                        );

                        Toast.makeText(
                                this,
                                monthLabel + " budget updated",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadMonthlyData();

                    } catch (NumberFormatException e) {

                        Toast.makeText(
                                this,
                                "Enter a valid amount",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .show();
    }

    private View wrapWithPadding(View view, int padding) {

        android.widget.FrameLayout frame =
                new android.widget.FrameLayout(this);

        frame.setPadding(padding, padding / 2, padding, 0);

        frame.addView(view);

        return frame;
    }


    // =========================================================
    // SMART MONEY CHECK
    // =========================================================

    private void showSmartMoneyCheck() {

        try {

            int month =
                    selectedMonth.get(
                            Calendar.MONTH
                    );

            int year =
                    selectedMonth.get(
                            Calendar.YEAR
                    );


            double budget =
                    databaseHelper.getMonthlyBudget(
                            userEmail,
                            month,
                            year
                    );


            double spent =
                    getMonthlyTotalFromDatabase(
                            month,
                            year
                    );


            double remaining =
                    budget - spent +
                            databaseHelper.getMonthlyCredits(
                                    userEmail,
                                    month,
                                    year
                            );


            String monthName =
                    new SimpleDateFormat(
                            "MMMM yyyy",
                            Locale.ENGLISH
                    ).format(
                            selectedMonth.getTime()
                    );


            String message;


            if (spent == 0) {

                message =
                        "No expenses recorded for " +
                                monthName +
                                ".\n\n" +
                                "Start tracking your spending to get smart insights.";

            } else if (budget <= 0) {

                message =
                        "Your budget is not set.\n\n" +
                                "Add a monthly budget to get better money insights.";

            } else if (spent > budget) {

                double extra =
                        spent - budget;


                message =
                        "⚠️ Budget exceeded!\n\n" +
                                "You spent ₹" +
                                databaseHelper.formatAmount(
                                        spent
                                ) +
                                " against a budget of ₹" +
                                databaseHelper.formatAmount(
                                        budget
                                ) +
                                ".\n\n" +
                                "You are over budget by ₹" +
                                databaseHelper.formatAmount(
                                        extra
                                ) +
                                ".";

            } else if (spent >= budget * 0.80) {

                message =
                        "⚡ You're close to your budget limit.\n\n" +
                                "Spent: ₹" +
                                databaseHelper.formatAmount(
                                        spent
                                ) +
                                "\nRemaining: ₹" +
                                databaseHelper.formatAmount(
                                        remaining
                                ) +
                                "\n\nTry to reduce unnecessary spending.";

            } else {

                message =
                        "✅ You're doing well!\n\n" +
                                "Spent: ₹" +
                                databaseHelper.formatAmount(
                                        spent
                                ) +
                                "\nRemaining: ₹" +
                                databaseHelper.formatAmount(
                                        remaining
                                ) +
                                "\n\nKeep tracking your expenses.";
            }


            new AlertDialog.Builder(this)

                    .setTitle(
                            "Smart Money Check"
                    )

                    .setMessage(
                            message
                    )

                    .setPositiveButton(
                            "Got it",
                            null
                    )

                    .show();


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to calculate smart money check",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // NOTIFICATION PERMISSION (Android 13+)
    // =========================================================

    private void requestNotificationPermission() {

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                androidx.core.app.ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    201
            );
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)

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

                            SessionManager.logout(HomeActivity.this);

                            Intent intent =
                                    new Intent(
                                            HomeActivity.this,
                                            LoginActivity.class
                                    );


                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );


                            startActivity(intent);

                            finish();
                        }
                )

                .show();
    }


    // =========================================================
    // REFRESH HOME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (databaseHelper != null &&
                userEmail != null &&
                !userEmail.trim().isEmpty()) {

            loadUserData();

            loadMonthlyData();
        }
    }
}