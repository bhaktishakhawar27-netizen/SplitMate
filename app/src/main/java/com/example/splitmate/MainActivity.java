package com.example.splitmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {

            String savedEmail = SessionManager.getEmail(MainActivity.this);

            Intent intent;


            if (savedEmail != null &&
                    new DatabaseHelper(MainActivity.this).userExists(savedEmail)) {

                intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("email", savedEmail);

            } else {

                SessionManager.logout(MainActivity.this);

                intent = new Intent(MainActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

        }, SPLASH_TIME);
    }
}
