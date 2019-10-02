package com.kainalu.leaguepingtester

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_custom_item_checkable.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstanceState ?: firstRun()

        viewModel = ViewModelProviders.of(this)[PingViewModel::class.java]

        chart.setup(viewModel.lineData)
        toolbar.inflateMenu(R.menu.menu_overflow)
        toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.resume_pause -> {
                    toggleJob()
                    true
                }
                R.id.default_server -> {
                    val popupMenu = popupMenu {
                        section {
                            title = getString(R.string.default_server)
                            val defaultServerName = getDefaultServerName()
                            for (server in Server.values()) {
                                customItem {
                                    layoutResId = R.layout.view_custom_item_checkable
                                    viewBoundCallback = { view ->
                                        if (server.name == defaultServerName) {
                                            view.customItemRadioButton.isChecked = true
                                        }
                                        view.customItemTextView.text = server.name
                                    }
                                    callback = {
                                        setDefaultServer(server.name)
                                    }
                                }
                            }
                        }
                    }
                    // The toolbar can hold multiple views. The ActionMenuView that holds the overflow
                    // button is always the last view in the toolbar.
                    popupMenu.show(this, toolbar.getChildAt(toolbar.childCount - 1))
                    true
                }
                R.id.view_about -> {
                    val dialog = AboutDialog()
                    dialog.show(supportFragmentManager, "AboutDialog")
                    true
                }
                else -> false
            }
        }

        viewModel.viewState.observe(this, Observer { viewState ->
            button.text = getString(R.string.server, viewState.server.name)
            when (viewState.pingStatus) {
                is PingStatus.Success -> {
                    currentPingTextView.apply {
                        text = getString(R.string.ms_label, viewState.pingStatus.ping)
                        setTextColor(ContextCompat.getColor(this@MainActivity,
                                R.color.primaryTextColor))
                    }
                }
                is PingStatus.Error -> {
                    currentPingTextView.apply {
                        text = viewState.pingStatus.message
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.errorColor))
                    }
                }
            }

            if (viewState.successfulRequests > 0) {
                averagePingTextView.text = getString(R.string.average_ms_label,
                        viewState.totalPing / viewState.successfulRequests)
            }

            // Update toolbar
            toolbar.menu.apply {
                val item = findItem(R.id.resume_pause)
                item?.let {
                    if (viewState.jobStatus == JobStatus.ACTIVE) {
                        item.title = getString(R.string.pause)
                        item.icon = getDrawable(R.drawable.ic_pause_white_24dp)
                    } else {
                        item.title = getString(R.string.resume)
                        item.icon = getDrawable(R.drawable.ic_play_arrow_white_24dp)
                    }
                }
                invalidateOptionsMenu()
            }

            // Update chart
            chart.notifyDataSetChanged()
            chart.invalidate()
        })

        button.apply {
            setTextColor(ContextCompat.getColor(applicationContext, R.color.secondaryTextColor))
            val popupMenu = popupMenu {
                section {
                    for (server in Server.values()) {
                        item {
                            label = server.name
                            callback = {
                                viewModel.setServer(server)
                                this@apply.text = getString(R.string.server, label)
                            }
                        }
                    }
                }
            }
            setOnClickListener { popupMenu.show(this@MainActivity, it) }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumePingJob()
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelPingJob()
    }

    private fun firstRun() {
        if (!isConnectedToWifi(this)) {
            Snackbar.make(container, R.string.wifi_warning, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.wifi_settings) {
                        startActivityForResult(Intent(Settings.ACTION_WIFI_SETTINGS), 0)
                    }.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            finish()
            startActivity(intent)
        }
    }

    private fun toggleJob() {
        viewModel.togglePingJob()
        invalidateOptionsMenu()
    }
}