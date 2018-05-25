package com.example.leaguepingtest

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_custom_item_checkable.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch


class MainActivity : AppCompatActivity() {

    // We must have at least one data point
    // The label cannot be set because it is a string resource. Set it later in onCreate.
    private val dataSet = LineDataSet(mutableListOf(Entry(0f, 0f)), "").apply {
        setDrawFilled(true)
        color = R.color.primaryDarkColor
        setCircleColor(color)
        fillColor = color
        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    }
    private val lineData = LineData(dataSet).apply {
        setDrawValues(false)
        isHighlightEnabled = false
    }

    private lateinit var server: ServerAddress
    private var successfulRequests = 0
    private var totalPing = 0
    private var xPosition = 1
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        server = getDefaultServer(this)

        savedInstanceState?.let {
            server = it.getParcelable(SERVER)
            xPosition = it.getInt(X_POSITION)
            dataSet.values = it.getParcelableArrayList(DATASET)
            chart.notifyDataSetChanged()
        }

        chart.apply {
            this@MainActivity.lineData.getDataSetByIndex(0).label = getString(R.string.graph_label)
            data = this@MainActivity.lineData
            isAutoScaleMinMaxEnabled = true
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = 10f
            xAxis.setDrawLabels(false)
            // Disable grid background
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            description = Description().apply { text = "" }
        }
        // Force an empty chart to show if there is no data
        chart.invalidate()

        button.apply {
            text = getString(R.string.server, server.name)
            setTextColor(ContextCompat.getColor(applicationContext, R.color.secondaryTextColor))
            val popupMenu = popupMenu {
                section {
                    for (str in IP_ADDRESSES.keys) {
                        item {
                            label = str
                            callback = {
                                if (label != server.name) {
                                    server.name = label!!
                                    server.updateAddress()
                                    totalPing = 0
                                    successfulRequests = 0
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.default_server -> {
                val popupMenu = popupMenu {
                    section {
                        title = getString(R.string.default_server)
                        val defaultServerName = getDefaultServerName(this@MainActivity)
                        for (serverName in IP_ADDRESSES.keys) {
                            customItem {
                                layoutResId = R.layout.view_custom_item_checkable
                                viewBoundCallback = { view ->
                                    if (serverName == defaultServerName) view.customItemRadioButton.isChecked = true
                                    view.customItemTextView.text = serverName
                                }
                                callback = {
                                    setDefaultServer(this@MainActivity, serverName)
                                }
                            }
                        }
                    }
                }
                // There are two views in the toolbar. The first being the textView for the title
                // and the second being the ActionMenuView that holds the overflow button
                popupMenu.show(this, toolbar.getChildAt(1))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(X_POSITION, xPosition)
        outState?.putParcelableArrayList(DATASET, arrayListOf(*dataSet.values.toTypedArray()))
        outState?.putParcelable(SERVER, server)
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
                val ping = getPing(server.address).await()
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

    companion object {
        private const val X_POSITION = "xPosition"
        private const val DATASET = "dataset"
        private const val SERVER = "server"
    }
}