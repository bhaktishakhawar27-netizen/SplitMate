package com.example.splitmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private String userEmail;
    private String photoPath;

    private ImageView photoView;
    private TextView initialView, nameHeader, emailHeader;
    private EditText nameInput, emailInput, collegeInput, courseInput;
    private Button editButton, saveButton, cancelButton;
    private LinearLayout editActions;

    private boolean editing = false;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    this::onImagePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        userEmail = SessionManager.resolveEmail(this);

        if (userEmail == null || userEmail.trim().isEmpty()) {
            finish();
            return;
        }

        photoView = findViewById(R.id.profilePhoto);
        initialView = findViewById(R.id.profileInitial);
        nameHeader = findViewById(R.id.profileNameHeader);
        emailHeader = findViewById(R.id.profileEmailHeader);
        nameInput = findViewById(R.id.inputName);
        emailInput = findViewById(R.id.inputEmail);
        collegeInput = findViewById(R.id.inputCollege);
        courseInput = findViewById(R.id.inputCourse);
        editButton = findViewById(R.id.editProfileButton);
        saveButton = findViewById(R.id.saveProfileButton);
        cancelButton = findViewById(R.id.cancelProfileButton);
        editActions = findViewById(R.id.editActions);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        MenuHelper.attach(this, findViewById(R.id.menuButton));

        findViewById(R.id.changePhotoButton)
                .setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.removePhotoButton)
                .setOnClickListener(v -> removePhoto());
        findViewById(R.id.changePasswordButton)
                .setOnClickListener(v -> showChangePasswordDialog());

        editButton.setOnClickListener(v -> setEditing(true));
        cancelButton.setOnClickListener(v -> {
            loadProfile();
            setEditing(false);
        });
        saveButton.setOnClickListener(v -> saveProfile());

        loadProfile();
        setEditing(false);
    }

    private void loadProfile() {
        String[] p = db.getUserProfile(userEmail);
        if (p == null) {
            Toast.makeText(this, "User information not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        nameInput.setText(p[0]);
        emailInput.setText(p[1]);
        collegeInput.setText(p[2]);
        courseInput.setText(p[3]);
        photoPath = p[4];

        String display = p[0].isEmpty() ? "User" : p[0];
        nameHeader.setText(display);
        emailHeader.setText(p[1]);
        initialView.setText(display.substring(0, 1).toUpperCase());
        ProfileImageHelper.show(photoPath, photoView, initialView);
    }

    private void setEditing(boolean on) {
        editing = on;
        EditText[] fields = {nameInput, emailInput, collegeInput, courseInput};
        for (EditText f : fields) {
            f.setEnabled(on);
            f.setAlpha(on ? 1f : 0.85f);
        }
        editButton.setVisibility(on ? View.GONE : View.VISIBLE);
        editActions.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String college = collegeInput.getText().toString().trim();
        String course = courseInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Enter your name");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            return;
        }

        boolean ok = db.updateUserProfile(userEmail, name, email, college, course);
        if (!ok) {
            Toast.makeText(this,
                    "Could not save. Email may already be in use.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!email.equals(userEmail)) {
            userEmail = email;
            SessionManager.saveLogin(this, email);
            getIntent().putExtra("email", email);
        }

        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        loadProfile();
        setEditing(false);
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) return;
        String path = ProfileImageHelper.saveFromUri(this, uri, userEmail);
        if (path == null) {
            Toast.makeText(this, "Could not load that image",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String old = photoPath;
        if (db.setProfilePhoto(userEmail, path)) {
            ProfileImageHelper.delete(old);
            photoPath = path;
            ProfileImageHelper.show(photoPath, photoView, initialView);
            Toast.makeText(this, "Photo updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void removePhoto() {
        if (photoPath == null) {
            Toast.makeText(this, "No photo to remove",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ProfileImageHelper.delete(photoPath);
        db.setProfilePhoto(userEmail, null);
        photoPath = null;
        ProfileImageHelper.show(null, photoView, initialView);
    }

    private void showChangePasswordDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        EditText oldPass = new EditText(this);
        oldPass.setHint("Current password");
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText newPass = new EditText(this);
        newPass.setHint("New password (min 4 characters)");
        newPass.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(oldPass);
        box.addView(newPass);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(box)
                .setPositiveButton("Update", (d, w) -> {
                    String o = oldPass.getText().toString();
                    String n = newPass.getText().toString();
                    if (n.length() < 4) {
                        Toast.makeText(this, "New password is too short",
                                Toast.LENGTH_SHORT).show();
                    } else if (db.changePassword(userEmail, o, n)) {
                        Toast.makeText(this, "Password updated",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Current password is incorrect",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
