package com.kainalu.leaguepingtester

import android.content.Context
import android.net.ConnectivityManager
import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.icmp4j.IcmpPingUtil

val IP_ADDRESSES = mapOf(
        "NA" to "104.160.131.3",
        "EUW" to "104.160.141.3",
        "EUNE" to "104.160.142.3",
        "OCE" to "104.160.156.1",
        "LAN" to "104.160.136.3")

fun getPing(address: String): Deferred<PingStatus> = async {
    val TAG = this::class.java.canonicalName
    val pingRequest = IcmpPingUtil.createIcmpPingRequest().apply {
        host = address
    }
    val pingResponse = IcmpPingUtil.executePingRequest(pingRequest)
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

@Parcelize
class ServerAddress(var name: String = "NA") : Parcelable {
    val address: String
        get() {
            return IP_ADDRESSES[name]!!
        }
}

fun isConnectedToWifi(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    val isConnected = activeNetwork?.isConnectedOrConnecting == true
    val isWiFi = activeNetwork?.type == ConnectivityManager.TYPE_WIFI
    return isConnected && isWiFi
}
