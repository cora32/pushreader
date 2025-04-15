package io.alexarix.pushreader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.alexarix.pushreader.repo.SPM

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        SPM(application = this).init()
    }
}