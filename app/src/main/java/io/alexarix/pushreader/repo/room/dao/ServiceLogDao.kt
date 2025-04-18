package io.alexarix.pushreader.repo.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.alexarix.pushreader.repo.room.entity.PRServiceLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
@Dao
interface ServiceLogDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(log: PRServiceLogEntity): Long

    @Query("SELECT * FROM PRServiceLogEntity ORDER BY timestamp DESC")
    suspend fun getAll(): List<PRServiceLogEntity>

    @Query("SELECT * FROM PRServiceLogEntity ORDER BY timestamp DESC")
    fun getDataFlow(): Flow<PRServiceLogEntity?>
}