package com.zybooks.evidencetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuditLogAdapter extends ArrayAdapter<AuditLogEntry> {

    public AuditLogAdapter(
            @NonNull Context context,
            @NonNull List<AuditLogEntry> auditEntries) {

        super(context, 0, auditEntries);
    }

    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(
                            R.layout.item_audit_log,
                            parent,
                            false
                    );
        }

        AuditLogEntry auditEntry = getItem(position);

        TextView textDate =
                convertView.findViewById(
                        R.id.textAuditEntryDate
                );

        TextView textAction =
                convertView.findViewById(
                        R.id.textAuditEntryAction
                );

        TextView textChanges =
                convertView.findViewById(
                        R.id.textAuditEntryChanges
                );

        TextView textUser =
                convertView.findViewById(
                        R.id.textAuditEntryUser
                );

        if (auditEntry != null) {

            textDate.setText(
                    formatDateTime(
                            auditEntry.getDateTime()
                    )
            );

            textAction.setText(
                    auditEntry.getAction()
            );

            textChanges.setText(
                    auditEntry.getChanges()
            );

            textUser.setText(
                    "Performed By: " +
                            auditEntry.getPerformedBy()
            );
        }

        return convertView;
    }
    private String formatDateTime(String storedDateTime) {

        SimpleDateFormat databaseFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                );

        SimpleDateFormat displayFormat =
                new SimpleDateFormat(
                        "MMM d, yyyy\nh:mm a",
                        Locale.getDefault()
                );

        try {

            Date parsedDate =
                    databaseFormat.parse(storedDateTime);

            if (parsedDate != null) {
                return displayFormat.format(parsedDate);
            }

        } catch (ParseException ignored) {

            // Return the original value if it cannot be parsed.
        }

        return storedDateTime;
    }
}