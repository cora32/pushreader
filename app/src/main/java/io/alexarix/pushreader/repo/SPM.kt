package io.alexarix.pushreader.repo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

const val hostKey = "hostKey"
const val pathKey = "pathKey"
const val uTitleKey = "uTitleKey"
const val uTickerKey = "uTickerKey"
const val uBigTitleKey = "uBigTitleKey"
const val uTextKey = "uTextKey"
const val uBigTextKey = "uBigTextKey"
const val packagesNamesKey = "packagesNamesKey"

class SPM(val application: Application) {
    fun init() {
        preferences = application.getSharedPreferences("pushreader_sp", Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var preferences: SharedPreferences

        var host: String = ""
            get() = preferences.getString(hostKey, "") ?: ""
            set(newValue) {
                preferences.edit() { putString(hostKey, newValue) }
                field = newValue
            }

        var path: String = ""
            get() = preferences.getString(pathKey, "") ?: ""
            set(newValue) {
                preferences.edit() { putString(pathKey, newValue) }
                field = newValue
            }

        var isUniqueByTitle: Boolean = false
            get() = preferences.getBoolean(uTitleKey, false)
            set(newValue) {
                preferences.edit() { putBoolean(uTitleKey, newValue) }
                field = newValue
            }

        var isUniqueByTicker: Boolean = false
            get() = preferences.getBoolean(uTickerKey, false)
            set(newValue) {
                preferences.edit() { putBoolean(uTickerKey, newValue) }
                field = newValue
            }

        var isUniqueByBigTitle: Boolean = false
            get() = preferences.getBoolean(uBigTitleKey, false)
            set(newValue) {
                preferences.edit() { putBoolean(uBigTitleKey, newValue) }
                field = newValue
            }

        var isUniqueByText: Boolean = false
            get() = preferences.getBoolean(uTextKey, false)
            set(newValue) {
                preferences.edit() { putBoolean(uTextKey, newValue) }
                field = newValue
            }

        var isUniqueByBigText: Boolean = false
            get() = preferences.getBoolean(uBigTextKey, false)
            set(newValue) {
                preferences.edit() { putBoolean(uBigTextKey, newValue) }
                field = newValue
            }

        var savingPackages: Set<String> = setOf<String>()
            get() = preferences.getStringSet(packagesNamesKey, setOf()) ?: setOf()
            set(newValue) {
                preferences.edit() { putStringSet(packagesNamesKey, newValue) }
                field = newValue
            }
    }
}