package com.kainalu.leaguepingtester

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PingLiveData(var server: ServerAddress) : LiveData<PingStatus>(), CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    var pingEnabled = true

    private fun startJob() = launch {
        while (isActive) {
            postValue(getPing(server.address))
            yield()
            delay(1000)
        }
    }

    fun toggleJob() {
        if (pingEnabled) {
            pingEnabled = false
            coroutineContext.cancelChildren()
        } else {
            pingEnabled = true
            startJob()
        }
    }

    override fun onActive() {
        if (pingEnabled) {
            startJob()
        }
    }

    override fun onInactive() {
        coroutineContext.cancelChildren()
    }
}