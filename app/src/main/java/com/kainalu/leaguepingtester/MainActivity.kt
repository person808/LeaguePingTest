package com.kainalu.leaguepingtester

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_custom_item_checkable.view.*


class MainActivity : AppCompatActivity() {

    // We must have at least one data point
    // The label cannot be set because it is a string resource. Set it later in onCreate.
    private val dataSet = LineDataSet(mutableListOf(Entry(MAX_ENTRIES.toFloat(), 0f)), "").apply {
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
    private lateinit var viewModel: PingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this)[PingViewModel::class.java]

        savedInstanceState?.run {
            server = getParcelable(SERVER)
            dataSet.values = getParcelableArrayList(DATASET)

            chart.notifyDataSetChanged()
        } ?: firstRun()

        dataSet.label = getString(R.string.graph_label)
        chart.setup(lineData)

        viewModel.pingStatus.observe(this, Observer { pingStatus ->
            if (pingStatus != null) {
                dataSet.removeOutdatedEntries()
            }
            when (pingStatus) {
                is PingStatus.Success -> {
                    if (viewModel.pingJobActive) {
                        viewModel.successfulRequests++
                        viewModel.totalPing += pingStatus.ping
                    }
                    currentPingTextView.run {
                        text = getString(R.string.ms_label, pingStatus.ping)
                        setTextColor(ContextCompat.getColor(applicationContext,
                                                            R.color.primaryTextColor))
                    }
                    dataSet.addEntry(Entry(MAX_ENTRIES.toFloat(), pingStatus.ping.toFloat()))
                }
                is PingStatus.Error -> {
                    currentPingTextView.run {
                        text = pingStatus.message
                        setTextColor(ContextCompat.getColor(applicationContext, R.color.errorColor))
                    }
                    dataSet.addEntry(Entry(MAX_ENTRIES.toFloat(), 0f))
                }
            }

            if (viewModel.successfulRequests > 0) {
                averagePingTextView.text = getString(R.string.average_ms_label,
                                                     viewModel.totalPing / viewModel.successfulRequests)
            }
            // Update chart
            chart.notifyDataSetChanged()
            chart.invalidate()
        })

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
                                    viewModel.totalPing = 0
                                    viewModel.successfulRequests = 0
                                    this@apply.text = getString(R.string.server, label)
                                }
                            }
                        }
                    }
                }
            }
            setOnClickListener({ popupMenu.show(this@MainActivity, it) })
        }
    }

    private fun firstRun() {
        server = getDefaultServer(this)
        if (!isConnectedToWifi(this)) {
            Snackbar.make(container, R.string.wifi_warning, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.wifi_settings, {
                    startActivityForResult(Intent(Settings.ACTION_WIFI_SETTINGS), 0)
                }).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            finish()
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_overflow, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            val item = it.findItem(R.id.resume_pause)
            if (viewModel.pingJobActive) {
                item.title = getString(R.string.pause)
                item.icon = getDrawable(R.drawable.ic_pause_white_24dp)
            } else {
                item.title = getString(R.string.resume)
                item.icon = getDrawable(R.drawable.ic_play_arrow_white_24dp)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.resume_pause -> {
                toggleJob()
                return true
            }
            R.id.default_server -> {
                val popupMenu = popupMenu {
                    section {
                        title = getString(R.string.default_server)
                        val defaultServerName = getDefaultServerName(this@MainActivity)
                        for (serverName in IP_ADDRESSES.keys) {
                            customItem {
                                layoutResId = R.layout.view_custom_item_checkable
                                viewBoundCallback = { view ->
                                    if (serverName == defaultServerName) {
                                        view.customItemRadioButton.isChecked = true
                                    }
                                    view.customItemTextView.text = serverName
                                }
                                callback = {
                                    setDefaultServer(this@MainActivity, serverName)
                                }
                            }
                        }
                    }
                }
                // The toolbar can hold multiple views. The ActionMenuView that holds the overflow
                // button is always the last view in the toolbar.
                popupMenu.show(this, toolbar.getChildAt(toolbar.childCount - 1))
                return true
            }
            R.id.view_about -> {
                val dialog = AboutDialog()
                dialog.show(supportFragmentManager, "AboutDialog")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.run {
            putParcelableArrayList(DATASET, arrayListOf(*dataSet.values.toTypedArray()))
            putParcelable(SERVER, server)
        }
    }

    private fun toggleJob() {
        viewModel.toggleJob()
        invalidateOptionsMenu()
    }

    companion object {
        private const val DATASET = "dataset"
        private const val SERVER = "server"
    }
}