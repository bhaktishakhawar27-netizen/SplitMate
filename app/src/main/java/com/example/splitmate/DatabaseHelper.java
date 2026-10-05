package com.example.splitmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SplitMate.db";
    private static final int DATABASE_VERSION = 11;


    // =========================================================
    // USERS TABLE
    // =========================================================

    private static final String USERS = "users";

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String COLLEGE = "college";
    private static final String COURSE = "course";
    private static final String BUDGET = "budget";
    private static final String PASSWORD = "password";
    private static final String PROFILE_PHOTO = "profile_photo";


    // =========================================================
    // EXPENSES TABLE
    // =========================================================

    private static final String EXPENSES = "expenses";

    private static final String EXPENSE_ID = "expense_id";
    private static final String USER_EMAIL = "user_email";
    private static final String AMOUNT = "amount";
    private static final String CATEGORY = "category";
    private static final String OTHER_CATEGORY = "other_category";
    private static final String DESCRIPTION = "description";
    private static final String DATE = "date";
    private static final String TIME = "time";


    private static final String EXPENSE_SPLIT_BILL_ID = "split_bill_id";


    // =========================================================
    // SPLIT BILLS TABLE
    // =========================================================

    private static final String SPLIT_BILLS = "split_bills";

    private static final String BILL_ID = "bill_id";
    private static final String BILL_DESCRIPTION = "description";
    private static final String TOTAL_AMOUNT = "total_amount";
    private static final String PAID_BY = "paid_by";
    private static final String BILL_DATE = "bill_date";
    private static final String BILL_TIME = "bill_time";


    // =========================================================
    // SPLIT MEMBERS TABLE
    // =========================================================

    private static final String SPLIT_MEMBERS = "split_members";

    private static final String MEMBER_ID = "member_id";
    private static final String MEMBER_BILL_ID = "bill_id";
    private static final String MEMBER_NAME = "member_name";
    private static final String MEMBER_AMOUNT = "amount";
    private static final String MEMBER_PAID_AMOUNT = "paid_amount";


    // =========================================================
    // PAYMENT REMINDERS TABLE
    // =========================================================

    private static final String REMINDERS = "reminders";

    private static final String REMINDER_ID = "reminder_id";
    private static final String REMINDER_TITLE = "title";
    private static final String REMINDER_AMOUNT = "amount";
    private static final String REMINDER_DATE = "due_date";
    private static final String REMINDER_TIME = "due_time";
    private static final String REMINDER_NOTES = "notes";


    // =========================================================
    // SAVINGS GOALS TABLE
    // =========================================================

    private static final String SAVINGS_GOALS = "savings_goals";

    private static final String GOAL_ID = "goal_id";
    private static final String GOAL_NAME = "goal_name";
    private static final String GOAL_TARGET_AMOUNT = "target_amount";
    private static final String GOAL_SAVED_AMOUNT = "saved_amount";
    private static final String GOAL_TARGET_DATE = "target_date";


    // =========================================================
    // MONTHLY BUDGETS TABLE
    // =========================================================

    private static final String MONTHLY_BUDGETS = "monthly_budgets";

    private static final String MB_ID = "mb_id";
    private static final String MB_MONTH = "month";
    private static final String MB_YEAR = "year";
    private static final String MB_AMOUNT = "amount";


    // =========================================================
    // BALANCE CREDITS TABLE (money to receive from split bills)
    // =========================================================

    private static final String BALANCE_CREDITS = "balance_credits";

    private static final String BC_ID = "credit_id";
    private static final String BC_AMOUNT = "amount";
    private static final String BC_DESCRIPTION = "description";
    private static final String BC_DATE = "date";
    private static final String BC_TIME = "time";

    // Optional link for credits created by a split bill settlement.
    private static final String BC_SPLIT_BILL_ID = "split_bill_id";




    // =========================================================
    // EXTRA MONTHLY BUDGET TABLE
    // =========================================================

    private static final String BUDGET_ADDITIONS = "budget_additions";
    private static final String BA_ID = "addition_id";
    private static final String BA_MONTH = "month";
    private static final String BA_YEAR = "year";
    private static final String BA_AMOUNT = "amount";
    private static final String BA_NOTE = "note";
    private static final String BA_DATE = "added_date";

    // =========================================================
    // TO-DO TABLE
    // =========================================================

    private static final String TODOS = "todos";
    private static final String TODO_ID = "todo_id";
    private static final String TODO_TITLE = "title";
    private static final String TODO_DUE_DATE = "due_date";
    private static final String TODO_DONE = "is_done";

    // Split-bill settlement records
    private static final String SETTLEMENTS = "settlements";
    private static final String SETTLEMENT_ID = "settlement_id";
    private static final String SETTLEMENT_BILL_ID = "bill_id";
    private static final String SETTLEMENT_USER_EMAIL = "user_email";
    private static final String SETTLEMENT_MEMBER_NAME = "member_name";
    private static final String SETTLEMENT_AMOUNT = "amount";
    private static final String SETTLEMENT_TYPE = "type";
    private static final String SETTLEMENT_DONE = "is_done";
    // id of the balance credit / expense created when marked Paid,
    // so it can be reversed if the person is marked Not Paid again.
    private static final String SETTLEMENT_ENTRY_ID = "entry_id";

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DatabaseHelper(Context context) {

        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }


    // =========================================================
    // CREATE DATABASE
    // =========================================================

    @Override
    public void onCreate(SQLiteDatabase db) {


        // -----------------------------------------------------
        // USERS
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + USERS + " (" +

                        ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        NAME + " TEXT NOT NULL, " +

                        EMAIL + " TEXT UNIQUE NOT NULL, " +

                        COLLEGE + " TEXT, " +

                        COURSE + " TEXT, " +

                        BUDGET + " REAL, " +

                        PASSWORD + " TEXT NOT NULL, " +

                        PROFILE_PHOTO + " TEXT" +

                        ")"
        );


        // -----------------------------------------------------
        // EXPENSES
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + EXPENSES + " (" +

                        EXPENSE_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        USER_EMAIL +
                        " TEXT NOT NULL, " +

                        AMOUNT +
                        " REAL NOT NULL, " +

                        CATEGORY +
                        " TEXT NOT NULL, " +

                        OTHER_CATEGORY +
                        " TEXT, " +

                        DESCRIPTION +
                        " TEXT, " +

                        DATE +
                        " TEXT, " +

                        TIME +
                        " TEXT, " +

                        EXPENSE_SPLIT_BILL_ID +
                        " INTEGER" +

                        ")"
        );


        // -----------------------------------------------------
        // SPLIT BILLS
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + SPLIT_BILLS + " (" +

                        BILL_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        USER_EMAIL +
                        " TEXT NOT NULL, " +

                        BILL_DESCRIPTION +
                        " TEXT, " +

                        TOTAL_AMOUNT +
                        " REAL NOT NULL, " +

                        PAID_BY +
                        " TEXT NOT NULL, " +

                        BILL_DATE +
                        " TEXT, " +

                        BILL_TIME +
                        " TEXT" +

                        ")"
        );


        // -----------------------------------------------------
        // SPLIT MEMBERS
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + SPLIT_MEMBERS + " (" +

                        MEMBER_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        MEMBER_BILL_ID +
                        " INTEGER NOT NULL, " +

                        MEMBER_NAME +
                        " TEXT NOT NULL, " +

                        MEMBER_AMOUNT +
                        " REAL NOT NULL, " +

                        MEMBER_PAID_AMOUNT +
                        " REAL DEFAULT 0" +

                        ")"
        );


        // -----------------------------------------------------
        // PAYMENT REMINDERS
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + REMINDERS + " (" +

                        REMINDER_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        USER_EMAIL +
                        " TEXT NOT NULL, " +

                        REMINDER_TITLE +
                        " TEXT NOT NULL, " +

                        REMINDER_AMOUNT +
                        " REAL NOT NULL, " +

                        REMINDER_DATE +
                        " TEXT NOT NULL, " +

                        REMINDER_TIME +
                        " TEXT NOT NULL, " +

                        REMINDER_NOTES +
                        " TEXT" +

                        ")"
        );


        // -----------------------------------------------------
        // SAVINGS GOALS
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + SAVINGS_GOALS + " (" +

                        GOAL_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        USER_EMAIL +
                        " TEXT NOT NULL, " +

                        GOAL_NAME +
                        " TEXT NOT NULL, " +

                        GOAL_TARGET_AMOUNT +
                        " REAL NOT NULL, " +

                        GOAL_SAVED_AMOUNT +
                        " REAL DEFAULT 0, " +

                        GOAL_TARGET_DATE +
                        " TEXT" +

                        ")"
        );


        // -----------------------------------------------------
        // MONTHLY BUDGETS
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + MONTHLY_BUDGETS + " (" +

                        MB_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        USER_EMAIL +
                        " TEXT NOT NULL, " +

                        MB_MONTH +
                        " INTEGER NOT NULL, " +

                        MB_YEAR +
                        " INTEGER NOT NULL, " +

                        MB_AMOUNT +
                        " REAL NOT NULL, " +

                        "UNIQUE(" + USER_EMAIL + ", " + MB_MONTH + ", " + MB_YEAR + ")" +

                        ")"
        );


        // -----------------------------------------------------
        // BALANCE CREDITS
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " + BALANCE_CREDITS + " (" +
                        BC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_EMAIL + " TEXT NOT NULL, " +
                        BC_AMOUNT + " REAL NOT NULL, " +
                        BC_DESCRIPTION + " TEXT, " +
                        BC_DATE + " TEXT NOT NULL, " +
                        BC_TIME + " TEXT, " +
                        BC_SPLIT_BILL_ID + " INTEGER" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + BUDGET_ADDITIONS + " (" +
                        BA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_EMAIL + " TEXT NOT NULL, " +
                        BA_MONTH + " INTEGER NOT NULL, " +
                        BA_YEAR + " INTEGER NOT NULL, " +
                        BA_AMOUNT + " REAL NOT NULL, " +
                        BA_NOTE + " TEXT, " +
                        BA_DATE + " TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TODOS + " (" +
                        TODO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_EMAIL + " TEXT NOT NULL, " +
                        TODO_TITLE + " TEXT NOT NULL, " +
                        TODO_DUE_DATE + " TEXT, " +
                        TODO_DONE + " INTEGER DEFAULT 0" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + SETTLEMENTS + " (" +
                        SETTLEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SETTLEMENT_BILL_ID + " INTEGER NOT NULL, " +
                        SETTLEMENT_USER_EMAIL + " TEXT NOT NULL, " +
                        SETTLEMENT_MEMBER_NAME + " TEXT NOT NULL, " +
                        SETTLEMENT_AMOUNT + " REAL NOT NULL, " +
                        SETTLEMENT_TYPE + " TEXT NOT NULL, " +
                        SETTLEMENT_DONE + " INTEGER DEFAULT 0, " +
                        SETTLEMENT_ENTRY_ID + " INTEGER DEFAULT 0" +
                        ")"
        );
    }


    // =========================================================
    // DATABASE UPGRADE
    // =========================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Non-destructive upgrades: keep existing SplitMate data.
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE " + USERS + " ADD COLUMN "
                        + PROFILE_PHOTO + " TEXT");
            } catch (Exception ignored) {
                // column already exists
            }
        }
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + BUDGET_ADDITIONS + " (" +
                        BA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_EMAIL + " TEXT NOT NULL, " +
                        BA_MONTH + " INTEGER NOT NULL, " +
                        BA_YEAR + " INTEGER NOT NULL, " +
                        BA_AMOUNT + " REAL NOT NULL, " +
                        BA_NOTE + " TEXT, " +
                        BA_DATE + " TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TODOS + " (" +
                        TODO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_EMAIL + " TEXT NOT NULL, " +
                        TODO_TITLE + " TEXT NOT NULL, " +
                        TODO_DUE_DATE + " TEXT, " +
                        TODO_DONE + " INTEGER DEFAULT 0" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + SETTLEMENTS + " (" +
                        SETTLEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SETTLEMENT_BILL_ID + " INTEGER NOT NULL, " +
                        SETTLEMENT_USER_EMAIL + " TEXT NOT NULL, " +
                        SETTLEMENT_MEMBER_NAME + " TEXT NOT NULL, " +
                        SETTLEMENT_AMOUNT + " REAL NOT NULL, " +
                        SETTLEMENT_TYPE + " TEXT NOT NULL, " +
                        SETTLEMENT_DONE + " INTEGER DEFAULT 0, " +
                        SETTLEMENT_ENTRY_ID + " INTEGER DEFAULT 0" +
                        ")"
        );

        if (oldVersion < 10) {
            try {
                db.execSQL("ALTER TABLE " + SETTLEMENTS + " ADD COLUMN "
                        + SETTLEMENT_ENTRY_ID + " INTEGER DEFAULT 0");
            } catch (Exception ignored) {
                // column already exists
            }
        }

        // Version 11: link split-bill-created expenses/credits to their bill.
        // This lets deleting a history record reverse every related balance
        // change without touching unrelated expenses or credits.
        if (oldVersion < 11) {
            try {
                db.execSQL("ALTER TABLE " + EXPENSES + " ADD COLUMN "
                        + EXPENSE_SPLIT_BILL_ID + " INTEGER");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                db.execSQL("ALTER TABLE " + BALANCE_CREDITS + " ADD COLUMN "
                        + BC_SPLIT_BILL_ID + " INTEGER");
            } catch (Exception ignored) {
                // column already exists
            }
        }
    }


    // =========================================================
    // USER METHODS
    // =========================================================

    public boolean registerUser(
            String name,
            String email,
            String college,
            String course,
            double budget,
            String password) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(NAME, name);
        values.put(EMAIL, email);
        values.put(COLLEGE, college);
        values.put(COURSE, course);
        values.put(BUDGET, budget);
        values.put(PASSWORD, password);

        long result =
                db.insert(
                        USERS,
                        null,
                        values
                );

        return result != -1;
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public boolean checkLogin(
            String email,
            String password) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.query(
                        USERS,

                        new String[]{
                                ID
                        },

                        EMAIL + "=? AND " +
                                PASSWORD + "=?",

                        new String[]{
                                email,
                                password
                        },

                        null,
                        null,
                        null
                );

        boolean valid =
                cursor.moveToFirst();

        cursor.close();

        return valid;
    }


    // =========================================================
    // GET USER NAME
    // =========================================================

    public String getUserName(
            String email) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.query(
                        USERS,

                        new String[]{
                                NAME
                        },

                        EMAIL + "=?",

                        new String[]{
                                email
                        },

                        null,
                        null,
                        null
                );

        String name = "";

        if (cursor.moveToFirst()) {

            name =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    NAME
                            )
                    );
        }

        cursor.close();

        return name;
    }


    // =========================================================
    // GET USER BUDGET
    // =========================================================

    public double getUserBudget(
            String email) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.query(
                        USERS,

                        new String[]{
                                BUDGET
                        },

                        EMAIL + "=?",

                        new String[]{
                                email
                        },

                        null,
                        null,
                        null
                );

        double budget = 0;

        if (cursor.moveToFirst()) {

            budget =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    BUDGET
                            )
                    );
        }

        cursor.close();

        return budget;
    }


    // =========================================================
    // EXPENSE METHODS
    // =========================================================

    // ADD EXPENSE

    public boolean addExpense(
            String userEmail,
            double amount,
            String category,
            String otherCategory,
            String description,
            String date,
            String time) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                USER_EMAIL,
                userEmail
        );

        values.put(
                AMOUNT,
                amount
        );

        values.put(
                CATEGORY,
                category
        );

        values.put(
                OTHER_CATEGORY,
                otherCategory
        );

        values.put(
                DESCRIPTION,
                description
        );

        values.put(
                DATE,
                date
        );

        values.put(
                TIME,
                time
        );

        long result =
                db.insert(
                        EXPENSES,
                        null,
                        values
                );

        return result != -1;
    }


    /**
     * Adds the expense generated when the logged-in user paid a split bill.
     * The bill id is stored so deleting that split bill can reverse this
     * exact expense.
     */
    public boolean addSplitBillExpense(
            long billId,
            String userEmail,
            double amount,
            String category,
            String otherCategory,
            String description,
            String date,
            String time) {

        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, userEmail);
        values.put(AMOUNT, amount);
        values.put(CATEGORY, category);
        values.put(OTHER_CATEGORY, otherCategory);
        values.put(DESCRIPTION, description);
        values.put(DATE, date);
        values.put(TIME, time);
        values.put(EXPENSE_SPLIT_BILL_ID, billId);

        return getWritableDatabase().insert(
                EXPENSES, null, values
        ) != -1;
    }


    // =========================================================
    // ALL TIME TOTAL EXPENSE
    // =========================================================

    public double getTotalExpenses(
            String userEmail) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(

                        "SELECT SUM(" +
                                AMOUNT +
                                ") FROM " +
                                EXPENSES +
                                " WHERE " +
                                USER_EMAIL +
                                "=?",

                        new String[]{
                                userEmail
                        }
                );

        double total = 0;

        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                total =
                        cursor.getDouble(0);
            }
        }

        cursor.close();

        return total;
    }


    // =========================================================
    // ALL TIME CATEGORY TOTAL
    // =========================================================

    public double getCategoryTotal(
            String userEmail,
            String category) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(

                        "SELECT SUM(" +
                                AMOUNT +
                                ") FROM " +
                                EXPENSES +

                                " WHERE " +
                                USER_EMAIL +
                                "=? AND " +
                                CATEGORY +
                                "=?",

                        new String[]{
                                userEmail,
                                category
                        }
                );

        double total = 0;

        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                total =
                        cursor.getDouble(0);
            }
        }

        cursor.close();

        return total;
    }


    // =========================================================
    // MONTH-WISE TOTAL EXPENSE
    // =========================================================

    public double getMonthlyTotal(
            String userEmail,
            int month,
            int year) {

        SQLiteDatabase db =
                getReadableDatabase();

        String monthName =
                getMonthName(month);

        String searchPattern =
                "% " + monthName + " " + year;

        Cursor cursor =
                db.rawQuery(

                        "SELECT SUM(" +
                                AMOUNT +
                                ") FROM " +
                                EXPENSES +

                                " WHERE " +
                                USER_EMAIL +
                                "=? AND " +
                                DATE +
                                " LIKE ?",

                        new String[]{
                                userEmail,
                                searchPattern
                        }
                );

        double total = 0;

        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                total =
                        cursor.getDouble(0);
            }
        }

        cursor.close();

        return total;
    }


    // =========================================================
    // MONTH-WISE CATEGORY TOTAL
    // =========================================================

    public double getMonthlyCategoryTotal(
            String userEmail,
            String category,
            int month,
            int year) {

        SQLiteDatabase db =
                getReadableDatabase();

        String monthName =
                getMonthName(month);

        String searchPattern =
                "% " + monthName + " " + year;

        Cursor cursor =
                db.rawQuery(

                        "SELECT SUM(" +
                                AMOUNT +
                                ") FROM " +
                                EXPENSES +

                                " WHERE " +
                                USER_EMAIL +
                                "=? AND " +
                                CATEGORY +
                                "=? AND " +
                                DATE +
                                " LIKE ?",

                        new String[]{
                                userEmail,
                                category,
                                searchPattern
                        }
                );

        double total = 0;

        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                total =
                        cursor.getDouble(0);
            }
        }

        cursor.close();

        return total;
    }


    // =========================================================
    // MONTH-WISE ALL EXPENSES
    // =========================================================

    public Cursor getMonthlyExpenses(
            String userEmail,
            int month,
            int year) {

        SQLiteDatabase db =
                getReadableDatabase();

        String monthName =
                getMonthName(month);

        String searchPattern =
                "% " + monthName + " " + year;

        return db.query(
                EXPENSES,

                null,

                USER_EMAIL +
                        "=? AND " +
                        DATE +
                        " LIKE ?",

                new String[]{
                        userEmail,
                        searchPattern
                },

                null,
                null,

                EXPENSE_ID + " DESC"
        );
    }


    // =========================================================
    // MONTH-WISE CATEGORY EXPENSES
    // =========================================================

    public Cursor getMonthlyCategoryExpenses(
            String userEmail,
            String category,
            int month,
            int year) {

        SQLiteDatabase db =
                getReadableDatabase();

        String monthName =
                getMonthName(month);

        String searchPattern =
                "% " + monthName + " " + year;

        return db.query(
                EXPENSES,

                null,

                USER_EMAIL +
                        "=? AND " +
                        CATEGORY +
                        "=? AND " +
                        DATE +
                        " LIKE ?",

                new String[]{
                        userEmail,
                        category,
                        searchPattern
                },

                null,
                null,

                EXPENSE_ID + " DESC"
        );
    }


    // =========================================================
    // ALL EXPENSES
    // =========================================================

    public Cursor getAllExpenses(
            String userEmail) {

        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                EXPENSES,

                null,

                USER_EMAIL + "=?",

                new String[]{
                        userEmail
                },

                null,
                null,

                EXPENSE_ID + " DESC"
        );
    }


    // =========================================================
    // CATEGORY EXPENSES
    // =========================================================

    public Cursor getCategoryExpenses(
            String userEmail,
            String category) {

        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                EXPENSES,

                null,

                USER_EMAIL +
                        "=? AND " +
                        CATEGORY +
                        "=?",

                new String[]{
                        userEmail,
                        category
                },

                null,
                null,

                EXPENSE_ID + " DESC"
        );
    }


    // =========================================================
    // SPLIT BILL METHODS
    // =========================================================

    // CREATE SPLIT BILL

    public long createSplitBill(
            String userEmail,
            String description,
            double totalAmount,
            String paidBy,
            String date,
            String time) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                USER_EMAIL,
                userEmail
        );

        values.put(
                BILL_DESCRIPTION,
                description
        );

        values.put(
                TOTAL_AMOUNT,
                totalAmount
        );

        values.put(
                PAID_BY,
                paidBy
        );

        values.put(
                BILL_DATE,
                date
        );

        values.put(
                BILL_TIME,
                time
        );

        return db.insert(
                SPLIT_BILLS,
                null,
                values
        );
    }


    // =========================================================
    // ADD SPLIT MEMBER
    // =========================================================

    public boolean addSplitMember(
            long billId,
            String memberName,
            double amount,
            double paidAmount) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MEMBER_BILL_ID,
                billId
        );

        values.put(
                MEMBER_NAME,
                memberName
        );

        values.put(
                MEMBER_AMOUNT,
                amount
        );

        values.put(
                MEMBER_PAID_AMOUNT,
                paidAmount
        );

        long result =
                db.insert(
                        SPLIT_MEMBERS,
                        null,
                        values
                );

        return result != -1;
    }


    // =========================================================
    // GET MONTH NAME
    // =========================================================

    private String getMonthName(
            int month) {

        Calendar calendar =
                Calendar.getInstance();

        // Day must be 1 first, otherwise on the 31st setting a 30-day
        // month rolls over into the next month (wrong month name).
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        calendar.set(
                Calendar.MONTH,
                month
        );

        return new SimpleDateFormat(
                "MMM",
                Locale.ENGLISH
        ).format(
                calendar.getTime()
        );
    }


    // =========================================================
    // MONTHLY BUDGET METHODS
    // =========================================================

    public boolean setMonthlyBudget(
            String userEmail,
            int month,
            int year,
            double amount) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(USER_EMAIL, userEmail);
        values.put(MB_MONTH, month);
        values.put(MB_YEAR, year);
        values.put(MB_AMOUNT, amount);

        // insertWithOnConflict + REPLACE relies on the UNIQUE(email, month, year)
        // constraint, so setting a budget for a month that already has one
        // overwrites it instead of creating a duplicate row.
        long result =
                db.insertWithOnConflict(
                        MONTHLY_BUDGETS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );

        return result != -1;
    }

    /**
     * Returns the budget set specifically for this month/year, if any.
     * Falls back to the account's default budget (set at registration /
     * in Account settings) when no month-specific budget has been saved yet.
     */
    public double getMonthlyBudget(
            String userEmail,
            int month,
            int year) {

        // Public budget value now means the user's usable budget:
        // base monthly budget + any extra amount manually added for this month.
        return getMonthlyBaseBudget(userEmail, month, year)
                + getMonthlyBudgetAddition(userEmail, month, year);
    }

    public double getMonthlyBaseBudget(
            String userEmail,
            int month,
            int year) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                MONTHLY_BUDGETS,
                new String[]{MB_AMOUNT},
                USER_EMAIL + "=? AND " + MB_MONTH + "=? AND " + MB_YEAR + "=?",
                new String[]{
                        userEmail,
                        String.valueOf(month),
                        String.valueOf(year)
                },
                null,
                null,
                null
        );

        double result = getUserBudget(userEmail);

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MB_AMOUNT)
            );
        }

        if (cursor != null) {
            cursor.close();
        }

        return result;
    }

    public double getMonthlyBudgetAddition(
            String userEmail,
            int month,
            int year) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(" + BA_AMOUNT + "),0) FROM " +
                        BUDGET_ADDITIONS +
                        " WHERE " + USER_EMAIL + "=? AND " +
                        BA_MONTH + "=? AND " +
                        BA_YEAR + "=?",
                new String[]{
                        userEmail,
                        String.valueOf(month),
                        String.valueOf(year)
                }
        );

        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public boolean addMonthlyBudgetAddition(
            String userEmail,
            int month,
            int year,
            double amount,
            String note,
            String date) {

        if (amount <= 0) return false;

        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, userEmail);
        values.put(BA_MONTH, month);
        values.put(BA_YEAR, year);
        values.put(BA_AMOUNT, amount);
        values.put(BA_NOTE, note);
        values.put(BA_DATE, date);

        return getWritableDatabase().insert(
                BUDGET_ADDITIONS, null, values
        ) != -1;
    }


    // =========================================================
    // USER EXISTS (used to validate a saved login session)
    // =========================================================

    public boolean userExists(String email) {

        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                USERS,
                new String[]{ID},
                EMAIL + "=?",
                new String[]{email},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();

        cursor.close();

        return exists;
    }


    // =========================================================
    // BALANCE CREDITS (money the user should receive)
    // =========================================================

    public boolean addBalanceCredit(
            String userEmail,
            double amount,
            String description,
            String date,
            String time) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_EMAIL, userEmail);
        values.put(BC_AMOUNT, amount);
        values.put(BC_DESCRIPTION, description);
        values.put(BC_DATE, date);
        values.put(BC_TIME, time);

        return db.insert(BALANCE_CREDITS, null, values) != -1;
    }

    /**
     * Adds a balance credit generated by a split-bill payment.
     * The bill id is stored for exact reversal when the history record is deleted.
     */
    public long addSplitBillBalanceCredit(
            long billId,
            String userEmail,
            double amount,
            String description,
            String date,
            String time) {

        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, userEmail);
        values.put(BC_AMOUNT, amount);
        values.put(BC_DESCRIPTION, description);
        values.put(BC_DATE, date);
        values.put(BC_TIME, time);
        values.put(BC_SPLIT_BILL_ID, billId);

        return getWritableDatabase().insert(
                BALANCE_CREDITS, null, values
        );
    }


    /** month is 0-based (Calendar.MONTH), same as getMonthlyTotal(). */
    public double getMonthlyCredits(
            String userEmail,
            int month,
            int year) {

        SQLiteDatabase db = getReadableDatabase();

        String searchPattern =
                "% " + getMonthName(month) + " " + year;

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + BC_AMOUNT + ") FROM " + BALANCE_CREDITS +
                        " WHERE " + USER_EMAIL + "=? AND " +
                        BC_DATE + " LIKE ?",
                new String[]{userEmail, searchPattern}
        );

        double total = 0;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }


    // =========================================================
    // DELETE EXPENSE
    // =========================================================

    public boolean deleteExpense(long expenseId) {

        SQLiteDatabase db =
                getWritableDatabase();

        int rows =
                db.delete(
                        EXPENSES,
                        EXPENSE_ID + "=?",
                        new String[]{
                                String.valueOf(expenseId)
                        }
                );

        return rows > 0;
    }


    // =========================================================
    // PAYMENT REMINDER METHODS
    // =========================================================

    public long addReminder(
            String userEmail,
            String title,
            double amount,
            String dueDate,
            String dueTime,
            String notes) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(USER_EMAIL, userEmail);
        values.put(REMINDER_TITLE, title);
        values.put(REMINDER_AMOUNT, amount);
        values.put(REMINDER_DATE, dueDate);
        values.put(REMINDER_TIME, dueTime);
        values.put(REMINDER_NOTES, notes);

        return db.insert(
                REMINDERS,
                null,
                values
        );
    }

    public Cursor getReminders(String userEmail) {

        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                REMINDERS,
                null,
                USER_EMAIL + "=?",
                new String[]{userEmail},
                null,
                null,
                REMINDER_DATE + " ASC, " + REMINDER_TIME + " ASC"
        );
    }

    public boolean deleteReminder(long reminderId) {

        SQLiteDatabase db =
                getWritableDatabase();

        int rows =
                db.delete(
                        REMINDERS,
                        REMINDER_ID + "=?",
                        new String[]{
                                String.valueOf(reminderId)
                        }
                );

        return rows > 0;
    }


    // =========================================================
    // SAVINGS GOAL METHODS
    // =========================================================

    /**
     * Creates a new savings goal without replacing the user's existing goals.
     * Existing goals are intentionally preserved.
     */
    public boolean setSavingsGoal(
            String userEmail,
            String goalName,
            double targetAmount,
            String targetDate) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, userEmail);
        values.put(GOAL_NAME, goalName);
        values.put(GOAL_TARGET_AMOUNT, targetAmount);
        values.put(GOAL_SAVED_AMOUNT, 0);
        values.put(GOAL_TARGET_DATE, targetDate);

        return db.insert(SAVINGS_GOALS, null, values) != -1;
    }

    /**
     * Returns every savings goal belonging to the current user.
     */
    public Cursor getSavingsGoals(String userEmail) {
        return getReadableDatabase().query(
                SAVINGS_GOALS,
                null,
                USER_EMAIL + "=?",
                new String[]{userEmail},
                null,
                null,
                GOAL_ID + " DESC"
        );
    }

    /**
     * Kept for compatibility with any existing code that expects the old
     * single-goal method. It now simply returns the newest goal.
     */
    public Cursor getSavingsGoal(String userEmail) {
        return getReadableDatabase().query(
                SAVINGS_GOALS,
                null,
                USER_EMAIL + "=?",
                new String[]{userEmail},
                null,
                null,
                GOAL_ID + " DESC",
                "1"
        );
    }

    public boolean addToSavings(long goalId, double amountToAdd) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
                "UPDATE " + SAVINGS_GOALS +
                        " SET " + GOAL_SAVED_AMOUNT +
                        " = " + GOAL_SAVED_AMOUNT + " + ?" +
                        " WHERE " + GOAL_ID + " = ?",
                new Object[]{amountToAdd, goalId}
        );
        return true;
    }

    public boolean deleteSavingsGoal(long goalId) {
        SQLiteDatabase db = getWritableDatabase();

        int rows = db.delete(
                SAVINGS_GOALS,
                GOAL_ID + "=?",
                new String[]{String.valueOf(goalId)}
        );

        return rows > 0;
    }


    // =========================================================
    // SPLIT-BILL SETTLEMENT METHODS
    // =========================================================

    public long addSettlement(
            long billId,
            String userEmail,
            String memberName,
            double amount,
            String type) {

        ContentValues values = new ContentValues();
        values.put(SETTLEMENT_BILL_ID, billId);
        values.put(SETTLEMENT_USER_EMAIL, userEmail);
        values.put(SETTLEMENT_MEMBER_NAME, memberName);
        values.put(SETTLEMENT_AMOUNT, amount);
        values.put(SETTLEMENT_TYPE, type);
        values.put(SETTLEMENT_DONE, 0);
        values.put(SETTLEMENT_ENTRY_ID, 0);

        return getWritableDatabase().insert(SETTLEMENTS, null, values);
    }

    public Cursor getSettlements(long billId, String userEmail) {
        return getReadableDatabase().query(
                SETTLEMENTS,
                null,
                SETTLEMENT_BILL_ID + "=? AND " + SETTLEMENT_USER_EMAIL + "=?",
                new String[]{String.valueOf(billId), userEmail},
                null,
                null,
                SETTLEMENT_ID + " ASC"
        );
    }

    /**
     * Marks a settlement Paid / Not Paid and updates the real expense or
     * balance-credit record at the same time.
     */
    public boolean setSettlementPaid(
            long settlementId,
            boolean paid,
            String userEmail,
            String billDescription,
            String date,
            String time) {

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.query(
                SETTLEMENTS,
                null,
                SETTLEMENT_ID + "=? AND " + SETTLEMENT_USER_EMAIL + "=?",
                new String[]{String.valueOf(settlementId), userEmail},
                null, null, null);

        if (!c.moveToFirst()) {
            c.close();
            return false;
        }

        long billId = c.getLong(c.getColumnIndexOrThrow(SETTLEMENT_BILL_ID));
        String type = c.getString(c.getColumnIndexOrThrow(SETTLEMENT_TYPE));
        String member = c.getString(c.getColumnIndexOrThrow(SETTLEMENT_MEMBER_NAME));
        double amount = c.getDouble(c.getColumnIndexOrThrow(SETTLEMENT_AMOUNT));
        boolean done = c.getInt(c.getColumnIndexOrThrow(SETTLEMENT_DONE)) == 1;
        long entryId = c.getLong(c.getColumnIndexOrThrow(SETTLEMENT_ENTRY_ID));
        c.close();

        if (done == paid) {
            return true;
        }

        boolean isReceive = "RECEIVE".equals(type);
        String text = (isReceive ? "Received from " : "Paid to ")
                + member + ": " + billDescription;

        db.beginTransaction();
        try {
            if (paid) {
                long newId;

                if (isReceive) {
                    newId = addSplitBillBalanceCredit(
                            billId, userEmail, amount, text, date, time);
                } else {
                    ContentValues v = new ContentValues();
                    v.put(USER_EMAIL, userEmail);
                    v.put(AMOUNT, amount);
                    v.put(CATEGORY, "Split Bill");
                    v.put(OTHER_CATEGORY, "");
                    v.put(DESCRIPTION, text);
                    v.put(DATE, date);
                    v.put(TIME, time);
                    v.put(EXPENSE_SPLIT_BILL_ID, billId);
                    newId = db.insert(EXPENSES, null, v);
                }

                if (newId == -1) {
                    return false;
                }

                ContentValues settlementValues = new ContentValues();
                settlementValues.put(SETTLEMENT_DONE, 1);
                settlementValues.put(SETTLEMENT_ENTRY_ID, newId);

                db.update(
                        SETTLEMENTS,
                        settlementValues,
                        SETTLEMENT_ID + "=? AND " + SETTLEMENT_USER_EMAIL + "=?",
                        new String[]{String.valueOf(settlementId), userEmail}
                );

                updateSplitMemberPaidAmount(
                        db, billId, member, amount, true
                );

            } else {
                // Reverse the exact linked record created when this was marked Paid.
                if (entryId > 0) {
                    if (isReceive) {
                        db.delete(
                                BALANCE_CREDITS,
                                BC_ID + "=? AND " + USER_EMAIL + "=?",
                                new String[]{String.valueOf(entryId), userEmail}
                        );
                    } else {
                        db.delete(
                                EXPENSES,
                                EXPENSE_ID + "=? AND " + USER_EMAIL + "=?",
                                new String[]{String.valueOf(entryId), userEmail}
                        );
                    }
                } else {
                    // Compatibility with older paid settlements that predate
                    // the entry-id link.
                    if (isReceive) {
                        Cursor oldCredit = db.query(
                                BALANCE_CREDITS,
                                new String[]{BC_ID},
                                USER_EMAIL + "=? AND " +
                                        BC_AMOUNT + "=? AND " +
                                        BC_DESCRIPTION + "=? AND " +
                                        BC_DATE + "=? AND " +
                                        BC_TIME + "=?",
                                new String[]{
                                        userEmail,
                                        String.valueOf(amount),
                                        text,
                                        date,
                                        time
                                },
                                null, null, BC_ID + " DESC", "1"
                        );
                        if (oldCredit.moveToFirst()) {
                            db.delete(
                                    BALANCE_CREDITS,
                                    BC_ID + "=? AND " + USER_EMAIL + "=?",
                                    new String[]{
                                            String.valueOf(oldCredit.getLong(0)),
                                            userEmail
                                    }
                            );
                        }
                        oldCredit.close();
                    } else {
                        Cursor oldExpense = db.query(
                                EXPENSES,
                                new String[]{EXPENSE_ID},
                                USER_EMAIL + "=? AND " +
                                        AMOUNT + "=? AND " +
                                        DESCRIPTION + "=? AND " +
                                        DATE + "=? AND " +
                                        TIME + "=?",
                                new String[]{
                                        userEmail,
                                        String.valueOf(amount),
                                        text,
                                        date,
                                        time
                                },
                                null, null, EXPENSE_ID + " DESC", "1"
                        );
                        if (oldExpense.moveToFirst()) {
                            db.delete(
                                    EXPENSES,
                                    EXPENSE_ID + "=? AND " + USER_EMAIL + "=?",
                                    new String[]{
                                            String.valueOf(oldExpense.getLong(0)),
                                            userEmail
                                    }
                            );
                        }
                        oldExpense.close();
                    }
                }

                ContentValues settlementValues = new ContentValues();
                settlementValues.put(SETTLEMENT_DONE, 0);
                settlementValues.put(SETTLEMENT_ENTRY_ID, 0);

                db.update(
                        SETTLEMENTS,
                        settlementValues,
                        SETTLEMENT_ID + "=? AND " + SETTLEMENT_USER_EMAIL + "=?",
                        new String[]{String.valueOf(settlementId), userEmail}
                );

                updateSplitMemberPaidAmount(
                        db, billId, member, amount, false
                );
            }

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    private void updateSplitMemberPaidAmount(
            SQLiteDatabase db,
            long billId,
            String memberName,
            double amount,
            boolean paid) {

        ContentValues values = new ContentValues();
        values.put(MEMBER_PAID_AMOUNT, paid ? amount : 0);

        db.update(
                SPLIT_MEMBERS,
                values,
                MEMBER_BILL_ID + "=? AND " + MEMBER_NAME + "=?",
                new String[]{String.valueOf(billId), memberName}
        );
    }

    /**
     * Deletes one complete split-bill history record and reverses all
     * balance/expense entries created by that bill.
     */
    public boolean deleteSplitBill(
            long billId,
            String userEmail) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            Cursor bill = db.query(
                    SPLIT_BILLS,
                    new String[]{BILL_DESCRIPTION, BILL_DATE, BILL_TIME},
                    BILL_ID + "=? AND " + USER_EMAIL + "=?",
                    new String[]{String.valueOf(billId), userEmail},
                    null, null, null);

            if (!bill.moveToFirst()) {
                bill.close();
                return false;
            }

            String description = bill.getString(0);
            String billDate = bill.getString(1);
            String billTime = bill.getString(2);
            bill.close();

            // 1) Remove all settlement-created entries using their exact IDs.
            Cursor settlements = db.query(
                    SETTLEMENTS,
                    new String[]{
                            SETTLEMENT_TYPE,
                            SETTLEMENT_ENTRY_ID,
                            SETTLEMENT_MEMBER_NAME,
                            SETTLEMENT_AMOUNT
                    },
                    SETTLEMENT_BILL_ID + "=? AND " + SETTLEMENT_USER_EMAIL + "=?",
                    new String[]{String.valueOf(billId), userEmail},
                    null, null, null);

            while (settlements.moveToNext()) {
                String type = settlements.getString(0);
                long entryId = settlements.getLong(1);

                if (entryId > 0) {
                    if ("RECEIVE".equals(type)) {
                        db.delete(
                                BALANCE_CREDITS,
                                BC_ID + "=? AND " + USER_EMAIL + "=?",
                                new String[]{String.valueOf(entryId), userEmail}
                        );
                    } else {
                        db.delete(
                                EXPENSES,
                                EXPENSE_ID + "=? AND " + USER_EMAIL + "=?",
                                new String[]{String.valueOf(entryId), userEmail}
                        );
                    }
                } else {
                    // Compatibility with older records created before the
                    // entry-id link was introduced.
                    String member = settlements.getString(2);
                    double amount = settlements.getDouble(3);
                    String text = ("RECEIVE".equals(type)
                            ? "Received from " : "Paid to ")
                            + member + ": " + description;

                    if ("RECEIVE".equals(type)) {
                        Cursor oldCredit = db.query(
                                BALANCE_CREDITS,
                                new String[]{BC_ID},
                                USER_EMAIL + "=? AND " +
                                        BC_AMOUNT + "=? AND " +
                                        BC_DESCRIPTION + "=? AND " +
                                        BC_DATE + "=? AND " +
                                        BC_TIME + "=?",
                                new String[]{
                                        userEmail,
                                        String.valueOf(amount),
                                        text,
                                        billDate,
                                        billTime
                                },
                                null, null, BC_ID + " DESC", "1"
                        );
                        if (oldCredit.moveToFirst()) {
                            db.delete(
                                    BALANCE_CREDITS,
                                    BC_ID + "=? AND " + USER_EMAIL + "=?",
                                    new String[]{
                                            String.valueOf(oldCredit.getLong(0)),
                                            userEmail
                                    }
                            );
                        }
                        oldCredit.close();
                    } else {
                        Cursor oldExpense = db.query(
                                EXPENSES,
                                new String[]{EXPENSE_ID},
                                USER_EMAIL + "=? AND " +
                                        AMOUNT + "=? AND " +
                                        DESCRIPTION + "=? AND " +
                                        DATE + "=? AND " +
                                        TIME + "=?",
                                new String[]{
                                        userEmail,
                                        String.valueOf(amount),
                                        text,
                                        billDate,
                                        billTime
                                },
                                null, null, EXPENSE_ID + " DESC", "1"
                        );
                        if (oldExpense.moveToFirst()) {
                            db.delete(
                                    EXPENSES,
                                    EXPENSE_ID + "=? AND " + USER_EMAIL + "=?",
                                    new String[]{
                                            String.valueOf(oldExpense.getLong(0)),
                                            userEmail
                                    }
                            );
                        }
                        oldExpense.close();
                    }
                }
            }
            settlements.close();

            // 2) Remove the original expense made when the logged-in user
            // paid the whole bill. New records have an exact bill link.
            int linkedOriginalExpenses = db.delete(
                    EXPENSES,
                    USER_EMAIL + "=? AND " + EXPENSE_SPLIT_BILL_ID + "=?",
                    new String[]{userEmail, String.valueOf(billId)}
            );

            // Compatibility with older bills that do not have split_bill_id.
            if (linkedOriginalExpenses == 0) {
                String originalText = "Split: " +
                        (description == null ? "" : description) +
                        " (paid by you)";

                Cursor oldOriginal = db.query(
                        EXPENSES,
                        new String[]{EXPENSE_ID},
                        USER_EMAIL + "=? AND " +
                                CATEGORY + "=? AND " +
                                DESCRIPTION + "=? AND " +
                                DATE + "=? AND " +
                                TIME + "=?",
                        new String[]{
                                userEmail,
                                "Split Bill",
                                originalText,
                                billDate,
                                billTime
                        },
                        null, null, EXPENSE_ID + " DESC", "1"
                );

                if (oldOriginal.moveToFirst()) {
                    db.delete(
                            EXPENSES,
                            EXPENSE_ID + "=? AND " + USER_EMAIL + "=?",
                            new String[]{
                                    String.valueOf(oldOriginal.getLong(0)),
                                    userEmail
                            }
                    );
                }
                oldOriginal.close();
            }

            // 3) Remove all child records, then the bill itself.
            db.delete(
                    SETTLEMENTS,
                    SETTLEMENT_BILL_ID + "=? AND " + SETTLEMENT_USER_EMAIL + "=?",
                    new String[]{String.valueOf(billId), userEmail}
            );

            db.delete(
                    SPLIT_MEMBERS,
                    MEMBER_BILL_ID + "=?",
                    new String[]{String.valueOf(billId)}
            );

            int rows = db.delete(
                    SPLIT_BILLS,
                    BILL_ID + "=? AND " + USER_EMAIL + "=?",
                    new String[]{String.valueOf(billId), userEmail}
            );

            db.setTransactionSuccessful();
            return rows > 0;

        } finally {
            db.endTransaction();
        }
    }

    // =========================================================
    // TO-DO METHODS
    // =========================================================

    public long addTodo(
            String userEmail,
            String title,
            String dueDate) {

        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, userEmail);
        values.put(TODO_TITLE, title);
        values.put(TODO_DUE_DATE, dueDate);
        values.put(TODO_DONE, 0);

        return getWritableDatabase().insert(TODOS, null, values);
    }

    public Cursor getTodos(String userEmail) {
        return getReadableDatabase().query(
                TODOS,
                null,
                USER_EMAIL + "=?",
                new String[]{userEmail},
                null,
                null,
                TODO_DONE + " ASC, " + TODO_ID + " DESC"
        );
    }

    public boolean setTodoDone(long todoId, boolean done) {
        ContentValues values = new ContentValues();
        values.put(TODO_DONE, done ? 1 : 0);

        return getWritableDatabase().update(
                TODOS,
                values,
                TODO_ID + "=?",
                new String[]{String.valueOf(todoId)}
        ) > 0;
    }

    public boolean deleteTodo(long todoId) {
        return getWritableDatabase().delete(
                TODOS,
                TODO_ID + "=?",
                new String[]{String.valueOf(todoId)}
        ) > 0;
    }

    // =========================================================
    // FORMAT AMOUNT
    // =========================================================

    // =========================================================
    // PROFILE METHODS
    // =========================================================

    /** Returns {name, email, college, course, photoPath} or null. */
    public String[] getUserProfile(String email) {
        Cursor c = getReadableDatabase().query(
                USERS,
                new String[]{NAME, EMAIL, COLLEGE, COURSE, PROFILE_PHOTO},
                EMAIL + "=?",
                new String[]{email},
                null, null, null);
        String[] r = null;
        if (c.moveToFirst()) {
            r = new String[]{
                    c.getString(0) == null ? "" : c.getString(0),
                    c.getString(1) == null ? "" : c.getString(1),
                    c.getString(2) == null ? "" : c.getString(2),
                    c.getString(3) == null ? "" : c.getString(3),
                    c.getString(4)
            };
        }
        c.close();
        return r;
    }

    public boolean updateUserProfile(String oldEmail, String name,
                                     String newEmail, String college,
                                     String course) {
        SQLiteDatabase db = getWritableDatabase();
        boolean emailChanged = !oldEmail.equalsIgnoreCase(newEmail);

        if (emailChanged && userExists(newEmail)) return false;

        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put(NAME, name);
            v.put(EMAIL, newEmail);
            v.put(COLLEGE, college);
            v.put(COURSE, course);
            int rows = db.update(USERS, v, EMAIL + "=?",
                    new String[]{oldEmail});
            if (rows == 0) return false;

            if (emailChanged) {
                String[] tables = {EXPENSES, SPLIT_BILLS, REMINDERS,
                        SAVINGS_GOALS, MONTHLY_BUDGETS, BALANCE_CREDITS,
                        BUDGET_ADDITIONS, TODOS, SETTLEMENTS};
                for (String t : tables) {
                    try {
                        ContentValues e = new ContentValues();
                        e.put(USER_EMAIL, newEmail);
                        db.update(t, e, USER_EMAIL + "=?",
                                new String[]{oldEmail});
                    } catch (Exception ignored) {
                        // table without user_email column
                    }
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public boolean setProfilePhoto(String email, String path) {
        ContentValues v = new ContentValues();
        v.put(PROFILE_PHOTO, path);
        return getWritableDatabase().update(USERS, v, EMAIL + "=?",
                new String[]{email}) > 0;
    }

    public boolean changePassword(String email, String oldPass,
                                  String newPass) {
        if (!checkLogin(email, oldPass)) return false;
        ContentValues v = new ContentValues();
        v.put(PASSWORD, newPass);
        return getWritableDatabase().update(USERS, v, EMAIL + "=?",
                new String[]{email}) > 0;
    }

    // =========================================================
    // SPLIT STATUS (pending balances)
    // =========================================================

    /** Returns {pendingToReceive, pendingToPay, pendingCount}. */
    public double[] getPendingSplitSummary(String userEmail) {
        double receive = 0, pay = 0, count = 0;
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + SETTLEMENT_TYPE + ", SUM(" + SETTLEMENT_AMOUNT
                        + "), COUNT(*) FROM " + SETTLEMENTS
                        + " WHERE " + SETTLEMENT_USER_EMAIL + "=? AND "
                        + SETTLEMENT_DONE + "=0 GROUP BY " + SETTLEMENT_TYPE,
                new String[]{userEmail});
        while (c.moveToNext()) {
            if ("RECEIVE".equals(c.getString(0))) receive = c.getDouble(1);
            else if ("PAY".equals(c.getString(0))) pay = c.getDouble(1);
            count += c.getInt(2);
        }
        c.close();
        return new double[]{receive, pay, count};
    }

    /** Pending settlements: {member, amount, type} rows. */
    public Cursor getPendingSettlements(String userEmail) {
        return getReadableDatabase().query(
                SETTLEMENTS,
                new String[]{SETTLEMENT_MEMBER_NAME, SETTLEMENT_AMOUNT,
                        SETTLEMENT_TYPE},
                SETTLEMENT_USER_EMAIL + "=? AND " + SETTLEMENT_DONE + "=0",
                new String[]{userEmail},
                null, null, SETTLEMENT_ID + " DESC");
    }

    public String formatAmount(
            double amount) {

        if (amount == (long) amount) {

            return String.valueOf(
                    (long) amount
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}
