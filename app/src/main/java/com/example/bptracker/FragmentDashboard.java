package com.example.bptracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FragmentDashboard extends Fragment {
    private DatabaseHelper dbHelper;
    private TextView tvAvgSystolic, tvAvgDiastolic, tvAvgHeartRate, tvTotalRecords;
    private BPChartView chartTrend;
    private BPDistributionView chartDistribution;
    private Button btnWeek, btnMonth, btnQuarter, btnAll;
    private Button activeBtn;
    private String startDate, endDate;

    private static final int RANGE_WEEK = 0;
    private static final int RANGE_MONTH = 1;
    private static final int RANGE_QUARTER = 2;
    private static final int RANGE_ALL = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        dbHelper = new DatabaseHelper(getContext());

        tvAvgSystolic = view.findViewById(R.id.tvAvgSystolic);
        tvAvgDiastolic = view.findViewById(R.id.tvAvgDiastolic);
        tvAvgHeartRate = view.findViewById(R.id.tvAvgHeartRate);
        tvTotalRecords = view.findViewById(R.id.tvTotalRecords);
        chartTrend = view.findViewById(R.id.chartTrend);
        chartDistribution = view.findViewById(R.id.chartDistribution);

        btnWeek = view.findViewById(R.id.btnWeek);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnQuarter = view.findViewById(R.id.btnQuarter);
        btnAll = view.findViewById(R.id.btnAll);

        btnWeek.setOnClickListener(v -> selectRange(RANGE_WEEK, btnWeek));
        btnMonth.setOnClickListener(v -> selectRange(RANGE_MONTH, btnMonth));
        btnQuarter.setOnClickListener(v -> selectRange(RANGE_QUARTER, btnQuarter));
        btnAll.setOnClickListener(v -> selectRange(RANGE_ALL, btnAll));

        // Default: select "最近一周"
        selectRange(RANGE_WEEK, btnWeek);
        return view;
    }

    private void selectRange(int range, Button btn) {
        if (activeBtn != null) {
            activeBtn.setTextColor(0xFF666666);
        }
        activeBtn = btn;
        activeBtn.setTextColor(0xFF1976D2);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        endDate = sdf.format(cal.getTime()) + " 23:59";

        switch (range) {
            case RANGE_WEEK:
                cal.add(Calendar.DAY_OF_MONTH, -7);
                break;
            case RANGE_MONTH:
                cal.add(Calendar.MONTH, -1);
                break;
            case RANGE_QUARTER:
                cal.add(Calendar.MONTH, -3);
                break;
            case RANGE_ALL:
                startDate = null;
                endDate = null;
                updateDashboard();
                return;
        }
        startDate = sdf.format(cal.getTime()) + " 00:00";
        updateDashboard();
    }

    private void updateDashboard() {
        float[] avgs = dbHelper.getAverages(startDate, endDate);
        int count = dbHelper.getRecordCount(startDate, endDate);
        List<BloodPressureRecord> records = dbHelper.getRecordsInRange(startDate, endDate);
        int[] distribution = dbHelper.getDistribution(startDate, endDate);

        tvAvgSystolic.setText(avgs[0] > 0 ? String.valueOf(Math.round(avgs[0])) : "--");
        tvAvgDiastolic.setText(avgs[1] > 0 ? String.valueOf(Math.round(avgs[1])) : "--");
        tvAvgHeartRate.setText(avgs[2] > 0 ? String.valueOf(Math.round(avgs[2])) : "--");
        tvTotalRecords.setText(String.valueOf(count));

        chartTrend.setData(records);
        chartDistribution.setData(distribution);
    }
}