package com.zybooks.evidencetracker;

import android.content.Context;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

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
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String TABLE_EVIDENCE = "evidence";
    public static final String COLUMN_EVIDENCE_ID = "evidenceId";
    public static final String COLUMN_CASE_ID = "caseId";
    public static final String COLUMN_ITEM = "item";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATETIME = "dateTime";


    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    // Create tables
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_USERNAME +
                " TEXT, " + COLUMN_PASSWORD + " TEXT)";

        //Create evidence table
        String createEvidence = "CREATE TABLE " + TABLE_EVIDENCE + " (" +
                COLUMN_EVIDENCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CASE_ID + " TEXT, " +
                COLUMN_ITEM + " TEXT, " +
                COLUMN_STATUS + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_DATETIME + " TEXT)";

        db.execSQL(createTable);
        db.execSQL(createEvidence);
    }
    @Override
    // Upgrade database
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVIDENCE);
        onCreate(db);
    }
    // Add user to database
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }
    // Check if user exists
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE username=? AND password=?",
                new String[]{username, password}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    // Check if username exists
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE username=?",
                new String[]{username}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    // Add evidence to database
    public boolean addEvidence(String caseId, String item, String status, String location, String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CASE_ID, caseId);
        values.put(COLUMN_ITEM, item);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_DATETIME, dateTime);
        long result = db.insert(TABLE_EVIDENCE, null, values);
        return result != -1;
    }
    // Check if evidence exists
    public boolean checkEvidenceExists(String caseId, String item) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_EVIDENCE +
                        " WHERE " + COLUMN_CASE_ID + "=? AND " + COLUMN_ITEM + "=?",
                new String[]{caseId, item}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    // Update evidence in database
    public boolean updateEvidence(String oldCaseId, String oldItem, String newCaseId, String newItem,
                                  String status, String location, String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CASE_ID, newCaseId);
        values.put(COLUMN_ITEM, newItem);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_DATETIME, dateTime);
        int result = db.update(
                TABLE_EVIDENCE, values, COLUMN_CASE_ID + "=? AND " + COLUMN_ITEM + "=?",
                new String[]{oldCaseId, oldItem});
        return result > 0;
    }
    // Get all evidence from database
    public List<EvidenceItem> getAllEvidence() {
        List<EvidenceItem> evidenceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVIDENCE, null);

        // Iterate through cursor and add evidence to list
        if (cursor.moveToFirst()) {
            do {
                String caseId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CASE_ID));
                String item = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME));

                EvidenceItem evidenceItem = new EvidenceItem(caseId, item, status, location, dateTime);
                evidenceList.add(evidenceItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return evidenceList;
    }
    // Delete evidence from database
    public boolean deleteEvidence(String caseId, String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                TABLE_EVIDENCE, COLUMN_CASE_ID + "=? AND " + COLUMN_ITEM + "=?",
                new String[]{caseId, item}
        );
        return result > 0;
    }

}
