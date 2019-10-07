package com.kainalu.leaguepingtester

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AboutDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val horizontalPadding = resources.getDimensionPixelSize(R.dimen.dialogHorizontalPadding)
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.dialogVerticalPadding)
        val message = TextView(requireContext()).apply {
            setPaddingRelative(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            linksClickable = true
            movementMethod = LinkMovementMethod.getInstance()
            text = linkifyHtml(getString(R.string.about_body), Linkify.WEB_URLS)
        }
        R.attr.alertDialogTheme
        return MaterialAlertDialogBuilder(requireContext())
                .setTitle("${getString(R.string.app_name)} v${getVersion(requireContext())}")
                .setView(message)
                //.setMessage(linkifyHtml(getString(R.string.about_body), Linkify.WEB_URLS))
                .setPositiveButton(R.string.close) { _, _ -> dismiss() }
                .create()
    }

    override fun onStart() {
        super.onStart()
        view?.findViewById<TextView>(android.R.id.message)?.apply {
            linksClickable = true
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun linkifyHtml(html: String, linkifyMask: Int): Spannable {
        val text = Html.fromHtml(html)
        val currentSpans = text.getSpans(0, text.length, URLSpan::class.java)

        val buffer = SpannableString(text)
        Linkify.addLinks(buffer, linkifyMask)

        for (span in currentSpans) {
            val end = text.getSpanEnd(span)
            val start = text.getSpanStart(span)
            buffer.setSpan(span, start, end, 0)
        }
        return buffer
    }

    companion object {
        fun getVersion(context: Context): String {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                "Unknown"
            }
        }
    }
}
