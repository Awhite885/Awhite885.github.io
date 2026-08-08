package com.zybooks.evidencetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Collections;

/*
 * File: EvidenceGridActivity.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Displays the evidence grid screen.
 * Allows authorized users to add, update, and delete evidence.
 * Allows administrators to manage user accounts.
 * Allows detective accounts to view evidence without modifying it.
 * Allows the user to configure SMS notifications and log out.
 */

public class EvidenceGridActivity extends AppCompatActivity {

    // RecyclerView elements
    private RecyclerView evidenceRecyclerView;
    private EvidenceAdapter evidenceAdapter;
    private List<EvidenceItem> evidenceList;
    private List<EvidenceItem> filteredEvidenceList;

    // Database
    private DatabaseHelper dbHelper;

    // Buttons and other UI elements
    private Button buttonAddEvidence;
    private Button buttonSettings;
    private Button buttonLogout;
    private Button buttonPendingRequests;
    private Button buttonCurrentUsers;
    private TextView textPendingCount;
    private LinearLayout adminPanel;
    private EditText editSearch;
    private TextView currentEvidenceLogText;

    private Button buttonSearch;
    private Button buttonOpenFilters;
    private Button buttonClearFilters;

    private TextView textActiveFilters;
    // Current search and filter selections
    private String selectedStatusFilter = "All Statuses";
    private String selectedLocationFilter = "All Locations";
    private String selectedSortOption = "Case ID: Ascending";

    // Logged-in user information
    private String userRole;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display.
        EdgeToEdge.enable(this);

        // Set the activity layout.
        setContentView(R.layout.activity_evidence_grid);

        currentEvidenceLogText = findViewById(R.id.currentEvidenceLogText);

        // Adjust the layout for system bars.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.mainGrid),
                (view, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Initialize the database helper.
        dbHelper = new DatabaseHelper(this);

        // Connect the UI elements.
        buttonAddEvidence =
                findViewById(R.id.buttonAddEvidence);

        buttonSettings =
                findViewById(R.id.buttonSettings);

        buttonLogout =
                findViewById(R.id.buttonLogout);

        buttonPendingRequests =
                findViewById(R.id.buttonPendingRequests);

        buttonCurrentUsers =
                findViewById(R.id.buttonCurrentUsers);

        textPendingCount =
                findViewById(R.id.textPendingCount);

        adminPanel =
                findViewById(R.id.adminPanel);

        editSearch =
                findViewById(R.id.editSearch);

        buttonSearch =
                findViewById(R.id.buttonSearch);

        buttonOpenFilters =
                findViewById(R.id.buttonOpenFilters);

        buttonClearFilters =
                findViewById(R.id.buttonClearFilters);

        textActiveFilters =
                findViewById(R.id.textActiveFilters);

        evidenceRecyclerView =
                findViewById(R.id.evidenceRecyclerView);

        // Receive information about the logged-in user.
        userRole =
                getIntent().getStringExtra("USER_ROLE");

        loggedInUsername =
                getIntent().getStringExtra("username");

        setupRolePermissions();
        setupButtons();
        setupEvidenceRecyclerView();
    }

    /**
     * Updates the number of displayed evidence records.
     */
    private void updateEvidenceCount() {
        currentEvidenceLogText.setText(
                "Current Evidence Log (" + filteredEvidenceList.size() + ")"
        );
    }
    /**
     * Controls which options are visible based on the user's role.
     */
    private void setupRolePermissions() {

        boolean isAdministrator =
                DatabaseHelper.ROLE_ADMINISTRATOR.equals(
                        userRole
                );

        boolean isDetective =
                DatabaseHelper.ROLE_DETECTIVE.equals(
                        userRole
                );

        // Only administrators can view user-management controls.
        if (isAdministrator) {

            adminPanel.setVisibility(View.VISIBLE);
            updatePendingRequestCount();

        } else {

            adminPanel.setVisibility(View.GONE);
        }

        // Detectives have view-only access and cannot add evidence.
        if (isDetective) {

            buttonAddEvidence.setVisibility(View.GONE);

        } else {

            buttonAddEvidence.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configures the activity buttons.
     */
    private void setupButtons() {

        // Open the Add Evidence screen.
        buttonAddEvidence.setOnClickListener(view -> {

            if (DatabaseHelper.ROLE_DETECTIVE.equals(userRole)) {

                Toast.makeText(
                        EvidenceGridActivity.this,
                        "Detective accounts have view-only access.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    EvidenceGridActivity.this,
                    AddEvidenceActivity.class
            );

            intent.putExtra(
                    "USER_ROLE",
                    userRole
            );

            intent.putExtra(
                    "username",
                    loggedInUsername
            );

            startActivity(intent);
        });

        // Open the SMS settings screen.
        buttonSettings.setOnClickListener(view -> {

            Intent intent = new Intent(
                    EvidenceGridActivity.this,
                    SmsPermissionActivity.class
            );

            startActivity(intent);
        });

        // Open the current users screen.
        buttonCurrentUsers.setOnClickListener(view -> {

            Intent intent = new Intent(
                    EvidenceGridActivity.this,
                    CurrentUsersActivity.class
            );

            intent.putExtra(
                    "username",
                    loggedInUsername
            );

            startActivity(intent);
        });

        // Open the pending user requests screen.
        buttonPendingRequests.setOnClickListener(view -> {

            Intent intent = new Intent(
                    EvidenceGridActivity.this,
                    AdminApprovalActivity.class
            );

            startActivity(intent);
        });

        // Log the user out.
        buttonLogout.setOnClickListener(view -> {

            Intent intent = new Intent(
                    EvidenceGridActivity.this,
                    MainActivity.class
            );

            /*
             * Clear the activity stack so the user cannot press the
             * Back button and return to the evidence screen after
             * logging out.
             */
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
        // Search button
        buttonSearch.setOnClickListener(view -> {
            applySearchAndFilters();
        });

        // Filters button
        buttonOpenFilters.setOnClickListener(view -> {
            showFilterDialog();
        });

        // Clear button
        buttonClearFilters.setOnClickListener(view -> {
            clearSearchAndFilters();
        });
    }

    /**
     * Configures the evidence RecyclerView and adapter callbacks.
     */
    private void setupEvidenceRecyclerView() {

        // Use a vertical list.
        evidenceRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Disable nested scrolling.
        evidenceRecyclerView.setNestedScrollingEnabled(false);

        // Load evidence records from the database.
        evidenceList = dbHelper.getAllEvidence();

        filteredEvidenceList = new ArrayList<>(evidenceList);

        evidenceAdapter = new EvidenceAdapter(

                filteredEvidenceList,
                userRole,

                /*
                 * First callback:
                 * Delete the selected evidence record.
                 */
                position -> {

                    if (DatabaseHelper.ROLE_DETECTIVE.equals(
                            userRole
                    )) {

                        Toast.makeText(
                                EvidenceGridActivity.this,
                                "Detective accounts have view-only access.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    /*
                     * Verify the position is still valid before using
                     * it. This prevents a possible crash if the list
                     * changes while the button is being processed.
                     */
                    if (position < 0 ||
                            position >= evidenceList.size()) {

                        return;
                    }

                    EvidenceItem selectedItem =
                            evidenceList.get(position);

                    String caseId =
                            selectedItem.getCaseId();

                    String itemDescription =
                            selectedItem.getItemDescription();

                    boolean deleted =
                            dbHelper.deleteEvidence(
                                    caseId,
                                    itemDescription
                            );

                    if (deleted) {

                        evidenceList.remove(position);

                        evidenceAdapter.notifyItemRemoved(
                                position
                        );

                        evidenceAdapter.notifyItemRangeChanged(
                                position,
                                evidenceList.size() - position
                        );

                        sendSmsAlert(
                                "Evidence delete: Case ID: " +
                                        caseId +
                                        ", Item: " +
                                        itemDescription
                        );

                        Toast.makeText(
                                EvidenceGridActivity.this,
                                "Evidence Deleted",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                EvidenceGridActivity.this,
                                "Failed to delete evidence.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                },

                /*
                 * Second callback:
                 * Open UpdateEvidenceActivity for the selected record.
                 */
                position -> {

                    if (DatabaseHelper.ROLE_DETECTIVE.equals(
                            userRole
                    )) {

                        Toast.makeText(
                                EvidenceGridActivity.this,
                                "Detective accounts have view-only access.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (position < 0 ||
                            position >= evidenceList.size()) {

                        return;
                    }

                    EvidenceItem selectedItem =
                            evidenceList.get(position);

                    Intent intent = new Intent(
                            EvidenceGridActivity.this,
                            UpdateEvidenceActivity.class
                    );

                    intent.putExtra(
                            "CASE_ID",
                            selectedItem.getCaseId()
                    );

                    intent.putExtra(
                            "ITEM_DESCRIPTION",
                            selectedItem.getItemDescription()
                    );

                    intent.putExtra(
                            "STATUS",
                            selectedItem.getStatus()
                    );

                    intent.putExtra(
                            "LOCATION",
                            selectedItem.getLocation()
                    );

                    intent.putExtra(
                            "USER_ROLE",
                            userRole
                    );

                    intent.putExtra(
                            "username",
                            loggedInUsername
                    );

                    startActivity(intent);
                }
        );

        // Attach the adapter to the RecyclerView.
        evidenceRecyclerView.setAdapter(
                evidenceAdapter
        );
        // Display the initial count.
        updateEvidenceCount();
    }

    /**
     * Updates the number of pending account requests displayed
     * in the administrator panel.
     */
    private void updatePendingRequestCount() {

        int pending =
                dbHelper.getPendingUserCount();

        if (pending == 0) {

            textPendingCount.setVisibility(
                    View.GONE
            );

        } else {

            textPendingCount.setVisibility(
                    View.VISIBLE
            );

            textPendingCount.setText(
                    "Pending Requests (" +
                            pending +
                            ")"
            );
        }
    }

    /**
     * Reloads the evidence records from the database.
     */
    private void refreshEvidenceList() {

        /*
         * The adapter may not exist yet if this method is triggered
         * before setupEvidenceRecyclerView finishes.
         */
        if (evidenceList == null ||
                filteredEvidenceList == null ||
                evidenceAdapter == null) {

            return;
        }

        // Reload all evidence from the database.
        evidenceList.clear();

        evidenceList.addAll(
                dbHelper.getAllEvidence()
        );

        // For now, display all evidence.
        filteredEvidenceList.clear();
        filteredEvidenceList.addAll(evidenceList);

        evidenceAdapter.notifyDataSetChanged();
        updateEvidenceCount();
    }

    /**
     * Sends an SMS notification when SMS notifications are enabled.
     */
    private void sendSmsAlert(String message) {

        SharedPreferences preferences =
                getSharedPreferences(
                        "evidence_prefs",
                        MODE_PRIVATE
                );

        boolean smsEnabled =
                preferences.getBoolean(
                        "sms_enabled",
                        false
                );

        // Stop if SMS notifications are disabled.
        if (!smsEnabled) {

            return;
        }

        String phoneNumber =
                preferences.getString(
                        "sms_number",
                        ""
                );

        // Stop if a phone number has not been entered.
        if (phoneNumber == null ||
                phoneNumber.trim().isEmpty()) {

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
                    EvidenceGridActivity.this,
                    "SMS alert sent",
                    Toast.LENGTH_SHORT
            ).show();
        } catch (Exception exception) {

            Toast.makeText(
                    EvidenceGridActivity.this,
                    "Failed to send SMS alert",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Opens the filter dialog.
     */
    private void showFilterDialog() {

        // Inflate the custom filter dialog layout.
        LayoutInflater inflater = LayoutInflater.from(this);

        View dialogView = inflater.inflate(
                R.layout.dialog_filter_evidence,
                null
        );

        // Connect the spinner controls from the dialog layout.
        Spinner spinnerStatus =
                dialogView.findViewById(R.id.spinnerStatus);

        Spinner spinnerLocation =
                dialogView.findViewById(R.id.spinnerLocation);

        Spinner spinnerSort =
                dialogView.findViewById(R.id.spinnerSort);

        // Status filter options.
        String[] statusOptions = {
                "All Statuses",
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

        spinnerStatus.setSelection(
                statusAdapter.getPosition(selectedStatusFilter)
        );

        // Location filter options.
        String[] locationOptions = {
                "All Locations",
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

        spinnerLocation.setSelection(
                locationAdapter.getPosition(selectedLocationFilter)
        );

        // Sorting options.
        String[] sortOptions = {
                "Case ID: Ascending",
                "Case ID: Descending",
                "Description: A-Z",
                "Description: Z-A",
                "Status: A-Z",
                "Location: A-Z"
        };

        ArrayAdapter<String> sortAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        sortOptions
                );

        sortAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(
                sortAdapter.getPosition(selectedSortOption)
        );

        // Build and display the dialog.
        AlertDialog filterDialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setNegativeButton(
                                "Cancel",
                                (dialog, which) -> dialog.dismiss()
                        )
                        .setPositiveButton(
                                "Apply",
                                (dialog, which) -> {

                                    String selectedStatus =
                                            spinnerStatus
                                                    .getSelectedItem()
                                                    .toString();

                                    String selectedLocation =
                                            spinnerLocation
                                                    .getSelectedItem()
                                                    .toString();

                                    String selectedSort =
                                            spinnerSort
                                                    .getSelectedItem()
                                                    .toString();

                                    // The selections will be used by the
                                    // filtering algorithm in the next step.
                                    selectedStatusFilter = selectedStatus;
                                    selectedLocationFilter = selectedLocation;
                                    selectedSortOption = selectedSort;

                                    applySearchAndFilters();
                                }
                        )
                        .create();

        filterDialog.show();
    }

    /**
     * Applies the current search and filter selections to the
     * evidence list.
     */
    private void applySearchAndFilters() {

        String searchText =
                editSearch.getText()
                        .toString()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        filteredEvidenceList.clear();

        for (EvidenceItem item : evidenceList) {

            String caseId =
                    item.getCaseId() == null
                            ? ""
                            : item.getCaseId().toLowerCase(Locale.ROOT);

            String description =
                    item.getItemDescription() == null
                            ? ""
                            : item.getItemDescription().toLowerCase(Locale.ROOT);

            boolean matchesSearch =
                    searchText.isEmpty() ||
                            caseId.contains(searchText) ||
                            description.contains(searchText);

            boolean matchesStatus =
                    selectedStatusFilter.equals("All Statuses") ||
                            item.getStatus().equals(selectedStatusFilter);
            boolean matchesLocation =
                    selectedLocationFilter.equals("All Locations") ||
                            item.getLocation().equals(selectedLocationFilter);

            if (matchesSearch &&
                    matchesStatus &&
                    matchesLocation) {

                filteredEvidenceList.add(item);
            }
        }

        Toast.makeText(
                this,
                selectedSortOption,
                Toast.LENGTH_SHORT
        ).show();

        switch (selectedSortOption) {

            case "Case ID: Descending":
                Collections.sort(
                        filteredEvidenceList,
                        (item1, item2) ->
                                item2.getCaseId().compareToIgnoreCase(
                                        item1.getCaseId()
                                )
                );
                break;

            case "Description: A-Z":
                Collections.sort(
                        filteredEvidenceList,
                        (item1, item2) ->
                                item1.getItemDescription().compareToIgnoreCase(
                                        item2.getItemDescription()
                                )
                );
                break;

            case "Description: Z-A":
                Collections.sort(
                        filteredEvidenceList,
                        (item1, item2) ->
                                item2.getItemDescription().compareToIgnoreCase(
                                        item1.getItemDescription()
                                )
                );
                break;

            case "Status: A-Z":
                Collections.sort(
                        filteredEvidenceList,
                        (item1, item2) ->
                                item1.getStatus().compareToIgnoreCase(
                                        item2.getStatus()
                                )
                );
                break;

            case "Location: A-Z":
                Collections.sort(
                        filteredEvidenceList,
                        (item1, item2) ->
                                item1.getLocation().compareToIgnoreCase(
                                        item2.getLocation()
                                )
                );
                break;

            case "Case ID: Ascending":
            default:
                Collections.sort(
                        filteredEvidenceList,
                        (item1, item2) ->
                                item1.getCaseId().compareToIgnoreCase(
                                        item2.getCaseId()
                                )
                );
                break;
        }

        evidenceAdapter.notifyDataSetChanged();
        updateEvidenceCount();
    }

    /**
     * Clears the search and filter selections and restores the
     * full evidence list.
     */
    private void clearSearchAndFilters() {

        // Clear the search box.
        editSearch.setText("");

        // Reset filter selections.
        selectedStatusFilter = "All Statuses";
        selectedLocationFilter = "All Locations";
        selectedSortOption = "Case ID: Ascending";

        // Restore the full evidence list.
        filteredEvidenceList.clear();
        filteredEvidenceList.addAll(evidenceList);

        // Clear the active-filter message.
        textActiveFilters.setText("");

        // Refresh the RecyclerView.
        evidenceAdapter.notifyDataSetChanged();
        updateEvidenceCount();
    }

    /**
     * Refreshes the screen whenever the user returns from another
     * activity, such as AddEvidenceActivity or
     * UpdateEvidenceActivity.
     */
    @Override
    protected void onResume() {

        super.onResume();

        refreshEvidenceList();

        if (DatabaseHelper.ROLE_ADMINISTRATOR.equals(
                userRole
        )) {

            updatePendingRequestCount();
        }
    }
}