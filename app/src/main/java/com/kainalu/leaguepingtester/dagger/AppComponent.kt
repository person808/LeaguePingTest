package com.kainalu.leaguepingtester.dagger

import android.content.Context
import android.content.SharedPreferences
import dagger.Component
import javax.inject.Singleton

@Component(modules = [ContextModule::class])
@Singleton
interface AppComponent {
    fun appContext(): Context
    fun sharedPreferences(): SharedPreferences
}