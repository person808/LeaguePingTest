package com.kainalu.leaguepingtester

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

const val MAX_ENTRIES = 10

fun LineDataSet.removeOutdatedEntries() {
    while (entryCount > MAX_ENTRIES) {
        removeFirst()
    }

    for (entry in values) {
        entry.x--
    }
}

fun LineChart.setup(lineData: LineData) {
    data = lineData
    isAutoScaleMinMaxEnabled = true
    axisRight.isEnabled = false
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.axisMinimum = 0f
    xAxis.axisMaximum = 10f
    xAxis.setDrawLabels(false)
    // Disable grid background
    xAxis.setDrawGridLines(false)
    axisLeft.setDrawGridLines(false)
    description = Description().apply { text = "" }
    // Force an empty chart to show if there is no data
    invalidate()
}