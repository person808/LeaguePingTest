package com.kainalu.leaguepingtester.dagger

import com.kainalu.leaguepingtester.PingApplication

class Injector {
    companion object {
        fun get(): AppComponent =
           PingApplication.INSTANCE.component
    }
}
