package com.example.noisemonitor.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.example.noisemonitor.NoiseEvent
import com.example.noisemonitor.R

class SimpleLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dataPoints: List<NoiseEvent> = emptyList()

    private val maxValue = 120f
    private val minValue = 0f

    // XML attrs
    private var showTitle = true

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.SimpleLineChartView)
            showTitle = ta.getBoolean(R.styleable.SimpleLineChartView_showTitle, true)
            ta.recycle()
        }
    }

    // Colors
    private val surfaceColor = ContextCompat.getColor(context, R.color.surface)
    private val primaryColor = ContextCompat.getColor(context, R.color.primary)
    private val gridColor = ContextCompat.getColor(context, R.color.divider)
    private val textColor = ContextCompat.getColor(context, R.color.text_secondary)

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = surfaceColor
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor
        style = Paint.Style.FILL
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = gridColor
        strokeWidth = 1f
        alpha = 70
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 28f
        textAlign = Paint.Align.RIGHT
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 32f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val linePath = Path()
    private val fillPath = Path()

    // paddings (твои)
    private val leftPadding = 74f
    private val rightPadding = 8f
    private val topPadding = 36f
    private val bottomPadding = 54f

    private val cornerRadius = 28f
    private val clipPath = Path()
    private val rectF = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        rectF.set(0f, 0f, w.toFloat(), h.toFloat())
        clipPath.reset()
        clipPath.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)

        fillPaint.shader = LinearGradient(
            0f,
            topPadding,
            0f,
            h.toFloat(),
            ColorUtils.setAlphaComponent(primaryColor, 110),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }

    fun setData(points: List<NoiseEvent>) {
        dataPoints = points
        invalidate()
    }

    fun clear() {
        dataPoints = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(clipPath)

        // Background (card)
        canvas.drawRect(rectF, backgroundPaint)

        // Размеры графика (БЕЗ учёта подписи)
        val chartTop = topPadding
        val chartBottom = height - bottomPadding - if (showTitle) 28f else 0f
        val chartHeight = chartBottom - chartTop
        val chartWidth = width - leftPadding - rightPadding

        // Grid + axis
        drawGrid(canvas, chartWidth, chartHeight, chartTop)
        drawYAxis(canvas, chartHeight, chartTop)

        // Chart
        if (dataPoints.isNotEmpty()) {
            drawChart(canvas, chartWidth, chartHeight, chartTop)
        }

        // Подпись ПОД графиком, слева
        if (showTitle) {
            canvas.drawText(
                context.getString(R.string.last_10_min_label),
                leftPadding,
                height - bottomPadding + 26f,
                titlePaint
            )
        }

        canvas.restore()
    }


    private fun drawGrid(canvas: Canvas, chartWidth: Float, chartHeight: Float, chartTop: Float) {
        val horizontalSteps = 4
        val verticalSteps = 10

        for (i in 0..horizontalSteps) {
            val y = chartTop + i * (chartHeight / horizontalSteps)
            canvas.drawLine(leftPadding, y, leftPadding + chartWidth, y, gridPaint)
        }

        for (i in 0..verticalSteps) {
            val x = leftPadding + i * (chartWidth / verticalSteps)
            canvas.drawLine(x, chartTop, x, chartTop + chartHeight, gridPaint)
        }
    }

    private fun drawYAxis(canvas: Canvas, chartHeight: Float, chartTop: Float) {
        val steps = 4
        val stepValue = maxValue / steps

        for (i in 0..steps) {
            val value = maxValue - i * stepValue
            val y = chartTop + i * (chartHeight / steps) + 10f
            canvas.drawText(value.toInt().toString(), leftPadding - 10f, y, labelPaint)
        }
    }

    private fun drawChart(canvas: Canvas, chartWidth: Float, chartHeight: Float, chartTop: Float) {
        linePath.reset()
        fillPath.reset()

        val count = dataPoints.size
        val stepX = if (count > 1) chartWidth / (count - 1) else 0f

        dataPoints.forEachIndexed { index, point ->
            val x = leftPadding + index * stepX
            val normalized = (point.noiseLevel - minValue) / (maxValue - minValue)
            val y = chartTop + chartHeight * (1f - normalized.coerceIn(0f, 1f))

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, chartTop + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (count == 1) {
                canvas.drawCircle(x, y, 6f, pointPaint)
            }
        }

        if (count > 1) {
            fillPath.lineTo(leftPadding + (count - 1) * stepX, chartTop + chartHeight)
            fillPath.close()
            canvas.drawPath(fillPath, fillPaint)
            canvas.drawPath(linePath, linePaint)
        }
    }
}
