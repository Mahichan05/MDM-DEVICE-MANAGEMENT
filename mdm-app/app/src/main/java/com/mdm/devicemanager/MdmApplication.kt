package com.mdm.devicemanager

import android.app.Application

class MdmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MdmApplication
            private set
    }
}
