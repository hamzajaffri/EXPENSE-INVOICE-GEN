package com.example

import android.app.Application
import com.example.data.AppContainer
import com.example.data.AppContainerImpl

class BusinessApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}
