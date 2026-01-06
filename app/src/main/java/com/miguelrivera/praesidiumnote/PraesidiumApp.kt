package com.miguelrivera.praesidiumnote

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PraesidiumApp: Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
    }
}