package com.example.splitmate;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class TodoActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout todoContainer;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        databaseHelper = new DatabaseHelper(this);
        userEmail = SessionManager.resolveEmail(this);

        todoContainer = findViewById(R.id.todoContainer);
        MenuHelper.attach(this, findViewById(R.id.menuButton));

        Button addButton = findViewById(R.id.addTodoButton);
        addButton.setOnClickListener(v -> showAddTodoDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (databaseHelper != null && userEmail != null) {
            loadTodos();
        }
    }

    private void showAddTodoDialog() {
        EditText titleInput = new EditText(this);
        titleInput.setHint("e.g. Pay hostel fee");
        titleInput.setSingleLine(true);

        EditText dateInput = new EditText(this);
        dateInput.setHint("Due date (optional)");
        dateInput.setSingleLine(true);

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 0);
        form.addView(titleInput);
        form.addView(dateInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New To-Do")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = titleInput.getText().toString().trim();
                String dueDate = dateInput.getText().toString().trim();

                if (title.isEmpty()) {
                    titleInput.setError("Enter a task");
                    return;
                }

                long id = databaseHelper.addTodo(userEmail, title, dueDate);
                if (id != -1) {
                    Toast.makeText(this, "Task added ✓", Toast.LENGTH_SHORT).show();
                    loadTodos();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void loadTodos() {
        todoContainer.removeAllViews();

        Cursor cursor = databaseHelper.getTodos(userEmail);
        int total = 0;
        int completed = 0;

        while (cursor.moveToNext()) {
            total++;
            if (cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1) {
                completed++;
            }

            long id = cursor.getLong(cursor.getColumnIndexOrThrow("todo_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            boolean done = cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1;

            addTodoRow(id, title, dueDate, done);
        }
        cursor.close();

        TextView count = findViewById(R.id.todoCount);
        count.setText(completed + " completed  •  " + (total - completed) + " remaining");

        if (total == 0) {
            TextView empty = new TextView(this);
            empty.setText("No tasks yet. Add your first task ✦");
            empty.setTextSize(15);
            empty.setTextColor(0xFF6B7275);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 45, 0, 45);
            todoContainer.addView(empty);
        }
    }

    private void addTodoRow(
            long id,
            String title,
            String dueDate,
            boolean done) {

        CardView card = new CardView(this);
        card.setRadius(16);
        card.setCardElevation(2);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 10);
        card.setLayoutParams(cardParams);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(12, 10, 12, 10);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(done);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            databaseHelper.setTodoDone(id, isChecked);
            loadTodos();
        });

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTextColor(0xFF202124);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        if (done) {
            titleView.setPaintFlags(
                    titleView.getPaintFlags() |
                            android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            );
        }

        textBox.addView(titleView);

        if (dueDate != null && !dueDate.trim().isEmpty()) {
            TextView due = new TextView(this);
            due.setText("Due: " + dueDate);
            due.setTextSize(12);
            due.setTextColor(0xFF7A8288);
            due.setPadding(0, 3, 0, 0);
            textBox.addView(due);
        }

        Button delete = new Button(this);
        delete.setText("×");
        delete.setMinWidth(45);
        delete.setOnClickListener(v -> {
            databaseHelper.deleteTodo(id);
            loadTodos();
        });

        row.addView(checkBox);
        row.addView(textBox);
        row.addView(delete);

        card.addView(row);
        todoContainer.addView(card);
    }
}
