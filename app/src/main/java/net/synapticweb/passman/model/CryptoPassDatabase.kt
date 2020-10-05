package net.synapticweb.passman.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Entry::class, Hash::class], version = 1, exportSchema = true)
abstract class CryptoPassDatabase : RoomDatabase() {
    abstract val dao : CryptoPassDao
}