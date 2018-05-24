package com.example.leaguepingtest

import com.github.mikephil.charting.data.LineDataSet

fun LineDataSet.removeOutdatedEntries() {
    val MAX_ENTRIES = 10
    while (entryCount > MAX_ENTRIES) {
        removeFirst()
    }
}