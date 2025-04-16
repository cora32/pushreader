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
import kotlinx.coroutines.launch
import java.net.URL
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
    @IoDispatcher private val backgroundDispatcher: CoroutineContext,
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
        viewModelScope.launch(backgroundDispatcher) {
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
        viewModelScope.launch(backgroundDispatcher) {
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
        !SPM.isUniqueByTicker
        && !SPM.isUniqueByTitle
        && !SPM.isUniqueByBigTitle
        && !SPM.isUniqueByText
        && !SPM.isUniqueByBigText
    ) "None (Sending all notifications)"
    else {
        val distinctList = mutableListOf<String>()
        if (SPM.isUniqueByTicker)
            distinctList.add("Ticker")
        if (SPM.isUniqueByTitle)
            distinctList.add("Title")
        if (SPM.isUniqueByBigTitle)
            distinctList.add("Big title")
        if (SPM.isUniqueByText)
            distinctList.add("Text")
        if (SPM.isUniqueByBigText)
            distinctList.add("Big text")
        if (SPM.isUniqueBySummary)
            distinctList.add("Summary")
        if (SPM.isUniqueBySubtext)
            distinctList.add("Subtext")
        if (SPM.isUniqueByInfo)
            distinctList.add("Info")

        distinctList.joinToString(", ")
    }

    fun toggleUniqueByTitle(value: Boolean) {
        repo.toggleUniqueByTitle(value)

        updateDistinct()
    }

    fun toggleUniqueByBigTitle(value: Boolean) {
        repo.toggleUniqueByBigTitle(value)

        updateDistinct()
    }

    fun toggleUniqueByText(value: Boolean) {
        repo.toggleUniqueByText(value)

        updateDistinct()
    }

    fun toggleUniqueByBigText(value: Boolean) {
        repo.toggleUniqueByBigText(value)

        updateDistinct()
    }

    fun toggleUniqueByTicker(value: Boolean) {
        repo.toggleUniqueByTicker(value)

        updateDistinct()
    }

    fun toggleUniqueBySummaryText(value: Boolean) {
        repo.toggleUniqueBySummaryText(value)

        updateDistinct()
    }

    fun toggleUniqueByInfoText(value: Boolean) {
        repo.toggleUniqueByInfoText(value)

        updateDistinct()
    }

    fun toggleUniqueBySubText(value: Boolean) {
        repo.toggleUniqueBySubText(value)

        updateDistinct()
    }

    fun setUrl(url: String) {
        "--> Parsing url: $url".e

        parseUrl(url)?.let {
            SPM.protocol = it.protocol.trim()
            SPM.host = it.host.trim()
            SPM.port = if (it.port == -1) 443 else it.port
            SPM.path = it.path.trim()
            SPM.url = "${SPM.protocol}://${SPM.host}:${SPM.port}${SPM.path}"

            ("Url parsed: \n" +
                    "  host: ${SPM.host} \n" +
                    "  port: ${SPM.port} \n" +
                    "  port: ${SPM.path} \n" +
                    "  Result url: ${SPM.host}:${SPM.port}${SPM.path}").e
            "it.protocol.trim(): ${it.protocol.trim()}".e
        }
    }

    private fun parseUrl(urlString: String): URL? {
        return try {
            URL(urlString)
        } catch (e: Exception) {
            null
        }
    }
}