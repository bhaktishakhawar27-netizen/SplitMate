package com.example.splitmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText nameEditText, emailEditText, collegeEditText,
            courseEditText, budgetEditText,
            passwordEditText, confirmPasswordEditText;

    Button registerButton;
    TextView loginText;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        collegeEditText = findViewById(R.id.collegeEditText);
        courseEditText = findViewById(R.id.courseEditText);
        budgetEditText = findViewById(R.id.budgetEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText =
                findViewById(R.id.confirmPasswordEditText);

        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Register button
        registerButton.setOnClickListener(v -> {

            String name = nameEditText.getText().toString().trim();
            String email = emailEditText
                    .getText()
                    .toString()
                    .trim()
                    .toLowerCase();
            String college = collegeEditText.getText().toString().trim();
            String course = courseEditText.getText().toString().trim();
            String budget = budgetEditText.getText().toString().trim();
            String password =
                    passwordEditText.getText().toString().trim();
            String confirmPassword =
                    confirmPasswordEditText.getText().toString().trim();

            // Validation

            if (name.isEmpty()) {
                nameEditText.setError("Enter your name");
                return;
            }

            if (email.isEmpty()) {
                emailEditText.setError("Enter your email");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()) {

                emailEditText.setError("Enter a valid email");
                return;
            }

            if (college.isEmpty()) {
                collegeEditText.setError("Enter your college");
                return;
            }

            if (course.isEmpty()) {
                courseEditText.setError("Enter your course");
                return;
            }

            if (budget.isEmpty()) {
                budgetEditText.setError(
                        "Enter your monthly budget"
                );
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError(
                        "Create a password"
                );
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError(
                        "Password must contain at least 6 characters"
                );
                return;
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.setError(
                        "Confirm your password"
                );
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError(
                        "Passwords do not match"
                );
                return;
            }

            // Convert budget to number
            double budgetAmount;

            try {
                budgetAmount = Double.parseDouble(budget);
            } catch (NumberFormatException e) {

                budgetEditText.setError(
                        "Enter a valid amount"
                );
                return;
            }

            // Save user into SQLite database
            boolean success = databaseHelper.registerUser(
                    name,
                    email,
                    college,
                    course,
                    budgetAmount,
                    password
            );

            if (success) {

                Toast.makeText(
                        RegisterActivity.this,
                        "Account created successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                // Go to Login
                Intent intent = new Intent(
                        RegisterActivity.this,
                        LoginActivity.class
                );

                startActivity(intent);
                finish();

            } else {

                Toast.makeText(
                        RegisterActivity.this,
                        "Email already registered!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Go back to Login
        loginText.setOnClickListener(v -> {

            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}
