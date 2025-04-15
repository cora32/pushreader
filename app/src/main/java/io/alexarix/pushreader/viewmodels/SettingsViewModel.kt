package io.alexarix.pushreader.viewmodels

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.alexarix.pushreader.App
import io.alexarix.pushreader.IoDispatcher
import io.alexarix.pushreader.MainDispatcher
import io.alexarix.pushreader.repo.Repo
import io.alexarix.pushreader.repo.SPM
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


data class AppItemData(
    val drawable: Drawable,
    val name: String,
    val packageName: String,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    val repo: Repo,
    @IoDispatcher private val backgroundDispatcher: CoroutineContext,
    @MainDispatcher private val uiDispatcher: CoroutineContext
) : AndroidViewModel(application = application),
    DefaultLifecycleObserver {
    private val _isLoading = mutableStateOf<Boolean>(false)
    private val _appList = mutableStateOf<List<AppItemData>>(listOf())

    val appList: State<List<AppItemData>> = _appList
    val isLoading: State<Boolean> = _isLoading

    private fun getApps() {
        _isLoading.value = true
        viewModelScope.launch(backgroundDispatcher) {
            val context = getApplication<App>()
            val pm = context.packageManager
            val apps = repo.getInstalledApps(context)
            _appList.value = apps
                .filter { it != null && it.name != null && it.packageName != null}
                .map {
                    AppItemData(
                    drawable = it.loadIcon(pm),
//                    name = context.getAppName()?.toString() ?: "",
                    name = it.loadLabel(pm)?.toString() ?: "",
                    packageName = it.packageName
                ) }
                .sortedBy { it.name }
            _isLoading.value = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        getApps()
    }

    fun toggleApp(packageName: String, isToggled: Boolean) {
        viewModelScope.launch(backgroundDispatcher) {
            if (isToggled) {
                repo.addApp(packageName)
            } else {
                repo.removeApp(packageName)
            }
        }
    }

    fun toggleUniqueByTitle(value: Boolean) {
        repo.toggleUniqueByTitle(value)
    }

    fun toggleUniqueByBigTitle(value: Boolean) {
        repo.toggleUniqueByBigTitle(value)
    }

    fun toggleUniqueByText(value: Boolean) {
        repo.toggleUniqueByText(value)
    }

    fun toggleUniqueByBigText(value: Boolean) {
        repo.toggleUniqueByBigText(value)
    }

    fun toggleUniqueByTicker(value: Boolean) {
        repo.toggleUniqueByTicker(value)
    }
}