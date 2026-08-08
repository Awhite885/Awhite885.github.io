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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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

        evidenceAdapter = new EvidenceAdapter(

                evidenceList,
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
                evidenceAdapter == null) {

            return;
        }

        evidenceList.clear();

        evidenceList.addAll(
                dbHelper.getAllEvidence()
        );

        evidenceAdapter.notifyDataSetChanged();
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