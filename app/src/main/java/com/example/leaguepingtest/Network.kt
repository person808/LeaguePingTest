package com.example.leaguepingtest

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
    val pingRequest = IcmpPingUtil.createIcmpPingRequest().apply {
        host = address
    }
    val pingResponse = IcmpPingUtil.executePingRequest(pingRequest)
    if (pingResponse.successFlag) {
        PingStatus.Success(pingResponse.rtt)
    } else {
        Log.d(this::class.java.canonicalName, IcmpPingUtil.formatResponse(pingResponse))
        PingStatus.Error(pingResponse.errorMessage)
    }
}

sealed class PingStatus {
    data class Error(val message: String) : PingStatus()
    data class Success(val ping: Int) : PingStatus()
}

@Parcelize
class ServerAddress(var name: String = "NA", var address: String = IP_ADDRESSES[name]!!) : Parcelable {
    fun updateAddress() {
        address = IP_ADDRESSES[name]!!
    }
}
