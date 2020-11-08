package net.synapticweb.passman.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "entries")
data class Entry (
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0L,

    @ColumnInfo(name = "entry_name")
    var entryName : String = "",

    @ColumnInfo(name = "username")
    var username : String? = null,

    @ColumnInfo(name = "password")
    var password : String = "",

    @ColumnInfo(name = "url")
    var url : String? = null,

    @ColumnInfo(name = "comment")
    var comment : String? = null,

    @ColumnInfo(name = "insertion_date")
    var insertionDate : Long = System.currentTimeMillis(),

    @ColumnInfo(name = "modification_date")
    var modificationDate : Long = System.currentTimeMillis(),

    @ColumnInfo(name = "icon_res")
    var icon : Int = 0
) {
    val hrInsertionDate : String
        get() {
            return hrDate(insertionDate)
        }

    val hrModificationDate : String
        get() {
            return hrDate(modificationDate)
        }

    private fun hrDate(timestamp : Long) : String {
        val date = Date(timestamp)
        val format  = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        return format.format(date)
    }
}

@Entity(tableName = "hash")
class Hash(
    @ColumnInfo(name = "hash") var hash: String,
    @ColumnInfo(name = "salt") var salt: String
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0L
}

//https://sqlite.org/fts3.html
//https://developer.android.com/reference/androidx/room/Fts4#notIndexed()
//https://developer.android.com/training/data-storage/room/defining-data#search
//https://medium.com/@sienatime/enabling-sqlite-fts-in-room-2-1-75e17d0f0ff8
//https://android.jlelse.eu/offline-full-text-search-in-android-ios-b4dd5bed3acd
//https://stackoverflow.com/questions/60262415/room-2-1-sqlite-fts-inserting-new-single-fts-object-in-database
@Fts4(contentEntity = Entry::class)
@Entity(tableName = "entries_fts")
class EntryFts {
    @ColumnInfo(name = "entry_name")
    var entryName : String = ""

    @ColumnInfo(name = "username")
    var username : String? = null

    @ColumnInfo(name = "url")
    var url : String? = null

    @ColumnInfo(name = "comment")
    var comment : String? = null
}