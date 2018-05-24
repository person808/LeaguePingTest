package com.example.leaguepingtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.icmp4j.IcmpPingUtil


val IP_ADDRESSES = mapOf(
        "NA" to "104.160.131.3",
        "EUW" to "104.160.141.3",
        "EUNE" to "104.160.142.3",
        "OCE" to "104.160.156.1",
        "LAN" to "104.160.136.3")

class MainActivity : AppCompatActivity() {

    private val currentPingTextView: TextView by lazy { findViewById<TextView>(R.id.tv_current_ping) }
    private val averagePingTextView: TextView by lazy { findViewById<TextView>(R.id.tv_average_ping) }
    private val chart: LineChart by lazy {
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
    private val dataSet = LineDataSet(mutableListOf(Entry(0f, 0f)), "Ping").apply {
        setDrawFilled(true)
        color = R.color.secondaryDarkColor
        setCircleColor(color)
        fillColor = color
        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    }
    private val lineData = LineData(dataSet).apply {
        setDrawValues(false)
        isHighlightEnabled = false
    }

    private var currentServer = "NA"
    private var ipAddress = IP_ADDRESSES[currentServer]!!
    private var successfulRequests = 0
    private var totalPing = 0
    private var xPosition = 1
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstanceState?.let {
            currentServer = it.getString(SERVER)
            ipAddress = IP_ADDRESSES[currentServer]!!
            xPosition = it.getInt(X_POSITION)
            dataSet.values = it.getParcelableArrayList(DATASET)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }

        findViewById<Button>(R.id.button).apply {
            text = getString(R.string.server, currentServer)
            val popupMenu = popupMenu {
                section {
                    for (str in IP_ADDRESSES.keys) {
                        item {
                            label = str
                            callback = {
                                val newAddress = IP_ADDRESSES[label!!]!!
                                if (newAddress != ipAddress) {
                                    currentServer = label!!
                                    totalPing = 0
                                    successfulRequests = 0
                                    ipAddress = newAddress
                                    this@apply.text = getString(R.string.server, label)
                                }
                            }
                        }
                    }
                }
            }
            setOnClickListener({
                popupMenu.show(this@MainActivity, it)
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(X_POSITION, xPosition)
        outState?.putParcelableArrayList(DATASET, arrayListOf(*dataSet.values.toTypedArray()))
        outState?.putString(SERVER, currentServer)
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    override fun onResume() {
        super.onResume()
        job = pingJob()
    }

    private fun pingJob(): Job {
        return launch(UI) {
            while (isActive) {
                val ping = getPing(ipAddress).await()
                // We only want to show the last 10 requests in the graph
                dataSet.removeOutdatedEntries()
                if (dataSet.entryCount >= MAX_ENTRIES) {
                    for (entry in dataSet.values) {
                        entry.x--
                    }
                }

                when (ping) {
                    is PingStatus.Success -> {
                        successfulRequests++
                        totalPing += ping.ping
                        averagePingTextView.text = getString(R.string.average_ms_label, totalPing / successfulRequests)
                        currentPingTextView.text = getString(R.string.ms_label, ping.ping)
                        dataSet.addEntry(Entry(xPosition.toFloat(), ping.ping.toFloat()))
                        delay(1000)  // Wait 1 second before making another request
                    }
                    is PingStatus.Error -> {
                        currentPingTextView.text = ping.message
                        dataSet.addEntry(Entry(xPosition.toFloat(), 0f))
                    }
                }

                // Update chart
                chart.notifyDataSetChanged()
                chart.invalidate()
                if (xPosition < MAX_ENTRIES) {
                    xPosition++
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
            PingStatus.Error(pingResponse.errorMessage)
        }
    }

    companion object {
        private const val X_POSITION = "xPosition"
        private const val DATASET = "dataset"
        private const val SERVER = "server"
    }
}

sealed class PingStatus {
    data class Error(val message: String) : PingStatus()
    data class Success(val ping: Int) : PingStatus()
}
