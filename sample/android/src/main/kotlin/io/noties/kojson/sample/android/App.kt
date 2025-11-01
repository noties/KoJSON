package io.noties.kojson.sample.android

import android.app.Application

class App: Application() {
    companion object {
        lateinit var shared: App
    }

    override fun onCreate() {
        shared = this
        super.onCreate()
    }
}
