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
                    reason = "Filtering is disabled - Sending all entries",
                    entity = entity,
                )
        }

    private suspend fun anyOn(entity: PRLogEntity) = DistinctToggles.entries
        .filter { it.isToggled }
        .all {
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
        return allOff(entity = entity) || anyOn(entity = entity)
    }

    private suspend fun isUniqueBySummary(entity: PRLogEntity): Boolean =
        dao.countUniqueBySummary(entity.summaryText.toString()) == 0

    private suspend fun isUniqueByInfo(entity: PRLogEntity): Boolean =
        dao.countUniqueByInfo(entity.infoText.toString()) == 0

    private suspend fun isUniqueBySubtext(entity: PRLogEntity): Boolean =
        dao.countUniqueBySubText(entity.subText.toString()) == 0

    private suspend fun isUniqueByTicker(entity: PRLogEntity): Boolean =
        dao.countUniqueByTicker(entity.tickerText.toString()) == 0

    private suspend fun isUniqueByTitle(entity: PRLogEntity): Boolean =
        dao.countUniqueByTitle(entity.title.toString()) == 0

    private suspend fun isUniqueByBigTitle(entity: PRLogEntity): Boolean =
        dao.countUniqueByBigTitle(entity.bigTitle.toString()) == 0

    private suspend fun isUniqueByText(entity: PRLogEntity): Boolean =
        dao.countUniqueByText(entity.text.toString()) == 0

    private suspend fun isUniqueByBigText(entity: PRLogEntity): Boolean =
        dao.countUniqueByBigText(entity.bigText.toString()) == 0
}