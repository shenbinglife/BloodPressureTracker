package com.example.bptracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FragmentRecord extends Fragment implements RecordAdapter.OnRecordActionListener {
    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_EDIT = 2;
    private static final int REQUEST_RECORD_AUDIO = 3;
    private static final int REQUEST_VOICE_INPUT = 4;

    private DatabaseHelper dbHelper;
    private RecordAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd, fabVoice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        dbHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        fabAdd = view.findViewById(R.id.fabAdd);
        fabVoice = view.findViewById(R.id.fabVoice);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditActivity.class);
            startActivityForResult(intent, REQUEST_ADD);
        });

        fabVoice.setOnClickListener(v -> startVoiceRecognition());

        loadRecords();
        return view;
    }

    // ── Voice Recognition ──────────────────────────────────────────

    private void startVoiceRecognition() {
        // Android 6.0+ requires runtime permission for RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出血压值，例如：高压120低压80心率72");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            try {
                startActivityForResult(intent, REQUEST_VOICE_INPUT);
            } catch (Exception e) {
                Toast.makeText(getContext(), "无法启动语音识别: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "此设备无语音识别服务\n\n请安装 Google（谷歌搜索）或讯飞语记", Toast.LENGTH_LONG).show();
        }
    }

    private void parseAndCreateRecord(String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(getContext(), R.string.voice_parse_failed, Toast.LENGTH_LONG).show();
            return;
        }

        String normalized = text.replaceAll("\\s+", "").toLowerCase();

        int systolic = extractNumber(normalized, "(高压|收缩压|高压值|sbp)\\s*[:：]?\\s*(\\d{2,3})");
        if (systolic == 0) systolic = extractNumber(normalized, "(高压|收缩压)\\D*(\\d{2,3})");

        int diastolic = extractNumber(normalized, "(低压|舒张压|低压值|dbp)\\s*[:：]?\\s*(\\d{2,3})");
        if (diastolic == 0) diastolic = extractNumber(normalized, "(低压|舒张压)\\D*(\\d{2,3})");

        int heartRate = extractNumber(normalized, "(心率|心跳|脉博|脉搏|hr)\\s*[:：]?\\s*(\\d{2,3})");
        if (heartRate == 0) heartRate = extractNumber(normalized, "(心率|心跳|脉搏)\\D*(\\d{2,3})");

        if (systolic == 0 || diastolic == 0) {
            ArrayList<Integer> nums = extractAllNumbers(normalized);
            if (nums.size() >= 2) {
                systolic = nums.get(0);
                diastolic = nums.get(1);
                if (heartRate == 0 && nums.size() >= 3) heartRate = nums.get(2);
            }
        }

        if (systolic < 60 || systolic > 300 || diastolic < 30 || diastolic > 200) {
            Toast.makeText(getContext(), "语音识别失败，未识别到有效血压值\n识别内容: " + text, Toast.LENGTH_LONG).show();
            return;
        }
        if (heartRate < 30 || heartRate > 250) heartRate = 0;

        BloodPressureRecord record = new BloodPressureRecord();
        record.setSystolic(systolic);
        record.setDiastolic(diastolic);
        record.setHeartRate(heartRate);
        record.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
        record.setNote("");
        dbHelper.insertRecord(record);

        loadRecords();
        Toast.makeText(getContext(), "已创建: " + systolic + "/" + diastolic +
                (heartRate > 0 ? " 心率" + heartRate : ""), Toast.LENGTH_SHORT).show();
    }

    private int extractNumber(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(2)); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private ArrayList<Integer> extractAllNumbers(String text) {
        ArrayList<Integer> nums = new ArrayList<>();
        Matcher m = Pattern.compile("\\d{2,3}").matcher(text);
        while (m.find()) {
            int val = Integer.parseInt(m.group());
            if (val >= 30 && val <= 300) nums.add(val);
        }
        return nums;
    }

    // ── Record CRUD ────────────────────────────────────────────────

    void loadRecords() {
        List<BloodPressureRecord> records = dbHelper.getAllRecords();
        if (adapter == null) {
            adapter = new RecordAdapter(records, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setRecords(records);
        }
        updateEmptyView(records.isEmpty());
    }

    private void updateEmptyView(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEditClick(BloodPressureRecord record) {
        Intent intent = new Intent(getActivity(), AddEditActivity.class);
        intent.putExtra("record_id", record.getId());
        intent.putExtra("systolic", record.getSystolic());
        intent.putExtra("diastolic", record.getDiastolic());
        intent.putExtra("heart_rate", record.getHeartRate());
        intent.putExtra("date_time", record.getDateTime());
        intent.putExtra("note", record.getNote());
        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    public void onDeleteClick(BloodPressureRecord record) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.deleteRecord(record.getId());
                    loadRecords();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOICE_INPUT) {
            if (resultCode == getActivity().RESULT_OK && data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    parseAndCreateRecord(results.get(0));
                } else {
                    Toast.makeText(getContext(), "未识别到语音内容", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "语音识别取消或失败", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if ((requestCode == REQUEST_ADD || requestCode == REQUEST_EDIT) && resultCode == getActivity().RESULT_OK) {
            loadRecords();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition();
            } else {
                Toast.makeText(getContext(), "需要录音权限才能使用语音输入", Toast.LENGTH_SHORT).show();
            }
        }
    }
}