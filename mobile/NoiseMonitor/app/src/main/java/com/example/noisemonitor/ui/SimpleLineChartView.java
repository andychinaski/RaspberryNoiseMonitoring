package com.example.noisemonitor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import com.example.noisemonitor.api.NoiseStats;

import java.util.Collections;
import java.util.List;

public class SimpleLineChartView extends View {

    private List<NoiseStats.NoisePoint> dataPoints = Collections.emptyList();
    private final Paint linePaint;
    private final Paint fillPaint;
    private final Path linePath;
    private final Path fillPath;

    public SimpleLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#336DFF"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5f);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        linePath = new Path();
        fillPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Create a gradient for the fill paint once the view has a size
        if (h > 0) {
            int blueColor = Color.parseColor("#336DFF");
            fillPaint.setShader(new LinearGradient(
                    0, 0, 0, h,
                    Color.argb(90, Color.red(blueColor), Color.green(blueColor), Color.blue(blueColor)),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            ));
        }
    }

    public void setData(List<NoiseStats.NoisePoint> points) {
        if (points == null) {
            this.dataPoints = Collections.emptyList();
        } else {
            this.dataPoints = points;
        }
        // Trigger a redraw
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints.isEmpty() || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        linePath.reset();
        fillPath.reset();

        float minNoise = Float.MAX_VALUE;
        float maxNoise = Float.MIN_VALUE;
        for (NoiseStats.NoisePoint point : dataPoints) {
            if (point.noiseLevel < minNoise) minNoise = point.noiseLevel;
            if (point.noiseLevel > maxNoise) maxNoise = point.noiseLevel;
        }

        float range = maxNoise - minNoise;
        if (range == 0) range = 10f; // Add some padding if all values are the same
        maxNoise += range * 0.1f;
        minNoise -= range * 0.1f;
        if (minNoise < 0) minNoise = 0;

        float valueRange = maxNoise - minNoise;
        if (valueRange == 0) return;

        int pointCount = dataPoints.size();
        float width = getWidth();
        float height = getHeight();
        float xStep = width / (pointCount > 1 ? pointCount - 1 : 1);

        for (int i = 0; i < pointCount; i++) {
            float x = i * xStep;
            float y = height - ((dataPoints.get(i).noiseLevel - minNoise) / valueRange * height);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, height);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            if (i == pointCount - 1) {
                fillPath.lineTo(x, height);
                fillPath.close();
            }
        }

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
}
