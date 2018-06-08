package com.kainalu.leaguepingtester

import android.arch.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class PingViewModel : ViewModel() {

    val pingStatus = PingLiveData()
    val pingJobActive
        get() = pingStatus.isActive
    var successfulRequests = 0
    var totalPing = 0

    fun toggleJob() {
        pingStatus.toggleJob()
    }

    val dataSet = LineDataSet(mutableListOf(Entry(MAX_ENTRIES.toFloat(), 0f)), "").apply {
        setDrawFilled(true)
        color = R.color.primaryDarkColor
        setCircleColor(color)
        fillColor = color
        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    }
    val lineData = LineData(dataSet).apply {
        setDrawValues(false)
        isHighlightEnabled = false
    }

    fun addEntry(entry: Entry) {
        dataSet.removeOutdatedEntries()
        dataSet.addEntry(entry)
    }
}