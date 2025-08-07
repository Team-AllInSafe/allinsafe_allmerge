package de.blinkt.openvpn.ac0_patternpin

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class PatternLockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dots = mutableListOf<Dot>()
    private val selectedDots = mutableListOf<Int>()
    private val lines = mutableListOf<Line>()

    private var currentX = 0f
    private var currentY = 0f
    private var isDrawing = false

    private var onPatternListener: OnPatternListener? = null

    // 색상 및 크기 상수
    private val dotRadius = 60f
    private val selectedDotRadius = 80f
    private val lineWidth = 8f
    private val dotColor = 0xFFEEE5D3.toInt()      // Color.GRAY 대신
    private val selectedDotColor = 0xFF44BBA4.toInt() // Color.BLUE 대신
    private val lineColor = 0xFFEEE5D3.toInt()        // Color.BLUE 대신

    init {
        paint.color = dotColor
        paint.style = Paint.Style.FILL

        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = lineWidth
        linePaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupDots()
    }

    private fun setupDots() {
        dots.clear()
        val centerX = width / 2f
        val centerY = height / 2f
        val spacing = min(width, height) / 5f

        // 4x4 격자로 16개 점 생성
        for (row in 0 until 4) {
            for (col in 0 until 4) {
                val x = centerX + (col - 1.5f) * spacing
                val y = centerY + (row - 1.5f) * spacing
                dots.add(Dot(x, y, row * 4 + col))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 연결선 그리기
        for (line in lines) {
            canvas.drawLine(line.startX, line.startY, line.endX, line.endY, linePaint)
        }

        // 현재 드래그 중인 선 그리기
        if (isDrawing && selectedDots.isNotEmpty()) {
            val lastDot = dots[selectedDots.last()]
            canvas.drawLine(lastDot.x, lastDot.y, currentX, currentY, linePaint)
        }

        // 점들 그리기
        for (i in dots.indices) {
            val dot = dots[i]
            val isSelected = selectedDots.contains(i)

            paint.color = if (isSelected) selectedDotColor else dotColor
            val radius = if (isSelected) selectedDotRadius else dotRadius

            canvas.drawCircle(dot.x, dot.y, radius, paint)

            // 선택된 점에 순서 번호 표시
            if (isSelected) {
                val orderIndex = selectedDots.indexOf(i) + 1
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                textPaint.color = 0xFFEEE5D3.toInt() // ui_beige
                textPaint.textSize = 80f
                textPaint.textAlign = Paint.Align.CENTER

                val textY = dot.y + (textPaint.textSize / 3)
                canvas.drawText(orderIndex.toString(), dot.x, textY, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentX = event.x
                currentY = event.y
                isDrawing = true

                // 터치된 점 찾기
                val touchedDot = findTouchedDot(event.x, event.y)
                if (touchedDot != -1) {
                    selectedDots.add(touchedDot)
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDrawing) {
                    currentX = event.x
                    currentY = event.y

                    // 드래그 중 지나가는 점 찾기
                    val touchedDot = findTouchedDot(event.x, event.y)
                    if (touchedDot != -1 && !selectedDots.contains(touchedDot)) {
                        // 이전 점과 현재 점 사이에 선 추가
                        if (selectedDots.isNotEmpty()) {
                            val lastDot = dots[selectedDots.last()]
                            val currentDot = dots[touchedDot]
                            lines.add(Line(lastDot.x, lastDot.y, currentDot.x, currentDot.y))
                        }
                        selectedDots.add(touchedDot)
                    }

                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                isDrawing = false

                // 마지막 선 추가
                if (selectedDots.size > 1) {
                    val lastDot = dots[selectedDots[selectedDots.size - 2]]
                    val currentDot = dots[selectedDots.last()]
                    lines.add(Line(lastDot.x, lastDot.y, currentDot.x, currentDot.y))
                }

                // 패턴 완성 알림
                if (selectedDots.size >= 2) {
                    onPatternListener?.onPatternComplete(selectedDots.toList())
                }

                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findTouchedDot(x: Float, y: Float): Int {
        for (i in dots.indices) {
            val dot = dots[i]
            val distance = sqrt((x - dot.x).pow(2) + (y - dot.y).pow(2))
            if (distance <= selectedDotRadius) {
                return i
            }
        }
        return -1
    }

    fun clearPattern() {
        selectedDots.clear()
        lines.clear()
        invalidate()
    }

    fun setOnPatternListener(listener: OnPatternListener) {
        onPatternListener = listener
    }

    interface OnPatternListener {
        fun onPatternComplete(pattern: List<Int>)
    }

    private data class Dot(val x: Float, val y: Float, val index: Int)
    private data class Line(val startX: Float, val startY: Float, val endX: Float, val endY: Float)
}