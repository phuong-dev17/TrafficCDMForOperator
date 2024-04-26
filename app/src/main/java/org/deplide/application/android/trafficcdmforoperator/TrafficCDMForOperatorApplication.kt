package org.deplide.application.android.trafficcdmforoperator

import android.app.Application

class TrafficCDMForOperatorApplication: Application() {
    lateinit var authInfoProvider: AuthInfoProvider

    override fun onCreate() {
        super.onCreate()

        authInfoProvider = AuthInfoProvider.instance(applicationContext)
    }
}