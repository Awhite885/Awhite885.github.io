package com.zybooks.evidencetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateEvidenceActivity extends AppCompatActivity {

    private EditText editUpdateCaseId;
    private EditText editUpdateItemDescription;
    private Spinner spinnerUpdateStatus;
    private Spinner spinnerUpdateLocation;
    private Button buttonSaveUpdate;
    private Button buttonCancelUpdate;

    private DatabaseHelper dbHelper;

    private String originalCaseId;
    private String originalItemDescription;
    private String originalStatus;
    private String originalLocation;
    private String userRole;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_evidence);

        dbHelper = new DatabaseHelper(this);

        // Connect the layout controls.
        editUpdateCaseId =
                findViewById(R.id.editUpdateCaseId);

        editUpdateItemDescription =
                findViewById(R.id.editUpdateItemDescription);

        spinnerUpdateStatus =
                findViewById(R.id.spinnerUpdateStatus);

        spinnerUpdateLocation =
                findViewById(R.id.spinnerUpdateLocation);

        buttonSaveUpdate =
                findViewById(R.id.buttonSaveUpdate);

        buttonCancelUpdate =
                findViewById(R.id.buttonCancelUpdate);

        // Receive the original evidence information.
        originalCaseId = getIntent().getStringExtra("CASE_ID");

        originalItemDescription =
                getIntent().getStringExtra("ITEM_DESCRIPTION");

        originalStatus =
                getIntent().getStringExtra("STATUS");

        originalLocation =
                getIntent().getStringExtra("LOCATION");

        userRole =
                getIntent().getStringExtra("USER_ROLE");

        loggedInUsername =
                getIntent().getStringExtra("username");

        // Prevent detective accounts from accessing the update screen.
        if (DatabaseHelper.ROLE_DETECTIVE.equals(userRole)) {

            Toast.makeText(
                    this,
                    "Detective accounts have view-only access.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Make sure the selected evidence information was received.
        if (originalCaseId == null ||
                originalItemDescription == null) {

            Toast.makeText(
                    this,
                    "Unable to load the selected evidence.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setupStatusSpinner();
        setupLocationSpinner();
        displayExistingEvidence();

        buttonSaveUpdate.setOnClickListener(v ->
                updateEvidenceItem());

        buttonCancelUpdate.setOnClickListener(v ->
                finish());
    }

    /**
     * Sets up the evidence status spinner.
     */
    private void setupStatusSpinner() {

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

        spinnerUpdateStatus.setAdapter(statusAdapter);
    }

    /**
     * Sets up the evidence location spinner.
     */
    private void setupLocationSpinner() {

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

        spinnerUpdateLocation.setAdapter(locationAdapter);
    }

    /**
     * Places the existing evidence information into the form.
     */
    private void displayExistingEvidence() {

        editUpdateCaseId.setText(originalCaseId);

        editUpdateItemDescription.setText(
                originalItemDescription
        );

        setSpinnerSelection(
                spinnerUpdateStatus,
                originalStatus
        );

        setSpinnerSelection(
                spinnerUpdateLocation,
                originalLocation
        );
    }

    /**
     * Validates and updates the selected evidence record.
     */
    private void updateEvidenceItem() {

        String newCaseId =
                editUpdateCaseId.getText()
                        .toString()
                        .trim();

        String newItemDescription =
                editUpdateItemDescription.getText()
                        .toString()
                        .trim();

        String newStatus =
                spinnerUpdateStatus
                        .getSelectedItem()
                        .toString();

        String newLocation =
                spinnerUpdateLocation
                        .getSelectedItem()
                        .toString();

        // Check for empty fields.
        if (newCaseId.isEmpty() ||
                newItemDescription.isEmpty() ||
                newStatus.isEmpty() ||
                newLocation.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill in all fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Determine whether the user kept the same case ID and
         * item description.
         */
        boolean sameRecord =
                originalCaseId.equals(newCaseId) &&
                        originalItemDescription.equals(
                                newItemDescription
                        );

        /*
         * Check whether another evidence record already uses the
         * entered case ID and item description.
         */
        boolean duplicateExists =
                dbHelper.checkEvidenceExists(
                        newCaseId,
                        newItemDescription
                );

        if (duplicateExists && !sameRecord) {

            Toast.makeText(
                    this,
                    "Evidence already exists.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String dateTime =
                new SimpleDateFormat(
                        "MM/dd/yyyy HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

        boolean updated =
                dbHelper.updateEvidence(
                        originalCaseId,
                        originalItemDescription,
                        newCaseId,
                        newItemDescription,
                        newStatus,
                        newLocation,
                        dateTime
                );

        if (updated) {

            sendSmsAlert(
                    "Evidence update: Case ID: " +
                            newCaseId +
                            ", Item: " +
                            newItemDescription
            );

            Toast.makeText(
                    this,
                    "Evidence Updated",
                    Toast.LENGTH_SHORT
            ).show();

            /*
             * Close this screen and return to
             * EvidenceGridActivity.
             */
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to update evidence.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Selects the spinner option that matches the saved value.
     */
    private void setSpinnerSelection(
            Spinner spinner,
            String selectedValue) {

        if (selectedValue == null) {
            return;
        }

        ArrayAdapter<?> adapter =
                (ArrayAdapter<?>) spinner.getAdapter();

        for (int index = 0;
             index < adapter.getCount();
             index++) {

            Object item = adapter.getItem(index);

            if (item != null &&
                    selectedValue.equals(
                            item.toString()
                    )) {

                spinner.setSelection(index);
                return;
            }
        }
    }

    /**
     * Sends the same type of SMS notification used when evidence
     * is added.
     *
     * Replace the phone number below with the same number or
     * preference value used by AddEvidenceActivity.
     */
    private void sendSmsAlert(String message) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        /*
         * Put the same destination phone number used by your
         * AddEvidenceActivity here.
         */
        String phoneNumber = "";

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

        } catch (Exception exception) {

            Toast.makeText(
                    this,
                    "Evidence was updated, but the SMS could not be sent.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}