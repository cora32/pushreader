package io.alexarix.pushreader.repo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.alexarix.pushreader.repo.room.dao.PRDao
import io.alexarix.pushreader.repo.room.dao.ServiceLogDao
import io.alexarix.pushreader.repo.room.entity.PRLogEntity
import io.alexarix.pushreader.repo.room.entity.PRServiceLogEntity
import javax.inject.Singleton


@Database(
    entities = [PRLogEntity::class, PRServiceLogEntity::class],
    version = 2,
)

@TypeConverters(Converters::class)
@Singleton
abstract class CacheDB : RoomDatabase() {
    abstract fun dao(): PRDao
    abstract fun logDao(): ServiceLogDao
}

fun getDB(
    application: Context
): CacheDB = Room
    .databaseBuilder(application, CacheDB::class.java, "pr_db")
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()