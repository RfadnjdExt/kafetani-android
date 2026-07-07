package com.kafetani.app

import android.app.Application
import com.kafetani.app.data.AppContainer

class KafetaniApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
