package com.zybooks.evidencetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;
import android.view.LayoutInflater;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;



import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

/*
 * File: EvidenceGridActivity.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Displays the evidence grid screen. Allows the user to add and manage evidence items.
 * Allows the user to update and delete evidence items. Allows the user to set SMS notifications.
 * Allows the user to logout.
 */

public class EvidenceGridActivity extends AppCompatActivity {

    // UI elements
    private RecyclerView evidenceRecyclerView;
    private EvidenceAdapter evidenceAdapter;
    private List<EvidenceItem> evidenceList;
    private DatabaseHelper dbHelper;
    private EditText editCaseId;
    private EditText editItemDescription;
    private Spinner spinnerStatus;
    private Spinner spinnerLocation;
    private Button buttonAddEvidence;
    private Button buttonSettings;
    private Button buttonLogout;

    @Override
    // Set up the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge layout for full screen display
        EdgeToEdge.enable(this);

        // Set the layout for the activity
        setContentView(R.layout.activity_evidence_grid);

        // Adjust layout padding to account for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainGrid), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initialize database helper
        dbHelper = new DatabaseHelper(this);

        //Connect form fields
        editCaseId = findViewById(R.id.editCaseId);
        editItemDescription = findViewById(R.id.editItemDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        buttonAddEvidence = findViewById(R.id.buttonAddEvidence);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Set up the status and location spinners
        String[] statusOptions = {
                "Collected", "Stored", "Processed", "Transferred Out of Unit"
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        String[] locationOptions = {
                "Office", "Property", "Court", "Lab", "Outside Agency"
                };
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);

        // Set up the add evidence button
        buttonAddEvidence.setOnClickListener(v -> addEvidenceItem());
        // Set up the settings button
        buttonSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(EvidenceGridActivity.this, SmsPermissionActivity.class);
                    startActivity(intent);
        });
        // Set up the logout button
        buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(EvidenceGridActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });




        // Initialize RecyclerView
        evidenceRecyclerView = findViewById(R.id.evidenceRecyclerView);

        // Set layout manager for vertical scrolling list
        evidenceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Disable nested scrolling for better performance
        evidenceRecyclerView.setNestedScrollingEnabled(false);

        // Load evidence from database
        evidenceList = dbHelper.getAllEvidence();


        // Create adapter and define delete button behavior
        evidenceAdapter = new EvidenceAdapter(evidenceList, position -> {
           String caseId = evidenceList.get(position).getCaseId();
           String item = evidenceList.get(position).getItemDescription();

           // Delete evidence from database
           if (dbHelper.deleteEvidence(caseId, item)) {
               evidenceList.remove(position);
               evidenceAdapter.notifyItemRemoved(position);

               sendSmsAlert("Evidence delete: Case ID: " + caseId + ", Item: " + item);

               Toast.makeText(this, "Evidence Deleted", Toast.LENGTH_SHORT).show();
           }
        },
                position -> {
                    EvidenceItem selectedItem = evidenceList.get(position);
                    showUpdateDialog(selectedItem);
                }
        );

        // Attach adapter to RecyclerView
        evidenceRecyclerView.setAdapter(evidenceAdapter);

    }
    // Add a new evidence item
    private void addEvidenceItem() {
        String caseId = editCaseId.getText().toString().trim();
        String itemDescription = editItemDescription.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String location = spinnerLocation.getSelectedItem().toString();

        // Check for empty fields
        if (caseId.isEmpty() || itemDescription.isEmpty() || status.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for duplicate evidence
        if (dbHelper.checkEvidenceExists(caseId, itemDescription)) {
            Toast.makeText(this, "Evidence already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTime = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        boolean inserted = dbHelper.addEvidence(caseId, itemDescription, status, location, dateTime);

        // Display success or failure message
        if (inserted) {
            refreshEvidenceList();
            clearForm();

            sendSmsAlert("Evidence add: Case ID: " + caseId + ", Item: " + itemDescription);

            Toast.makeText(this, "Evidence Added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add evidence", Toast.LENGTH_SHORT).show();
        }
    }
    // Show update dialog
    private void showUpdateDialog(EvidenceItem selectedItem){
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_update_evidence, null);

        // Connect form fields
        EditText editUpdateCaseId = dialogView.findViewById(R.id.editUpdateCaseId);
        EditText editUpdateItemDescription = dialogView.findViewById(R.id.editUpdateItemDescription);
        Spinner spinnerUpdateStatus = dialogView.findViewById(R.id.spinnerUpdateStatus);
        Spinner spinnerUpdateLocation = dialogView.findViewById(R.id.spinnerUpdateLocation);

        // Set up the status and location spinners
        String[] statusOptions = {
                "Collected", "Stored", "Processed", "Transferred Out of Unit"
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpdateStatus.setAdapter(statusAdapter);

        String[] locationOptions = {
                "Office", "Property", "Court", "Lab", "Outside Agency"
        };
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpdateLocation.setAdapter(locationAdapter);

        // Set existing values
        editUpdateCaseId.setText(selectedItem.getCaseId());
        editUpdateItemDescription.setText(selectedItem.getItemDescription());
        setSpinnerSelection(spinnerUpdateStatus, selectedItem.getStatus());
        setSpinnerSelection(spinnerUpdateLocation, selectedItem.getLocation());

        // Set up the save button
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Update Evidence")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.show();

        // Handle save button click
        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setOnClickListener(v -> {
            String newCaseId = editUpdateCaseId.getText().toString().trim();
            String newItemDescription = editUpdateItemDescription.getText().toString().trim();
            String status = spinnerUpdateStatus.getSelectedItem().toString();
            String location = spinnerUpdateLocation.getSelectedItem().toString();

            // Check for empty fields
            if (newCaseId.isEmpty() || newItemDescription.isEmpty() || status.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check for duplicate evidence
            boolean duplicateExists = dbHelper.checkEvidenceExists(newCaseId, newItemDescription);
            boolean sameRecord = selectedItem.getCaseId().equals(newCaseId)
                    && selectedItem.getItemDescription().equals(newItemDescription);

            // Prevent updating to the same record
            if (duplicateExists && !sameRecord) {
                Toast.makeText(this, "Evidence already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            String dateTime = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

            // Update evidence in database
            boolean updated = dbHelper.updateEvidence(selectedItem.getCaseId(), selectedItem.getItemDescription(),
                    newCaseId, newItemDescription, status, location, dateTime);

            // Display success or failure message
            if (updated) {
                refreshEvidenceList();

                sendSmsAlert("Evidence update: Case ID: " + newCaseId + ", Item: " + newItemDescription);

                Toast.makeText(this, "Evidence Updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }else {
                Toast.makeText(this, "Failed to update evidence", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Reload RecyclerView from database
    private void refreshEvidenceList(){
        evidenceList.clear();
        evidenceList.addAll(dbHelper.getAllEvidence());
        evidenceAdapter.notifyDataSetChanged();
    }
   // Clear user input fields
    private void clearForm() {
        editCaseId.setText("");
        editItemDescription.setText("");
        spinnerStatus.setSelection(0);
        spinnerLocation.setSelection(0);
    }
    // Set spinner selection
    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    // Send SMS alert
    private void sendSmsAlert(String message) {
        SharedPreferences prefs = getSharedPreferences("evidence_prefs", MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);

        // Check if SMS notifications are enabled
        if (!smsEnabled) {
            return;
        }
        // Get phone number from shared preferences
        String phoneNumber = prefs.getString("sms_number", "");
        if (phoneNumber.isEmpty()) {
            return;
        }

        // Send SMS using SmsManager
        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS alert sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS alert", Toast.LENGTH_SHORT).show();
        }
    }
}