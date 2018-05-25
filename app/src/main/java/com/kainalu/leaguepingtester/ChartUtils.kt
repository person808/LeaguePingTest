package com.kainalu.leaguepingtester

import com.github.mikephil.charting.data.LineDataSet

const val MAX_ENTRIES = 10

fun LineDataSet.removeOutdatedEntries() {
    while (entryCount > MAX_ENTRIES) {
        removeFirst()
    }
}