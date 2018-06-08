package com.kainalu.leaguepingtester

import android.arch.lifecycle.ViewModel

class PingViewModel : ViewModel() {

    val pingStatus = PingLiveData()
    val pingJobActive
        get() = pingStatus.isActive
    var successfulRequests = 0
    var totalPing = 0

    fun toggleJob() {
        pingStatus.toggleJob()
    }
}