package com.example.bptracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

public class BPChartView extends View {
    private Paint linePaint, pointPaint, textPaint, gridPaint, fillPaint;
    private List<BloodPressureRecord> records;
    private int minVal = 40, maxVal = 200;

    public BPChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0xFFE0E0E0);
        gridPaint.setStrokeWidth(1f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF999999);
        textPaint.setTextSize(24f);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<BloodPressureRecord> records) {
        this.records = records;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int paddingLeft = 80, paddingRight = 40, paddingTop = 20, paddingBottom = 50;
        int chartW = w - paddingLeft - paddingRight;
        int chartH = h - paddingTop - paddingBottom;

        if (chartW <= 0 || chartH <= 0) return;

        // Draw background
        Paint bgPaint = new Paint();
        bgPaint.setColor(0xFFFFFFFF);
        canvas.drawRect(paddingLeft, paddingTop, paddingLeft + chartW, paddingTop + chartH, bgPaint);

        // Draw grid lines
        for (int i = 0; i <= 4; i++) {
            int y = paddingTop + chartH * i / 4;
            canvas.drawLine(paddingLeft, y, paddingLeft + chartW, y, gridPaint);
            String label = String.valueOf(maxVal - (maxVal - minVal) * i / 4);
            canvas.drawText(label, paddingLeft - textPaint.measureText(label) - 8, y + 8, textPaint);
        }

        if (records == null || records.isEmpty()) {
            textPaint.setColor(0xFF999999);
            String msg = getContext().getString(R.string.no_data);
            canvas.drawText(msg, paddingLeft + chartW / 2f - textPaint.measureText(msg) / 2, paddingTop + chartH / 2f, textPaint);
            return;
        }

        int n = records.size();
        if (n < 2) {
            textPaint.setColor(0xFF999999);
            String msg = getContext().getString(R.string.no_data);
            canvas.drawText(msg, paddingLeft + chartW / 2f - textPaint.measureText(msg) / 2, paddingTop + chartH / 2f, textPaint);
            return;
        }

        float[] xs = new float[n];
        float[] ysSys = new float[n];
        float[] ysDia = new float[n];

        for (int i = 0; i < n; i++) {
            // Reverse order so oldest is left
            int idx = n - 1 - i;
            BloodPressureRecord r = records.get(idx);
            xs[i] = paddingLeft + chartW * i / (float)(n - 1);
            ysSys[i] = paddingTop + chartH * (1 - (r.getSystolic() - minVal) / (float)(maxVal - minVal));
            ysDia[i] = paddingTop + chartH * (1 - (r.getDiastolic() - minVal) / (float)(maxVal - minVal));
        }

        // Draw systolic line
        drawLine(canvas, xs, ysSys, 0xFFE53935, 0x33E53935);
        // Draw diastolic line
        drawLine(canvas, xs, ysDia, 0xFF1E88E5, 0x331E88E5);

        // Draw date labels
        textPaint.setColor(0xFF999999);
        textPaint.setTextSize(20f);
        for (int i = 0; i < n; i += Math.max(1, n / 5)) {
            int idx = n - 1 - i;
            String date = records.get(idx).getDateTime();
            if (date.length() >= 10) date = date.substring(5, 10);
            float tx = xs[i] - textPaint.measureText(date) / 2;
            canvas.drawText(date, tx, paddingTop + chartH + 35, textPaint);
        }

        // Legend
        textPaint.setTextSize(24f);
        Paint legPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legPaint.setStyle(Paint.Style.FILL);

        legPaint.setColor(0xFFE53935);
        canvas.drawRect(paddingLeft + 10, paddingTop + 5, paddingLeft + 30, paddingTop + 20, legPaint);
        textPaint.setColor(0xFF333333);
        canvas.drawText("收缩压", paddingLeft + 38, paddingTop + 20, textPaint);

        legPaint.setColor(0xFF1E88E5);
        canvas.drawRect(paddingLeft + 120, paddingTop + 5, paddingLeft + 140, paddingTop + 20, legPaint);
        canvas.drawText("舒张压", paddingLeft + 148, paddingTop + 20, textPaint);
    }

    private void drawLine(Canvas canvas, float[] xs, float[] ys, int color, int fillColor) {
        Path path = new Path();
        path.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++) {
            path.lineTo(xs[i], ys[i]);
        }

        linePaint.setColor(color);
        canvas.drawPath(path, linePaint);

        // Fill under line
        Path fillPath = new Path(path);
        fillPath.lineTo(xs[xs.length - 1], getHeight() - 50);
        fillPath.lineTo(xs[0], getHeight() - 50);
        fillPath.close();
        fillPaint.setColor(fillColor);
        canvas.drawPath(fillPath, fillPaint);

        // Draw points
        pointPaint.setColor(color);
        for (int i = 0; i < xs.length; i++) {
            canvas.drawCircle(xs[i], ys[i], 4, pointPaint);
        }
    }
}