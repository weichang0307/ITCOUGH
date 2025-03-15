package com.example.itcough

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ScatterChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.RED  // 設定點的顏色
        alpha = 80
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val axisPaint = Paint().apply {
        color = Color.WHITE  // 軸線顏色
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE  // 文字顏色
        textSize = 40f
        isAntiAlias = true
    }
    private val backgroundPaint = Paint().apply {
        color = Color.BLACK  // 設定點的顏色
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 橫軸標籤 (一週七天)
    private var dataPoints: List<Pair<Int, Float>> = emptyList()
    private val xLabel = arrayListOf<String>()
    private val yLabel = arrayListOf<String>()
    private var xGap: Int = 1
    private var yGap: Int = 1
    private var xLength: Int = 10
    private var yLength: Int = 10
    private var yLabelTitle: String = "Time(hours)"
    private var xLabelTitle: String = "Weekday"



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 80f  // 預留間距
        val graphWidth = width - padding * 2
        val graphHeight = height - padding * 2

        canvas.drawRect(0f, 0f, width, height, backgroundPaint)

        // 畫橫軸與縱軸
        canvas.drawLine(padding*2, height - 2*padding, width, height - 2*padding, axisPaint) // X 軸
        canvas.drawLine(padding*2, 0f, padding*2, height - padding*2, axisPaint) // Y 軸

        // X 軸標籤
        val xStep = graphWidth / xLength
        xLabel.forEachIndexed { index, label ->
            val x = padding*2 + xStep * index * xGap
            canvas.drawText(label, x - 20f, height - padding - 20f, textPaint) // 畫出 X 軸標籤
        }

        // Y 軸標籤
        val yStep = graphHeight / yLength
        yLabel.forEachIndexed { index, label ->
            val y = height - padding*2 - yStep * index * yGap
            canvas.drawText(label, 20f + padding, y, textPaint) // 畫出 Y 軸標籤
        }

        // **添加坐標軸標籤**
        canvas.drawText(yLabelTitle, width / 2, height - padding*2 + 135f, textPaint) // X 軸標題
        canvas.save()
        canvas.rotate(-90f, 50f, height / 2)
        canvas.drawText(xLabelTitle, 50f, height / 2, textPaint) // Y 軸標題
        canvas.restore()

        // 畫散點
        val xUnit = xStep

        for ((day, hour) in dataPoints) {
            val x = padding*2 + xUnit * (day - 1) + xUnit
            val y = height - padding*2 - (yStep * hour)
            canvas.drawCircle(x, y, 15f, paint)
        }
    }


    // 提供更新數據的方法
    fun updateData(newData: List<Pair<Int, Float>>, mode: Int) {
        dataPoints = newData
        when (mode) {
            DAY -> initDay()
            WEEK -> initWeek()
            MONTH -> initMonth()
        }
        invalidate() // 重新繪製
    }
    private fun initDay() {
        xLength = 25
        yLength = 65
        xLabelTitle = "minute"
        yLabelTitle = "hours"
        xGap = 4
        yGap = 10
        xLabel.clear()
        yLabel.clear()
        for (i in 0 until yLength step yGap) {
            yLabel.add(i.toString())
        }
        for (i in 0 until xLength step xGap) {
            xLabel.add(i.toString())
        }
    }
    private fun initWeek() {
        xLength = 8
        yLength = 25
        xLabelTitle = "time(hours)"
        yLabelTitle = "weekday"
        xGap = 1
        yGap = 4
        xLabel.clear()
        yLabel.clear()
        for (i in 0 until yLength step yGap) {
            yLabel.add(i.toString())
        }
        xLabel.addAll(listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
    }
    private fun initMonth() {
        xLength = 32
        yLength = 25
        xLabelTitle = "time(hours)"
        yLabelTitle = "date"
        xGap = 1
        yGap = 4
        xLabel.clear()
        yLabel.clear()
        for (i in 0 until yLength step yGap) {
            yLabel.add(i.toString())
        }
        for (i in 0 until xLength step xGap) {
            if (i % 5 == 1) {
                xLabel.add(i.toString())
            }else {
                xLabel.add("")
            }
        }
    }

    companion object {
        const val DAY = 0
        const val WEEK = 1
        const val MONTH = 2
    }

}
