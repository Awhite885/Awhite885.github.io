package com.zybooks.evidencetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEvidenceActivity extends AppCompatActivity {

    // UI elements
    private EditText editCaseId;
    private EditText editItemDescription;
    private Spinner spinnerStatus;
    private Spinner spinnerLocation;
    private Button buttonAddEvidence;
    private Button buttonBack;

    // Database
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_evidence);

        // Adjust layout padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.mainAddEvidence),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Connect form fields
        editCaseId = findViewById(R.id.editCaseId);
        editItemDescription =
                findViewById(R.id.editItemDescription);

        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerLocation = findViewById(R.id.spinnerLocation);

        buttonAddEvidence =
                findViewById(R.id.buttonAddEvidence);

        buttonBack =
                findViewById(R.id.buttonBack);

        // Set up status spinner
        String[] statusOptions = {
                "Collected",
                "Stored",
                "Processed",
                "Transferred Out of Unit"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        statusOptions
                );

        statusAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerStatus.setAdapter(statusAdapter);

        // Set up location spinner
        String[] locationOptions = {
                "Office",
                "Property",
                "Court",
                "Lab",
                "Outside Agency"
        };

        ArrayAdapter<String> locationAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        locationOptions
                );

        locationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerLocation.setAdapter(locationAdapter);

        // Add Evidence button
        buttonAddEvidence.setOnClickListener(
                v -> addEvidenceItem()
        );

        // Back button
        buttonBack.setOnClickListener(
                v -> finish()
        );
    }

    // Add a new evidence item
    private void addEvidenceItem() {

        String caseId =
                editCaseId.getText().toString().trim();

        String itemDescription =
                editItemDescription.getText().toString().trim();

        String status =
                spinnerStatus.getSelectedItem().toString();

        String location =
                spinnerLocation.getSelectedItem().toString();

        // Check for empty fields
        if (caseId.isEmpty()
                || itemDescription.isEmpty()
                || status.isEmpty()
                || location.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Check for duplicate evidence
        if (dbHelper.checkEvidenceExists(
                caseId,
                itemDescription)) {

            Toast.makeText(
                    this,
                    "Evidence already exists",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Create date and time
        String dateTime =
                new SimpleDateFormat(
                        "MM/dd/yyyy HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

        // Add evidence to database
        boolean inserted =
                dbHelper.addEvidence(
                        caseId,
                        itemDescription,
                        status,
                        location,
                        dateTime
                );

        if (inserted) {

            sendSmsAlert(
                    "Evidence add: Case ID: "
                            + caseId
                            + ", Item: "
                            + itemDescription
            );

            Toast.makeText(
                    this,
                    "Evidence Added",
                    Toast.LENGTH_SHORT
            ).show();

            // Return to evidence grid
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to add evidence",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Send SMS alert
    private void sendSmsAlert(String message) {

        SharedPreferences prefs =
                getSharedPreferences(
                        "evidence_prefs",
                        MODE_PRIVATE
                );

        boolean smsEnabled =
                prefs.getBoolean(
                        "sms_enabled",
                        false
                );

        // Stop if SMS notifications are disabled
        if (!smsEnabled) {
            return;
        }

        String phoneNumber =
                prefs.getString(
                        "sms_number",
                        ""
                );

        // Stop if no phone number is saved
        if (phoneNumber.isEmpty()) {
            return;
        }

        try {

            SmsManager smsManager =
                    SmsManager.getDefault();

            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
            );

            Toast.makeText(
                    this,
                    "SMS alert sent",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Failed to send SMS alert",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}