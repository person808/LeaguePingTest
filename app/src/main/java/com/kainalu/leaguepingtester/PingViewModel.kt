package com.kainalu.leaguepingtester

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.kainalu.leaguepingtester.dagger.Injector
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PingViewModel : ViewModel() {

    private val _viewState = MutableLiveData<PingViewState>().apply {
        value = PingViewState()
    }
    private val currentViewState: PingViewState
        get() = _viewState.value!!
    val viewState: LiveData<PingViewState>
        get() = _viewState

    private val dataSet = LineDataSet(mutableListOf(Entry(MAX_ENTRIES.toFloat(), 0f)), "").apply {
        setDrawFilled(true)
        color = R.color.primaryDarkColor
        setCircleColor(color)
        fillColor = color
        label = Injector.get().appContext().getString(R.string.graph_label)
        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    }
    val lineData = LineData(dataSet).apply {
        setDrawValues(false)
        isHighlightEnabled = false
    }

    private var jobStarted = false

    @Synchronized
    fun startPingJob() {
        if (jobStarted) {
            return
        }

        jobStarted = true
        viewModelScope.launch {
            while (isActive) {
                val ping = getPing(currentViewState.server.ipAddress)
                var newState = currentViewState.copy(pingStatus = ping, jobStatus = JobStatus.ACTIVE)
                when (ping) {
                    is PingStatus.Success -> {
                        newState = newState.copy(
                                successfulRequests = newState.successfulRequests + 1,
                                totalPing = newState.totalPing + ping.ping
                        )
                        addEntry(Entry(MAX_ENTRIES.toFloat(), ping.ping.toFloat()))
                    }
                    is PingStatus.Error -> {
                        addEntry(Entry(MAX_ENTRIES.toFloat(), 0f))
                    }
                }
                _viewState.value = newState
                delay(1000L)
            }
        }
    }

    fun resumePingJob() {
        if (currentViewState.jobStatus == JobStatus.ACTIVE) {
            startPingJob()
        }
    }

    fun pausePingJob() {
        cancelPingJob()
        _viewState.value = currentViewState.copy(jobStatus = JobStatus.PAUSED)
    }

    fun cancelPingJob() {
        viewModelScope.coroutineContext.cancelChildren()
        jobStarted = false
    }

    fun setServer(server: Server) {
        if (server != currentViewState.server) {
            _viewState.value = currentViewState.copy(
                    server = server,
                    successfulRequests = 0,
                    totalPing = 0
            )
        }
    }

    fun togglePingJob() {
        if (currentViewState.jobStatus == JobStatus.ACTIVE) {
            pausePingJob()
        } else {
            startPingJob()
        }
    }

    private fun addEntry(entry: Entry) {
        dataSet.removeOutdatedEntries()
        dataSet.addEntry(entry)
    }
}