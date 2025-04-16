package io.alexarix.pushreader.repo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

const val protocolKey = "protocolKey"
const val hostKey = "hostKey"
const val portKey = "portKey"
const val pathKey = "pathKey"
const val uTitleKey = "uTitleKey"
const val uTickerKey = "uTickerKey"
const val uBigTitleKey = "uBigTitleKey"
const val uTextKey = "uTextKey"
const val uBigTextKey = "uBigTextKey"
const val packagesNamesKey = "packagesNamesKey"
const val sentKey = "sentKey"
const val processedKey = "processedKey"
const val ignoredKey = "ignoredKey"
const val filteredKey = "filteredKey"
const val errorsKey = "errorsKey"
const val urlKey = "urlKey"
//const val countUniqueKey = "countUniqueKey"

class SPM(val application: Application) {
    fun init() {
        preferences = application.getSharedPreferences("pushreader_sp", Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var preferences: SharedPreferences

        var protocol: String = ""
            get() = preferences.getString(protocolKey, "") ?: ""
            set(newValue) {
                preferences.edit() { putString(protocolKey, newValue) }
                field = newValue
            }

        var host: String = ""
            get() = preferences.getString(hostKey, "") ?: ""
            set(newValue) {
                preferences.edit() { putString(hostKey, newValue) }
                field = newValue
            }

        var port: Int = 443
            get() = preferences.getInt(portKey, 443)
            set(newValue) {
                preferences.edit() { putInt(portKey, newValue) }
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

        var selectedApps: Set<String> = setOf<String>()
            get() = preferences.getStringSet(packagesNamesKey, setOf()) ?: setOf()
            set(newValue) {
                preferences.edit(commit = true) { putStringSet(packagesNamesKey, newValue) }
                field = newValue
            }

        var sent: Int = 0
            get() = preferences.getInt(sentKey, 0)
            set(newValue) {
                preferences.edit() { putInt(sentKey, newValue) }
                field = newValue
            }

        var processed: Int = 0
            get() = preferences.getInt(processedKey, 0)
            set(newValue) {
                preferences.edit() { putInt(processedKey, newValue) }
                field = newValue
            }

        var ignored: Int = 0
            get() = preferences.getInt(ignoredKey, 0)
            set(newValue) {
                preferences.edit() { putInt(ignoredKey, newValue) }
                field = newValue
            }

        var filtered: Int = 0
            get() = preferences.getInt(filteredKey, 0)
            set(newValue) {
                preferences.edit() { putInt(filteredKey, newValue) }
                field = newValue
            }

        var errors: Int = 0
            get() = preferences.getInt(errorsKey, 0)
            set(newValue) {
                preferences.edit() { putInt(errorsKey, newValue) }
                field = newValue
            }

        var url: String = ""
            get() = preferences.getString(urlKey, "") ?: ""
            set(newValue) {
                preferences.edit() { putString(urlKey, newValue) }
                field = newValue
            }

//        var countUnique: Int = 0
//            get() = preferences.getInt(countUniqueKey, 0)
//            set(newValue) {
//                preferences.edit() { putInt(countUniqueKey, newValue) }
//                field = newValue
//            }
    }
}