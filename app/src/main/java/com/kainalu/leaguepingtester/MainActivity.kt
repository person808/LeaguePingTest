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
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_custom_item_checkable.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        savedInstanceState ?: firstRun()

        viewModel = ViewModelProviders.of(this)[PingViewModel::class.java]

        viewModel.dataSet.label = getString(R.string.graph_label)
        chart.setup(viewModel.lineData)

        viewModel.pingStatus.observe(this, Observer { pingStatus ->
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
                    viewModel.addEntry(Entry(MAX_ENTRIES.toFloat(), pingStatus.ping.toFloat()))
                }
                is PingStatus.Error -> {
                    currentPingTextView.run {
                        text = pingStatus.message
                        setTextColor(ContextCompat.getColor(applicationContext, R.color.errorColor))
                    }
                    viewModel.addEntry(Entry(MAX_ENTRIES.toFloat(), 0f))
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
            text = getString(R.string.server, viewModel.server.name)
            setTextColor(ContextCompat.getColor(applicationContext, R.color.secondaryTextColor))
            val popupMenu = popupMenu {
                section {
                    for (str in IP_ADDRESSES.keys) {
                        item {
                            label = str
                            callback = {
                                if (label != viewModel.server.name) {
                                    viewModel.server = ServerAddress(label!!)
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
                        val defaultServerName = getDefaultServerName()
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
                                    setDefaultServer(serverName)
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

    private fun toggleJob() {
        viewModel.toggleJob()
        invalidateOptionsMenu()
    }
}