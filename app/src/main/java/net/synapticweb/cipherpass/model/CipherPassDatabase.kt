package net.synapticweb.cipherpass.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Entry::class, CustomField::class, Hash::class, EntryFts::class, CustomFieldFts::class], version = 1, exportSchema = true)
abstract class CipherPassDatabase : RoomDatabase() {
    abstract val dao : CipherPassDao
}