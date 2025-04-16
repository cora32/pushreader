package io.alexarix.pushreader.repo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import javax.inject.Singleton


@Database(
    entities = [PRLogEntity::class,],
    version = 2,

//    autoMigrations = [
//        AutoMigration (
//            from = 3,
//            to = 4,
//            spec = MyAutoMigration::class
//        )
//    ]
)

@TypeConverters(Converters::class)
@Singleton
abstract class CacheDB : RoomDatabase() {
    abstract fun dao(): PRDao
}

fun getDB(
    application: Context
): CacheDB = Room
    .databaseBuilder(application, CacheDB::class.java, "pr_db")
//    .addMigrations(MIGRATION_3_4)
//    .addMigrations(MIGRATION_4_5)
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()