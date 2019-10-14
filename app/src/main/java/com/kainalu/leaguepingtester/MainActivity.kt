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


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstanceState ?: firstRun()

        viewModel = ViewModelProviders.of(this)[PingViewModel::class.java]

        chart.setup(viewModel.lineData)
        setupToolbar()
        button.apply {
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

        viewModel.viewState.observe(this, Observer { render(it) })

    }

    override fun onResume() {
        super.onResume()
        viewModel.resumePingJob()
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelPingJob()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            finish()
            startActivity(intent)
        }
    }

    private fun firstRun() {
        if (!isConnectedToWifi(this)) {
            Snackbar.make(container, R.string.wifi_warning, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.wifi_settings) {
                        startActivityForResult(Intent(Settings.ACTION_WIFI_SETTINGS), 0)
                    }.show()
        }
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.menu_overflow)
        toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.resume_pause -> {
                    toggleJob()
                    true
                }
                R.id.default_server -> {
                    val menu = toolbar.menu
                    val defaultServerItem = when (getDefaultServerName()) {
                        "NA" -> menu.findItem(R.id.na_server)
                        "EUW" -> menu.findItem(R.id.euw_server)
                        "EUNE" -> menu.findItem(R.id.eune_server)
                        "OCE" -> menu.findItem(R.id.oce_server)
                        "LAN" -> menu.findItem(R.id.lan_server)
                        else -> menu.findItem(R.id.na_server)
                    }
                    defaultServerItem.isChecked = true
                    true
                }
                R.id.na_server -> {
                    item.isChecked = true
                    setDefaultServer(Server.NA)
                    true
                }
                R.id.euw_server -> {
                    item.isChecked = true
                    setDefaultServer(Server.EUW)
                    true
                }
                R.id.eune_server -> {
                    item.isChecked = true
                    setDefaultServer(Server.EUNE)
                    true
                }
                R.id.oce_server -> {
                    item.isChecked = true
                    setDefaultServer(Server.OCE)
                    true
                }
                R.id.lan_server -> {
                    item.isChecked = true
                    setDefaultServer(Server.LAN)
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
    }

    private fun render(viewState: PingViewState) {
        button.text = getString(R.string.server, viewState.server.name)
        when (viewState.pingStatus) {
            is PingStatus.Success -> {
                currentPingTextView.apply {
                    text = getString(R.string.ms_label, viewState.pingStatus.ping)
                    setTextColor(ContextCompat.getColor(this@MainActivity,
                            R.color.secondaryTextColor))
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
    }

    private fun toggleJob() {
        viewModel.togglePingJob()
        invalidateOptionsMenu()
    }
}