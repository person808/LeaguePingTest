package com.example.leaguepingtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.icmp4j.IcmpPingUtil


val IP_ADDRESSES = mapOf(
        "NA" to "104.160.131.3",
        "EUW" to "104.160.141.3",
        "EUNE" to "104.160.142.3",
        "OCE" to "104.160.156.1",
        "LAN" to "104.160.136.3")

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    val textView: TextView by lazy { findViewById<TextView>(R.id.textView) }
    val spinner: Spinner by lazy { findViewById<Spinner>(R.id.spinner) }
    val chart: LineChart by lazy {
        findViewById<LineChart>(R.id.chart).apply {
            data = this@MainActivity.lineData
            isAutoScaleMinMaxEnabled = true
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = 10f
            // Disable grid background
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            description = Description().apply { text = "" }
        }
    }
    // We must have at least one data point
    val dataSet = LineDataSet(mutableListOf(Entry(0f, 0f)), "Ping").apply {
        setDrawFilled(true)
        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    }
    val lineData = LineData(dataSet).apply {
        setDrawValues(false)
        isHighlightEnabled = false
    }

    private var ipAddress = IP_ADDRESSES["NA"]!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.servers, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        launch(UI) {
            var counter = 0
            while (isActive) {
                val ping = getPing(ipAddress).await()
                when (ping) {
                    is PingStatus.Success -> {
                        textView.text = getString(R.string.ms_label, ping.ping)
                        // We only want to show the last 10 requests in the graph
                        dataSet.removeOutdatedEntries()
                        if (dataSet.entryCount >= 10) {
                            for (entry in dataSet.values) {
                                entry.x--
                            }
                            chart.moveViewToX(counter.toFloat())
                        }
                        dataSet.addEntry(Entry(counter.toFloat(), ping.ping.toFloat()))
                        chart.notifyDataSetChanged()
                        chart.invalidate()
                        if (counter < 10) {
                            counter++
                        }
                        delay(1000)
                    }
                    is PingStatus.Error -> textView.text = ping.message
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        ipAddress = IP_ADDRESSES[parent?.getItemAtPosition(position)]!!
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun getPing(address: String): Deferred<PingStatus> = async {
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
}

sealed class PingStatus {
    data class Error(val message: String) : PingStatus()
    data class Success(val ping: Int) : PingStatus()
}
