package com.kainalu.leaguepingtester

private const val DEFAULT_SERVER_NAME = "defaultServer"
private val sharedPreferences = Injector.get().sharedPreferences()

fun getDefaultServerName(): String? {
    return sharedPreferences.getString(DEFAULT_SERVER_NAME, null)
}

fun getDefaultServer(): ServerAddress {
    getDefaultServerName()?.let {
        return ServerAddress(name = it)
    } ?: setDefaultServer("NA")
    return ServerAddress("NA")
}

fun setDefaultServer(serverName: String) {
    sharedPreferences.edit()
        .putString(DEFAULT_SERVER_NAME, serverName)
        .apply()
}
