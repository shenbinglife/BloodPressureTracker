package com.example.bptracker;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<BloodPressureRecord> records;
    private OnRecordActionListener listener;

    public interface OnRecordActionListener {
        void onEditClick(BloodPressureRecord record);
        void onDeleteClick(BloodPressureRecord record);
    }

    public RecordAdapter(List<BloodPressureRecord> records, OnRecordActionListener listener) {
        this.records = records;
        this.listener = listener;
    }

    public void setRecords(List<BloodPressureRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodPressureRecord record = records.get(position);
        holder.tvDateTime.setText(record.getDateTime());
        holder.tvSystolic.setText(String.valueOf(record.getSystolic()));
        holder.tvDiastolic.setText(String.valueOf(record.getDiastolic()));
        holder.tvHeartRate.setText(String.valueOf(record.getHeartRate()));

        int level = BPLevel.getLevel(record.getSystolic(), record.getDiastolic());
        holder.tvLevel.setText(BPLevel.getLevelName(level));
        int levelColor = BPLevel.getLevelColor(level);
        GradientDrawable bg = (GradientDrawable) holder.tvLevel.getBackground();
        bg.setColor(levelColor);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(record);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(record);
        });
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvSystolic, tvDiastolic, tvHeartRate, tvLevel;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvSystolic = itemView.findViewById(R.id.tvSystolic);
            tvDiastolic = itemView.findViewById(R.id.tvDiastolic);
            tvHeartRate = itemView.findViewById(R.id.tvHeartRate);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnItemDelete);
        }
    }
}