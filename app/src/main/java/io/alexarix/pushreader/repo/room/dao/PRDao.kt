package io.alexarix.pushreader.repo.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.alexarix.pushreader.repo.room.entity.PRLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
@Dao
interface PRDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(log: PRLogEntity): Long

    @Query("SELECT * FROM PRLogEntity WHERE packageName = :packageName")
    suspend fun getLogsForPackage(packageName: String): List<PRLogEntity>


    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE title = :title")
    suspend fun countUniqueByTitle(title: String): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE tickerText = :ticker")
    suspend fun countUniqueByTicker(ticker: String): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE bigTitle = :bigTitle")
    suspend fun countUniqueByBigTitle(bigTitle: String): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE text = :text")
    suspend fun countUniqueByText(text: String): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE bigText = :bigText")
    suspend fun countUniqueByBigText(bigText: String): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE summaryText = :summaryText")
    suspend fun countUniqueBySummary(summaryText: String): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE infoText = :infoText")
    suspend fun countUniqueByInfo(infoText: String): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE subText = :subText")
    suspend fun countUniqueBySubText(subText: String): Int


    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE summaryText IS NULL")
    suspend fun countNullSummary(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE title IS NULL")
    suspend fun countNullTitle(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE tickerText IS NULL")
    suspend fun countNullTickerText(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE bigTitle IS NULL")
    suspend fun countNullBigTitle(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE text IS NULL")
    suspend fun countNullText(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE bigText IS NULL")
    suspend fun countNullBigText(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE infoText IS NULL")
    suspend fun countNullInfoText(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE subText IS NULL")
    suspend fun countNullSubText(): Int

    @Query("UPDATE PRLogEntity SET isSent = :value WHERE uid = :id")
    suspend fun setIsSent(id: Long, value: Boolean)

    @Query("SELECT * FROM PRLogEntity")
    fun dataFlow(): Flow<PRLogEntity?>

    @Query("SELECT COUNT(*) FROM PRLogEntity")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM PRLogEntity WHERE isSent = 0")
    suspend fun countUnsent(): Int

    @Query("SELECT * FROM PRLogEntity ORDER BY timestamp DESC LIMIT 100")
    suspend fun getLast100Items(): List<PRLogEntity>

    @Query("SELECT * FROM PRLogEntity WHERE isSent = 0 ORDER BY timestamp")
    suspend fun getUnsent(): List<PRLogEntity>
}