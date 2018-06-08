package com.kainalu.leaguepingtester

class Injector {
    companion object {
        fun get(): AppComponent =
           PingApplication.INSTANCE.component;
    }
}
