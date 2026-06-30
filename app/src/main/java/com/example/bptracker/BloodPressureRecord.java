package com.example.bptracker;

public class BloodPressureRecord {
    private long id;
    private int systolic;
    private int diastolic;
    private int heartRate;
    private String dateTime;
    private String note;

    public BloodPressureRecord() {}

    public BloodPressureRecord(long id, int systolic, int diastolic, int heartRate, String dateTime, String note) {
        this.id = id;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.heartRate = heartRate;
        this.dateTime = dateTime;
        this.note = note;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getSystolic() { return systolic; }
    public void setSystolic(int systolic) { this.systolic = systolic; }

    public int getDiastolic() { return diastolic; }
    public void setDiastolic(int diastolic) { this.diastolic = diastolic; }

    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}