package com.zybooks.evidencetracker;

import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;

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

        // Check for empty fields
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password",
                    Toast.LENGTH_SHORT).show();
            return;
            }
        // Check if the user exists in the database
        boolean validUser = dbHelper.checkUser(username, password);
        // Navigate to EvidenceGridActivity if the user is valid
        if (validUser) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, EvidenceGridActivity.class);
            startActivity(intent);
        }
        // Display an error message if the user is not valid
        else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    // Show the create account dialog
    private void showCreateAccountDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_create_account, null);

        EditText editNewUsername = dialogView.findViewById(R.id.editNewUsername);
        EditText editNewPassword = dialogView.findViewById(R.id.editNewPassword);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Account");
        builder.setView(dialogView);

        // Handle positive and negative button clicks
        builder.setPositiveButton("Create", (dialog, which) -> {
            String newUsername = editNewUsername.getText().toString().trim();
            String newPassword = editNewPassword.getText().toString().trim();

            // Check for empty fields
            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if the username already exists
            if (dbHelper.checkUsername(newUsername)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add the new user to the database
            boolean userAdded = dbHelper.addUser(newUsername, newPassword);
            // Display success or failure message
            if (userAdded) {
                Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();
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