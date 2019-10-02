package com.kainalu.leaguepingtester

import com.kainalu.leaguepingtester.dagger.Injector

private const val DEFAULT_SERVER_NAME = "defaultServer"
private val sharedPreferences = Injector.get().sharedPreferences()

fun getDefaultServerName(): String? {
    return sharedPreferences.getString(DEFAULT_SERVER_NAME, null)
}

fun getDefaultServer(): Server {
    val defaultServerName = getDefaultServerName()
    return if (defaultServerName == null) {
        setDefaultServer(Server.NA.name)
        Server.NA
    } else {
        try {
            Server.valueOf(defaultServerName)
        } catch (e: IllegalArgumentException) {
            setDefaultServer(Server.NA.name)
            Server.NA
        }
    }
}

fun setDefaultServer(serverName: String) {
    sharedPreferences.edit()
        .putString(DEFAULT_SERVER_NAME, serverName)
        .apply()
}
