package com.example.leaguepingtest

import android.content.Context

private const val DEFAULT_SERVER_NAME = "defaultServer"
private const val SHARED_PREFERENCES_KEY = "com.kainalu.leaguepingtest.sharedPreferences"

fun getDefaultServerName(context: Context): String? {
    return context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).getString(DEFAULT_SERVER_NAME, null)
}

fun getDefaultServer(context: Context): ServerAddress {
    getDefaultServerName(context)?.let {
        return ServerAddress(name = it)
    } ?: setDefaultServer(context, "NA")
    return ServerAddress("NA")
}

fun setDefaultServer(context: Context, serverName: String) {
    context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit()
            .putString(DEFAULT_SERVER_NAME, serverName)
            .apply()
}
