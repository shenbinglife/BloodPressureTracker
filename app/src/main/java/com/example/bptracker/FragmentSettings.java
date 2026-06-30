package com.example.bptracker;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentSettings extends Fragment {
    private static final int REQUEST_EXPORT = 100;
    private static final int REQUEST_IMPORT = 101;

    private SwitchCompat switchNotification;
    private Button btnNotificationTime, btnExport, btnImport, btnBPStandard;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        dbHelper = new DatabaseHelper(getContext());

        switchNotification = view.findViewById(R.id.switchNotification);
        btnNotificationTime = view.findViewById(R.id.btnNotificationTime);
        btnExport = view.findViewById(R.id.btnExport);
        btnImport = view.findViewById(R.id.btnImport);
        btnBPStandard = view.findViewById(R.id.btnBPStandard);

        boolean enabled = NotificationHelper.isEnabled(getContext());
        switchNotification.setChecked(enabled);
        btnNotificationTime.setEnabled(enabled);
        updateTimeButton();

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setEnabled(getContext(), isChecked);
            btnNotificationTime.setEnabled(isChecked);
        });

        btnNotificationTime.setOnClickListener(v -> {
            int hour = NotificationHelper.getHour(getContext());
            int minute = NotificationHelper.getMinute(getContext());
            new TimePickerDialog(getContext(), (view1, h, m) -> {
                NotificationHelper.setTime(getContext(), h, m);
                updateTimeButton();
            }, hour, minute, true).show();
        });

        btnExport.setOnClickListener(v -> {
            String fileName = "BloodPressure_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            startActivityForResult(intent, REQUEST_EXPORT);
        });

        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            startActivityForResult(intent, REQUEST_IMPORT);
        });

        btnBPStandard.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), BPStandardActivity.class));
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();

        if (requestCode == REQUEST_EXPORT) {
            try {
                OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                if (os != null) {
                    CSVHelper.exportCSV(dbHelper, os);
                    os.close();
                    Toast.makeText(getContext(), R.string.export_success, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), R.string.export_failed, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.export_failed, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_IMPORT) {
            try {
                InputStream is = getContext().getContentResolver().openInputStream(uri);
                if (is != null) {
                    int count = CSVHelper.importCSV(dbHelper, is);
                    is.close();
                    Toast.makeText(getContext(), getString(R.string.import_success) + " (" + count + " 条)", Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshRecords();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.import_failed, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.import_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateTimeButton() {
        int hour = NotificationHelper.getHour(getContext());
        int minute = NotificationHelper.getMinute(getContext());
        btnNotificationTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }
}