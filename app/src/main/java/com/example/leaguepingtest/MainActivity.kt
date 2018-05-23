package com.example.leaguepingtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.icmp4j.IcmpPingUtil

val IP_ADDRESSES = mapOf(
        "NA" to "104.160.131.3",
        "EUW" to "104.160.141.3",
        "EUNE" to "104.160.142.3",
        "OCE" to "104.160.156.1",
        "LAN" to "104.160.136.3")

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    val textView: TextView by lazy { findViewById<TextView>(R.id.textView) }
    val button: Button by lazy { findViewById<Button>(R.id.button) }
    val spinner: Spinner by lazy { findViewById<Spinner>(R.id.spinner) }
    private var ipAddress = IP_ADDRESSES["NA"]!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.servers, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        button.setOnClickListener({ runPingTest() })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        ipAddress = IP_ADDRESSES[parent?.getItemAtPosition(position)]!!
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun runPingTest() {
        launch(UI) {
            while (true) {
                val ping = getPing(ipAddress).await()
                when (ping) {
                    is PingStatus.Success -> {
                        textView.text = ping.ping.toString()
                        delay(1000)
                    }
                    is PingStatus.Error -> textView.text = "ERROR"
                }
            }
        }
    }

    private fun getPing(address: String): Deferred<PingStatus> = async {
        val pingRequest = IcmpPingUtil.createIcmpPingRequest().apply {
            host = address
        }
        val pingResponse = IcmpPingUtil.executePingRequest(pingRequest)
        if (pingResponse.successFlag) {
            PingStatus.Success(pingResponse.rtt)
        } else {
            Log.d(this::class.java.canonicalName, IcmpPingUtil.formatResponse(pingResponse))
            PingStatus.Error
        }
    }
}

sealed class PingStatus {
    object Error : PingStatus()
    data class Success(val ping: Int) : PingStatus()
}
