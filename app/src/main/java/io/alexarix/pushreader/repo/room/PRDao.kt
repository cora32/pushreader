package io.alexarix.pushreader.repo.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton


@Singleton
@Dao
interface PRDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
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

    @Query("UPDATE PRLogEntity SET isSent = :value WHERE uid = :id")
    suspend fun setIsSent(id: Long, value: Boolean)

    @Query("SELECT * FROM PRLogEntity ORDER BY timestamp DESC LIMIT 100")
    fun dataFlow(): Flow<PRLogEntity>
}