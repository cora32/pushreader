package io.alexarix.pushreader.repo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

const val protocolKey = "protocolKey"
const val hostKey = "hostKey"
const val portKey = "portKey"
const val pathKey = "pathKey"
const val isLogEnabledKey = "isLogEnabledKey"
const val packagesNamesKey = "packagesNamesKey"
const val sentKey = "sentKey"
const val processedKey = "processedKey"
const val ignoredKey = "ignoredKey"
const val filteredKey = "filteredKey"
const val errorsKey = "errorsKey"
const val urlKey = "urlKey"

class SPM(val application: Application) {
    fun init() {
        preferences = application.getSharedPreferences("pushreader_sp", Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var preferences: SharedPreferences

        var protocol: String = ""
            get() = preferences.getString(protocolKey, "") ?: ""
            set(newValue) {
                preferences.edit(commit = true) { putString(protocolKey, newValue) }
                field = newValue
            }

        var host: String = ""
            get() = preferences.getString(hostKey, "") ?: ""
            set(newValue) {
                preferences.edit(commit = true) { putString(hostKey, newValue) }
                field = newValue
            }

        var port: Int = 443
            get() = preferences.getInt(portKey, 443)
            set(newValue) {
                preferences.edit(commit = true) { putInt(portKey, newValue) }
                field = newValue
            }

        var path: String = ""
            get() = preferences.getString(pathKey, "") ?: ""
            set(newValue) {
                preferences.edit(commit = true) { putString(pathKey, newValue) }
                field = newValue
            }

        var isLogEnabled: Boolean = false
            get() = preferences.getBoolean(isLogEnabledKey, false)
            set(newValue) {
                preferences.edit(commit = true) { putBoolean(isLogEnabledKey, newValue) }
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
                preferences.edit(commit = true) { putString(urlKey, newValue) }
                field = newValue
            }

        fun isDistinctToggled(name: String): Boolean = preferences.getBoolean(name, false)

        fun toggleDistinct(name: String, value: Boolean) =
            preferences.edit(commit = true) { putBoolean(name, value) }

//        var countUnique: Int = 0
//            get() = preferences.getInt(countUniqueKey, 0)
//            set(newValue) {
//                preferences.edit() { putInt(countUniqueKey, newValue) }
//                field = newValue
//            }
    }
}