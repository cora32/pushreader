package io.alexarix.pushreader.viewmodels

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.alexarix.pushreader.IoDispatcher
import io.alexarix.pushreader.MainDispatcher
import io.alexarix.pushreader.repo.Repo
import io.alexarix.pushreader.repo.room.entity.PRServiceLogEntity
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@HiltViewModel
class LogsViewModel @Inject constructor(
    application: Application,
    val repo: Repo,
    @IoDispatcher private val bgDispatcher: CoroutineContext,
    @MainDispatcher private val uiDispatcher: CoroutineContext
) : AndroidViewModel(application = application),
    DefaultLifecycleObserver {
    private val _isLogEnabled = mutableStateOf<Boolean>(repo.isLogEnabled())
    private val _isShowUnknown = mutableStateOf<Boolean>(repo.isShowUnknownEnabled())
    private val _isShowInfo = mutableStateOf<Boolean>(repo.isShowInfoEnabled())
    private val _isShowOk = mutableStateOf<Boolean>(repo.isShowOkEnabled())
    private val _isShowErrors = mutableStateOf<Boolean>(repo.isShowErrorsEnabled())
    private val _logs = mutableStateOf<List<PRServiceLogEntity>>(listOf())
    private val _isLoading = mutableStateOf<Boolean>(true)

    val isShowErrors: State<Boolean> = _isShowErrors
    val isShowInfo: State<Boolean> = _isShowInfo
    val isShowOk: State<Boolean> = _isShowOk
    val isShowUnknown: State<Boolean> = _isShowUnknown
    val isLogEnabled: State<Boolean> = _isLogEnabled
    val logs: State<List<PRServiceLogEntity>> = _logs
    val isLoading: State<Boolean> = _isLoading

    init {
        viewModelScope.launch(bgDispatcher) {
            repo.getLogsDataFlow().collect { newLogEntry ->
                "--> Item collected: $newLogEntry".e
                newLogEntry?.let {
                    _logs.value = mutableListOf<PRServiceLogEntity>().apply {
                        add(newLogEntry)
                        addAll(_logs.value)
                    }
                }
            }
        }
    }

    private fun getLogs() {
        _isLoading.value = true
        viewModelScope.launch(bgDispatcher) {
            _logs.value = repo.getLogs(
                getUnknown = repo.isShowUnknownEnabled(),
                getInfo = repo.isShowInfoEnabled(),
                getOk = repo.isShowOkEnabled(),
                getErrors = repo.isShowErrorsEnabled(),
            ).apply {
                "Logs: We got ${this.size} entries".e
            }
            _isLoading.value = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        getLogs()
    }

    fun toggleLog(value: Boolean) {
        viewModelScope.launch(bgDispatcher) {

            repo.toggleLog(value = value)

            _isLogEnabled.value = repo.isLogEnabled()
        }

        getLogs()
    }

    fun toggleShowErrors(value: Boolean) {
        repo.toggleShowErrors(value = value)

        getLogs()
    }

    fun toggleShowInfo(value: Boolean) {
        repo.toggleShowInfo(value = value)

        getLogs()
    }

    fun toggleShowOk(value: Boolean) {
        repo.toggleShowOk(value = value)

        getLogs()
    }

    fun toggleShowUnknown(value: Boolean) {
        repo.toggleShowUnknown(value = value)

        getLogs()
    }
}