package com.example.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import java.lang.Math.min
import kotlin.math.cos
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // position variable which will be used to draw label and indicator circle position
    private val m_pointPosition: PointF = PointF(0.0f, 0.0f)
    private var m_radius = 0.0f                   // Radius of the circle.
    private var m_fanSpeed = FanSpeed.OFF         // The active selection.

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var m_fanSpeedLowColor = 0
    private var m_fanSpeedMediumColor = 0
    private var m_fanSeedMaxColor = 0

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.DialView) {
            m_fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            m_fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            m_fanSeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        m_fanSpeed = m_fanSpeed.next()
        contentDescription = resources.getString(m_fanSpeed.label)

        // Forces onDraw to be called and re-draw the view
        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        m_radius = (min(w, h) / 2.0 * 0.8).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Set dial background color based on FanSpeed
        paint.color = when (m_fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> m_fanSpeedLowColor
            FanSpeed.MEDIUM -> m_fanSpeedMediumColor
            FanSpeed.HIGH -> m_fanSeedMaxColor
        } as Int

        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), m_radius, paint)

        // Draw speed indicator marks
        val markerRadius = m_radius + RADIUS_OFFSET_INDICATOR
        m_pointPosition.computeXYForSpeed(m_fanSpeed, markerRadius)
        paint.color = Color.WHITE
        canvas.drawCircle(m_pointPosition.x, m_pointPosition.y, m_radius / 12, paint)

        // Draw fan speed labels
        val labelRadius = m_radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            m_pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, m_pointPosition.x, m_pointPosition.y, paint)
        }
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // Angles are in radians
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)

        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }
}