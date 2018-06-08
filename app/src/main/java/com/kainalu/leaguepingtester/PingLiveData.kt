package com.kainalu.leaguepingtester

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield

class PingLiveData : LiveData<PingStatus>() {

    var isActive = true
    private lateinit var pingJob: Job

    private fun startPingJob(): Job {
        isActive = true
        return launch {
            while (isActive) {
                postValue(getPing("104.160.131.3").await())
                // If job is not cancelled, wait 1 second before making another request
                yield()
                delay(1000)
            }
        }
    }

    fun toggleJob() {
        if (isActive) {
            isActive = false
            onInactive()
        } else {
            isActive = true
            onActive()
        }
    }

    override fun onActive() {
        if (isActive) {
            pingJob = startPingJob()
        }
    }

    override fun onInactive() {
        pingJob.cancel()
    }
}