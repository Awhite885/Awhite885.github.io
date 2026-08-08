package com.zybooks.evidencetracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CurrentUsersActivity extends AppCompatActivity
        implements CurrentUserAdapter.OnEditClickListener,
        CurrentUserAdapter.OnDisableClickListener {

    // UI
    private RecyclerView recyclerView;

    // Adapter
    private CurrentUserAdapter adapter;

    // Data
    private List<UserAccount> userList;
    private String currentUsername;

    // Database
    private DatabaseHelper dbHelper;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_users);
        currentUsername = getIntent().getStringExtra("username");

        recyclerView = findViewById(R.id.recyclerUsers);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);

        userList = new ArrayList<>();

        adapter = new CurrentUserAdapter(
                userList,
                currentUsername,
                this,
                this

        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {

        userList.clear();

        userList.addAll(dbHelper.getAllUsers());

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditClick(int position) {

        UserAccount user = userList.get(position);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 0);

        EditText editFirstName = new EditText(this);
        editFirstName.setHint("First Name");
        editFirstName.setText(user.getFirstName());

        EditText editLastName = new EditText(this);
        editLastName.setHint("Last Name");
        editLastName.setText(user.getLastName());

        EditText editEmail = new EditText(this);
        editEmail.setHint("Email");
        editEmail.setText(user.getEmail());

        Spinner spinnerRole = new Spinner(this);

        String[] roles = {
                DatabaseHelper.ROLE_CSI,
                DatabaseHelper.ROLE_DETECTIVE,
                DatabaseHelper.ROLE_ADMINISTRATOR
        };

        ArrayAdapter<String> roleAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        roles
                );

        roleAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerRole.setAdapter(roleAdapter);

        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(user.getRole())) {
                spinnerRole.setSelection(i);
                break;
            }
        }

        layout.addView(editFirstName);
        layout.addView(editLastName);
        layout.addView(editEmail);
        layout.addView(spinnerRole);

        new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {

                    String firstName =
                            editFirstName.getText().toString().trim();

                    String lastName =
                            editLastName.getText().toString().trim();

                    String email =
                            editEmail.getText().toString().trim();

                    String role =
                            spinnerRole.getSelectedItem().toString();

                    if (firstName.isEmpty() ||
                            lastName.isEmpty() ||
                            email.isEmpty()) {

                        Toast.makeText(
                                this,
                                "All fields are required.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    boolean success = dbHelper.updateUser(
                            user.getId(),
                            firstName,
                            lastName,
                            email,
                            role
                    );

                    if (success) {

                        Toast.makeText(
                                this,
                                "User account updated.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadUsers();

                    } else {

                        Toast.makeText(
                                this,
                                "Unable to update user.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDisableClick(int position) {

        UserAccount user = userList.get(position);
        if (user.getUsername().equals(currentUsername)) {

            Toast.makeText(
                    this,
                    "You cannot disable your own account.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Disable User")
                .setMessage(
                        "Are you sure you want to disable " +
                                user.getFirstName() + " " +
                                user.getLastName() + "?")
                .setPositiveButton("Disable", (dialog, which) -> {

                    boolean success = dbHelper.disableUser(user.getId());

                    if (success) {

                        Toast.makeText(
                                this,
                                "User account disabled.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadUsers();

                    } else {

                        Toast.makeText(
                                this,
                                "Unable to disable user.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
}