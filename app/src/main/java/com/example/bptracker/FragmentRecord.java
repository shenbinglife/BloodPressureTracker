package com.example.bptracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class FragmentRecord extends Fragment implements RecordAdapter.OnRecordActionListener {
    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_EDIT = 2;

    private DatabaseHelper dbHelper;
    private RecordAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        dbHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditActivity.class);
            startActivityForResult(intent, REQUEST_ADD);
        });

        loadRecords();
        return view;
    }

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
        if (requestCode == REQUEST_ADD || requestCode == REQUEST_EDIT) {
            if (resultCode == getActivity().RESULT_OK) {
                loadRecords();
            }
        }
    }
}