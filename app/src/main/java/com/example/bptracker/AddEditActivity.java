package com.example.bptracker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditActivity extends AppCompatActivity {
    private TextInputEditText etSystolic, etDiastolic, etHeartRate, etNote;
    private Button btnDate, btnTime, btnSave, btnDelete;
    private DatabaseHelper dbHelper;
    private long recordId = -1;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();

        etSystolic = findViewById(R.id.etSystolic);
        etDiastolic = findViewById(R.id.etDiastolic);
        etHeartRate = findViewById(R.id.etHeartRate);
        etNote = findViewById(R.id.etNote);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        recordId = getIntent().getLongExtra("record_id", -1);

        if (recordId != -1) {
            // Edit mode
            setTitle(R.string.edit_record);
            btnDelete.setVisibility(View.VISIBLE);
            loadRecordData();
        } else {
            setTitle(R.string.add_record);
            updateDateTimeButtons();
        }

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveRecord());
        btnDelete.setOnClickListener(v -> deleteRecord());
    }

    private void loadRecordData() {
        int systolic = getIntent().getIntExtra("systolic", 0);
        int diastolic = getIntent().getIntExtra("diastolic", 0);
        int heartRate = getIntent().getIntExtra("heart_rate", 0);
        String dateTime = getIntent().getStringExtra("date_time");
        String note = getIntent().getStringExtra("note");

        etSystolic.setText(String.valueOf(systolic));
        etDiastolic.setText(String.valueOf(diastolic));
        etHeartRate.setText(String.valueOf(heartRate));
        if (note != null && !note.isEmpty()) {
            etNote.setText(note);
        }

        if (dateTime != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                calendar.setTime(sdf.parse(dateTime));
            } catch (Exception e) {
                // ignore
            }
        }
        updateDateTimeButtons();
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeButtons();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateDateTimeButtons();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateDateTimeButtons() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        btnDate.setText(dateFmt.format(calendar.getTime()));
        btnTime.setText(timeFmt.format(calendar.getTime()));
    }

    private String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void saveRecord() {
        String systolicStr = etSystolic.getText().toString().trim();
        String diastolicStr = etDiastolic.getText().toString().trim();
        String heartRateStr = etHeartRate.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (systolicStr.isEmpty() || diastolicStr.isEmpty() || heartRateStr.isEmpty()) {
            Toast.makeText(this, R.string.input_error, Toast.LENGTH_SHORT).show();
            return;
        }

        int systolic = Integer.parseInt(systolicStr);
        int diastolic = Integer.parseInt(diastolicStr);
        int heartRate = Integer.parseInt(heartRateStr);

        if (systolic < 60 || systolic > 300) {
            Toast.makeText(this, R.string.systolic_range, Toast.LENGTH_SHORT).show();
            return;
        }
        if (diastolic < 30 || diastolic > 200) {
            Toast.makeText(this, R.string.diastolic_range, Toast.LENGTH_SHORT).show();
            return;
        }
        if (heartRate < 30 || heartRate > 250) {
            Toast.makeText(this, R.string.heart_rate_range, Toast.LENGTH_SHORT).show();
            return;
        }

        BloodPressureRecord record = new BloodPressureRecord();
        record.setSystolic(systolic);
        record.setDiastolic(diastolic);
        record.setHeartRate(heartRate);
        record.setDateTime(getFormattedDateTime());
        record.setNote(note);

        if (recordId != -1) {
            record.setId(recordId);
            dbHelper.updateRecord(record);
        } else {
            dbHelper.insertRecord(record);
        }

        setResult(RESULT_OK);
        finish();
    }

    private void deleteRecord() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.deleteRecord(recordId);
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}