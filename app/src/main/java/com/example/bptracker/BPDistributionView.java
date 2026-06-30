package com.example.bptracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BPDistributionView extends View {
    private int[] distribution;
    private int total;
    private Paint barPaint, textPaint, labelPaint;
    private String[] labels = {"正常", "正常高值", "1级", "2级", "3级"};
    private int[] colors = {0xFF4CAF50, 0xFFFFC107, 0xFFFF9800, 0xFFF44336, 0xFFB71C1C};

    public BPDistributionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF333333);
        textPaint.setTextSize(28f);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(0xFF666666);
        labelPaint.setTextSize(22f);
    }

    public void setData(int[] distribution) {
        this.distribution = distribution;
        this.total = 0;
        for (int d : distribution) total += d;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int paddingLeft = 80, paddingRight = 40, paddingTop = 20, paddingBottom = 30;
        int chartW = w - paddingLeft - paddingRight;

        if (total == 0 || distribution == null) {
            textPaint.setColor(0xFF999999);
            String msg = getContext().getString(R.string.no_data);
            canvas.drawText(msg, w / 2f - textPaint.measureText(msg) / 2, h / 2f, textPaint);
            return;
        }

        int barCount = distribution.length;
        float barWidth = chartW / (float) barCount * 0.6f;
        float gap = chartW / (float) barCount * 0.4f;
        int maxVal = 0;
        for (int d : distribution) if (d > maxVal) maxVal = d;
        if (maxVal == 0) maxVal = 1;

        int chartH = h - paddingTop - paddingBottom;

        for (int i = 0; i < barCount; i++) {
            float left = paddingLeft + i * (barWidth + gap);
            float barHeight = distribution[i] / (float) maxVal * chartH;
            float top = paddingTop + chartH - barHeight;

            barPaint.setColor(colors[i]);
            canvas.drawRect(left, top, left + barWidth, paddingTop + chartH, barPaint);

            // Count label on top
            String countStr = String.valueOf(distribution[i]);
            textPaint.setColor(colors[i]);
            canvas.drawText(countStr, left + barWidth / 2 - textPaint.measureText(countStr) / 2, top - 6, textPaint);

            // Label below
            labelPaint.setColor(0xFF666666);
            canvas.drawText(labels[i], left + barWidth / 2 - labelPaint.measureText(labels[i]) / 2, paddingTop + chartH + 24, labelPaint);
        }
    }
}