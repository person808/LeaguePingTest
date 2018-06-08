package com.kainalu.leaguepingtester

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides

@Module
class ContextModule(private val appContext: Context) {
    @Provides
    fun appContext(): Context = appContext

    @Provides
    fun sharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)

    companion object {
        private const val SHARED_PREFERENCES_KEY = "com.kainalu.leaguepingtest.sharedPreferences"
    }
}