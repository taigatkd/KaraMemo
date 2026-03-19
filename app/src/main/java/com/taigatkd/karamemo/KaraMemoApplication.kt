package com.taigatkd.karamemo

import android.app.Application
import com.taigatkd.karamemo.data.repository.AppContainer

class KaraMemoApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

