package com.example.askguru

import android.app.Application
import com.example.askguru.utils.Const
import com.example.askguru.utils.ReleaseTree
import timber.log.Timber

class AskGuruApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (Const.IS_DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }
}