package io.alexarix.pushreader.repo.room.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import io.alexarix.pushreader.repo.managers.LogType

@Keep
@Entity(indices = [Index(value = ["timestamp"], unique = true)])
data class PRServiceLogEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @Expose val timestamp: Long = 0L,
    @Expose @ColumnInfo(index = true) val packageName: String? = null,
    @Expose @ColumnInfo(index = true) val tickerText: String? = null,
    @Expose @ColumnInfo(index = true) val title: String? = null,
    @Expose @ColumnInfo(index = true) val bigTitle: String? = null,
    @Expose @ColumnInfo(index = true) val text: String? = null,
    @Expose @ColumnInfo(index = true) val bigText: String? = null,
    @Expose val actions: List<String>? = null,
    @Expose val category: String? = null,
    @Expose val summaryText: String? = null,
    @Expose val infoText: String? = null,
    @Expose val subText: String? = null,
    @Expose val logType: LogType? = null,
    @Expose val reason: String? = null,
) {
    override fun toString(): String {
        return "packageName: $packageName\n" +
                "tickerText: $tickerText\n" +
                "title: $title\n" +
                "bigTitle: $bigTitle\n" +
                "text: $text\n" +
                "bigText: $bigText\n" +
                "actions: $actions\n" +
                "category: $category\n" +
                "summaryText: $summaryText\n" +
                "infoText: $infoText\n" +
                "subText: $subText\n" +
                ""
    }
}