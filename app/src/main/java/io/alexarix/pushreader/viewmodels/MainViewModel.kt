package io.alexarix.pushreader.viewmodels

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.alexarix.pushreader.activity.MainActivity
import io.alexarix.pushreader.repo.Repo
import io.alexarix.pushreader.services.PushReaderService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.contains

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    val repo: Repo
) : AndroidViewModel(application = application),
    DefaultLifecycleObserver {
    private val _isPermissionGranted = mutableStateOf(false)
    private val _isServiceRunning = mutableStateOf(false)

    val isPermissionGranted: State<Boolean> = _isPermissionGranted
    val isServiceRunning: State<Boolean> = _isServiceRunning

    private fun checkPermissionStatus() {
        _isPermissionGranted.value = isNotificationListenerEnabled()
    }

    private fun checkServiceStatus() {
        _isServiceRunning.value = isServiceRunning()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val context = getApplication<Application>()

        val packageName = context.packageName
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )

        "EnabledListeners: $enabledListeners".e
        "Contains ${packageName}: ${enabledListeners.contains(packageName)}".e

        return enabledListeners != null && enabledListeners.contains(packageName)
    }

    private fun isServiceRunning(): Boolean {
        val result = getRunningServicesForApp()

        "We have ${result.size} running services...".e
        result.forEach {
            ("Service: ${it.clientPackage} " +
                    "${it.service.packageName} " +
                    "${it.service.className} ").e
        }

        return result.map { it.service.className }.contains(PushReaderService::class.java.name)
    }

    private fun getRunningServicesForApp(): List<ActivityManager.RunningServiceInfo> {
        val context = getApplication<Application>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        return activityManager.getRunningServices(Integer.MAX_VALUE)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        checkPermissionStatus()
        viewModelScope.launch {
            delay(1000L)
            checkServiceStatus()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        startService(owner as MainActivity)
    }

    fun startService(activity: MainActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(
                Intent(
                    activity,
                    PushReaderService::class.java
                )
            )
        } else {
            activity.startService(
                Intent(
                    activity,
                    PushReaderService::class.java
                )
            )
        }

        viewModelScope.launch {
            delay(1000L)
            checkServiceStatus()
        }
    }

    fun requestNotificationListenerAccess(context: MainActivity) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
    }
}

val String.e
    get() = Log.e("PushReader", this)