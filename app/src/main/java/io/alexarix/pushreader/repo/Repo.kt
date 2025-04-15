package io.alexarix.pushreader.repo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import io.alexarix.pushreader.viewmodels.e
import io.alexarix.pushreader.repo.room.PRDao
import io.alexarix.pushreader.repo.room.PRLogEntity
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
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA).sortedBy { it.packageName }
    }

    suspend fun sendData(entity: PRLogEntity) {
        val isProcessing = checkProcessing(entity)

        // Processing only selected packageNames
        if (!isProcessing) {
            "Ignoring notification from ${entity.packageName}: $entity".e
            return
        }

        "Processing notification from ${entity.packageName}: $entity".e

        val isUnique = checkIsUnique(entity)

        // Saving only unique notifications
        if (isUnique) {
            "Omitting copy of notification from ${entity.packageName}: $entity".e
            return
        }

        "Notification from ${entity.packageName} is unique - saving to DB... $entity".e

        // Save to DB and transmit to server
        val id = storeData(entity)

        "Notification from ${entity.packageName} is saved to DB with id ${id}! $entity".e

        // If saved to DB successfully, transmit to server
        if (id != -1L) {
            "Sending notification from ${entity.packageName} to server... $entity".e

            val isTransmitted = try {
                transmitData(entity)
            } catch (ex: Exception) {
                "Could not send notification from ${entity.packageName} to server. $entity".e
                ex.printStackTrace()
                false
            }

            "Notification from ${entity.packageName} successfully sent to server... $entity".e

            // If transmitted successfully, set isSent to true
            if (isTransmitted) {
                setIsSent(id = id, value = true)
            }
        }
    }

    private fun checkProcessing(entity: PRLogEntity): Boolean =
        entity.packageName?.let { packageName ->
            SPM.savingPackages.contains(packageName)
        } == true


    private suspend fun checkIsUnique(entity: PRLogEntity): Boolean {
        val result = arrayOf(
            if (SPM.isUniqueByTitle) isUniqueByTitle(entity) else true,
            if (SPM.isUniqueByTicker) isUniqueByTicker(entity) else true,
            if (SPM.isUniqueByBigTitle) isUniqueByBigTitle(entity) else true,
            if (SPM.isUniqueByText) isUniqueByText(entity) else true,
            if (SPM.isUniqueByBigText) isUniqueByBigText(entity) else true
        )

        return result.all { it == true }
    }

    private suspend fun isUniqueByTitle(entity: PRLogEntity): Boolean =
        if (entity.title != null) dao.countUniqueByTitle(entity.title) == 0 else true

    private suspend fun isUniqueByTicker(entity: PRLogEntity): Boolean =
        if (entity.tickerText != null) dao.countUniqueByTicker(entity.tickerText) == 0 else true

    private suspend fun isUniqueByBigTitle(entity: PRLogEntity): Boolean =
        if (entity.bigTitle != null) dao.countUniqueByBigTitle(entity.bigTitle) == 0 else true

    private suspend fun isUniqueByText(entity: PRLogEntity): Boolean =
        if (entity.text != null) dao.countUniqueByText(entity.text) == 0 else true

    private suspend fun isUniqueByBigText(entity: PRLogEntity): Boolean =
        if (entity.bigText != null) dao.countUniqueByBigText(entity.bigText) == 0 else true

    private suspend fun storeData(entity: PRLogEntity): Long = dao.insert(entity)

    private suspend fun transmitData(entity: PRLogEntity): Boolean = restApi.saveData(entity).code() == 200

    private suspend fun setIsSent(id: Long, value: Boolean) {
        dao.setIsSent(id = id, value = value)
    }
}