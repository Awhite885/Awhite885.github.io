package com.zybooks.evidencetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * File: DatabaseHelper.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Helper class for managing the SQLite database.
 * Responsible for creating and updating the database, as well as adding and retrieving data.
 * Allows the user to add and manage evidence items.
 * Allows the user to update and delete evidence items.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database information
    private static final String DATABASE_NAME = "Evidence.db";
    private static final int DATABASE_VERSION = 7;

    //User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIRST_NAME = "firstName";
    public static final String COLUMN_LAST_NAME = "lastName";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_ACCOUNT_STATUS = "accountStatus";
    public static final String COLUMN_REQUEST_DATE = "requestDate";

    // Supported roles
    public static final String ROLE_CSI = "Crime Scene Investigator";
    public static final String ROLE_DETECTIVE = "Detective";
    public static final String ROLE_ADMINISTRATOR = "Administrator";

    //Account status
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;


    //Evidence table
    public static final String TABLE_EVIDENCE = "evidence";
    public static final String COLUMN_EVIDENCE_ID = "evidenceId";
    public static final String COLUMN_CASE_ID = "caseId";
    public static final String COLUMN_ITEM = "item";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATETIME = "dateTime";

    // Audit log table
    public static final String TABLE_AUDIT_LOG = "auditLog";
    public static final String COLUMN_AUDIT_ID = "auditId";
    public static final String COLUMN_AUDIT_EVIDENCE_ID = "evidenceId";
    public static final String COLUMN_AUDIT_CASE_ID = "caseId";
    public static final String COLUMN_AUDIT_ITEM = "item";
    public static final String COLUMN_AUDIT_ACTION = "auditAction";
    public static final String COLUMN_AUDIT_PERFORMED_BY = "performedBy";
    public static final String COLUMN_AUDIT_DATETIME = "dateTime";
    public static final String COLUMN_AUDIT_STATUS = "status";
    public static final String COLUMN_AUDIT_LOCATION = "location";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    // Create tables
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                        COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                        COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                        COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL, " +
                        COLUMN_ROLE + " TEXT NOT NULL," +
                        COLUMN_ACCOUNT_STATUS + " INTEGER NOT NULL DEFAULT " +
                        STATUS_PENDING + "," +
                        COLUMN_REQUEST_DATE + " TEXT)";

        //Create evidence table
        String createEvidence = "CREATE TABLE " + TABLE_EVIDENCE + " (" +
                COLUMN_EVIDENCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CASE_ID + " TEXT NOT NULL, " +
                COLUMN_ITEM + " TEXT NOT NULL, " +
                COLUMN_STATUS + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_DATETIME + " TEXT)";

        //Create audit log table
        String createAuditLog = "CREATE TABLE " + TABLE_AUDIT_LOG + " (" +
                COLUMN_AUDIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AUDIT_EVIDENCE_ID + " INTEGER, " +
                COLUMN_AUDIT_CASE_ID + " TEXT NOT NULL, " +
                COLUMN_AUDIT_ITEM + " TEXT NOT NULL, " +
                COLUMN_AUDIT_ACTION + " TEXT NOT NULL, " +
                COLUMN_AUDIT_PERFORMED_BY + " TEXT NOT NULL, " +
                COLUMN_AUDIT_DATETIME + " TEXT NOT NULL, " +
                COLUMN_AUDIT_STATUS + " TEXT, " +
                COLUMN_AUDIT_LOCATION + " TEXT)";

        db.execSQL(createTable);
        db.execSQL(createEvidence);
        db.execSQL(createAuditLog);


        // Create default administrator account
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_FIRST_NAME, "Admin");
        adminValues.put(COLUMN_LAST_NAME, "User");
        adminValues.put(COLUMN_EMAIL, "admin@agency.local");

        adminValues.put(COLUMN_USERNAME, "admin");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_ROLE, ROLE_ADMINISTRATOR);
        adminValues.put(COLUMN_ACCOUNT_STATUS, STATUS_APPROVED);
        adminValues.put(
                COLUMN_REQUEST_DATE,
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault())
                        .format(new Date()));

        db.insert(TABLE_USERS, null, adminValues);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Version 2 added the role column
        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE " + TABLE_USERS +
                            " ADD COLUMN " + COLUMN_ROLE +
                            " TEXT NOT NULL DEFAULT '" + ROLE_CSI + "'"
            );
        }

        // Version 3 adds administrator approval status
        if (oldVersion < 3) {
            db.execSQL(
                    "ALTER TABLE " + TABLE_USERS +
                            " ADD COLUMN " + COLUMN_ACCOUNT_STATUS +
                            " INTEGER NOT NULL DEFAULT " + STATUS_PENDING
            );
            db.execSQL(
                    "ALTER TABLE " + TABLE_USERS +
                            " ADD COLUMN " + COLUMN_REQUEST_DATE +
                            " TEXT"
            );
        }
        // Version 4 adds the user's name and email address
        if (oldVersion < 4) {

            db.execSQL(
                    "ALTER TABLE " + TABLE_USERS +
                            " ADD COLUMN " + COLUMN_FIRST_NAME +
                            " TEXT NOT NULL DEFAULT ''"
            );

            db.execSQL(
                    "ALTER TABLE " + TABLE_USERS +
                            " ADD COLUMN " + COLUMN_LAST_NAME +
                            " TEXT NOT NULL DEFAULT ''"
            );

            db.execSQL(
                    "ALTER TABLE " + TABLE_USERS +
                            " ADD COLUMN " + COLUMN_EMAIL +
                            " TEXT NOT NULL DEFAULT ''"
            );
        }
        // Version 5 adds the evidence table
        if (oldVersion < 5) {
            db.execSQL(
                    "CREATE TABLE " + TABLE_AUDIT_LOG + " (" +
                            COLUMN_AUDIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            COLUMN_AUDIT_CASE_ID + " TEXT NOT NULL, " +
                            COLUMN_AUDIT_ITEM + " TEXT NOT NULL, " +
                            COLUMN_AUDIT_ACTION + " TEXT NOT NULL, " +
                            COLUMN_AUDIT_PERFORMED_BY + " TEXT NOT NULL, " +
                            COLUMN_AUDIT_DATETIME + " TEXT NOT NULL)"
            );
        }
        // Version 6 links audit records to a permanent evidence ID
        if (oldVersion < 6) {

            db.execSQL(
                    "ALTER TABLE " + TABLE_AUDIT_LOG +
                            " ADD COLUMN " +
                            COLUMN_AUDIT_EVIDENCE_ID +
                            " INTEGER"
            );
        }
        // Version 7 stores status and location in the audit log
        if (oldVersion < 7) {

            db.execSQL(
                    "ALTER TABLE " + TABLE_AUDIT_LOG +
                            " ADD COLUMN " +
                            COLUMN_AUDIT_STATUS +
                            " TEXT"
            );

            db.execSQL(
                    "ALTER TABLE " + TABLE_AUDIT_LOG +
                            " ADD COLUMN " +
                            COLUMN_AUDIT_LOCATION +
                            " TEXT"
            );
        }
    }
    // Add user to database
    public boolean addUser(String firstName,
                           String lastName,
                           String email,
                           String username,
                           String password,
                           String role) {

        if (firstName == null ||
                lastName == null ||
                email == null ||
                username == null ||
                password == null ||
                role == null) {
            return false;
        }

        String cleanFirstName = firstName.trim();
        String cleanLastName = lastName.trim();
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        String cleanUsername = username.trim();
        String cleanPassword = password.trim();
        String cleanRole = role.trim();

        if (cleanFirstName.isEmpty() ||
                cleanLastName.isEmpty() ||
                cleanEmail.isEmpty() ||
                cleanUsername.isEmpty() ||
                cleanPassword.isEmpty() ||
                cleanRole.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, cleanFirstName);
        values.put(COLUMN_LAST_NAME, cleanLastName);
        values.put(COLUMN_EMAIL, cleanEmail);
        values.put(COLUMN_USERNAME, cleanUsername);
        values.put(COLUMN_PASSWORD, cleanPassword);
        values.put(COLUMN_ROLE, cleanRole);
        values.put(COLUMN_ACCOUNT_STATUS, STATUS_PENDING);
        values.put(
                COLUMN_REQUEST_DATE,
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date())
        );


        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Authenticates a user and returns the user's role.
     * @return the user's role when authentication is successful,
     * or null when the credentials are invalid.
     */

    public AuthenticationResult authenticateUser(
            String username,
            String password) {

        if (username == null || password == null) {
            return new AuthenticationResult(
                    false,
                    STATUS_PENDING,
                    null
            );
        }

        SQLiteDatabase db = getReadableDatabase();

        String selection =
                COLUMN_USERNAME + "=? AND " +
                        COLUMN_PASSWORD + "=?";

        String[] selectionArgs = {
                username.trim(),
                password.trim()
        };

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{
                        COLUMN_ROLE,
                        COLUMN_ACCOUNT_STATUS
                },
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {

                String role = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_ROLE)
                );

                int accountStatus = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_STATUS)
                );

                return new AuthenticationResult(
                        true,
                        accountStatus,
                        role
                );
            }
        }

        return new AuthenticationResult(
                false,
                STATUS_PENDING,
                null
        );
    }
    public String getFullName(String username) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{
                        COLUMN_FIRST_NAME,
                        COLUMN_LAST_NAME
                },
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {

                String firstName =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_FIRST_NAME));

                String lastName =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_LAST_NAME));

                return firstName + " " + lastName;
            }
        }

        return "";
    }

    // Update the password for an authenticated user
    public boolean updatePassword(
            String username,
            String currentPassword,
            String newPassword) {

        if (username == null ||
                currentPassword == null ||
                newPassword == null) {
            return false;
        }

        String cleanUsername = username.trim();
        String cleanCurrentPassword = currentPassword.trim();
        String cleanNewPassword = newPassword.trim();

        if (cleanUsername.isEmpty() ||
                cleanCurrentPassword.isEmpty() ||
                cleanNewPassword.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        // Verify the user's current password
        String selection =
                COLUMN_USERNAME + "=? AND " +
                        COLUMN_PASSWORD + "=?";

        String[] selectionArgs = {
                cleanUsername,
                cleanCurrentPassword
        };

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            if (!cursor.moveToFirst()) {
                return false;
            }
        }

        // Update the password
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, cleanNewPassword);

        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                COLUMN_USERNAME + "=?",
                new String[]{cleanUsername}
        );

        return rowsUpdated > 0;
    }

    // Check if username exists
    public boolean checkUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + "=?";
        String[] selectionArgs = {username.trim()};

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }
    public boolean checkEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getReadableDatabase();

        String selection = COLUMN_EMAIL + "=?";

        String[] selectionArgs = {
                email.trim().toLowerCase(Locale.ROOT)
        };

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            return cursor.moveToFirst();
        }
    }

    // Get all users waiting for administrator approval
    public List<UserAccount> getPendingUsers() {

        List<UserAccount> pendingUsers = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String selection = COLUMN_ACCOUNT_STATUS + "=?";

        String[] selectionArgs = {
                String.valueOf(STATUS_PENDING)
        };

        try (Cursor cursor = db.query(
                TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_REQUEST_DATE + " ASC"
        )) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_ID));

                String firstName = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));

                String lastName = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));

                String email = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_EMAIL));

                String username = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_USERNAME));

                String role = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_ROLE));

                String requestDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_REQUEST_DATE));

                pendingUsers.add(
                        new UserAccount(
                                id,
                                firstName,
                                lastName,
                                email,
                                username,
                                role,
                                requestDate
                        )
                );
            }
        }

        return pendingUsers;
    }

    // Get all approved users
    public List<UserAccount> getAllUsers() {

        List<UserAccount> userList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String selection = COLUMN_ACCOUNT_STATUS + "=?";

        String[] selectionArgs = {
                String.valueOf(STATUS_APPROVED)
        };

        String orderBy =
                COLUMN_LAST_NAME + " ASC, " +
                        COLUMN_FIRST_NAME + " ASC";

        try (Cursor cursor = db.query(
                TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        )) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_ID));

                String firstName = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));

                String lastName = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));

                String email = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_EMAIL));

                String username = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_USERNAME));

                String role = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_ROLE));

                String requestDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_REQUEST_DATE));

                userList.add(
                        new UserAccount(
                                id,
                                firstName,
                                lastName,
                                email,
                                username,
                                role,
                                requestDate
                        )
                );
            }
        }

        return userList;
    }
    public int getPendingUserCount() {

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT COUNT(*) FROM " +
                        TABLE_USERS +
                        " WHERE " +
                        COLUMN_ACCOUNT_STATUS +
                        "=?";

        Cursor cursor = db.rawQuery(
                query,
                new String[]{
                        String.valueOf(STATUS_PENDING)
                });

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    // Update a user's approval status
    public boolean updateAccountStatus(int userId, int newStatus) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ACCOUNT_STATUS, newStatus);

        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                COLUMN_ID + "=?",
                new String[]{
                        String.valueOf(userId)
                }
        );

        return rowsUpdated > 0;
    }
    // Update an existing user account
    public boolean updateUser(int userId,
                              String firstName,
                              String lastName,
                              String email,
                              String role) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName.trim());
        values.put(COLUMN_LAST_NAME, lastName.trim());
        values.put(
                COLUMN_EMAIL,
                email.trim().toLowerCase(Locale.ROOT)
        );
        values.put(COLUMN_ROLE, role);

        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)}
        );

        return rowsUpdated > 0;
    }
    // Disable an approved user account
    public boolean disableUser(int userId) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ACCOUNT_STATUS, STATUS_REJECTED);

        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)}
        );

        return rowsUpdated > 0;
    }

    /**
     * Creates a ContentValue object used by both
     * addEvidence() and updateEvidence().
     * This reduces duplicated code and improves maintainability.
     */
    private ContentValues createEvidenceValues(String caseId,
                                               String item,
                                               String status,
                                               String location,
                                               String dateTime) {

        ContentValues values = new ContentValues();

        values.put(COLUMN_CASE_ID, caseId);
        values.put(COLUMN_ITEM, item);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_DATETIME, dateTime);

        return values;
    }

    // Add evidence to database
    public boolean addEvidence(String caseId,
                               String item,
                               String status,
                               String location,
                               String dateTime) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = createEvidenceValues(
                caseId,
                item,
                status,
                location,
                dateTime);

        long result = db.insert(TABLE_EVIDENCE, null, values);

        return result != -1;
    }
    // Check if evidence exists
    public boolean checkEvidenceExists(String caseId, String item) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection =
                COLUMN_CASE_ID + "=? COLLATE NOCASE AND " + COLUMN_ITEM + "=? COLLATE NOCASE";

        String[] selectionArgs = {
                caseId,
                item
        };

        try (Cursor cursor = db.query(
                TABLE_EVIDENCE,
                new String[]{COLUMN_EVIDENCE_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }
    private Cursor getEvidenceRecord(
            String caseId,
            String item) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_EVIDENCE,
                null,
                COLUMN_CASE_ID + "=? COLLATE NOCASE AND " +
                        COLUMN_ITEM + "=? COLLATE NOCASE",
                new String[]{caseId, item},
                null,
                null,
                null
        );
    }
    public long getEvidenceId(
            String caseId,
            String item) {

        SQLiteDatabase db = getReadableDatabase();

        long evidenceId = -1;

        try (Cursor cursor = db.query(
                TABLE_EVIDENCE,
                new String[]{COLUMN_EVIDENCE_ID},
                COLUMN_CASE_ID + "=? COLLATE NOCASE AND " +
                        COLUMN_ITEM + "=? COLLATE NOCASE",
                new String[]{caseId, item},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {

                evidenceId =
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_EVIDENCE_ID
                                )
                        );
            }
        }

        return evidenceId;
    }

    public long getAuditEvidenceId(String caseId, String item) {

        SQLiteDatabase db = getReadableDatabase();

        long evidenceId = -1;

        try (Cursor cursor = db.query(
                TABLE_AUDIT_LOG,
                new String[]{COLUMN_AUDIT_EVIDENCE_ID},
                COLUMN_AUDIT_CASE_ID + "=? COLLATE NOCASE AND " +
                        COLUMN_AUDIT_ITEM + "=? COLLATE NOCASE",
                new String[]{caseId.trim(), item.trim()},
                null,
                null,
                COLUMN_AUDIT_ID + " DESC",
                "1"
        )) {

            if (cursor.moveToFirst()) {
                evidenceId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_AUDIT_EVIDENCE_ID
                        )
                );
            }
        }

        return evidenceId;
    }

    // Update evidence in database
    public boolean updateEvidence(String oldCaseId,
                                  String oldItem,
                                  String newCaseId,
                                  String newItem,
                                  String status,
                                  String location,
                                  String dateTime,
                                  String performedBy) {

        SQLiteDatabase db = getWritableDatabase();

        // Retrieve the current record before updating
        String oldStatus = "";
        String oldLocation = "";

        try (Cursor cursor = getEvidenceRecord(oldCaseId, oldItem)) {

            if (cursor.moveToFirst()) {

                oldStatus = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_STATUS
                        )
                );

                oldLocation = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_LOCATION
                        )
                );
            }
        }

        // Create the updated values
        ContentValues values = createEvidenceValues(
                newCaseId,
                newItem,
                status,
                location,
                dateTime
        );

        // Update the database
        int result = db.update(
                TABLE_EVIDENCE,
                values,
                COLUMN_CASE_ID + "=? COLLATE NOCASE AND " + COLUMN_ITEM + "=?",
                new String[]{oldCaseId, oldItem}
        );

        // Record exactly what changed
        if (result > 0) {

            StringBuilder changes =
                    new StringBuilder("Evidence Updated");

            boolean valueChanged = false;

            if (!oldCaseId.equals(newCaseId)) {
                valueChanged = true;

                changes.append("\n\nCase ID")
                        .append("\n")
                        .append(oldCaseId)
                        .append(" → ")
                        .append(newCaseId);
            }

            if (!oldItem.equals(newItem)) {
                valueChanged = true;

                changes.append("\n\nItem")
                        .append("\n")
                        .append(oldItem)
                        .append(" → ")
                        .append(newItem);
            }

            if (!oldStatus.equals(status)) {
                valueChanged = true;

                changes.append("\n\nStatus")
                        .append("\n")
                        .append(oldStatus)
                        .append(" → ")
                        .append(status);
            }

            if (!oldLocation.equals(location)) {
                valueChanged = true;

                changes.append("\n\nLocation")
                        .append("\n")
                        .append(oldLocation)
                        .append(" → ")
                        .append(location);
            }

            if (!valueChanged) {
                changes.append("\n\nNo tracked fields changed.");
            }

            long evidenceId = getEvidenceId(
                    newCaseId,
                    newItem
            );

            logAudit(
                    newCaseId,
                    evidenceId,
                    newItem,
                    changes.toString(),
                    performedBy,
                    status,
                    location
            );
        }

        return result > 0;
    }
    // Get all evidence from database
    public List<EvidenceItem> getAllEvidence() {

        List<EvidenceItem> evidenceList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String orderBy =
                COLUMN_CASE_ID + " ASC, " +
                        COLUMN_ITEM + " ASC";

        try (Cursor cursor = db.query(
                TABLE_EVIDENCE,
                null,
                null,
                null,
                null,
                null,
                orderBy
        )) {

            while (cursor.moveToNext()) {

                String caseId = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_CASE_ID)
                );

                String item = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_ITEM)
                );

                String status = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_STATUS)
                );

                String location = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_LOCATION)
                );

                String dateTime = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_DATETIME)
                );

                EvidenceItem evidenceItem = new EvidenceItem(
                        caseId,
                        item,
                        status,
                        location,
                        dateTime
                );

                evidenceList.add(evidenceItem);
            }
        }

        return evidenceList;
    }
    // Get all evidence associated with a specific Case ID
    public Cursor getAuditEvidenceByCaseId(String caseId) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                true,   // DISTINCT
                TABLE_AUDIT_LOG,
                new String[]{
                        COLUMN_AUDIT_CASE_ID,
                        COLUMN_AUDIT_ITEM,
                        COLUMN_AUDIT_STATUS,
                        COLUMN_AUDIT_LOCATION
                },
                COLUMN_AUDIT_CASE_ID + "=? COLLATE NOCASE",
                new String[]{caseId},
                null,
                null,
                COLUMN_AUDIT_ITEM + " ASC",
                null
        );
    }
    // Delete evidence from database
    public boolean deleteEvidence(String caseId, String item, String performedBy) {
        SQLiteDatabase db = this.getWritableDatabase();

        long evidenceId = getEvidenceId(
                caseId,
                item
        );
        String status = "";
        String location = "";

        try (Cursor cursor = getEvidenceRecord(caseId, item)) {

            if (cursor.moveToFirst()) {

                status = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_STATUS
                        )
                );

                location = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_LOCATION
                        )
                );
            }
        }

        int result = db.delete(
                TABLE_EVIDENCE, COLUMN_CASE_ID + "=? COLLATE NOCASE AND " + COLUMN_ITEM + "=?",
                new String[]{caseId, item}
        );

        if (result > 0) {
            logAudit(
                    caseId,
                    evidenceId,
                    item,
                    "Evidence deleted",
                    performedBy,
                    status,
                    location
            );
        }
        return result > 0;
    }

    public boolean isEvidenceDeleted(
            String caseId,
            String item) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_AUDIT_LOG,
                new String[]{COLUMN_AUDIT_ACTION},
                COLUMN_AUDIT_CASE_ID + "=? COLLATE NOCASE AND " +
                        COLUMN_AUDIT_ITEM + "=?",
                new String[]{caseId, item},
                null,
                null,
                COLUMN_AUDIT_ID + " DESC",
                "1"
        )) {

            if (cursor.moveToFirst()) {

                String latestAction =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_AUDIT_ACTION
                                )
                        );

                return "Evidence deleted"
                        .equalsIgnoreCase(latestAction);
            }
        }

        return false;
    }
    // Audit log
    public boolean logAudit(String caseId,
                            long evidenceId,
                            String item,
                            String action,
                            String performedBy,
                            String status,
                            String location) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_AUDIT_CASE_ID, caseId);
        values.put(COLUMN_AUDIT_EVIDENCE_ID, evidenceId);
        values.put(COLUMN_AUDIT_ITEM, item);
        values.put(COLUMN_AUDIT_ACTION, action);
        values.put(COLUMN_AUDIT_PERFORMED_BY, performedBy);
        values.put(COLUMN_AUDIT_STATUS, status);
        values.put(COLUMN_AUDIT_LOCATION, location);

        String currentDateTime =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

        values.put(COLUMN_AUDIT_DATETIME, currentDateTime);

        long result =
                db.insert(TABLE_AUDIT_LOG, null, values);

        return result != -1;
    }
    // Get audit history for an evidence item
    public Cursor getAuditHistory(long evidenceId) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_AUDIT_LOG,
                null,
                COLUMN_AUDIT_EVIDENCE_ID + "=?",
                new String[]{
                        String.valueOf(evidenceId)
                },
                null,
                null,
                COLUMN_AUDIT_DATETIME + " DESC"
        );
    }

}
