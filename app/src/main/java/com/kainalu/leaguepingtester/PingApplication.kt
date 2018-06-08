package com.kainalu.leaguepingtester

import android.app.Application
import com.kainalu.leaguepingtester.dagger.AppComponent
import com.kainalu.leaguepingtester.dagger.ContextModule
import com.kainalu.leaguepingtester.dagger.DaggerAppComponent

class PingApplication : Application() {

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        component = DaggerAppComponent.builder()
            .contextModule(ContextModule(this))
            .build()
    }

    companion object {
        lateinit var INSTANCE: PingApplication
            private set
    }
}