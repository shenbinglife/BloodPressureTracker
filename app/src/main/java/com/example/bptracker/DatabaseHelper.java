package com.example.bptracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "blood_pressure.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "records";
    private static final String COL_ID = "_id";
    private static final String COL_SYSTOLIC = "systolic";
    private static final String COL_DIASTOLIC = "diastolic";
    private static final String COL_HEART_RATE = "heart_rate";
    private static final String COL_DATE_TIME = "date_time";
    private static final String COL_NOTE = "note";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SYSTOLIC + " INTEGER NOT NULL, "
                + COL_DIASTOLIC + " INTEGER NOT NULL, "
                + COL_HEART_RATE + " INTEGER NOT NULL, "
                + COL_DATE_TIME + " TEXT NOT NULL, "
                + COL_NOTE + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertRecord(BloodPressureRecord record) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYSTOLIC, record.getSystolic());
        values.put(COL_DIASTOLIC, record.getDiastolic());
        values.put(COL_HEART_RATE, record.getHeartRate());
        values.put(COL_DATE_TIME, record.getDateTime());
        values.put(COL_NOTE, record.getNote());
        return db.insert(TABLE_NAME, null, values);
    }

    public int updateRecord(BloodPressureRecord record) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYSTOLIC, record.getSystolic());
        values.put(COL_DIASTOLIC, record.getDiastolic());
        values.put(COL_HEART_RATE, record.getHeartRate());
        values.put(COL_DATE_TIME, record.getDateTime());
        values.put(COL_NOTE, record.getNote());
        return db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(record.getId())});
    }

    public int deleteRecord(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<BloodPressureRecord> getAllRecords() {
        return getRecordsInRange(null, null);
    }

    public List<BloodPressureRecord> getRecordsInRange(String startDate, String endDate) {
        List<BloodPressureRecord> records = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;
        if (startDate != null && endDate != null) {
            selection = COL_DATE_TIME + " >= ? AND " + COL_DATE_TIME + " <= ?";
            selectionArgs = new String[]{startDate, endDate};
        }
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, COL_DATE_TIME + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                records.add(readRecord(cursor));
            }
            cursor.close();
        }
        return records;
    }

    public BloodPressureRecord getRecordById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                BloodPressureRecord r = readRecord(cursor);
                cursor.close();
                return r;
            }
            cursor.close();
        }
        return null;
    }

    public boolean hasRecordToday() {
        SQLiteDatabase db = getReadableDatabase();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, COL_DATE_TIME + " LIKE ?", new String[]{today + "%"}, null, null, null, "1");
        boolean has = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return has;
    }

    public int getRecordCount(String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;
        if (startDate != null && endDate != null) {
            selection = COL_DATE_TIME + " >= ? AND " + COL_DATE_TIME + " <= ?";
            selectionArgs = new String[]{startDate, endDate};
        }
        Cursor cursor = db.query(TABLE_NAME, new String[]{"COUNT(*)"}, selection, selectionArgs, null, null, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public float[] getAverages(String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;
        if (startDate != null && endDate != null) {
            selection = COL_DATE_TIME + " >= ? AND " + COL_DATE_TIME + " <= ?";
            selectionArgs = new String[]{startDate, endDate};
        }
        Cursor cursor = db.query(TABLE_NAME, new String[]{"AVG(" + COL_SYSTOLIC + ")", "AVG(" + COL_DIASTOLIC + ")", "AVG(" + COL_HEART_RATE + ")"}, selection, selectionArgs, null, null, null);
        float[] avgs = new float[]{0, 0, 0};
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getColumnCount() >= 3) {
                avgs[0] = cursor.getFloat(0);
                avgs[1] = cursor.getFloat(1);
                avgs[2] = cursor.getFloat(2);
            }
            cursor.close();
        }
        return avgs;
    }

    public int[] getDistribution(String startDate, String endDate) {
        int[] dist = new int[5]; // normal, elevated, stage1, stage2, crisis
        List<BloodPressureRecord> records = getRecordsInRange(startDate, endDate);
        for (BloodPressureRecord r : records) {
            int level = BPLevel.getLevel(r.getSystolic(), r.getDiastolic());
            if (level >= 0 && level < 5) dist[level]++;
        }
        return dist;
    }

    private BloodPressureRecord readRecord(Cursor cursor) {
        BloodPressureRecord record = new BloodPressureRecord();
        record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        record.setSystolic(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SYSTOLIC)));
        record.setDiastolic(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DIASTOLIC)));
        record.setHeartRate(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HEART_RATE)));
        record.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_TIME)));
        record.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE)));
        return record;
    }
}