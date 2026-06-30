package com.example.bptracker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddEditActivity extends AppCompatActivity {
    private TextInputEditText etSystolic, etDiastolic, etHeartRate, etNote;
    private Button btnDate, btnTime, btnSave, btnDelete, btnVoice;
    private DatabaseHelper dbHelper;
    private long recordId = -1;
    private Calendar calendar;
    private SpeechRecognizer speechRecognizer;

    private static final int REQUEST_SPEECH = 100;

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
        btnVoice = findViewById(R.id.btnVoice);

        recordId = getIntent().getLongExtra("record_id", -1);

        if (recordId != -1) {
            setTitle(R.string.edit_record);
            btnDelete.setVisibility(View.VISIBLE);
            btnVoice.setVisibility(View.GONE);
            loadRecordData();
        } else {
            setTitle(R.string.add_record);
            updateDateTimeButtons();
        }

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveRecord());
        btnDelete.setOnClickListener(v -> deleteRecord());
        btnVoice.setOnClickListener(v -> startVoiceRecognition());
    }

    private void startVoiceRecognition() {
        // Try SpeechRecognizer API directly (works even without Google app)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            startDirectRecognition();
            return;
        }

        // Fallback: try the intent-based approach
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出血压值，例如：高压120低压80心率72");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                startActivityForResult(intent, REQUEST_SPEECH);
                return;
            } catch (Exception e) {
                // ignore, fall through
            }
        }

        Toast.makeText(this, "此设备无语音识别服务\n\n请安装以下任一应用：\n• Google（谷歌搜索）\n• 讯飞语记\n• 百度输入法（含语音版）", Toast.LENGTH_LONG).show();
    }

    private void startDirectRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                btnVoice.setText("正在聆听...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                btnVoice.setText("语音输入血压");
            }

            @Override
            public void onError(int error) {
                btnVoice.setText("语音输入血压");
                String msg;
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK:
                        msg = "网络连接失败，请检查网络";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        msg = "网络超时，请重试";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        msg = "未识别到语音内容，请重试";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        msg = "未检测到语音输入";
                        break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        msg = "音频录制错误";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        msg = "语音服务异常，请重试";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        msg = "语音识别服务器错误";
                        break;
                    default:
                        msg = "识别失败（错误码: " + error + "）";
                        break;
                }
                Toast.makeText(AddEditActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                btnVoice.setText("语音输入血压");
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    parseVoiceResult(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(intent);
    }

    private void parseVoiceResult(String text) {
        if (text == null || text.isEmpty()) return;

        String normalized = text.replaceAll("\\s+", "").toLowerCase();

        int systolic = 0, diastolic = 0, heartRate = 0;

        systolic = extractNumber(normalized, "(高压|收缩压|高压值|sbp)\\s*[:：]?\\s*(\\d{2,3})");
        if (systolic == 0) systolic = extractNumber(normalized, "(高压|收缩压)\\D*(\\d{2,3})");

        diastolic = extractNumber(normalized, "(低压|舒张压|低压值|dbp)\\s*[:：]?\\s*(\\d{2,3})");
        if (diastolic == 0) diastolic = extractNumber(normalized, "(低压|舒张压)\\D*(\\d{2,3})");

        heartRate = extractNumber(normalized, "(心率|心跳|脉博|脉搏|hr)\\s*[:：]?\\s*(\\d{2,3})");
        if (heartRate == 0) heartRate = extractNumber(normalized, "(心率|心跳|脉搏)\\D*(\\d{2,3})");

        if (systolic == 0 || diastolic == 0) {
            ArrayList<Integer> nums = extractAllNumbers(normalized);
            if (nums.size() >= 2) {
                if (systolic == 0 && diastolic == 0) {
                    systolic = nums.get(0);
                    diastolic = nums.get(1);
                    if (heartRate == 0 && nums.size() >= 3) {
                        heartRate = nums.get(2);
                    }
                } else if (systolic == 0) {
                    systolic = nums.get(0);
                } else if (diastolic == 0) {
                    diastolic = nums.get(0);
                }
            }
        }

        boolean filled = false;
        if (systolic >= 60 && systolic <= 300) {
            etSystolic.setText(String.valueOf(systolic));
            filled = true;
        }
        if (diastolic >= 30 && diastolic <= 200) {
            etDiastolic.setText(String.valueOf(diastolic));
            filled = true;
        }
        if (heartRate >= 30 && heartRate <= 250) {
            etHeartRate.setText(String.valueOf(heartRate));
            filled = true;
        }

        if (filled) {
            Toast.makeText(this, "已识别: " + (systolic > 0 ? systolic + "/" : "") +
                    (diastolic > 0 ? diastolic : "") +
                    (heartRate > 0 ? " 心率" + heartRate : ""), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未识别到有效血压值，请重试\n识别内容: " + text, Toast.LENGTH_LONG).show();
        }
    }

    private int extractNumber(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(2));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private ArrayList<Integer> extractAllNumbers(String text) {
        ArrayList<Integer> nums = new ArrayList<>();
        Pattern p = Pattern.compile("\\d{2,3}");
        Matcher m = p.matcher(text);
        while (m.find()) {
            try {
                int val = Integer.parseInt(m.group());
                if (val >= 30 && val <= 300) {
                    nums.add(val);
                }
            } catch (NumberFormatException ignored) {}
        }
        return nums;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                parseVoiceResult(results.get(0));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
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