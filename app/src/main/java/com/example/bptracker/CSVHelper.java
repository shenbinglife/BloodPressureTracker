package com.example.bptracker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CSVHelper {

    public static void exportCSV(DatabaseHelper dbHelper, OutputStream outputStream) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write("\uFEFF");
        writer.write("日期时间,收缩压(mmHg),舒张压(mmHg),心率(bpm),备注\n");

        List<BloodPressureRecord> records = dbHelper.getAllRecords();
        for (BloodPressureRecord r : records) {
            writer.write(String.format(Locale.getDefault(), "%s,%d,%d,%d,\"%s\"\n",
                    r.getDateTime(), r.getSystolic(), r.getDiastolic(), r.getHeartRate(),
                    r.getNote() != null ? r.getNote().replace("\"", "\"\"") : ""));
        }
        writer.close();
    }

    public static int importCSV(DatabaseHelper dbHelper, InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        int count = 0;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            if (line.startsWith("\uFEFF")) line = line.substring(1);
            if (firstLine) { firstLine = false; continue; }
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                BloodPressureRecord record = new BloodPressureRecord();
                record.setDateTime(parts[0].trim());
                record.setSystolic(Integer.parseInt(parts[1].trim()));
                record.setDiastolic(Integer.parseInt(parts[2].trim()));
                record.setHeartRate(Integer.parseInt(parts[3].trim()));
                if (parts.length >= 5) {
                    String note = parts[4].trim();
                    if (note.startsWith("\"") && note.endsWith("\"")) note = note.substring(1, note.length() - 1);
                    record.setNote(note);
                }
                dbHelper.insertRecord(record);
                count++;
            }
        }
        reader.close();
        return count;
    }
}