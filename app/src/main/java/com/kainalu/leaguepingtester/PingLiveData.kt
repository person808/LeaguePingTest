package com.kainalu.leaguepingtester

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield

class PingLiveData(var server: ServerAddress) : LiveData<PingStatus>() {

    var isActive = true
    private lateinit var pingJob: Job

    private fun startPingJob(): Job {
        isActive = true
        return launch {
            while (isActive) {
                postValue(getPing(server.address).await())
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