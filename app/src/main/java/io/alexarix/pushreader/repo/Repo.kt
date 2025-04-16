package io.alexarix.pushreader.repo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import io.alexarix.pushreader.BuildConfig
import io.alexarix.pushreader.repo.room.PRDao
import io.alexarix.pushreader.repo.room.PRLogEntity
import io.alexarix.pushreader.viewmodels.e
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repo @Inject constructor(
    private val dao: PRDao,
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
            "Ignoring notification from ${entity.packageName}: $entity".e
            SPM.ignored++

            return
        }

        "Processing notification from ${entity.packageName}: $entity".e

        val isUnique = checkIsUnique(entity)

        // Saving only unique notifications
        if (!isUnique) {
            "Omitting copy of notification from ${entity.packageName}: $entity".e
            SPM.filtered++

            return
        }

        "Notification from ${entity.packageName} is unique - saving to DB... $entity".e

        // Save to DB and transmit to server
        val id = storeData(entity)

        "Notification from ${entity.packageName} is saved to DB with id ${id}! $entity".e

        // If saved to DB successfully, transmit to server
        if (id != -1L) {
            "Sending notification from ${entity.packageName} to server... $entity".e

            trySend(id = id, entity = entity)
        }
    }

    private suspend fun trySend(id: Long, entity: PRLogEntity) {
        if (SPM.url.isEmpty()) {
            "URL is empty! Doing nothing.".e
            return
        }

        "Sending $entity to server... ".e

        val isTransmitted = try {
            transmitData(entity)
        } catch (ex: Exception) {
            "Could not send notification from ${entity.packageName} to server. $entity".e
            ex.printStackTrace()
            false
        }

        // If transmitted successfully, set isSent to true
        if (isTransmitted) {
            "Notification from ${entity.packageName} successfully sent to server... $entity".e
            SPM.sent += 1
            setIsSent(id = id, value = true)
        } else {
            SPM.errors += 1
            "Failed to transmit notification from ${entity.packageName}! $entity".e
        }
    }

    suspend fun attemptSendUnsent() {
        "--> Attempting to send unsent entries...".e
        val unsent = dao.getUnsent()
        "--> We have ${unsent.size} entries to send...".e

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


    private suspend fun checkIsUnique(entity: PRLogEntity): Boolean {
        if (!SPM.isUniqueByTitle
            && !SPM.isUniqueByTicker
            && !SPM.isUniqueByBigTitle
            && !SPM.isUniqueByText
            && !SPM.isUniqueByBigText
            && !SPM.isUniqueBySummary
            && !SPM.isUniqueByInfo
            && !SPM.isUniqueBySubtext
        ) {
//            return isUniqueByTitle(entity)
//                    && isUniqueByTicker(entity)
//                    && isUniqueByBigTitle(entity)
//                    && isUniqueByText(entity)
//                    && isUniqueByBigText(entity)
            return true
        } else {
            val result = arrayOf(
                if (SPM.isUniqueByTitle) isUniqueByTitle(entity) else true,
                if (SPM.isUniqueByTicker) isUniqueByTicker(entity) else true,
                if (SPM.isUniqueByBigTitle) isUniqueByBigTitle(entity) else true,
                if (SPM.isUniqueByText) isUniqueByText(entity) else true,
                if (SPM.isUniqueByBigText) isUniqueByBigText(entity) else true,
                if (SPM.isUniqueBySummary) isUniqueBySummary(entity) else true,
                if (SPM.isUniqueByInfo) isUniqueByInfo(entity) else true,
                if (SPM.isUniqueBySubtext) isUniqueBySubtext(entity) else true,
            )

            return result.all { it == true }
        }
    }

    private suspend fun isUniqueByTitle(entity: PRLogEntity): Boolean =
        (if (entity.title != null) dao.countUniqueByTitle(entity.title) == 0 else true).apply {
            "Checking uniqueness of $entity by title: $this".e
        }

    private suspend fun isUniqueByTicker(entity: PRLogEntity): Boolean =
        (if (entity.tickerText != null) dao.countUniqueByTicker(entity.tickerText) == 0 else true).apply {
            "Checking uniqueness of $entity by Ticker: $this".e
        }

    private suspend fun isUniqueByBigTitle(entity: PRLogEntity): Boolean =
        (if (entity.bigTitle != null) dao.countUniqueByBigTitle(entity.bigTitle) == 0 else true).apply {
            "Checking uniqueness of $entity by BigTitle: $this".e
        }

    private suspend fun isUniqueByText(entity: PRLogEntity): Boolean =
        (if (entity.text != null) dao.countUniqueByText(entity.text) == 0 else true).apply {
            "Checking uniqueness of $entity by text: $this".e
        }

    private suspend fun isUniqueByBigText(entity: PRLogEntity): Boolean =
        (if (entity.bigText != null) dao.countUniqueByBigText(entity.bigText) == 0 else true).apply {
            "Checking uniqueness of $entity by BigText: $this".e
        }

    private suspend fun isUniqueBySummary(entity: PRLogEntity): Boolean =
        (if (entity.summaryText != null) dao.countUniqueBySummary(entity.summaryText) == 0 else true).apply {
            "Checking uniqueness of $entity by Summary: $this".e
        }

    private suspend fun isUniqueByInfo(entity: PRLogEntity): Boolean =
        (if (entity.infoText != null) dao.countUniqueByInfo(entity.infoText) == 0 else true).apply {
            "Checking uniqueness of $entity by Info: $this".e
        }

    private suspend fun isUniqueBySubtext(entity: PRLogEntity): Boolean =
        (if (entity.subText != null) dao.countUniqueBySubText(entity.subText) == 0 else true).apply {
            "Checking uniqueness of $entity by subText: $this".e
        }

    private suspend fun storeData(entity: PRLogEntity): Long = dao.insert(entity)

    private suspend fun transmitData(entity: PRLogEntity): Boolean {
        "Sending $entity to ${SPM.url}.".e
        return restApi.sendData(
            url = SPM.url,
            data = entity
        ).code() == 200
    }

    private suspend fun setIsSent(id: Long, value: Boolean) {
        dao.setIsSent(id = id, value = value)
    }

    fun addApp(packageName: String) {
        SPM.selectedApps = SPM.selectedApps.toMutableSet().apply {
            add(packageName)
            verbosePackages(this)
        }
    }

    fun removeApp(packageName: String) {
        SPM.selectedApps = SPM.selectedApps.toMutableSet().apply {
            remove(packageName)
            verbosePackages(this)
        }
    }

    private fun verbosePackages(packages: MutableSet<String>) {
        val str = if (packages.isEmpty()) "all" else packages.toString()
        "Current savingPackages: $str".e
    }

    private fun verboseUniques() {
        ("Current uniques: \n" +
                "Title: ${SPM.isUniqueByTitle}\n" +
                "BigTitle: ${SPM.isUniqueByBigTitle}\n" +
                "Text: ${SPM.isUniqueByText}\n" +
                "BigText: ${SPM.isUniqueByBigText}\n" +
                "Ticker: ${SPM.isUniqueByTicker}"
                ).e
    }

    fun toggleUniqueByTitle(value: Boolean) {
        SPM.isUniqueByTitle = value
        verboseUniques()
    }

    fun toggleUniqueByBigTitle(value: Boolean) {
        SPM.isUniqueByBigTitle = value
        verboseUniques()
    }

    fun toggleUniqueByText(value: Boolean) {
        SPM.isUniqueByText = value
        verboseUniques()
    }

    fun toggleUniqueByBigText(value: Boolean) {
        SPM.isUniqueByBigText = value
        verboseUniques()
    }

    fun toggleUniqueByTicker(value: Boolean) {
        SPM.isUniqueByTicker = value
        verboseUniques()
    }

    fun toggleUniqueBySummaryText(value: Boolean) {
        SPM.isUniqueBySummary = value
        verboseUniques()
    }

    fun toggleUniqueByInfoText(value: Boolean) {
        SPM.isUniqueByInfo = value
        verboseUniques()
    }

    fun toggleUniqueBySubText(value: Boolean) {
        SPM.isUniqueBySubtext = value
        verboseUniques()
    }

    suspend fun count() = dao.count()

    suspend fun getLast100Items() = dao.getLast100Items().apply {
        "--> Requesting 100 items...".e
    }

    suspend fun countUnsent() = dao.countUnsent()
}