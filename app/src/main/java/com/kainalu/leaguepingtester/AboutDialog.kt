package com.kainalu.leaguepingtester

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog


class AboutDialog : androidx.fragment.app.DialogFragment() {

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
