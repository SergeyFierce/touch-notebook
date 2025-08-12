package com.example.otebookbeta

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OtebookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // App-level init here if needed
    }
}
