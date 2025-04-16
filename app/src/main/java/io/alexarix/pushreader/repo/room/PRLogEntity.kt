package io.alexarix.pushreader.repo.room

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Keep
@Entity(indices = [Index(value = ["timestamp"], unique = true)])
data class PRLogEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @Expose val timestamp: Long = 0L,
    @Expose @ColumnInfo(index = true) val packageName: String? = null,
    @Expose @ColumnInfo(index = true) val tickerText: String? = null,
    @Expose @ColumnInfo(index = true) val title: String? = null,
    @Expose @ColumnInfo(index = true) val bigTitle: String? = null,
    @Expose @ColumnInfo(index = true) val text: String? = null,
    @Expose @ColumnInfo(index = true) val bigText: String? = null,
    @ColumnInfo(index = true) val isSent: Boolean = false,
    @Expose val smallIconStr: String? = null,
    @Expose val largeIconStr: String? = null,
    @Expose val largeIconBigStr: String? = null,
    @Expose val largeIconBitmapStr: String? = null,
    @Expose val extraPictureStr: String? = null,
    @Expose val actions: List<String>? = null,
    @Expose val category: String? = null,
    @Expose val summaryText: String? = null,
    @Expose val infoText: String? = null,
    @Expose val subText: String? = null,
) {
    override fun toString(): String {
        return "packageName: $packageName\n" +
                "tickerText: $tickerText\n" +
                "title: $title\n" +
                "bigTitle: $bigTitle\n" +
                "text: $text\n" +
                "bigText: $bigText\n" +
                "smallIconStr: ${smallIconStr?.length}\n" +
                "largeIconStr1: ${largeIconStr?.length}\n" +
                "largeIconStr2: ${largeIconBigStr?.length}\n" +
                "largeIconBitmapStr: ${largeIconBitmapStr?.length}\n" +
                "extraPictureStr: ${extraPictureStr?.length}\n" +
                "actions: $actions\n" +
                "category: $category\n" +
                "summaryText: $summaryText\n" +
                "infoText: $infoText\n" +
                "subText: $subText\n" +
                ""
    }
}