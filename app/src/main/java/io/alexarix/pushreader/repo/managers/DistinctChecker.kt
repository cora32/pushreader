package io.alexarix.pushreader.repo.managers

import io.alexarix.pushreader.repo.SPM
import io.alexarix.pushreader.repo.room.dao.PRDao
import io.alexarix.pushreader.repo.room.entity.PRLogEntity
import io.alexarix.pushreader.viewmodels.e
import javax.inject.Inject
import javax.inject.Singleton

enum class DistinctToggles {
    Summary,
    Info,
    Subtext,
    Ticker,
    Title,
    BigTitle,
    Text,
    BigText,
}

val DistinctToggles.isToggled
    get() = SPM.Companion.isDistinctToggled(name)


@Singleton
class DistinctChecker @Inject constructor(
    private val dao: PRDao,
    private val logger: PRLogger
) {
    private suspend fun onFail(entity: PRLogEntity, toggle: DistinctToggles) {
        logger.logError(
            reason = "Entry is NOT unique by: $toggle",
            entity = entity,
        )
    }

    private suspend fun Boolean.afterCheck(entity: PRLogEntity, toggle: DistinctToggles) {
        "Is $entity unique by ${toggle.name}: $this".e

        if (!this)
            onFail(entity, toggle)

    }

    private suspend fun allOff(entity: PRLogEntity) =
        DistinctToggles.entries.all { !it.isToggled }.apply {
            if (true)
                logger.logUnknown(
                    reason = "Filtering is disabled - Processing all entries",
                    entity = entity,
                )
        }

    private suspend fun allToggledTrue(entity: PRLogEntity) = DistinctToggles.entries
        .filter { it.isToggled }
        .any {
            when (it) {
                DistinctToggles.Summary -> isUniqueBySummary(
                    entity
                )

                DistinctToggles.Info -> isUniqueByInfo(entity)

                DistinctToggles.Subtext -> isUniqueBySubtext(entity)

                DistinctToggles.Ticker -> isUniqueByTicker(entity)

                DistinctToggles.Title -> isUniqueByTitle(entity)

                DistinctToggles.BigTitle -> isUniqueByBigTitle(entity)

                DistinctToggles.Text -> isUniqueByText(entity)

                DistinctToggles.BigText -> isUniqueByBigText(entity)
            }.apply {
                afterCheck(
                    entity,
                    it
                )
            }
        }.apply {
            if (this) {
                logger.logOk(
                    reason = "Entry is unique",
                    entity = entity,
                )
            }
        }

    suspend fun isUnique(entity: PRLogEntity): Boolean {
        return allOff(entity = entity) || allToggledTrue(entity = entity)
    }

    private suspend fun isUniqueBySummary(entity: PRLogEntity): Boolean =
        (entity.summaryText?.let {
            dao.countUniqueBySummary(it).apply {
                "There is $this entries with summaryText = $it".e
            }
        } ?: dao.countNullSummary()) == 0

    private suspend fun isUniqueByInfo(entity: PRLogEntity): Boolean =
        (entity.infoText?.let {
            dao.countUniqueByInfo(it).apply {
                "There is $this entries with infoText = $it".e
            }
        } ?: dao.countNullInfoText()) == 0

    private suspend fun isUniqueBySubtext(entity: PRLogEntity): Boolean =
        (entity.subText?.let {
            dao.countUniqueBySubText(it).apply {
                "There is $this entries with subText = $it".e
            }
        } ?: dao.countNullSubText()) == 0

    private suspend fun isUniqueByTicker(entity: PRLogEntity): Boolean =
        (entity.tickerText?.let {
            dao.countUniqueByTicker(it).apply {
                "There is $this entries with tickerText = $it".e
            }
        } ?: dao.countNullTickerText()) == 0

    private suspend fun isUniqueByTitle(entity: PRLogEntity): Boolean =
        (entity.title?.let {
            dao.countUniqueByTitle(it).apply {
                "There is $this entries with title = $it".e
            }
        } ?: dao.countNullTitle()) == 0

    private suspend fun isUniqueByBigTitle(entity: PRLogEntity): Boolean =
        (entity.bigTitle?.let {
            dao.countUniqueByBigTitle(it).apply {
                "There is $this entries with bigTitle = $it".e
            }
        } ?: dao.countNullBigTitle()) == 0

    private suspend fun isUniqueByText(entity: PRLogEntity): Boolean =
        (entity.text?.let {
            dao.countUniqueByText(it).apply {
                "There is $this entries with text = $it".e
            }
        } ?: dao.countNullText()) == 0

    private suspend fun isUniqueByBigText(entity: PRLogEntity): Boolean =
        (entity.bigText?.let {
            dao.countUniqueByBigText(it).apply {
                "There is $this entries with bigText = $it".e
            }
        } ?: dao.countNullBigText()) == 0
}