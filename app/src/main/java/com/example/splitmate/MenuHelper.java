package com.example.splitmate;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public final class MenuHelper {

    private MenuHelper() {}

    public static void attach(Activity activity, View existingButton) {
        View button = existingButton;

        if (button == null) {
            button = createFloatingButton(activity);
        }

        final View menuButton = button;


        if (menuButton instanceof TextView) {
            TextView textButton = (TextView) menuButton;
            textButton.setText("☰");
            textButton.setTextSize(27);
            textButton.setGravity(Gravity.CENTER);
            textButton.setContentDescription("Main menu");
        }

        menuButton.setOnClickListener(v -> showMenu(activity, menuButton));
    }

    private static View createFloatingButton(Activity activity) {
        TextView button = new TextView(activity);
        button.setText("☰");
        button.setTextSize(27);
        button.setTextColor(0xFF202124);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setElevation(8);

        android.widget.FrameLayout root =
                activity.findViewById(android.R.id.content);

        // Put the menu button in its own bar at the TOP-LEFT and push the
        // existing screen content below it, so nothing gets covered.
        View content = root.getChildAt(0);

        if (content == null) {
            return button;
        }

        root.removeView(content);

        android.widget.LinearLayout wrapper =
                new android.widget.LinearLayout(activity);
        wrapper.setOrientation(android.widget.LinearLayout.VERTICAL);
        wrapper.setBackgroundColor(0xFFF5F7F7);

        android.widget.LinearLayout bar =
                new android.widget.LinearLayout(activity);
        bar.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        bar.setPadding(dp(activity, 8), dp(activity, 8), 0, 0);

        bar.addView(button, new android.widget.LinearLayout.LayoutParams(
                dp(activity, 48),
                dp(activity, 48)
        ));

        wrapper.addView(bar, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        wrapper.addView(content, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        root.addView(wrapper, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ));

        return button;
    }

    private static void showMenu(Activity activity, View anchor) {
        PopupMenu popup = new PopupMenu(activity, anchor);

        popup.getMenu().add("Home");
        popup.getMenu().add("Account");
        popup.getMenu().add("Add Expense");
        popup.getMenu().add("Split Bill");
        popup.getMenu().add("Monthly Data");
        popup.getMenu().add("All Expenses");
        popup.getMenu().add("Split Bill History");
        popup.getMenu().add("Payment History");
        popup.getMenu().add("To-Do List");
        popup.getMenu().add("Payment Reminder");
        popup.getMenu().add("Savings Goal");
        popup.getMenu().add("Logout");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.equals("Logout")) {
                SessionManager.logout(activity);
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );
                activity.startActivity(intent);
                return true;
            }

            Class<?> target = null;

            if (title.equals("Home")) target = HomeActivity.class;
            else if (title.equals("Account")) target = ProfileActivity.class;
            else if (title.equals("Add Expense")) target = AddExpenseActivity.class;
            else if (title.equals("Split Bill")) target = SplitBillActivity.class;
            else if (title.equals("Monthly Data")) target = MonthlyDataActivity.class;
            else if (title.equals("All Expenses")) target = AllExpensesActivity.class;
            else if (title.equals("Split Bill History")) target = SplitBillHistoryActivity.class;
            else if (title.equals("Payment History")) target = PaymentHistoryActivity.class;
            else if (title.equals("To-Do List")) target = TodoActivity.class;
            else if (title.equals("Payment Reminder")) target = PaymentReminderActivity.class;
            else if (title.equals("Savings Goal")) target = SavingsGoalActivity.class;

            if (target != null && !activity.getClass().equals(target)) {
                String email = SessionManager.resolveEmail(activity);
                Intent intent = new Intent(activity, target);
                if (email != null) intent.putExtra("email", email);
                activity.startActivity(intent);
            }
            return true;
        });

        popup.show();
    }

    private static int dp(Activity activity, int value) {
        return Math.round(
                value * activity.getResources().getDisplayMetrics().density
        );
    }
}
