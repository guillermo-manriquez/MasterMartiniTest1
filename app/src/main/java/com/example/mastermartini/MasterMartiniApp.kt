package com.example.mastermartini

import android.app.Application
import com.example.mastermartini.di.AppContainer
import com.example.mastermartini.di.DefaultAppContainer

class MasterMartiniApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
