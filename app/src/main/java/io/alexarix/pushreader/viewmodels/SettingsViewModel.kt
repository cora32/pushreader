package io.alexarix.pushreader.viewmodels

import android.app.Application
import android.graphics.drawable.Drawable
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
import io.alexarix.pushreader.repo.Repo
import io.alexarix.pushreader.repo.SPM
import io.alexarix.pushreader.repo.managers.DistinctToggles
import io.alexarix.pushreader.repo.managers.isToggled
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


data class AppItemData(
    val drawable: Drawable,
    val name: String,
    val packageName: String,
    val isToggled: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    val repo: Repo,
    @IoDispatcher private val bgDispatcher: CoroutineContext,
    @MainDispatcher private val uiDispatcher: CoroutineContext
) : AndroidViewModel(application = application),
    DefaultLifecycleObserver {
    private val _isLoading = mutableStateOf<Boolean>(false)
    private val _appList = mutableStateOf<List<AppItemData>>(listOf())
    private val _selectedApps = mutableIntStateOf(SPM.selectedApps.size)
    private val _distinctBy = mutableStateOf(getDistinctString())

    val appList: State<List<AppItemData>> = _appList
    val isLoading: State<Boolean> = _isLoading
    val selectedApps: IntState = _selectedApps
    val distinctBy: State<String> = _distinctBy

    private fun getApps() {
        _isLoading.value = true
        viewModelScope.launch(bgDispatcher) {
            val context = getApplication<App>()
            val pm = context.packageManager
            val apps = repo.getInstalledApps(context)
            _appList.value = apps
                .filter { it != null && it.packageName != null }
                .map {
                    AppItemData(
                        drawable = it.loadIcon(pm),
                        name = it.loadLabel(pm)?.toString() ?: "",
                        packageName = it.packageName,
                        isToggled = SPM.selectedApps.contains(it.packageName)
                    )
                }
                .sortedBy { it.name }
            _isLoading.value = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        getApps()
    }

    fun toggleApp(packageName: String, isToggled: Boolean) {
        viewModelScope.launch(bgDispatcher) {
            if (isToggled) {
                repo.addApp(packageName)
            } else {
                repo.removeApp(packageName)
            }
            _selectedApps.intValue = SPM.selectedApps.size
        }
    }

    private fun updateDistinct() {
        _distinctBy.value = getDistinctString()
    }

    private fun getDistinctString() = if (
        DistinctToggles.entries.all { !it.isToggled }
    ) "None (Sending all notifications)"
    else {
        val distinctList = DistinctToggles.entries
            .filter { it.isToggled }
            .map { it.name }

        distinctList.joinToString(", ")
    }

    fun setUrl(url: String) = viewModelScope.launch(bgDispatcher) { repo.setUrl(url) }

    fun toggleDistinct(toggle: DistinctToggles, value: Boolean) {
        viewModelScope.launch(bgDispatcher) {
            repo.toggleDistinct(
                name = toggle.name,
                value = value
            )

            updateDistinct()
        }
    }
}