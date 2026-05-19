package com.acry.penguinpurse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Summary> data = new ArrayList<>();
    private String currency = "$";

    public BarChartView(Context context) {
        super(context);
        setMinimumHeight(280);
    }

    public void setData(List<Summary> data, String currency) {
        this.data = data;
        this.currency = currency;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        paint.setColor(Color.rgb(255, 255, 255));
        canvas.drawRoundRect(0, 0, w, h, 26, 26, paint);
        if (data == null || data.isEmpty()) {
            paint.setColor(Color.rgb(95, 117, 130));
            paint.setTextSize(34);
            canvas.drawText("No data yet. Feed the penguin numbers.", 32, h / 2f, paint);
            return;
        }
        double max = 1;
        for (Summary s : data) max = Math.max(max, Math.max(s.income, s.expense));
        float left = 36, top = 50, bottom = h - 58;
        float group = (w - left - 24) / Math.max(1, data.size());
        paint.setTextSize(22);
        for (int i = 0; i < data.size(); i++) {
            Summary s = data.get(i);
            float x = left + i * group + 10;
            float bw = Math.max(12, group / 4);
            float incH = (float) ((s.income / max) * (bottom - top));
            float expH = (float) ((s.expense / max) * (bottom - top));
            paint.setColor(Color.rgb(88, 166, 207));
            canvas.drawRoundRect(x, bottom - incH, x + bw, bottom, 12, 12, paint);
            paint.setColor(Color.rgb(244, 171, 94));
            canvas.drawRoundRect(x + bw + 8, bottom - expH, x + bw * 2 + 8, bottom, 12, 12, paint);
            paint.setColor(Color.rgb(70, 86, 98));
            String label = s.label.length() > 5 ? s.label.substring(0, 5) : s.label;
            canvas.drawText(label, x, h - 22, paint);
        }
        paint.setTextSize(24);
        paint.setColor(Color.rgb(88, 166, 207));
        canvas.drawText("Income", 34, 30, paint);
        paint.setColor(Color.rgb(244, 171, 94));
        canvas.drawText("Expense   max " + currency + Math.round(max), 150, 30, paint);
    }
}
