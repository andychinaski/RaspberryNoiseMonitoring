package com.example.noisemonitor.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.example.noisemonitor.NoiseEvent  // Импорт из вашего NoiseStats.kt (или замените на NoisePoint)

class SimpleLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dataPoints: List<NoiseEvent> = emptyList()  // Используем ваш NoiseEvent (noiseLevel)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#336DFF")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val linePath = Path()
    private val fillPath = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h > 0) {
            val blueColor = Color.parseColor("#336DFF")
            fillPaint.shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                Color.argb(90, Color.red(blueColor), Color.green(blueColor), Color.blue(blueColor)),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
    }

    fun setData(points: List<NoiseEvent>) {
        dataPoints = points ?: emptyList()
        invalidate()  // Перерисовка
    }

    fun clear() {
        dataPoints = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty() || width == 0 || height == 0) return

        linePath.reset()
        fillPath.reset()

        var minNoise = Float.MAX_VALUE
        var maxNoise = Float.MIN_VALUE
        dataPoints.forEach { point ->
            val level = point.noiseLevel.toFloat()
            if (level < minNoise) minNoise = level
            if (level > maxNoise) maxNoise = level
        }

        var range = maxNoise - minNoise
        if (range == 0f) range = 10f
        maxNoise += range * 0.1f
        minNoise -= range * 0.1f
        if (minNoise < 0) minNoise = 0f

        val valueRange = maxNoise - minNoise
        if (valueRange == 0f) return

        val pointCount = dataPoints.size
        val width = width.toFloat()
        val height = height.toFloat()
        val xStep = width / if (pointCount > 1) (pointCount - 1) else 1

        dataPoints.forEachIndexed { i, point ->
            val x = i * xStep
            val y = height - ((point.noiseLevel.toFloat() - minNoise) / valueRange * height)

            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (i == pointCount - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)
    }
}