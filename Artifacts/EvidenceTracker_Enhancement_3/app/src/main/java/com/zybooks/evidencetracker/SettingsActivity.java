package com.zybooks.evidencetracker;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
 * File: SmsPermissionActivity.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Displays the application settings screen.
 * Allows users to manage account settings,
 * configure SMS notifications,
 * change their password,
 * and log out of the application.
 */

public class SettingsActivity extends AppCompatActivity {
    // UI elements
    private Button buttonAllowSms;
    private Button buttonDenySms;
    private Button buttonBack;
    private Button buttonLogout;
    private Button buttonChangePassword;
    private TextView textUsername;
    private TextView textSmsStatus;
    private EditText editPhoneNumber;
    private SharedPreferences prefs;

    // Request SMS permission
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Handle permission result
                if (isGranted) {
                    prefs.edit().putBoolean("sms_enabled", true).apply();
                    textSmsStatus.setText("Current setting: SMS Allowed");
                    Toast.makeText(this,
                            "SMS notifications allowed", Toast.LENGTH_SHORT).show();
                } else {
                    prefs.edit().putBoolean("sms_enabled", false).apply();
                    textSmsStatus.setText("Current setting: SMS Denied");
                    Toast.makeText(this,
                            "SMS notifications denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    // Set up the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        // Adjust layout padding to account for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainSms), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize UI elements
        prefs = getSharedPreferences("evidence_prefs", MODE_PRIVATE);

        // Connect form fields
        buttonAllowSms = findViewById(R.id.buttonAllowSms);
        buttonDenySms = findViewById(R.id.buttonDenySms);
        buttonBack = findViewById(R.id.buttonBack);
        textSmsStatus = findViewById(R.id.textSmsStatus);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        textUsername = findViewById(R.id.textUsername);
        String fullName =
                prefs.getString("logged_in_user", "");

        textUsername.setText(fullName);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Check if SMS notifications are enabled
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);
        // Display current SMS notification setting
        textSmsStatus.setText(smsEnabled
                ? "Current setting: SMS Allowed"
                : "Current setting: SMS Denied");

        // Enables SMS notifications
        buttonAllowSms.setOnClickListener(v -> {
            // Check for SMS permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                String phone = editPhoneNumber.getText().toString().trim();
                // Check for empty fields
                if (phone.isEmpty()) {
                    Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save phone number and enable SMS notifications
                prefs.edit().putBoolean("sms_enabled", true).putString("sms_number", phone).apply();
                textSmsStatus.setText("Current setting: SMS Allowed");
                Toast.makeText(this, "SMS notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            }
        });

        // Disables SMS notifications
        buttonDenySms.setOnClickListener(v -> {
            // Disable SMS notifications
            prefs.edit().putBoolean("sms_enabled", false).apply();
            textSmsStatus.setText("Current setting: SMS Denied");
            Toast.makeText(this, "SMS notifications disabled", Toast.LENGTH_SHORT).show();
        });

        buttonChangePassword.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SettingsActivity.this,
                    ChangePasswordActivity.class);

            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {

            prefs.edit()
                    .remove("logged_in_user")
                    .apply();

            Intent intent = new Intent(
                    SettingsActivity.this,
                    MainActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });

        // Return to previous screen
        buttonBack.setOnClickListener(v -> finish());

    }
}