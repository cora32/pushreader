package io.alexarix.pushreader

import android.app.Application
import io.alexarix.pushreader.repo.SPM

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        SPM(application = this).init()
    }
}