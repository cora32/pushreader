package io.alexarix.pushreader.repo.managers

import io.alexarix.pushreader.repo.SPM
import io.alexarix.pushreader.repo.room.dao.ServiceLogDao
import io.alexarix.pushreader.repo.room.entity.PRLogEntity
import io.alexarix.pushreader.repo.room.entity.PRServiceLogEntity
import io.alexarix.pushreader.viewmodels.e
import javax.inject.Inject
import javax.inject.Singleton

enum class LogType {
    OK,
    Fail,
    Info,
    Unknown
}

private fun PRLogEntity.toLogEntry(
    logType: LogType,
    reason: String
) = PRServiceLogEntity(
    timestamp = System.currentTimeMillis(),
    packageName = this.packageName,
    tickerText = this.tickerText,
    title = this.title,
    bigTitle = this.bigTitle,
    text = this.text,
    bigText = this.bigText,
    actions = this.actions,
    category = this.category,
    summaryText = this.summaryText,
    infoText = this.infoText,
    subText = this.subText,
    logType = logType,
    reason = reason
)

@Singleton
class PRLogger @Inject constructor(val dao: ServiceLogDao) {
    fun getDataFlow() = dao.getDataFlow()

    private suspend fun logRaw(reason: String, logType: LogType) {
        dao.insert(
            PRServiceLogEntity(
                timestamp = System.currentTimeMillis(),
                logType = logType,
                reason = reason
            )
        )
    }

    private suspend fun logEntity(reason: String, logType: LogType, entity: PRLogEntity?) {
        if (!SPM.isLogEnabled) return

        entity?.let {
            dao.insert(
                entity.toLogEntry(
                    logType = logType,
                    reason = reason
                )
            )
        } ?: logRaw(logType = logType, reason = reason)
    }

    suspend fun logError(reason: String, entity: PRLogEntity? = null) {
        reason.e

        logEntity(reason = reason, logType = LogType.Fail, entity = entity)
    }

    suspend fun logUnknown(reason: String, entity: PRLogEntity? = null) {
        reason.e

        logEntity(reason = reason, logType = LogType.Unknown, entity = entity)
    }

    suspend fun logOk(reason: String, entity: PRLogEntity? = null) {
        reason.e

        logEntity(reason = reason, logType = LogType.OK, entity = entity)
    }

    suspend fun logInfo(reason: String, entity: PRLogEntity? = null) {
        reason.e

        logEntity(reason = reason, logType = LogType.Info, entity = entity)
    }

    suspend fun getLogs(): List<PRServiceLogEntity> = dao.getAll()

    suspend fun getLogs(flags: List<LogType>) = dao.getAll(flags)
}