package io.alexarix.pushreader.viewmodels

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.IntState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.alexarix.pushreader.App
import io.alexarix.pushreader.IoDispatcher
import io.alexarix.pushreader.MainDispatcher
import io.alexarix.pushreader.R
import io.alexarix.pushreader.activity.MainActivity
import io.alexarix.pushreader.repo.Repo
import io.alexarix.pushreader.repo.SPM
import io.alexarix.pushreader.repo.room.entity.PRLogEntity
import io.alexarix.pushreader.services.PushReaderService4
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

data class AppDisplayItem(val entity: PRLogEntity, val name: String, val icon: Drawable)

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    val repo: Repo,
    @IoDispatcher private val bgDispatcher: CoroutineContext,
    @MainDispatcher private val uiDispatcher: CoroutineContext
) : AndroidViewModel(application = application),
    DefaultLifecycleObserver {
    private val _filtered = mutableIntStateOf(0)
    private val _processed = mutableIntStateOf(0)
    private val _sent = mutableIntStateOf(0)
    private val _notSent = mutableIntStateOf(0)
    private val _entriesInDB = mutableIntStateOf(0)
    private val _errors = mutableIntStateOf(0)
    private val _ignored = mutableIntStateOf(0)
    private val _url = mutableStateOf("")
    private val _isPermissionGranted = mutableStateOf(false)
    private val _isServiceRunning = mutableStateOf(false)
    private val _last100Items = mutableStateOf<List<AppDisplayItem>>(listOf())

    val processed: IntState = _processed
    val sent: IntState = _sent
    val notSent: IntState = _notSent
    val entriesInDB: IntState = _entriesInDB
    val errors: IntState = _errors
    val ignored: IntState = _ignored
    val filtered: IntState = _filtered
    val url: State<String> = _url
    val isPermissionGranted: State<Boolean> = _isPermissionGranted
    val isServiceRunning: State<Boolean> = _isServiceRunning
    val last100Items: State<List<AppDisplayItem>> = _last100Items

    init {
        val context = getApplication<App>()
        viewModelScope.launch(bgDispatcher) {
            repo.getDataFlow().collect {
                "--> Item collected: $it".e
                _last100Items.value = repo.getLast100Items().toAppDisplayItem(context)
            }
        }
        viewModelScope.launch(bgDispatcher) {
            while (true) {
                delay(2000L)
                checkStatus()
            }
        }
    }

    private fun List<PRLogEntity>.toAppDisplayItem(app: App): List<AppDisplayItem> {
        val pm = app.packageManager

        return map {
            it.packageName?.let { packageName ->
                val appInfo = try {
                    pm.getApplicationInfo(packageName, 0)
                } catch (e: Exception) {
                    null
                }

                if (appInfo != null) {
                    AppDisplayItem(
                        entity = it,
                        name = appInfo.loadLabel(pm).toString(),
                        icon = appInfo.loadIcon(pm)
                    )
                } else {
                    AppDisplayItem(
                        entity = it,
                        name = "ERROR",
                        icon = AppCompatResources.getDrawable(app, R.drawable.ic_stat_name)!!
                    )
                }
            } ?: AppDisplayItem(
                entity = it,
                name = "ERROR",
                icon = AppCompatResources.getDrawable(app, R.drawable.ic_stat_name)!!
            )
        }
    }

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

        return result.map { it.service.className }.contains(PushReaderService4::class.java.name)
    }

    private fun getRunningServicesForApp(): List<ActivityManager.RunningServiceInfo> {
        val context = getApplication<Application>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        return activityManager.getRunningServices(Integer.MAX_VALUE)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        attemptSendUnsent()

        checkPermissionStatus()
        checkAsync()
        checkStatus()
    }

    private fun attemptSendUnsent() {
        viewModelScope.launch(bgDispatcher) {
            repo.attemptSendUnsent()
        }
    }

    private fun checkStatus() {
        viewModelScope.launch(bgDispatcher) {
            _sent.intValue = SPM.sent
            _notSent.intValue = repo.countUnsent()
            _processed.intValue = SPM.processed
            _filtered.intValue = SPM.filtered
            _entriesInDB.intValue = repo.count()
            _errors.intValue = SPM.errors
            _url.value = SPM.url.trim()
            _ignored.intValue = SPM.ignored
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        if (isNotificationListenerEnabled()) startService(owner as MainActivity)
    }

    fun startService(activity: MainActivity) {
        val intent =
            Intent(
                activity,
                PushReaderService4::class.java
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }

        checkAsync()
    }

    private fun checkAsync() {
        viewModelScope.launch(bgDispatcher) {
            delay(500L)
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