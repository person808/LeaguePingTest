package com.example.leaguepingtest

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog


class AboutDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
             val builder = MaterialDialog.Builder(activity as Context)
                    .title("${getString(R.string.app_name)} ${getVersion(activity as Context)}")
                    .content(R.string.about_body, true)
                    .positiveText(R.string.close)
            return builder.build()
    }

    companion object {
        fun getVersion(context: Context): String {
            try {
                return context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                return "Unknown"
            }
        }
    }
}
