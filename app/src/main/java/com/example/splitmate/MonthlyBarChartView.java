package com.example.splitmate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lightweight dependency-free monthly bar chart.
 * It is intentionally simple so SplitMate does not need another chart library.
 */
public class MonthlyBarChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<String> labels = new ArrayList<>();
    private final List<Double> values = new ArrayList<>();
    private double maxValue = 1;

    private final int dark = 0xFF202124;
    private final int grey = 0xFF7A8288;
    private final int mint = 0xFF2F9D8F;
    private final int grid = 0xFFE4E9E8;
    private final int white = 0xFFFFFFFF;

    public MonthlyBarChartView(Context context) {
        super(context);
        init();
    }

    public MonthlyBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MonthlyBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setBackgroundColor(white);
    }

    public void setData(List<String> newLabels, List<Double> newValues) {
        labels.clear();
        values.clear();

        if (newLabels != null) labels.addAll(newLabels);
        if (newValues != null) values.addAll(newValues);

        maxValue = 1;
        for (Double value : values) {
            if (value != null && value > maxValue) {
                maxValue = value;
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float left = 42 * density;
        float right = 14 * density;
        float top = 22 * density;
        float bottom = 42 * density;

        float width = getWidth();
        float height = getHeight();

        float chartWidth = Math.max(1, width - left - right);
        float chartHeight = Math.max(1, height - top - bottom);

        paint.setStrokeWidth(1 * density);
        paint.setColor(grid);

        for (int i = 0; i <= 4; i++) {
            float y = top + chartHeight - (chartHeight * i / 4f);
            canvas.drawLine(left, y, width - right, y, paint);

            paint.setTextSize(10 * density);
            paint.setColor(grey);
            String value = compact(maxValue * i / 4.0);
            canvas.drawText(value, 5 * density, y + 4 * density, paint);
            paint.setColor(grid);
        }

        if (values.isEmpty()) {
            paint.setColor(grey);
            paint.setTextSize(14 * density);
            canvas.drawText(
                    "Add expenses to see your monthly graph",
                    left,
                    top + chartHeight / 2,
                    paint
            );
            return;
        }

        int count = values.size();
        float slot = chartWidth / Math.max(1, count);
        float barWidth = Math.min(34 * density, slot * 0.58f);

        for (int i = 0; i < count; i++) {
            double value = values.get(i) == null ? 0 : values.get(i);
            float barHeight = (float) ((value / maxValue) * (chartHeight - 3 * density));

            float centerX = left + slot * i + slot / 2f;
            float x = centerX - barWidth / 2f;
            float y = top + chartHeight - barHeight;

            paint.setColor(mint);
            RectF bar = new RectF(x, y, x + barWidth, top + chartHeight);
            canvas.drawRoundRect(bar, 8 * density, 8 * density, paint);

            paint.setColor(dark);
            paint.setTextSize(9 * density);
            String amount = compact(value);
            float amountWidth = paint.measureText(amount);
            canvas.drawText(amount, centerX - amountWidth / 2f,
                    Math.max(top + 11 * density, y - 5 * density), paint);

            paint.setColor(grey);
            paint.setTextSize(10 * density);
            String label = labels.get(i);
            float labelWidth = paint.measureText(label);
            canvas.drawText(label, centerX - labelWidth / 2f,
                    height - 14 * density, paint);
        }
    }

    private String compact(double amount) {
        if (amount >= 100000) {
            return String.format(Locale.ENGLISH, "%.1fL", amount / 100000.0);
        }
        if (amount >= 1000) {
            return String.format(Locale.ENGLISH, "%.1fk", amount / 1000.0);
        }
        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }
        return String.format(Locale.ENGLISH, "%.0f", amount);
    }
}
