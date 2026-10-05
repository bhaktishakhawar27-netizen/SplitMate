package com.example.splitmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;

    Button loginButton;
    TextView registerText;

    DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SessionManager.isLoggedIn(this)) {

            Intent home = new Intent(this, HomeActivity.class);
            home.putExtra("email", SessionManager.getEmail(this));
            startActivity(home);
            finish();
            return;
        }

        // Connect XML
        emailEditText =
                findViewById(R.id.emailEditText);

        passwordEditText =
                findViewById(R.id.passwordEditText);

        loginButton =
                findViewById(R.id.loginButton);

        registerText =
                findViewById(R.id.registerText);


        // Database
        databaseHelper =
                new DatabaseHelper(this);


        // LOGIN BUTTON

        loginButton.setOnClickListener(v -> {

            String email =
                    emailEditText.getText()
                            .toString()
                            .trim()
                            .toLowerCase();

            String password =
                    passwordEditText.getText()
                            .toString();


            // Validation

            if (email.isEmpty()) {

                emailEditText.setError(
                        "Enter your email"
                );

                return;
            }

            if (password.isEmpty()) {

                passwordEditText.setError(
                        "Enter your password"
                );

                return;
            }


            // Check database

            boolean validUser =
                    databaseHelper.checkLogin(
                            email,
                            password
                    );


            if (validUser) {

                SessionManager.saveLogin(LoginActivity.this, email);

                Toast.makeText(
                        LoginActivity.this,
                        "Login successful!",
                        Toast.LENGTH_SHORT
                ).show();


                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                HomeActivity.class
                        );

                intent.putExtra(
                        "email",
                        email
                );

                startActivity(intent);

                finish();

            } else {

                Toast.makeText(
                        LoginActivity.this,
                        "Invalid email or password!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });


        // REGISTER LINK

        registerText.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            RegisterActivity.class
                    );

            startActivity(intent);
        });
    }
}
