package net.synapticweb.passman.model

import androidx.room.ColumnInfo
import androidx.room.Entity
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
    var insertionDate : Long = System.currentTimeMillis()
) {
    val hrInsertionDate : String
        get() {
            val date = Date(insertionDate)
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