package com.zybooks.evidencetracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AuditHistoryActivity extends AppCompatActivity {

    private EditText editAuditCaseId;
    private Button buttonSearchAudit;
    private Button buttonBack;
    private ListView listAuditHistory;

    private DatabaseHelper dbHelper;

    /*
     * Stores the information displayed in the ListView.
     */
    private final List<String> evidenceDisplayList =
            new ArrayList<>();
    private final List<Boolean> evidenceDeletedStatuses =
            new ArrayList<>();

    /*
     * Stores the Case ID and item separately so they can
     * be passed to AuditLogDetailsActivity when selected.
     */
    private final List<String> evidenceCaseIds =
            new ArrayList<>();

    private final List<String> evidenceItems =
            new ArrayList<>();

    private final List<String> evidenceStatuses =
            new ArrayList<>();

    private final List<String> evidenceLocations =
            new ArrayList<>();

    private ArrayAdapter<String> evidenceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit_history);

        dbHelper = new DatabaseHelper(this);

        editAuditCaseId =
                findViewById(R.id.editAuditCaseId);

        buttonSearchAudit =
                findViewById(R.id.buttonSearchAudit);

        buttonBack =
                findViewById(R.id.buttonBack);

        listAuditHistory =
                findViewById(R.id.listAuditHistory);

        evidenceAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                evidenceDisplayList
        ) {
            @Override
            public View getView(
                    int position,
                    View convertView,
                    ViewGroup parent) {

                View view = super.getView(
                        position,
                        convertView,
                        parent
                );

                TextView textView =
                        view.findViewById(android.R.id.text1);

                boolean isDeleted =
                        position < evidenceDeletedStatuses.size()
                                && evidenceDeletedStatuses.get(position);

                if (isDeleted) {
                    textView.setTextColor(Color.RED);
                } else {
                    textView.setTextColor(Color.BLACK);
                }

                return view;
            }
        };

        listAuditHistory.setAdapter(evidenceAdapter);

        /*
         * Search the evidence table using the entered Case ID.
         */
        buttonSearchAudit.setOnClickListener(
                v -> searchEvidenceByCaseId()
        );

        /*
         * Open the audit details for the selected evidence item.
         */
        listAuditHistory.setOnItemClickListener(
                (parent, view, position, id) ->
                        openAuditDetails(position)
        );

        /*
         * Return to the Evidence Grid.
         */
        buttonBack.setOnClickListener(
                v -> finish()
        );
    }

    private void searchEvidenceByCaseId() {

        String caseId =
                editAuditCaseId.getText()
                        .toString()
                        .trim();

        if (caseId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter a Case ID",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Clear the previous results before loading
         * evidence for another case.
         */
        evidenceDisplayList.clear();
        evidenceCaseIds.clear();
        evidenceItems.clear();
        evidenceStatuses.clear();
        evidenceLocations.clear();
        evidenceDeletedStatuses.clear();

        try (Cursor cursor =
                     dbHelper.getAuditEvidenceByCaseId(caseId)) {

            while (cursor.moveToNext()) {

                String evidenceCaseId =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_AUDIT_CASE_ID));

                String item =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_AUDIT_ITEM));

                String status =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_AUDIT_STATUS));

                String location =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_AUDIT_LOCATION));

                boolean isDeleted =
                        dbHelper.isEvidenceDeleted(
                                evidenceCaseId,
                                item
                        );

                evidenceCaseIds.add(evidenceCaseId);
                evidenceItems.add(item);
                evidenceStatuses.add(status);
                evidenceLocations.add(location);
                evidenceDeletedStatuses.add(isDeleted);


                String displayEntry =
                        "Case: " + evidenceCaseId +
                                "\nItem: " + item +
                                "\nStatus: " + status +
                                "\nLocation: " + location;

                evidenceDisplayList.add(displayEntry);
            }
        }

        evidenceAdapter.notifyDataSetChanged();

        if (evidenceDisplayList.isEmpty()) {

            Toast.makeText(
                    this,
                    "No evidence found for this Case ID",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openAuditDetails(int position) {

        if (position < 0 ||
                position >= evidenceItems.size()) {

            return;
        }

        Intent intent = new Intent(
                AuditHistoryActivity.this,
                AuditLogDetailsActivity.class
        );

        intent.putExtra(
                "CASE_ID",
                evidenceCaseIds.get(position)
        );

        intent.putExtra(
                "ITEM",
                evidenceItems.get(position)
        );

        intent.putExtra(
                "STATUS",
                evidenceStatuses.get(position)
        );

        intent.putExtra(
                "LOCATION",
                evidenceLocations.get(position)
        );

        startActivity(intent);
    }
}