package com.kainalu.leaguepingtester

enum class JobStatus {
    ACTIVE, PAUSED
}

data class PingViewState(
        val pingStatus: PingStatus? = null,
        val server: Server = getDefaultServer(),
        val jobStatus: JobStatus = JobStatus.ACTIVE,
        val successfulRequests: Int = 0,
        val totalPing: Int = 0
)