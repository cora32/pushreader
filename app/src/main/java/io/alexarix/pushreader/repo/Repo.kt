package io.alexarix.pushreader.repo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import io.alexarix.pushreader.BuildConfig
import io.alexarix.pushreader.repo.managers.DistinctChecker
import io.alexarix.pushreader.repo.managers.DistinctToggles
import io.alexarix.pushreader.repo.managers.LogType
import io.alexarix.pushreader.repo.managers.PRLogger
import io.alexarix.pushreader.repo.managers.isToggled
import io.alexarix.pushreader.repo.room.dao.PRDao
import io.alexarix.pushreader.repo.room.entity.PRLogEntity
import io.alexarix.pushreader.repo.room.entity.PRServiceLogEntity
import io.alexarix.pushreader.viewmodels.e
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repo @Inject constructor(
    private val dao: PRDao,
    private val logger: PRLogger,
    private val distinctChecker: DistinctChecker,
    private val restApi: RestApi
) {
    fun getDataFlow() = dao.dataFlow()

    fun getInstalledApps(context: Context): List<ApplicationInfo> {
        val packageManager: PackageManager = context.packageManager
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    }

    suspend fun sendData(entity: PRLogEntity) {
        val isProcessing = checkProcessing(entity)
        SPM.processed += 1

        // Processing only selected packageNames
        if (!isProcessing) {
            logger.logInfo(
                reason = "Ignoring notification from ${entity.packageName} because it is not selected",
                entity = entity
            )
            SPM.ignored++

            return
        }

        logger.logInfo(
            reason = "Processing notification from ${entity.packageName}",
            entity = entity
        )

        val isUnique = distinctChecker.isUnique(entity)

        // Saving only unique notifications
        if (!isUnique) {
            logger.logInfo(
                reason = "Omitting copy of notification from ${entity.packageName}",
                entity = entity
            )
            SPM.filtered++

            return
        }

        logger.logInfo(
            reason = "Notification from ${entity.packageName} is unique - saving to DB...",
            entity = entity
        )

        // Save to DB and transmit to server
        val id = storeData(entity)

        logger.logOk(
            reason = "Notification from ${entity.packageName} is saved to DB with id ${id}!",
            entity = entity
        )

        // If saved to DB successfully, transmit to server
        if (id != -1L) {
            logger.logInfo(
                "Sending notification from ${entity.packageName} to server...",
                entity = entity
            )

            trySend(id = id, entity = entity)
        }
    }

    private suspend fun trySend(id: Long, entity: PRLogEntity) {
        if (SPM.url.isEmpty()) {
            logger.logUnknown(
                reason = "URL is empty! Notification will not be sent.",
                entity = entity
            )

            return
        }

        val isTransmitted = try {
            transmitData(entity)
        } catch (ex: Exception) {
            logger.logError(
                reason = "Could not send notification from ${entity.packageName} to server\n\n" +
                        "Exception: $ex",
                entity = entity
            )
            ex.printStackTrace()

            false
        }

        // If transmitted successfully, set isSent to true
        if (isTransmitted) {
            logger.logOk(
                reason = "Notification from ${entity.packageName} successfully sent to server...",
                entity = entity
            )
            SPM.sent += 1
            setIsSent(id = id, value = true)
        } else {
            SPM.errors += 1
            logger.logError(
                reason = "Failed to transmit notification from ${entity.packageName}!",
                entity = entity
            )
        }
    }

    suspend fun attemptSendUnsent() {
        if (SPM.url.isEmpty()) return

        val unsent = dao.getUnsent()
        logger.logInfo(reason = "Startup check: We have ${unsent.size} stale entries to send...")

        for (entity in unsent) {
            trySend(id = entity.uid.toLong(), entity = entity)
        }
    }

    private fun checkProcessing(entity: PRLogEntity): Boolean =
        entity.packageName != BuildConfig.APPLICATION_ID
                && (
                SPM.selectedApps.isEmpty()
                        || entity.packageName?.let { packageName ->
                    SPM.selectedApps.contains(packageName)
                } == true)

    private suspend fun storeData(entity: PRLogEntity): Long = dao.insert(entity)

    private suspend fun transmitData(entity: PRLogEntity): Boolean {
        return (restApi.sendData(
            url = SPM.url,
            data = entity
        ).code().apply {
            logger.logInfo(reason = "Sending entity to ${SPM.url}; code: $this", entity = entity)
        } == 200)
    }

    private suspend fun setIsSent(id: Long, value: Boolean) {
        dao.setIsSent(id = id, value = value)
    }

    suspend fun addApp(packageName: String) {
        SPM.selectedApps = SPM.selectedApps.toMutableSet().apply {
            add(packageName)
            verbosePackages(this)
        }
    }

    suspend fun removeApp(packageName: String) {
        SPM.selectedApps = SPM.selectedApps.toMutableSet().apply {
            remove(packageName)
            verbosePackages(this)
        }
    }

    private suspend fun verbosePackages(packages: MutableSet<String>) {
        val str = if (packages.isEmpty()) "all" else packages.toString()
        logger.logInfo(reason = "Listening notifications from: $str")
    }


    suspend fun count() = dao.count()

    suspend fun getLast100Items() = dao.getLast100Items().apply {
        "--> Requesting 100 items...".e
    }

    suspend fun countUnsent() = dao.countUnsent()

    suspend fun toggleDistinct(name: String, value: Boolean) =
        SPM.toggleDistinct(name = name, value = value).apply {
            logger.logInfo(reason = if (value) "Enabling filtering by $name" else "Disabling filtering by $name")
            logger.logInfo(
                reason = "Current filters: \n\n" + DistinctToggles.entries.map { "${it.name}: ${it.isToggled} \n" }
            )
        }

    suspend fun getLogs(
        getUnknown: Boolean,
        getInfo: Boolean,
        getOk: Boolean,
        getErrors: Boolean
    ): List<PRServiceLogEntity> {
        val flags = mutableListOf<LogType>().apply {
            if (getUnknown)
                add(LogType.Unknown)
            if (getInfo)
                add(LogType.Info)
            if (getOk)
                add(LogType.OK)
            if (getErrors)
                add(LogType.Fail)
        }

        return if (flags.isEmpty())
            logger.getLogs()
        else
            logger.getLogs(flags = flags.apply { "Requesting logs with flags: $flags".e })
    }

    fun getLogsDataFlow() = logger.getDataFlow()

    suspend fun setUrl(url: String) {
        parseUrl(url)?.let {
            if (it.host.trim().isEmpty())
                SPM.url = ""
            else {
                SPM.protocol = it.protocol.trim()
                SPM.host = it.host.trim()
                SPM.port = if (it.port == -1) 443 else it.port
                SPM.path = it.path.trim()
                SPM.url = "${SPM.protocol}://${SPM.host}:${SPM.port}${SPM.path}"
            }

            logger.logInfo(
                reason = "Url parsed: \n" +
                        "  host: ${SPM.host} \n" +
                        "  port: ${SPM.port} \n" +
                        "  port: ${SPM.path} \n" +
                        "  Result url: ${SPM.host}:${SPM.port}${SPM.path}"
            )
        }
    }

    private fun parseUrl(urlString: String): URL? {
        return try {
            URL(urlString)
        } catch (e: Exception) {
            null
        }
    }

    fun isLogEnabled(): Boolean = SPM.isLogEnabled

    fun isShowUnknownEnabled(): Boolean = SPM.isShowUnknownEnabled

    fun isShowOkEnabled(): Boolean = SPM.isShowOkEnabled

    fun isShowErrorsEnabled(): Boolean = SPM.isShowErrorsEnabled

    fun isShowInfoEnabled(): Boolean = SPM.isShowInfoEnabled

    suspend fun toggleLog(value: Boolean) {
        logger.logInfo(reason = if (value) "Log enabled" else "Log disabled")
        SPM.isLogEnabled = value
    }

    fun toggleShowErrors(value: Boolean) {
        SPM.isShowErrorsEnabled = value
    }

    fun toggleShowInfo(value: Boolean) {
        SPM.isShowInfoEnabled = value
    }

    fun toggleShowOk(value: Boolean) {
        SPM.isShowOkEnabled = value
    }

    fun toggleShowUnknown(value: Boolean) {
        SPM.isShowUnknownEnabled = value
    }
}
