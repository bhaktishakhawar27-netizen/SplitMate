package com.example.splitmate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Keeps the user logged in until they press Logout.
 */
public class















































SessionManager {

    private static final String PREFS = "splitmate_session";
    private static final String KEY_EMAIL = "logged_in_email";

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void saveLogin(Context context, String email) {
        prefs(context).edit().putString(KEY_EMAIL, email).apply();
    }

    public static String getEmail(Context context) {
        return prefs(context).getString(KEY_EMAIL, null);
    }

    public static boolean isLoggedIn(Context context) {
        String email = getEmail(context);
        return email != null && !email.trim().isEmpty();
    }

    public static void logout(Context context) {
        prefs(context).edit().remove(KEY_EMAIL).apply();
    }

    /**
     * Email passed through the Intent if present, otherwise the saved
     * session. This removes the "User information not found" problem
     * when a screen is opened without the email extra (notification tap,
     * app restored after being killed, etc.).
     */
    public static String resolveEmail(Activity activity) {

        String email = activity.getIntent().getStringExtra("email");

        if (email == null || email.trim().isEmpty()) {
            email = getEmail(activity);
        }

        return email;
    }
}
