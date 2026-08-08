package com.zybooks.evidencetracker;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AuditLogDetailsActivity extends AppCompatActivity {

    private TextView textAuditCaseId;
    private TextView textAuditItem;
    private TextView textAuditStatus;
    private TextView textAuditLocation;

    private ListView listAuditLogDetails;
    private Button buttonAuditDetailsBack;

    private DatabaseHelper dbHelper;

    private String caseId;
    private String item;
    private String status;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit_log_details);

        dbHelper = new DatabaseHelper(this);

        textAuditCaseId =
                findViewById(R.id.textAuditCaseId);

        textAuditItem =
                findViewById(R.id.textAuditItem);

        textAuditStatus =
                findViewById(R.id.textAuditStatus);

        textAuditLocation =
                findViewById(R.id.textAuditLocation);

        listAuditLogDetails =
                findViewById(R.id.listAuditLogDetails);

        buttonAuditDetailsBack =
                findViewById(R.id.buttonAuditDetailsBack);

        caseId = getIntent().getStringExtra("CASE_ID");
        item = getIntent().getStringExtra("ITEM");
        status = getIntent().getStringExtra("STATUS");
        location = getIntent().getStringExtra("LOCATION");

        if (caseId == null ||
                item == null ||
                status == null ||
                location == null) {

            Toast.makeText(
                    this,
                    "Evidence information could not be loaded",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        textAuditCaseId.setText(
                "Case ID: " + caseId
        );

        textAuditItem.setText(
                "Item: " + item
        );

        textAuditStatus.setText(
                "Status: " + status
        );

        textAuditLocation.setText(
                "Location: " + location
        );

        buttonAuditDetailsBack.setOnClickListener(
                v -> finish()
        );

        loadAuditHistory();
    }

    private void loadAuditHistory() {

        List<AuditLogEntry> auditEntries =
                new ArrayList<>();

        long evidenceId =
                dbHelper.getAuditEvidenceId(
                        caseId,
                        item
                );

        try (Cursor cursor =
                     dbHelper.getAuditHistory(evidenceId)) {

            while (cursor.moveToNext()) {

                String storedAction =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_AUDIT_ACTION
                                )
                        );

                String performedBy =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_AUDIT_PERFORMED_BY
                                )
                        );

                String dateTime =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_AUDIT_DATETIME
                                )
                        );

                String action;
                String changes;

                int firstLineBreak =
                        storedAction.indexOf("\n");

                if (firstLineBreak >= 0) {

                    action = storedAction
                            .substring(0, firstLineBreak)
                            .trim();

                    changes = storedAction
                            .substring(firstLineBreak)
                            .trim();

                } else {

                    action = storedAction;
                    changes = "";
                }

                AuditLogEntry auditEntry =
                        new AuditLogEntry(
                                dateTime,
                                action,
                                changes,
                                performedBy
                        );

                auditEntries.add(auditEntry);
            }
        }

        AuditLogAdapter adapter =
                new AuditLogAdapter(
                        this,
                        auditEntries
                );

        listAuditLogDetails.setAdapter(adapter);

        if (auditEntries.isEmpty()) {

            Toast.makeText(
                    this,
                    "No audit history found for this evidence item",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}