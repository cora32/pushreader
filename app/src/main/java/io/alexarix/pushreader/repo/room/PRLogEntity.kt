package io.alexarix.pushreader.repo.room

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(indices = [Index(value = ["timestamp"], unique = true)])
data class PRLogEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val timestamp: Long = 0L,
    @ColumnInfo(index = true) val packageName: String? = null,
    @ColumnInfo(index = true) val tickerText: String? = null,
    @ColumnInfo(index = true) val title: String? = null,
    @ColumnInfo(index = true) val bigTitle: String? = null,
    @ColumnInfo(index = true) val text: String? = null,
    @ColumnInfo(index = true) val bigText: String? = null,
    @ColumnInfo(index = true) val isSent: Boolean = false,
    val smallIconStr: String? = null,
    val largeIconStr1: String? = null,
    val largeIconStr2: String? = null,
    val largeIconBig: String? = null,
    val extraPictureStr: String? = null,
    val actions: List<String>? = null,
    val category: String? = null,
) {
    override fun toString(): String {
        return "packageName: $packageName\n" +
                "tickerText: $tickerText\n" +
                "title: $title\n" +
                "bigTitle: $bigTitle\n" +
                "text: $text\n" +
                "bigText: $bigText\n" +
                "smallIconStr: ${smallIconStr?.length}\n" +
                "largeIconStr1: ${largeIconStr1?.length}\n" +
                "largeIconStr2: ${largeIconStr2?.length}\n" +
                "largeIconBig: ${largeIconBig?.length}\n" +
                "extraPictureStr: ${extraPictureStr?.length}\n" +
                "actions: $actions\n" +
                "category: $category\n" +
                ""
    }
}