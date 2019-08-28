package com.example.marinar.smackchat.Controller

import android.app.Application
import com.example.marinar.smackchat.Utils.SharedPrefs

class App : Application() {

    companion object {
        lateinit var sharedPreferences: SharedPrefs
    }
    override fun onCreate() {
        sharedPreferences = SharedPrefs(applicationContext)
        super.onCreate()
    }
}