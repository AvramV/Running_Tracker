package com.avramhorseman.runningtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyBaseApplication: Application(){

    override fun onCreate() {
        super.onCreate()


    }
}