/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.model

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.SimpleDateFormat
import java.util.*

const val KEY_DRAWABLE_NAME = "item_key"

@Serializable
@Entity(tableName = "entries")
data class Entry (
    @ColumnInfo(name = "entry_name")
    var entryName : String,

    @ColumnInfo(name = "username")
    var username : String? = null,

    @ColumnInfo(name = "password")
    var password : String? = null,

    @ColumnInfo(name = "url")
    var url : String? = null,

    @ColumnInfo(name = "comment")
    var comment : String? = null,

    @ColumnInfo(name = "insertion_date")
    var insertionDate : Long = System.currentTimeMillis(),

    @ColumnInfo(name = "modification_date")
    var modificationDate : Long = System.currentTimeMillis(),

    @ColumnInfo(name = "icon")
    var icon : String = KEY_DRAWABLE_NAME
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    @Transient
    var id : Long = 0L

    val hrInsertionDate : String
        get() {
            return hrDate(insertionDate)
        }

    val hrModificationDate : String
        get() {
            return hrDate(modificationDate)
        }

    @Ignore
    var customFields : List<CustomField>? = null

    private fun hrDate(timestamp : Long) : String {
        val date = Date(timestamp)
        val format  = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        return format.format(date)
    }
}

@Serializable
@Entity(tableName = "custom_fields")
 data class CustomField (
    @Transient
    @ColumnInfo(name = "entry")
    var entry : Long = 0L,

    @ColumnInfo(name = "field_name")
    var fieldName : String,

    @ColumnInfo(name = "is_protected")
    var isProtected : Boolean = false,

    @ColumnInfo(name = "value")
    var value : String? = null
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    @Transient
    var id : Long = 0L

    @Ignore
    var inMemoryState : String? = null
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

    @ColumnInfo(name = "password")
    var password : String? = null

    @ColumnInfo(name = "url")
    var url : String? = null

    @ColumnInfo(name = "comment")
    var comment : String? = null
}

@Fts4(contentEntity = CustomField::class)
@Entity(tableName = "custom_fields_fts")
class CustomFieldFts {
    @ColumnInfo(name = "field_name")
    var fieldName : String = ""

    @ColumnInfo(name = "value")
    var value : String = ""
}