package com.zybooks.evidencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
 * File: MainActivity.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Displays the login screen. Allows the user to log in or create a new account.
 * Allows the user to navigate to the evidence grid screen.
 */

public class MainActivity extends AppCompatActivity {

    // UI elements
    private DatabaseHelper dbHelper;
    private EditText editUsername;
    private EditText editPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;

    @Override
    // Set up the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge layout for full screen display
        EdgeToEdge.enable(this);

        // Set the layout for the activity
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Adjust layout padding to account for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLogin), (v, insets) -> {
            // Navigate to EvidenceGridActivity when the login button is clicked
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect form fields
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Navigate to EvidenceGridActivity when the login button is clicked
        buttonLogin.setOnClickListener(v -> loginUser());
        buttonCreateAccount.setOnClickListener(v -> showCreateAccountDialog());
    }

    // Check if the user is logged in
    private void loginUser() {

        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Please enter both username and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AuthenticationResult result =
                dbHelper.authenticateUser(username, password);

        if (!result.areCredentialsValid()) {
            Toast.makeText(this,
                    "Invalid username or password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        switch (result.getAccountStatus()) {

            case DatabaseHelper.STATUS_PENDING:
                Toast.makeText(this,
                        "Your account is awaiting administrator approval.",
                        Toast.LENGTH_LONG).show();
                break;

            case DatabaseHelper.STATUS_REJECTED:
                Toast.makeText(this,
                        "Your account request has been rejected.",
                        Toast.LENGTH_LONG).show();
                break;

            case DatabaseHelper.STATUS_APPROVED:

                Toast.makeText(this,
                        "Login successful: " + result.getRole(),
                        Toast.LENGTH_SHORT).show();
                String fullName =
                        dbHelper.getFullName(username);

                getSharedPreferences(
                        "evidence_prefs",
                        MODE_PRIVATE
                ).edit()
                        .putString(
                                "logged_in_user",
                                fullName
                        )
                        .putString(
                                "logged_in_username",
                                username
                        )
                        .apply();

                Intent intent = new Intent(
                        MainActivity.this,
                        EvidenceGridActivity.class);

                intent.putExtra(
                        "USER_ROLE",
                        result.getRole());

                intent.putExtra(
                        "username",
                        username);

                startActivity(intent);
                break;
        }
    }

    // Show the create account dialog
    private void showCreateAccountDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_create_account, null);

        EditText editNewUsername = dialogView.findViewById(R.id.editNewUsername);
        EditText editNewPassword = dialogView.findViewById(R.id.editNewPassword);
        EditText editFirstName = dialogView.findViewById(R.id.editFirstName);
        EditText editLastName = dialogView.findViewById(R.id.editLastName);
        EditText editEmail = dialogView.findViewById(R.id.editEmail);

        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);

        String[] roles = {
                DatabaseHelper.ROLE_CSI,
                DatabaseHelper.ROLE_DETECTIVE,
                DatabaseHelper.ROLE_ADMINISTRATOR
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);


        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Account");
        builder.setView(dialogView);

        // Handle positive and negative button clicks
        builder.setPositiveButton("Create", (dialog, which) -> {
            String firstName =
                    editFirstName.getText().toString().trim();

            String lastName =
                    editLastName.getText().toString().trim();

            String email =
                    editEmail.getText().toString().trim();

            String newUsername =
                    editNewUsername.getText().toString().trim();

            String newPassword =
                    editNewPassword.getText().toString().trim();

            String selectedRole =
                    spinnerRole.getSelectedItem().toString();

            // Check for empty fields
            if (firstName.isEmpty() ||
                    lastName.isEmpty() ||
                    email.isEmpty() ||
                    newUsername.isEmpty() ||
                    newPassword.isEmpty()) {
                Toast.makeText(this, "Please complete all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if the username already exists
            if (dbHelper.checkUsername(newUsername)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.checkEmail(email)) {

                Toast.makeText(this, "Email address already exists", Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Add the new user to the database
            boolean userAdded =
                    dbHelper.addUser(
                            firstName,
                            lastName,
                            email,
                            newUsername,
                            newPassword,
                            selectedRole);
            // Display success or failure message
            if (userAdded) {
                Toast.makeText(
                        this,
                        "Account request submitted. Awaiting administrator approval.",
                        Toast.LENGTH_LONG
                ).show();
                dialog.dismiss();
            }
            else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show();
            }
        });
        // Handle negative button click
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
        }

}