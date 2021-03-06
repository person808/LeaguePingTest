package com.kainalu.leaguepingtester

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.icmp4j.IcmpPingUtil

enum class Server(val ipAddress: String) {
    NA("104.160.131.3"),
    EUW("104.160.141.3"),
    EUNE("104.160.142.3"),
    OCE("104.160.156.1"),
    LAN("104.160.136.3")
}

suspend fun getPing(address: String): PingStatus =
        withContext(Dispatchers.IO) {
            val TAG = this::class.java.canonicalName
            val pingResponse = IcmpPingUtil.createIcmpPingRequest().apply {
                host = address
            }.run {
                IcmpPingUtil.executePingRequest(this)
            }
            if (pingResponse.successFlag) {
                Log.d(TAG, IcmpPingUtil.formatResponse(pingResponse))
                PingStatus.Success(pingResponse.rtt)
            } else {
                Log.d(TAG, IcmpPingUtil.formatResponse(pingResponse))
                PingStatus.Error(pingResponse.errorMessage)
            }
        }

sealed class PingStatus {
    data class Error(val message: String) : PingStatus()
    data class Success(val ping: Int) : PingStatus()
}

fun isConnectedToWifi(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    val isConnected = activeNetwork?.isConnectedOrConnecting == true
    val isWiFi = activeNetwork?.type == ConnectivityManager.TYPE_WIFI
    return isConnected && isWiFi
}
