package com.zybooks.evidencetracker;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
 * Displays the SMS permission screen. Allows the user to grant or deny SMS notifications.
 * Allows the user to navigate to the evidence grid screen.
 */

public class SmsPermissionActivity extends AppCompatActivity {
    // UI elements
    private Button buttonAllowSms;
    private Button buttonDenySms;
    private Button buttonBack;
    private TextView textSmsStatus;
    private TextView editPhoneNumber;
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
        setContentView(R.layout.activity_sms_permission);
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

        // Check if SMS notifications are enabled
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);
        // Display current SMS notification setting
        textSmsStatus.setText(smsEnabled
                ? "Current setting: SMS Allowed"
                : "Current setting: SMS Denied");

        // Handle button clicks
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

        // Handle button clicks
        buttonDenySms.setOnClickListener(v -> {
            // Disable SMS notifications
            prefs.edit().putBoolean("sms_enabled", false).apply();
            textSmsStatus.setText("Current setting: SMS Denied");
            Toast.makeText(this, "SMS notifications disabled", Toast.LENGTH_SHORT).show();
        });
        // Handle button clicks for return to evidence grid
        buttonBack.setOnClickListener(v -> finish());
    }
}